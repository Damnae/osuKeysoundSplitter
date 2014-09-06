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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

import com.damnae.osukeysoundsplitter.OsuDiff.DiffEvent;
import com.damnae.osukeysoundsplitter.strategy.KeysoundingStrategy;

public class KeysoundProcessor {
	private static final long SHORT_AUDIO_AREA_THRESHOLD = 10; // ms

	private KeysoundingStrategy keysoundingStrategy;
	private List<Keysound> keysounds = new ArrayList<Keysound>();
	private List<String> keysoundFiles = new ArrayList<String>();

	public KeysoundProcessor(KeysoundingStrategy keysoundingStrategy) {
		this.keysoundingStrategy = keysoundingStrategy;
	}

	public KeysoundExtractor process(File diffFile, File keysoundsFile,
			int offset, ExecutorService executorService) throws IOException {

		OsuDiff osuDiff = new OsuDiff(diffFile);
		List<Keysound> diffKeysounds = getKeysounds(osuDiff);
		if (diffKeysounds.isEmpty())
			return null;

		KeysoundExtractor keysoundExtractor = getKeysoundExtractor(
				keysoundsFile, diffKeysounds, offset, executorService);

		keysounds.addAll(diffKeysounds);
		keysoundFiles.add(Utils.getFileNameWithoutExtension(keysoundsFile));

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
			keysound.type = isAutosound ? Keysound.Type.AUTO : diffEvent
					.isLine() ? Keysound.Type.LINE : Keysound.Type.HITOBJECT;
			keysound.data = diffEvent.data;
			keysounds.add(keysound);
		}

		return keysounds;
	}

	private KeysoundExtractor getKeysoundExtractor(File keysoundsFile,
			List<Keysound> keysounds, int offset,
			ExecutorService executorService) throws IOException {

		KeysoundWriter writer = new KeysoundWriter(keysoundingStrategy,
				executorService);

		return new KeysoundExtractor(keysounds, writer, offset);
	}

	private void insertKeysounds(File diffFile, List<Keysound> keysounds,
			List<String> keysoundFolderPaths) throws IOException {

		// The file is assumed to exist at this point
		File backupFile = new File(diffFile.getCanonicalPath() + ".bak");
		if (diffFile.length() == 0) {
			System.out.println("diff file is empty, restoring from backup");

			diffFile.delete();
			Files.copy(backupFile.toPath(), diffFile.toPath());

		} else {
			if (backupFile.exists())
				backupFile.delete();
			Files.copy(diffFile.toPath(), backupFile.toPath());
		}

		List<String> lines = retrieveLines(diffFile);
		List<String> timingPointLines = new ArrayList<String>();
		FileOutputStream os = new FileOutputStream(diffFile);
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(os,
					Charset.forName("UTF-8"));
			BufferedWriter writer = new BufferedWriter(outputStreamWriter);

			String sectionName = null;
			try {
				for (int i = 0, size = lines.size(); i < size; ++i) {
					String line = lines.get(i);
					if (line.length() == 0 && sectionName != null) {
						// Leave section
						if (sectionName.equals("TimingPoints")) {
							timingPointLines = keysoundingStrategy
									.rewriteTimingPoints(timingPointLines,
											keysounds);

							for (String timingPointLine : timingPointLines) {
								writer.append(timingPointLine);
								writer.newLine();
							}
						}

						sectionName = null;
					}

					if (line.startsWith("[") && line.endsWith("]")) {
						// Enter section
						sectionName = line.substring(1, line.length() - 1);

						writer.append(line);
						writer.newLine();

						if (sectionName.equals("HitObjects")) {
							for (Keysound keysound : keysounds) {
								if (keysound.type != Keysound.Type.HITOBJECT)
									continue;

								String[] keysoundDataLines = keysound.data
										.split("\n");
								int volume = 133 / keysoundDataLines.length;
								for (String keysoundData : keysoundDataLines) {
									writer.append(keysoundingStrategy
											.rewriteKeysoundData(keysound,
													keysoundData, volume));
									writer.newLine();
								}
							}
						}

					} else if (sectionName != null) {
						// Inside a section
						if (sectionName.equals("Events")) {
							if (!line.startsWith("Sample")) {
								writer.append(line);
								writer.newLine();

								if (line.equals("//Storyboard Sound Samples")) {
									for (Keysound keysound : keysounds) {
										if (keysound.type != Keysound.Type.AUTO)
											continue;

										writer.append("Sample,"
												+ keysound.startTime + ",0,\""
												+ keysound.filename + "\",100");
										writer.newLine();
									}
								}
							}

						} else if (sectionName.equals("TimingPoints")) {
							timingPointLines.add(line);

						} else if (!sectionName.equals("HitObjects")) {
							writer.append(line);
							writer.newLine();
						}

					} else {
						// Outside any section
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
