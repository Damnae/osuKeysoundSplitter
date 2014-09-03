package com.damnae.osukeysoundsplitter.pathprovider;

public class CounterKeysoundPathProvider extends BaseKeysoundPathProvider {
	private int fileIndex;

	@Override
	protected String getNewKeysoundPath(String extension) {
		++fileIndex;
		return fileIndex + "." + extension;
	}
}
