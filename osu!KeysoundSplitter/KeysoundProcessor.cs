using osuKeysoundSplitter.Strategy;
using System.Collections.Generic;
using System.IO;

namespace osuKeysoundSplitter
{
public class KeysoundProcessor {
	private static const long SHORT_AUDIO_AREA_THRESHOLD = 10; // ms

	private KeysoundingStrategy keysoundingStrategy;
	private List<Keysound> keysounds = new List<Keysound>();
	private List<TimingPoint> timingPoints = new List<TimingPoint>();
	private List<string> keysoundFiles = new List<string>();

	public KeysoundProcessor(KeysoundingStrategy keysoundingStrategy) {
		this.keysoundingStrategy = keysoundingStrategy;
	}

	public KeysoundExtractor process(File diffFile, File keysoundsFile,
			int offset, ExecutorService executorService) {

		OsuDiff osuDiff = new OsuDiff(diffFile);
		List<Keysound> diffKeysounds = getKeysounds(osuDiff);
		if (diffKeysounds.Count == 0)
			return null;

		timingPoints.AddRange(osuDiff.timingPoints);

		KeysoundExtractor keysoundExtractor = getKeysoundExtractor(
				keysoundsFile, diffKeysounds, offset, executorService);

		keysounds.AddRange(diffKeysounds);
		keysoundFiles.Add(Utils.getFileNameWithoutExtension(keysoundsFile));

		return keysoundExtractor;
	}

	public void insertKeysounds(File diffFile)  {
		TimingPoint.sortTimingPoints(timingPoints);
		TimingPoint.simplifyTimingPoints(timingPoints);

		insertKeysounds(diffFile, keysounds, timingPoints, keysoundFiles);
		keysounds.clear();
		keysoundFiles.clear();
	}

	private List<Keysound> getKeysounds(OsuDiff osuDiff) {
		List<Keysound> keysounds = new List<Keysound>();

		bool inSoundSection = false;
		for (int i = 0, size = osuDiff.diffEvents.Count; i < size - 1; ++i) {
			osuKeysoundSplitter.OsuDiff.DiffEvent diffEvent = osuDiff.diffEvents[i];
			osuKeysoundSplitter.OsuDiff.DiffEvent nextDiffEvent = osuDiff.diffEvents[i + 1];

			bool isSplittingPoint = diffEvent.isSplittingPoint();
			if (isSplittingPoint)
				inSoundSection = !nextDiffEvent.isSplittingPoint()
						|| !inSoundSection;
			else
				inSoundSection = true;

			if (!inSoundSection)
				continue;

			bool isAutosound = isSplittingPoint;

			long duration = nextDiffEvent.time - diffEvent.time;
			if (isAutosound && duration < SHORT_AUDIO_AREA_THRESHOLD)
				continue;

			Keysound keysound = new Keysound();
			keysound.startTime = diffEvent.time;
			keysound.endTime = nextDiffEvent.time;
			keysound.type = isAutosound ? Keysound.Type.AUTO : diffEvent
					.isLine() ? Keysound.Type.LINE : Keysound.Type.HITOBJECT;
			keysound.data = diffEvent.data;
			keysounds.Add(keysound);
		}

		return keysounds;
	}

	private KeysoundExtractor getKeysoundExtractor(File keysoundsFile,
			List<Keysound> keysounds, int offset,
			ExecutorService executorService) {

		KeysoundWriter writer = new KeysoundWriter(keysoundingStrategy,
				executorService);

		return new KeysoundExtractor(keysounds, writer, offset);
	}

	private void insertKeysounds(File diffFile, List<Keysound> keysounds,
			List<TimingPoint> timingPoints, List<string> keysoundFolderPaths)
			 {

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

		List<string> lines = retrieveLines(diffFile);
		FileOutputStream os = new FileOutputStream(diffFile);
		try {
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(os,
					Charset.forName("UTF-8"));
			BufferedWriter writer = new BufferedWriter(outputStreamWriter);

			string sectionName = null;
			try {
				for (int i = 0, size = lines.Count; i < size; ++i) {
					string line = lines[i];
					if (line.length() == 0 && sectionName != null) {
						// Leave section
						if (sectionName == "TimingPoints") {
							List<string> timingPointLines = keysoundingStrategy
									.rewriteTimingPoints(timingPoints,
											keysounds);

							foreach (string timingPointLine in timingPointLines) {
								writer.append(timingPointLine);
								writer.newLine();
							}
						}

						sectionName = null;
					}

					if (line.StartsWith("[") && line.EndsWith("]")) {
						// Enter section
						sectionName = line.Substring(1, line.Length - 1);

						writer.append(line);
						writer.newLine();

						if (sectionName == "HitObjects") {
							foreach (Keysound keysound in keysounds) {
								if (keysound.type != Keysound.Type.HITOBJECT)
									continue;

								string[] keysoundDataLines = keysound.data
										.split("\n");
								int volume = 133 / keysoundDataLines.Length;
								for (string keysoundData : keysoundDataLines) {
									writer.append(keysoundingStrategy
											.rewriteKeysoundData(keysound,
													keysoundData, volume));
									writer.newLine();
								}
							}
						}

					} else if (sectionName != null) {
						// Inside a section
						if (sectionName == "Events") {
							if (!line.StartsWith("Sample")) {
								writer.append(line);
								writer.newLine();

								if (line == "//Storyboard Sound Samples") {
									foreach (Keysound keysound in keysounds) {
										if (keysound.type != Keysound.Type.AUTO)
											continue;

										writer.append("Sample,"
												+ keysound.startTime + ",0,\""
												+ keysound.filename + "\",100");
										writer.newLine();
									}
								}
							}

						} else if (sectionName != "HitObjects"
								&& sectionName != "TimingPoints") {

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

	private List<string> retrieveLines(File file) {

		List<string> lines = new List<string>();
		FileInputStream is = new FileInputStream(file);
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(is,
					Charset.forName("UTF-8"));
			BufferedReader reader = new BufferedReader(inputStreamReader);
			try {
				String line;
				while ((line = reader.readLine()) != null) {
					line = line.Trim();
					lines.Add(line);
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
}