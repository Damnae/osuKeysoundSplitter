package com.damnae.osukeysoundsplitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OsuDiff {

	public class DiffEvent {
		public long time;
		public String data;
		public boolean isHitObject;

		public boolean isLine() {
			return isHitObject && data == null;
		}

		public boolean isSplittingPoint() {
			return !isHitObject && data == null;
		}

		@Override
		public String toString() {
			return time + " " + data;
		}
	}

	public List<DiffEvent> diffEvents = new ArrayList<DiffEvent>();
	public List<TimingPoint> timingPoints = new ArrayList<TimingPoint>();
	private double sliderMultiplier;

	public OsuDiff(File file) throws IOException {
		FileInputStream is = new FileInputStream(file);
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(is,
					Charset.forName("UTF-8"));
			BufferedReader reader = new BufferedReader(inputStreamReader);

			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("[") && line.endsWith("]")) {
					String sectionName = line.substring(1, line.length() - 1);

					if (sectionName.equals("Editor")) {
						parseOsuEditorSection(reader);

					} else if (sectionName.equals("Difficulty")) {
						parseOsuDifficultySection(reader);

					} else if (sectionName.equals("TimingPoints")) {
						parseOsuTimingPointsSection(reader);

					} else if (sectionName.equals("HitObjects")) {
						parseOsuHitObjectsSection(reader);
					}
				}
			}

		} finally {
			is.close();
		}

		Collections.sort(diffEvents, new Comparator<DiffEvent>() {

			@Override
			public int compare(DiffEvent event1, DiffEvent event2) {
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
