package com.damnae.osukeysoundsplitter.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.damnae.osukeysoundsplitter.KeysoundProcessor.Keysound;
import com.damnae.osukeysoundsplitter.Utils;
import com.damnae.osukeysoundsplitter.pathprovider.HitnormalKeysoundPathProvider;

public class StandardKeysoundingStrategy extends BaseKeysoundingStrategy {
	private int initialSampleType;

	public StandardKeysoundingStrategy(int initialSampleType) {
		super(new HitnormalKeysoundPathProvider(initialSampleType));
		this.initialSampleType = initialSampleType;
	}

	@Override
	public String rewriteKeysoundData(Keysound keysound, String keysoundData,
			int volume) {

		String[] values = keysoundData.split(",");
		final int flags = Integer.parseInt(values[3]);

		if (Utils.isNoteOrCircle(flags)) {
			String[] hitsoundValues = Utils.splitValues(values[5], ':');
			hitsoundValues[0] = String.valueOf(HitnormalKeysoundPathProvider
					.getAdditionsSampleset(keysound.filename));
			hitsoundValues[1] = "0";
			hitsoundValues[2] = String.valueOf(HitnormalKeysoundPathProvider
					.getSampleType(keysound.filename));
			hitsoundValues[3] = String.valueOf(volume);
			hitsoundValues[4] = "";

			values[4] = "0";
			values[5] = Utils.joinValues(hitsoundValues, ":");
			keysoundData = Utils.joinValues(values, ",");

		} else if (Utils.isLongNote(flags)) {
			String[] lnValues = Utils.splitValues(values[5], ':');
			lnValues[1] = String.valueOf(HitnormalKeysoundPathProvider
					.getAdditionsSampleset(keysound.filename));
			lnValues[2] = "0";
			lnValues[3] = String.valueOf(HitnormalKeysoundPathProvider
					.getSampleType(keysound.filename));
			lnValues[4] = String.valueOf(volume);
			lnValues[5] = "";

			values[4] = "0";
			values[5] = Utils.joinValues(lnValues, ":");
			keysoundData = Utils.joinValues(values, ",");

		} else if (Utils.isSlider(flags)) {
			if (values.length < 10)
				values = Arrays.copyOf(values, 10);

			final int nodeCount = Integer.parseInt(values[6]) + 1;

			String[] additionValues = new String[nodeCount];
			additionValues[0] = "0";
			for (int i = 1; i < nodeCount; ++i)
				additionValues[i] = "0";

			String[] sampleTypeValues = new String[nodeCount];
			sampleTypeValues[0] = String.valueOf(HitnormalKeysoundPathProvider
					.getAdditionsSampleset(keysound.filename)) + ":0";
			for (int i = 1; i < nodeCount; ++i)
				sampleTypeValues[i] = "0:0";

			values[8] = Utils.joinValues(additionValues, "|");
			values[9] = Utils.joinValues(sampleTypeValues, "|");
			keysoundData = Utils.joinValues(values, ",");

		} else if (Utils.isSpinner(flags)) {

		}

		return keysoundData;
	}

	@Override
	public List<String> rewriteTimingPoints(List<String> timingPointLines,
			List<Keysound> keysounds) {

		List<TimingPoint> timingPoints = parseTimingPoints(timingPointLines);
		removeKeysounding(timingPoints);
		insertKeysounds(timingPoints, keysounds);

		return buildTimingPointLines(timingPoints);
	}

	private void insertKeysounds(List<TimingPoint> timingPoints,
			List<Keysound> keysounds) {

		for (Keysound keysound : keysounds) {
			if (keysound.isAutosound)
				continue;

			String[] keysoundDataLines = keysound.data.split("\n");
			if (keysoundDataLines.length > 1)
				continue;

			String keysoundData = keysoundDataLines[0];
			String[] values = keysoundData.split(",");
			final long startTime = Integer.parseInt(values[2]);
			final int flags = Integer.parseInt(values[3]);

			if (!Utils.isSlider(flags))
				continue;

			// Reset normal timing point values
			getOrCreateTimingPoint(timingPoints, keysound.endTime);

			// Silence slider body / repeats / end
			TimingPoint silentTimingPoint = getOrCreateTimingPoint(
					timingPoints, startTime + 10);
			silentTimingPoint.volume = 5;

			// Set head sampleset and volume
			TimingPoint timingPoint = getOrCreateTimingPoint(timingPoints,
					startTime);
			timingPoint.sampleSet = HitnormalKeysoundPathProvider
					.getSampleType(keysound.filename);
			timingPoint.volume = 100;
		}
		simplifyTimingPoints(timingPoints);
	}

	private TimingPoint getOrCreateTimingPoint(List<TimingPoint> timingPoints,
			long time) {

		TimingPoint timingPoint = Utils
				.getTimingPointAtTime(timingPoints, time);

		if (Math.abs(timingPoint.time - time) > 1) {
			timingPoint = timingPoint.createInherited(time);
			timingPoints.add(timingPoint);
		}
		sortTimingPoints(timingPoints);

		return timingPoint;
	}

	private void removeKeysounding(List<TimingPoint> timingPoints) {
		for (TimingPoint timingPoint : timingPoints) {
			timingPoint.sampleType = 2;
			timingPoint.sampleSet = 0;
			timingPoint.volume = 100;
		}
		simplifyTimingPoints(timingPoints);
	}

	private void simplifyTimingPoints(List<TimingPoint> timingPoints) {
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

	private List<TimingPoint> parseTimingPoints(List<String> timingPointLines) {
		List<TimingPoint> timingPoints = new ArrayList<TimingPoint>(
				timingPointLines.size());

		double previousNonInheritedBeatDuration = 0;
		for (String timingPointLine : timingPointLines) {
			String[] values = timingPointLine.split(",");

			if (values.length < 2)
				throw new RuntimeException(
						"Timing point has less than the 2 values: "
								+ timingPointLine);

			TimingPoint timingPoint = new TimingPoint();
			timingPoint.time = Long.parseLong(values[0]);
			timingPoint.secondValue = Double.parseDouble(values[1]);
			timingPoint.beatPerMeasure = values.length > 2 ? Integer
					.parseInt(values[2]) : 4;
			timingPoint.sampleType = values.length > 3 ? Integer
					.parseInt(values[3]) : 1;
			timingPoint.sampleSet = values.length > 4 ? Integer
					.parseInt(values[4]) : 1;
			timingPoint.volume = values.length > 5 ? Integer
					.parseInt(values[5]) : 100;
			timingPoint.isInherited = values.length > 6 ? Integer
					.parseInt(values[6]) == 0 : false;
			timingPoint.isKiai = values.length > 7 ? Integer
					.parseInt(values[7]) != 0 : false;

			if (timingPoint.isInherited) {
				timingPoint.prevousBeatDuration = previousNonInheritedBeatDuration;

			} else {
				previousNonInheritedBeatDuration = timingPoint.secondValue;
			}
			timingPoints.add(timingPoint);
		}

		sortTimingPoints(timingPoints);

		return timingPoints;
	}

	private void sortTimingPoints(List<TimingPoint> timingPoints) {
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

	private List<String> buildTimingPointLines(List<TimingPoint> timingPoints) {
		List<String> timingPointLines = new ArrayList<String>(
				timingPoints.size());

		for (TimingPoint timingPoint : timingPoints)
			timingPointLines.add(timingPoint.toString());

		return timingPointLines;
	}

	public static class TimingPoint {
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
			if (getBeatDuration() != previousTimingPoint.getBeatDuration()
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
	}
}
