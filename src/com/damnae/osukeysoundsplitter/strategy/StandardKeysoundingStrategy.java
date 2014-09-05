package com.damnae.osukeysoundsplitter.strategy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.damnae.osukeysoundsplitter.KeysoundCache;
import com.damnae.osukeysoundsplitter.KeysoundProcessor.Keysound;
import com.damnae.osukeysoundsplitter.TimingPoint;
import com.damnae.osukeysoundsplitter.Utils;
import com.damnae.osukeysoundsplitter.pathprovider.HitnormalKeysoundPathProvider;

public class StandardKeysoundingStrategy extends BaseKeysoundingStrategy {

	public StandardKeysoundingStrategy(KeysoundCache keysoundCache,
			int initialSampleType) {
		super(new HitnormalKeysoundPathProvider(keysoundCache,
				initialSampleType));
	}

	@Override
	public String rewriteKeysoundData(Keysound keysound, String keysoundData,
			int volume) {

		String[] values = keysoundData.split(",");
		final int flags = Integer.parseInt(values[3]);

		if (Utils.isNoteOrCircle(flags)) {
			String[] hitsoundValues = Utils.splitValues(values[5], ':');
			hitsoundValues[0] = String.valueOf(HitnormalKeysoundPathProvider
					.getSampleType(keysound.filename));
			hitsoundValues[1] = "0";
			hitsoundValues[2] = String.valueOf(HitnormalKeysoundPathProvider
					.getSampleSet(keysound.filename));
			hitsoundValues[3] = String.valueOf(volume);
			hitsoundValues[4] = "";

			values[4] = "0";
			values[5] = Utils.joinValues(hitsoundValues, ":");
			keysoundData = Utils.joinValues(values, ",");

		} else if (Utils.isLongNote(flags)) {
			String[] lnValues = Utils.splitValues(values[5], ':');
			lnValues[1] = String.valueOf(HitnormalKeysoundPathProvider
					.getSampleType(keysound.filename));
			lnValues[2] = "0";
			lnValues[3] = String.valueOf(HitnormalKeysoundPathProvider
					.getSampleSet(keysound.filename));
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
			for (int i = 0; i < nodeCount; ++i)
				additionValues[i] = "0";

			String[] sampleTypeValues = new String[nodeCount];
			for (int i = 0; i < nodeCount; ++i)
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

		return Utils.buildTimingPointLines(timingPoints);
	}

	private List<TimingPoint> parseTimingPoints(List<String> timingPointLines) {
		List<TimingPoint> timingPoints = new ArrayList<TimingPoint>(
				timingPointLines.size());

		double previousNonInheritedBeatDuration = 0;
		for (String timingPointLine : timingPointLines) {
			TimingPoint timingPoint = Utils.parseTimingPoint(timingPointLine,
					previousNonInheritedBeatDuration);

			if (!timingPoint.isInherited)
				previousNonInheritedBeatDuration = timingPoint.secondValue;
			timingPoints.add(timingPoint);
		}

		Utils.sortTimingPoints(timingPoints);

		return timingPoints;
	}

	private void removeKeysounding(List<TimingPoint> timingPoints) {
		for (TimingPoint timingPoint : timingPoints) {
			timingPoint.sampleType = 2;
			timingPoint.sampleSet = 0;
			timingPoint.volume = 100;
		}
		Utils.simplifyTimingPoints(timingPoints);
	}

	private void insertKeysounds(List<TimingPoint> timingPoints,
			List<Keysound> keysounds) {

		for (Keysound keysound : keysounds) {
			if (keysound.type == Keysound.Type.HITOBJECT) {

				String[] keysoundDataLines = keysound.data.split("\n");
				if (keysoundDataLines.length > 1)
					continue;

				String keysoundData = keysoundDataLines[0];
				String[] values = keysoundData.split(",");
				final int flags = Integer.parseInt(values[3]);

				if (!Utils.isSlider(flags))
					continue;

			} else if (keysound.type != Keysound.Type.LINE) {
				continue;
			}

			if (keysound.filename == null)
				continue;

			long startTime = keysound.startTime;

			// Reset normal timing point values
			Utils.getOrCreateTimingPoint(timingPoints, keysound.endTime);

			// Silence slider body / repeats / end
			TimingPoint silentTimingPoint = Utils.getOrCreateTimingPoint(
					timingPoints, startTime + 10);
			silentTimingPoint.volume = 5;

			// Set head sampleset and volume
			TimingPoint timingPoint = Utils.getOrCreateTimingPoint(
					timingPoints, startTime);
			timingPoint.sampleType = HitnormalKeysoundPathProvider
					.getSampleType(keysound.filename);
			timingPoint.sampleSet = HitnormalKeysoundPathProvider
					.getSampleSet(keysound.filename);
			timingPoint.volume = 100;
		}
		Utils.simplifyTimingPoints(timingPoints);
	}
}
