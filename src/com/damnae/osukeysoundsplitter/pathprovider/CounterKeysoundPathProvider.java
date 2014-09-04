package com.damnae.osukeysoundsplitter.pathprovider;

public class CounterKeysoundPathProvider extends BaseKeysoundPathProvider {
	private String keysoundsFolderName;
	private int fileIndex;

	public CounterKeysoundPathProvider() {
	}

	public CounterKeysoundPathProvider(String keysoundsFolderName) {
		this.keysoundsFolderName = keysoundsFolderName;
	}

	@Override
	protected String getNewKeysoundPath(String extension) {
		++fileIndex;

		if (keysoundsFolderName == null)
			return fileIndex + "." + extension;

		return keysoundsFolderName + "/" + fileIndex + "." + extension;
	}
}
