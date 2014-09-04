package com.damnae.osukeysoundsplitter.strategy;

import java.util.Arrays;

import com.damnae.osukeysoundsplitter.KeysoundProcessor.Keysound;
import com.damnae.osukeysoundsplitter.Utils;
import com.damnae.osukeysoundsplitter.pathprovider.HitnormalKeysoundPathProvider;

public class StandardKeysoundingStrategy extends BaseKeysoundingStrategy {

	public StandardKeysoundingStrategy(int initialSampleType) {
		super(new HitnormalKeysoundPathProvider(initialSampleType));
	}

	@Override
	public String rewriteKeysoundData(Keysound keysound, String keysoundData,
			int volume) {

		String[] values = keysoundData.split(",");
		final int flags = Integer.parseInt(values[3]);

		if (isNoteOrCircle(flags)) {
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

		} else if (isLongNote(flags)) {
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

		} else if (isSlider(flags)) {
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

		} else if (isSpinner(flags)) {

		}

		return keysoundData;
	}

}
