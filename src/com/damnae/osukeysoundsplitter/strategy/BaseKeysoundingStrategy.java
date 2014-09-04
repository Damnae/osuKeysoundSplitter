package com.damnae.osukeysoundsplitter.strategy;

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
