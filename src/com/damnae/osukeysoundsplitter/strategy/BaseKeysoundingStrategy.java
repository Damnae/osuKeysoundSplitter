package com.damnae.osukeysoundsplitter.strategy;

import com.damnae.osukeysoundsplitter.pathprovider.KeysoundPathProvider;

public class BaseKeysoundingStrategy implements KeysoundingStrategy {
	KeysoundPathProvider keysoundPathProvider;

	public BaseKeysoundingStrategy(KeysoundPathProvider keysoundPathProvider) {
		this.keysoundPathProvider = keysoundPathProvider;
	}

	@Override
	public KeysoundPathProvider getKeysoundPathProvider() {
		return keysoundPathProvider;
	}
}
