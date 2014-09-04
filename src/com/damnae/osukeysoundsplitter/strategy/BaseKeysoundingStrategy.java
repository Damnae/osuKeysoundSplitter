package com.damnae.osukeysoundsplitter.strategy;

import java.util.List;

import com.damnae.osukeysoundsplitter.KeysoundProcessor.Keysound;
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

	protected boolean isNoteOrCircle(int flags) {
		return (flags & 1) != 0;
	}

	protected boolean isLongNote(int flags) {
		return (flags & 128) != 0;
	}

	protected boolean isSlider(int flags) {
		return (flags & 2) != 0;
	}

	protected boolean isSpinner(int flags) {
		return (flags & 8) != 0;
	}
}
