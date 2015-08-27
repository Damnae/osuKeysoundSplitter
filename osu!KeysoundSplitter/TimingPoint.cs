using System;
using System.Collections.Generic;
using System.Text;

namespace osuKeysoundSplitter
{
public class TimingPoint {
	private static const int TIMING_POINT_LENIENCY = 5; //ms
	
	public long time;
	public double secondValue;
	public int beatPerMeasure;
	public int sampleType;
	public int sampleSet;
	public int volume;
	public bool isInherited;
	public bool isKiai;

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

	public bool isSimilar(TimingPoint previousTimingPoint) {
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

	public override string toString() {
		StringBuilder sb = new StringBuilder();
		sb.Append(time);
		sb.Append(",");
		sb.Append(secondValue);
		sb.Append(",");
		sb.Append(beatPerMeasure);
		sb.Append(",");
		sb.Append(sampleType);
		sb.Append(",");
		sb.Append(sampleSet);
		sb.Append(",");
		sb.Append(volume);
		sb.Append(",");
		sb.Append(isInherited ? 0 : 1);
		sb.Append(",");
		sb.Append(isKiai ? 1 : 0);
		return sb.ToString();
	}

	public static TimingPoint parseTimingPoint(string timingPointLine,
			double previousNonInheritedBeatDuration) {

		string[] values = timingPointLine.Split(",");

		if (values.Length < 2)
			throw new Exception("Timing point has less than 2 values: "
					+ timingPointLine);

		TimingPoint timingPoint = new TimingPoint();
		timingPoint.time = (long) Double.parseDouble(values[0]);
		timingPoint.secondValue = Double.parseDouble(values[1]);
		timingPoint.beatPerMeasure = values.Length > 2 ? Integer
				.parseInt(values[2]) : 4;
		timingPoint.sampleType = values.Length > 3 ? Integer
				.parseInt(values[3]) : 1;
		timingPoint.sampleSet = values.Length > 4 ? Integer.parseInt(values[4])
				: 1;
		timingPoint.volume = values.Length > 5 ? Integer.parseInt(values[5])
				: 100;
		timingPoint.isInherited = values.Length > 6 ? Integer
				.parseInt(values[6]) == 0 : false;
		timingPoint.isKiai = values.Length > 7 ? Integer.parseInt(values[7]) != 0
				: false;

		if (timingPoint.isInherited)
			timingPoint.prevousBeatDuration = previousNonInheritedBeatDuration;

		return timingPoint;
	}

	public static List<string> buildTimingPointLines(
			List<TimingPoint> timingPoints) {

		List<String> timingPointLines = new List<string>(
				timingPoints.Count);

		foreach (TimingPoint timingPoint in timingPoints)
			timingPointLines.Add(timingPoint.toString());

		return timingPointLines;
	}

	public static void simplifyTimingPoints(List<TimingPoint> timingPoints) {
		TimingPoint previousTimingPoint = null;

		int i = 0;
		while (i < timingPoints.Count) {
			TimingPoint timingPoint = timingPoints[i];
			if (previousTimingPoint != null) {

				if (timingPoint.isSimilar(previousTimingPoint)) {

					timingPoints.RemoveAt(i);
					continue;
				}
			}
			previousTimingPoint = timingPoint;
			++i;
		}
	}

	public static TimingPoint getTimingPointAtTime(
			List<TimingPoint> timingPoints, long time) {

		if (timingPoints == null || timingPoints.Count == 0)
			return null;

		TimingPoint currentTimingPoint = timingPoints[0];
		foreach (TimingPoint timingsPoint in timingPoints) {
			if (timingsPoint.time - time <= TIMING_POINT_LENIENCY) {
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

		if (Math.Abs(timingPoint.time - time) > TIMING_POINT_LENIENCY) {
			timingPoint = timingPoint.createInherited(time);
			timingPoints.Add(timingPoint);
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
}