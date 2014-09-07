package com.damnae.osukeysoundsplitter.strategy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.damnae.osukeysoundsplitter.Keysound;
import com.damnae.osukeysoundsplitter.Keysound.Type;
import com.damnae.osukeysoundsplitter.KeysoundCache;
import com.damnae.osukeysoundsplitter.TimingPoint;
import com.damnae.osukeysoundsplitter.Utils;
import com.damnae.osukeysoundsplitter.audio.encode.AudioEncoder;
import com.damnae.osukeysoundsplitter.audio.encode.WavAudioEncoder;
import com.damnae.osukeysoundsplitter.pathprovider.HitnormalKeysoundPathProvider;

public class StandardKeysoundingStrategy extends BaseKeysoundingStrategy {
	private AudioEncoder silentAudioEncoder = new WavAudioEncoder();

	public StandardKeysoundingStrategy(File mapsetFolder,
			KeysoundCache keysoundCache, AudioEncoder audioEncoder,
			int initialSampleType) {

		super(mapsetFolder, new HitnormalKeysoundPathProvider(keysoundCache,
				initialSampleType), audioEncoder);
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
			String[] hitsoundValues = Utils.splitValues(values[6], ':');
			hitsoundValues[0] = "0";
			hitsoundValues[1] = "0";
			hitsoundValues[2] = "0";
			hitsoundValues[3] = "0";
			hitsoundValues[4] = "";

			values[4] = "0";
			values[6] = Utils.joinValues(hitsoundValues, ":");
			keysoundData = Utils.joinValues(values, ",");
		}

		return keysoundData;
	}

	@Override
	public List<String> rewriteTimingPoints(List<String> timingPointLines,
			List<Keysound> keysounds) {

		List<TimingPoint> timingPoints = parseTimingPoints(timingPointLines);
		removeKeysounding(timingPoints);
		insertKeysounds(timingPoints, keysounds);

		return TimingPoint.buildTimingPointLines(timingPoints);
	}

	private List<TimingPoint> parseTimingPoints(List<String> timingPointLines) {
		List<TimingPoint> timingPoints = new ArrayList<TimingPoint>(
				timingPointLines.size());

		double previousNonInheritedBeatDuration = 0;
		for (String timingPointLine : timingPointLines) {
			TimingPoint timingPoint = TimingPoint.parseTimingPoint(
					timingPointLine, previousNonInheritedBeatDuration);

			if (!timingPoint.isInherited)
				previousNonInheritedBeatDuration = timingPoint.secondValue;
			timingPoints.add(timingPoint);
		}

		TimingPoint.sortTimingPoints(timingPoints);

		return timingPoints;
	}

	private void removeKeysounding(List<TimingPoint> timingPoints) {
		for (TimingPoint timingPoint : timingPoints) {
			timingPoint.sampleType = 2;
			timingPoint.sampleSet = 0;
			timingPoint.volume = 100;
		}
		TimingPoint.simplifyTimingPoints(timingPoints);
	}

	private void insertKeysounds(List<TimingPoint> timingPoints,
			List<Keysound> keysounds) {

		for (int i = 0, size = keysounds.size(); i < size; ++i) {
			Keysound keysound = keysounds.get(i);

			boolean muteBody = keysound.type == Keysound.Type.LINE;
			if (keysound.type == Keysound.Type.HITOBJECT) {
				String[] keysoundDataLines = keysound.data.split("\n");
				if (keysoundDataLines.length > 1)
					continue;

				String keysoundData = keysoundDataLines[0];
				String[] values = keysoundData.split(",");
				final int flags = Integer.parseInt(values[3]);

				if (!Utils.isSlider(flags) && !Utils.isSpinner(flags))
					continue;

				muteBody = Utils.isSlider(flags);

			} else if (keysound.type != Keysound.Type.LINE) {
				continue;
			}

			muteBody = muteBody && i < size - 1
					&& keysounds.get(i + 1).type == Type.LINE;

			long startTime = keysound.startTime;
			long endTime = keysound.endTime;

			// Reset normal timing point values
			TimingPoint.getOrCreateTimingPoint(timingPoints, endTime);

			// Set head sampleset and volume
			TimingPoint timingPoint = TimingPoint.getOrCreateTimingPoint(
					timingPoints, startTime);
			timingPoint.sampleType = HitnormalKeysoundPathProvider
					.getSampleType(keysound.filename);
			timingPoint.sampleSet = HitnormalKeysoundPathProvider
					.getSampleSet(keysound.filename);
			timingPoint.volume = 100;

			if (muteBody) {
				// Silence slider ticks
				TimingPoint silentTimingPoint = TimingPoint
						.getOrCreateTimingPoint(timingPoints, startTime + 10);
				silentTimingPoint.volume = 5;

				// Make silent slider slide sample
				String sliderSliderPath = HitnormalKeysoundPathProvider
						.getSampleTypeName(timingPoint.sampleType)
						+ "-sliderslide"
						+ timingPoint.sampleSet
						+ "."
						+ silentAudioEncoder.getExtension();

				File silentFile = new File(getMapsetFolder(), sliderSliderPath);
				if (!silentFile.exists()) {
					try {
						silentAudioEncoder.encodeSilence(silentFile);

					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		TimingPoint.simplifyTimingPoints(timingPoints);
	}
}
