package com.damnae.osukeysoundsplitter.pathprovider;

import com.damnae.osukeysoundsplitter.KeysoundCache;

public class HitnormalKeysoundPathProvider extends BaseKeysoundPathProvider {
	private int sampleSetIndex;
	private int sampleTypeIndex;

	public HitnormalKeysoundPathProvider(KeysoundCache keysoundCache,
			int initialSampleType) {

		super(keysoundCache);
		sampleSetIndex = Math.max(1, initialSampleType);
	}

	@Override
	protected String getNewKeysoundPath(String extension) {
		String path = buildPath(extension);
		incrementPath();
		return path;
	}

	private String buildPath(String extension) {
		return sampleTypeNames[sampleTypeIndex] + "-hitnormal"
				+ (sampleSetIndex > 1 ? sampleSetIndex : "") + "." + extension;
	}

	private void incrementPath() {
		++sampleTypeIndex;
		if (sampleTypeIndex >= sampleTypeNames.length) {
			sampleTypeIndex = 0;
			++sampleSetIndex;
		}
	}

	protected boolean isPathValid(String keysoundPath) {
		return !keysoundPath.contains("/") && !keysoundPath.contains("\\")
				&& keysoundPath.contains("-hitnormal")
				&& getSampleType(keysoundPath) != 0;
	}

	public static int getSampleSet(String path) {
		int endPosition = path.lastIndexOf('.');
		for (int i = endPosition - 1; i >= 0; --i) {
			char c = path.charAt(i);
			if (!(c >= '0' && c <= '9'))
				return Integer.valueOf(path.substring(i + 1, endPosition));

		}
		return 0;
	}

	public static int getSampleType(String path) {
		for (int i = 0, size = sampleTypeNames.length; i < size; ++i) {
			if (path.startsWith(sampleTypeNames[i]))
				return i + 1;
		}
		return 0;
	}

	private static String[] sampleTypeNames = { "normal", "soft", "drum" };
}
