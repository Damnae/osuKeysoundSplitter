
using System.Collections.Generic;
using System.IO;
namespace osuKeysoundSplitter
{
public class OsuDiff {

	public class DiffEvent {
		public long time;
		public string data;
		public bool isHitObject;

		public bool isLine() {
			return isHitObject && data == null;
		}

		public bool isSplittingPoint() {
			return !isHitObject && data == null;
		}

		public override string toString() {
			return time + " " + data;
		}
	}

	public List<DiffEvent> diffEvents = new List<DiffEvent>();
	public List<TimingPoint> timingPoints = new List<TimingPoint>();
	private double sliderMultiplier;

	public OsuDiff(File file) {
		FileInputStream is = new FileInputStream(file);
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(is,
					Charset.forName("UTF-8"));
			BufferedReader reader = new BufferedReader(inputStreamReader);

			string line;
			while ((line = reader.readLine()) != null) {
				line = line.Trim();
				if (line.StartsWith("[") && line.EndsWith("]")) {
					string sectionName = line.Substring(1, line.Length - 1);

					if (sectionName == "Editor") {
						parseOsuEditorSection(reader);

					} else if (sectionName == "Difficulty") {
						parseOsuDifficultySection(reader);

					} else if (sectionName == "TimingPoints") {
						parseOsuTimingPointsSection(reader);

					} else if (sectionName == "HitObjects") {
						parseOsuHitObjectsSection(reader);
					}
				}
			}

		} finally {
			is.close();
		}

		Collections.sort(diffEvents, new Comparator<DiffEvent>() {

			public override int compare(DiffEvent event1, DiffEvent event2) {
				int value = (int) (event1.time - event2.time);
				if (value == 0)
					value = (event1.data != null ? 1 : 0)
							- (event2.data != null ? 1 : 0);
				return value;
			}
		});
	}

	private void parseOsuTimingPointsSection(BufferedReader reader)
			throws IOException {

		double previousNonInheritedBeatDuration = 0;

		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty())
				break;

			TimingPoint timingPoint = TimingPoint.parseTimingPoint(line,
					previousNonInheritedBeatDuration);

			if (!timingPoint.isInherited)
				previousNonInheritedBeatDuration = timingPoint.secondValue;

			timingPoints.add(timingPoint);
		}

		TimingPoint.sortTimingPoints(timingPoints);
	}

	private void parseOsuEditorSection(BufferedReader reader)
			throws IOException {

		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty())
				break;

			String key = Utils.parseKeyValueKey(line);
			String value = Utils.parseKeyValueValue(line);

			if (key.equals("Bookmarks")) {
				String[] bookmarks = value.split(",");

				for (String bookmark : bookmarks) {
					DiffEvent diffEvent = new DiffEvent();
					diffEvent.time = Integer.valueOf(bookmark);
					diffEvent.isHitObject = false;
					diffEvents.add(diffEvent);
				}
			}
		}
	}

	private void parseOsuDifficultySection(BufferedReader reader)
			throws IOException {

		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty())
				break;

			String key = Utils.parseKeyValueKey(line);
			String value = Utils.parseKeyValueValue(line);

			if (key.equals("SliderMultiplier")) {
				sliderMultiplier = Double.parseDouble(value);
			}
		}
	}

	private void parseOsuHitObjectsSection(BufferedReader reader)
			throws IOException {

		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty())
				break;

			String[] values = line.split(",");
			final long startTime = Integer.parseInt(values[2]);
			final int flags = Integer.parseInt(values[3]);

			if (Utils.isSpinner(flags)) {
				final long endTime = Integer.parseInt(values[5]);

				DiffEvent startDiffEvent = new DiffEvent();
				startDiffEvent.time = startTime;
				startDiffEvent.data = null;
				startDiffEvent.isHitObject = false;
				diffEvents.add(startDiffEvent);

				DiffEvent endDiffEvent = new DiffEvent();
				endDiffEvent.time = endTime;
				endDiffEvent.data = line;
				endDiffEvent.isHitObject = true;
				diffEvents.add(endDiffEvent);

			} else {
				boolean isSimultaneous = false;
				for (DiffEvent diffEvent : diffEvents) {
					if (Math.abs(diffEvent.time - startTime) <= 2
							&& diffEvent.data != null) {

						diffEvent.data += "\n" + line;
						isSimultaneous = true;
						break;
					}
				}

				if (!isSimultaneous) {
					DiffEvent diffEvent = new DiffEvent();
					diffEvent.time = startTime;
					diffEvent.data = line;
					diffEvent.isHitObject = true;
					diffEvents.add(diffEvent);

					if (Utils.isSlider(flags)) {
						TimingPoint timingPoint = TimingPoint
								.getTimingPointAtTime(timingPoints, startTime);

						final int nodeCount = Integer.parseInt(values[6]) + 1;
						final double length = Double.parseDouble(values[7]);

						double sliderMultiplierLessLength = length
								/ sliderMultiplier;
						double lengthInBeats = sliderMultiplierLessLength / 100
								* timingPoint.getMultiplier();
						long repeatDuration = (long) (timingPoint
								.getBeatDuration() * lengthInBeats);

						for (int i = 1; i < nodeCount; ++i) {
							long nodeStartTime = startTime + i * repeatDuration;

							DiffEvent nodeDiffEvent = new DiffEvent();
							nodeDiffEvent.time = nodeStartTime;
							nodeDiffEvent.isHitObject = true;
							diffEvents.add(nodeDiffEvent);
						}
					}
				}
			}
		}
	}
}
}