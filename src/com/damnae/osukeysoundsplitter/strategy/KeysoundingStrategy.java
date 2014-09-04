package com.damnae.osukeysoundsplitter.strategy;

import com.damnae.osukeysoundsplitter.KeysoundProcessor.Keysound;
import com.damnae.osukeysoundsplitter.pathprovider.KeysoundPathProvider;

public interface KeysoundingStrategy {
	KeysoundPathProvider getKeysoundPathProvider();

	String rewriteKeysoundData(Keysound keysound, String keysoundData,
			int volume);
}
