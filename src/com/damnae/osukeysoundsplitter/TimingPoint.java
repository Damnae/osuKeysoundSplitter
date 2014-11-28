package com.damnae.osukeysoundsplitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TimingPoint {
	public long time;
	public double secondValue;
	public int beatPerMeasure;
	public int sampleType;
	public int sampleSet;
	public int volume;
	public boolean isInherited;
	public boolean isKiai;

	public double prevousBeatDuration;

	public double getBeatDuration() {
		if (isInherited)
			return prevousBeatDuration;
		return secondValue;
	}

	public TimingPoint createInherited(long startTime) {
		TimingPoint timingPoint = new TimingPoint();
		timingPoint.time = startTime;
		timingPoint.secondValue = isInherited ? secondValue : -100;
		timingPoint.beatPerMeasure = beatPerMeasure;
		timingPoint.sampleType = sampleType;
		timingPoint.sampleSet = sampleSet;
		timingPoint.volume = volume;
		timingPoint.isInherited = true;
		timingPoint.isKiai = isKiai;
		timingPoint.prevousBeatDuration = isInherited ? prevousBeatDuration
				: secondValue;
		return timingPoint;
	}

	public double getMultiplier() {
		if (isInherited)
			return -(secondValue / 100.0);
		return 1;
	}

	public boolean isSimilar(TimingPoint previousTimingPoint) {
		if (!isInherited && previousTimingPoint.isInherited
				|| getBeatDuration() != previousTimingPoint.getBeatDuration()
				|| getMultiplier() != previousTimingPoint.getMultiplier()
				|| beatPerMeasure != previousTimingPoint.beatPerMeasure
				|| sampleType != previousTimingPoint.sampleType
				|| sampleSet != previousTimingPoint.sampleSet
				|| volume != previousTimingPoint.volume) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(time);
		sb.append(",");
		sb.append(secondValue);
		sb.append(",");
		sb.append(beatPerMeasure);
		sb.append(",");
		sb.append(sampleType);
		sb.append(",");
		sb.append(sampleSet);
		sb.append(",");
		sb.append(volume);
		sb.append(",");
		sb.append(isInherited ? 0 : 1);
		sb.append(",");
		sb.append(isKiai ? 1 : 0);
		return sb.toString();
	}

	public static TimingPoint parseTimingPoint(String timingPointLine,
			double previousNonInheritedBeatDuration) {

		String[] values = timingPointLine.split(",");

		if (values.length < 2)
			throw new RuntimeException("Timing point has less than 2 values: "
					+ timingPointLine);

		TimingPoint timingPoint = new TimingPoint();
		timingPoint.time = (long) Double.parseDouble(values[0]);
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

	public static List<String> buildTimingPointLines(
			List<TimingPoint> timingPoints) {

		List<String> timingPointLines = new ArrayList<String>(
				timingPoints.size());

		for (TimingPoint timingPoint : timingPoints)
			timingPointLines.add(timingPoint.toString());

		return timingPointLines;
	}

	public static void simplifyTimingPoints(List<TimingPoint> timingPoints) {
		TimingPoint previousTimingPoint = null;

		int i = 0;
		while (i < timingPoints.size()) {
			TimingPoint timingPoint = timingPoints.get(i);
			if (previousTimingPoint != null) {

				if (timingPoint.isSimilar(previousTimingPoint)) {

					timingPoints.remove(i);
					continue;
				}
			}
			previousTimingPoint = timingPoint;
			++i;
		}
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

	public static void sortTimingPoints(List<TimingPoint> timingPoints) {
		Collections.sort(timingPoints, new Comparator<TimingPoint>() {

			@Override
			public int compare(TimingPoint t1, TimingPoint t2) {
				// Earlier timing points first
				int value = (int) (t1.time - t2.time);
				// Uninherited timing points first
				if (value == 0)
					value = (t2.isInherited ? 0 : 1) - (t1.isInherited ? 0 : 1);
				return value;
			}
		});
	}
}