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
import java.util.concurrent.ExecutorService;

import com.damnae.osukeysoundsplitter.OsuDiff.DiffEvent;

public class KeysoundProcessor {
	private static final long SHORT_AUDIO_AREA_THRESHOLD = 10; // ms

	class Keysound {
		public String filename;
		public long startTime;
		public long endTime;
		public boolean isAutosound;
		public String data;
	}

	private List<Keysound> keysounds = new ArrayList<Keysound>();
	private List<String> keysoundFiles = new ArrayList<String>();

	public KeysoundExtractor process(File diffFile, File keysoundsFile,
			int offset, KeysoundPathProvider keysoundPathProvider,
			ExecutorService executorService) throws IOException {

		OsuDiff osuDiff = new OsuDiff(diffFile);
		List<Keysound> diffKeysounds = getKeysounds(osuDiff);
		if (diffKeysounds.isEmpty())
			return null;

		KeysoundExtractor keysoundExtractor = getKeysoundExtractor(
				keysoundsFile, diffKeysounds, offset, keysoundPathProvider,
				executorService);

		keysounds.addAll(diffKeysounds);
		keysoundFiles.add(getKeysoundsFolderPath(keysoundsFile));

		return keysoundExtractor;
	}

	public void insertKeysounds(File diffFile) throws IOException {
		insertKeysounds(diffFile, keysounds, keysoundFiles);
		keysounds.clear();
		keysoundFiles.clear();
	}

	private List<Keysound> getKeysounds(OsuDiff osuDiff) {
		List<Keysound> keysounds = new ArrayList<Keysound>();

		boolean inSoundSection = false;
		for (int i = 0, size = osuDiff.diffEvents.size(); i < size - 1; ++i) {
			DiffEvent diffEvent = osuDiff.diffEvents.get(i);
			DiffEvent nextDiffEvent = osuDiff.diffEvents.get(i + 1);

			boolean isSplittingPoint = diffEvent.isSplittingPoint();
			if (isSplittingPoint)
				inSoundSection = !nextDiffEvent.isSplittingPoint()
						|| !inSoundSection;
			else
				inSoundSection = true;

			if (!inSoundSection)
				continue;

			boolean isAutosound = isSplittingPoint;

			long duration = nextDiffEvent.time - diffEvent.time;
			if (isAutosound && duration < SHORT_AUDIO_AREA_THRESHOLD)
				continue;

			Keysound keysound = new Keysound();
			keysound.startTime = diffEvent.time;
			keysound.endTime = nextDiffEvent.time;
			keysound.isAutosound = isAutosound;
			keysound.data = diffEvent.data;
			keysounds.add(keysound);
		}

		return keysounds;
	}

	private KeysoundExtractor getKeysoundExtractor(File keysoundsFile,
			List<Keysound> keysounds, int offset,
			KeysoundPathProvider keysoundPathProvider,
			ExecutorService executorService) throws IOException {

		KeysoundWriter writer = new OggKeysoundWriter(keysoundsFile
				.getParentFile().getCanonicalPath() + "/",
				getKeysoundsFolderPath(keysoundsFile), keysoundPathProvider,
				executorService);

		return new KeysoundExtractor(keysounds, writer, offset);
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

	private void insertKeysounds(File diffFile, List<Keysound> keysounds,
			List<String> keysoundFolderPaths) throws IOException {

		File backupFile = new File(diffFile.getCanonicalPath() + ".bak");
		if (backupFile.exists())
			backupFile.delete();
		Files.copy(diffFile.toPath(), backupFile.toPath());

		List<String> lines = retrieveLines(diffFile);
		FileOutputStream os = new FileOutputStream(diffFile);
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(os,
					Charset.forName("UTF-8"));
			BufferedWriter writer = new BufferedWriter(outputStreamWriter);

			String sectionName = null;
			try {
				for (int i = 0, size = lines.size(); i < size; ++i) {
					String line = lines.get(i);
					if (line.length() == 0)
						sectionName = null;

					if (line.startsWith("[") && line.endsWith("]")) {
						sectionName = line.substring(1, line.length() - 1);

						writer.append(line);
						writer.newLine();

						if (sectionName.equals("HitObjects")) {
							for (Keysound keysound : keysounds) {
								if (keysound.isAutosound)
									continue;

								String keysoundData = keysound.data;

								int colonPos = keysoundData.lastIndexOf(":");
								if (colonPos > -1) {
									keysoundData = keysoundData.substring(0,
											colonPos) + ":" + keysound.filename;
								}

								writer.append(keysoundData);
								writer.newLine();
							}
						}

					} else if (sectionName != null) {
						if (sectionName.equals("Events")) {
							if (line.startsWith("Sample")) {
								String[] values = line.split(",");
								String samplePath = values[3];

								boolean keepSample = !isSampleInKeysoundFolder(
										samplePath, keysoundFolderPaths);

								if (keepSample) {
									writer.append(line);
									writer.newLine();
								}

							} else {
								writer.append(line);
								writer.newLine();

								if (line.equals("//Storyboard Sound Samples")) {
									for (Keysound keysound : keysounds) {
										if (!keysound.isAutosound)
											continue;

										writer.append("Sample,"
												+ keysound.startTime + ",0,\""
												+ keysound.filename + "\",100");
										writer.newLine();
									}
								}
							}

						} else if (!sectionName.equals("HitObjects")) {
							writer.append(line);
							writer.newLine();
						}

					} else {
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

	private boolean isSampleInKeysoundFolder(String samplePath,
			List<String> keysoundFolderPaths) {

		boolean keepSample = false;
		for (String keysoundFolderPath : keysoundFolderPaths) {
			if (samplePath.startsWith("\"" + keysoundFolderPath)
					|| samplePath.startsWith("\""
							+ keysoundFolderPath.replace('/', '\\'))) {

				keepSample = true;
				break;
			}
		}
		return keepSample;
	}
}
