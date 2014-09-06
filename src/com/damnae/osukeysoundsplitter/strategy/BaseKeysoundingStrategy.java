package com.damnae.osukeysoundsplitter.strategy;

import java.util.List;

import com.damnae.osukeysoundsplitter.Keysound;
import com.damnae.osukeysoundsplitter.pathprovider.KeysoundPathProvider;

public abstract class BaseKeysoundingStrategy implements KeysoundingStrategy {
	KeysoundPathProvider keysoundPathProvider;

	public BaseKeysoundingStrategy(KeysoundPathProvider keysoundPathProvider) {
		this.keysoundPathProvider = keysoundPathProvider;
	}

	@Override
	public KeysoundPathProvider getKeysoundPathProvider() {
		return keysoundPathProvider;
	}

	@Override
	public List<String> rewriteTimingPoints(List<String> timingPointLines,
			List<Keysound> keysounds) {

		return timingPointLines;
	}
}
