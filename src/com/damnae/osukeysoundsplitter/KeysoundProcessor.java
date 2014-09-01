package com.damnae.osukeysoundsplitter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.kc7bfi.jflac.FLACDecoder;

import com.damnae.osukeysoundsplitter.OsuDiff.AudioArea;

public class KeysoundProcessor {
	private static final long SHORT_AUDIO_AREA_THREASHOLD = 10; // ms

	class Keysound {
		public String filename;
		public long startTime;
		public long endTime;
		public boolean isEvent;
	}

	public void process(File diffFile, File keysoundsFile, int offset)
			throws IOException {

		OsuDiff osuDiff = new OsuDiff(diffFile);
		List<Keysound> keysounds = getKeysounds(osuDiff);
		extractKeysounds(keysoundsFile, keysounds, offset);
		insertKeysounds(diffFile, keysounds);
	}

	private List<Keysound> getKeysounds(OsuDiff osuDiff) {
		// This assumes audioArea.noteTimes is sorted,
		// which is as long as the .osu is

		List<Keysound> keysounds = new ArrayList<Keysound>();
		for (AudioArea audioArea : osuDiff.audioAreas) {
			if (audioArea.noteTimes.isEmpty()) {
				long areaDuration = audioArea.endTime - audioArea.startTime;

				if (areaDuration > SHORT_AUDIO_AREA_THREASHOLD) {
					Keysound keysound = new Keysound();
					keysound.startTime = audioArea.startTime;
					keysound.endTime = audioArea.endTime;
					keysound.isEvent = true;
					keysounds.add(keysound);
				}

			} else {
				long areaDuration = audioArea.noteTimes.get(0)
						- audioArea.startTime;

				if (areaDuration > SHORT_AUDIO_AREA_THREASHOLD) {
					Keysound keysound = new Keysound();
					keysound.startTime = audioArea.startTime;
					keysound.endTime = audioArea.noteTimes.get(0);
					keysound.isEvent = true;
					keysounds.add(keysound);
				}
			}

			for (int i = 0, size = audioArea.noteTimes.size(); i < size; ++i) {
				long startTime = audioArea.noteTimes.get(i);
				long endTime = i < size - 1 ? audioArea.noteTimes.get(i + 1)
						: audioArea.endTime;

				Keysound keysound = new Keysound();
				keysound.startTime = startTime;
				keysound.endTime = endTime;
				keysound.isEvent = false;
				keysounds.add(keysound);
			}
		}

		return keysounds;
	}

	private void extractKeysounds(File keysoundsFile, List<Keysound> keysounds,
			int offset) throws IOException {

		KeysoundWriter writer = new OggKeysoundWriter(keysoundsFile
				.getParentFile().getCanonicalPath() + "/",
				getKeysoundsFolderPath(keysoundsFile));
		KeysoundExtractor keysoundExtractor = new KeysoundExtractor(keysounds,
				writer, offset);

		FileInputStream is = new FileInputStream(keysoundsFile);
		FLACDecoder decoder = new FLACDecoder(is);
		decoder.addPCMProcessor(keysoundExtractor);
		decoder.decode();

		keysoundExtractor.complete();
	}

	private String getKeysoundsFolderPath(File keysoundsFile)
			throws IOException {

		String keysoundsPath = keysoundsFile.getCanonicalPath();

		int separatorPos = keysoundsPath.lastIndexOf(File.separator);
		if (separatorPos < 0)
			throw new InvalidParameterException(keysoundsPath);

		int extensionPos = keysoundsPath.lastIndexOf('.');
		if (extensionPos < 0)
			throw new InvalidParameterException(keysoundsPath);

		return keysoundsPath.substring(separatorPos + File.separator.length(),
				extensionPos) + "/";
	}

	private void insertKeysounds(File diffFile, List<Keysound> keysounds)
			throws IOException {

		File backupFile = new File(diffFile.getCanonicalPath() + ".bak");
		if (!backupFile.exists())
			Files.copy(diffFile.toPath(), backupFile.toPath());

		List<String> lines = retrieveLines(diffFile);
		FileOutputStream os = new FileOutputStream(diffFile);
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(os,
					Charset.forName("UTF-8"));
			BufferedWriter writer = new BufferedWriter(outputStreamWriter);

			String sectionName = null;
			String ignoreUntilLine = null;
			try {
				for (int i = 0, size = lines.size(); i < size; ++i) {
					String line = lines.get(i);
					if (line.startsWith("[") && line.endsWith("]")) {
						sectionName = line.substring(1, line.length() - 1);

						writer.append(line);
						writer.newLine();

						ignoreUntilLine = null;

					} else if (ignoreUntilLine != null) {
						if (line.equals(ignoreUntilLine)) {
							ignoreUntilLine = null;

							writer.append(line);
							writer.newLine();
						}

					} else if (ignoreUntilLine == null && sectionName != null) {
						if (sectionName.equals("Events")) {
							writer.append(line);
							writer.newLine();

							if (line.equals("//Storyboard Sound Samples")) {
								for (Keysound keysound : keysounds) {
									if (keysound.isEvent) {
										writer.append("Sample,"
												+ keysound.startTime + ",0,\""
												+ keysound.filename + "\",100");
										writer.newLine();
									}
								}

								ignoreUntilLine = "//Background Colour Transformations";
							}

						} else if (sectionName.equals("HitObjects")) {
							String[] values = line.split(",");
							final long startTime = Integer.parseInt(values[2]);

							for (Keysound keysound : keysounds) {
								if (keysound.startTime == startTime) {
									int colonPos = line.lastIndexOf(":");
									if (colonPos > -1) {
										line = line.substring(0, colonPos)
												+ ":" + keysound.filename;
									}
									break;
								}
							}

							writer.append(line);
							writer.newLine();

						} else {
							writer.append(line);
							writer.newLine();
						}

					} else if (ignoreUntilLine == null) {
						writer.append(line);
						writer.newLine();
					}
				}

			} finally {
				writer.close();
			}

		} finally {
			os.close();
		}
	}

	private List<String> retrieveLines(File file) throws FileNotFoundException,
			IOException {

		List<String> lines = new ArrayList<String>();
		FileInputStream is = new FileInputStream(file);
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(is,
					Charset.forName("UTF-8"));
			BufferedReader reader = new BufferedReader(inputStreamReader);
			try {
				String line;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					lines.add(line);
				}

			} finally {
				reader.close();
			}

		} finally {
			is.close();
		}

		return lines;
	}
}
