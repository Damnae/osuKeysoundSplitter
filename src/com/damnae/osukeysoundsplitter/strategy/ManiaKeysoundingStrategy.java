package com.damnae.osukeysoundsplitter.strategy;

import com.damnae.osukeysoundsplitter.KeysoundProcessor.Keysound;
import com.damnae.osukeysoundsplitter.pathprovider.CounterKeysoundPathProvider;

public class ManiaKeysoundingStrategy extends BaseKeysoundingStrategy {

	public ManiaKeysoundingStrategy() {
		super(new CounterKeysoundPathProvider());
	}

	public ManiaKeysoundingStrategy(String keysoundsFolderName) {
		super(new CounterKeysoundPathProvider(keysoundsFolderName));
	}

	@Override
	public String rewriteKeysoundData(Keysound keysound, String keysoundData,
			int volume) {

		int colonPos = keysoundData.lastIndexOf(":");
		if (colonPos > -1) {
			keysoundData = keysoundData.substring(0, colonPos) + ":"
					+ keysound.filename;
		}

		return keysoundData;
	}
}
