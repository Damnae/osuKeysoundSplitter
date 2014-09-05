package com.damnae.osukeysoundsplitter;

import java.io.File;
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
			List<TimingPoint> timingPoints, double time) {

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
}
