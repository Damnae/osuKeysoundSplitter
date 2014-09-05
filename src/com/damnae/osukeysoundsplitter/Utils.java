package com.damnae.osukeysoundsplitter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Utils {

	public static String getFileNameWithoutExtension(File file) {
		String name = file.getName();
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex == -1)
			return name;
		return name.substring(0, dotIndex);
	}

	public static String[] splitValues(String value, char separator) {
		int[] indexes = new int[value.length()];
		int indexCount = 0;
		for (int i = 0, size = value.length(); i < size; ++i) {
			char c = value.charAt(i);
			if (c == separator) {
				indexes[indexCount] = i;
				++indexCount;
			}
		}

		String[] values = new String[indexCount + 1];
		values[0] = value.substring(0, indexes[0]);
		for (int i = 1; i < indexCount; ++i) {
			values[i] = value.substring(indexes[i - 1] + 1, indexes[i]);
		}
		values[values.length - 1] = value.substring(
				indexes[indexCount - 1] + 1, value.length());

		return values;
	}

	public static String joinValues(String[] hitsoundValues, String separator) {
		StringBuilder sb = new StringBuilder();
		for (String hitsoundValue : hitsoundValues) {
			if (sb.length() > 0)
				sb.append(separator);
			sb.append(hitsoundValue);
		}
		return sb.toString();
	}

	public static TimingPoint getTimingPointAtTime(
			List<TimingPoint> timingPoints, long time) {

		if (timingPoints == null || timingPoints.size() == 0)
			return null;

		TimingPoint currentTimingPoint = timingPoints.get(0);
		for (TimingPoint timingsPoint : timingPoints) {
			if (timingsPoint.time - 1 < time) {
				currentTimingPoint = timingsPoint;

			} else {
				break;
			}
		}

		return currentTimingPoint;
	}

	public static boolean isNoteOrCircle(int flags) {
		return (flags & 1) != 0;
	}

	public static boolean isLongNote(int flags) {
		return (flags & 128) != 0;
	}

	public static boolean isSlider(int flags) {
		return (flags & 2) != 0;
	}

	public static boolean isSpinner(int flags) {
		return (flags & 8) != 0;
	}

	public static void sortTimingPoints(List<TimingPoint> timingPoints) {
		Collections.sort(timingPoints, new Comparator<TimingPoint>() {

			@Override
			public int compare(TimingPoint t1, TimingPoint t2) {
				int value = (int) (t1.time - t2.time);
				if (value == 0)
					value = (t1.isInherited ? 0 : 1) - (t2.isInherited ? 0 : 1);
				return value;
			}
		});
	}

	public static TimingPoint getOrCreateTimingPoint(
			List<TimingPoint> timingPoints, long time) {

		TimingPoint timingPoint = getTimingPointAtTime(timingPoints, time);

		if (Math.abs(timingPoint.time - time) > 1) {
			timingPoint = timingPoint.createInherited(time);
			timingPoints.add(timingPoint);
		}
		sortTimingPoints(timingPoints);

		return timingPoint;
	}

	public static void simplifyTimingPoints(List<TimingPoint> timingPoints) {
		TimingPoint previousTimingPoint = null;

		int i = 0;
		while (i < timingPoints.size()) {
			TimingPoint timingPoint = timingPoints.get(i);
			if (previousTimingPoint != null) {

				if (timingPoint.isInherited
						&& timingPoint.isSimilar(previousTimingPoint)) {

					timingPoints.remove(i);
					continue;
				}
			}
			previousTimingPoint = timingPoint;
			++i;
		}
	}

	public static List<String> buildTimingPointLines(
			List<TimingPoint> timingPoints) {

		List<String> timingPointLines = new ArrayList<String>(
				timingPoints.size());

		for (TimingPoint timingPoint : timingPoints)
			timingPointLines.add(timingPoint.toString());

		return timingPointLines;
	}

	public static TimingPoint parseTimingPoint(String timingPointLine,
			double previousNonInheritedBeatDuration) {

		String[] values = timingPointLine.split(",");

		if (values.length < 2)
			throw new RuntimeException("Timing point has less than 2 values: "
					+ timingPointLine);

		TimingPoint timingPoint = new TimingPoint();
		timingPoint.time = Long.parseLong(values[0]);
		timingPoint.secondValue = Double.parseDouble(values[1]);
		timingPoint.beatPerMeasure = values.length > 2 ? Integer
				.parseInt(values[2]) : 4;
		timingPoint.sampleType = values.length > 3 ? Integer
				.parseInt(values[3]) : 1;
		timingPoint.sampleSet = values.length > 4 ? Integer.parseInt(values[4])
				: 1;
		timingPoint.volume = values.length > 5 ? Integer.parseInt(values[5])
				: 100;
		timingPoint.isInherited = values.length > 6 ? Integer
				.parseInt(values[6]) == 0 : false;
		timingPoint.isKiai = values.length > 7 ? Integer.parseInt(values[7]) != 0
				: false;

		if (timingPoint.isInherited)
			timingPoint.prevousBeatDuration = previousNonInheritedBeatDuration;

		return timingPoint;
	}
}
