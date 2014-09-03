package com.damnae.osukeysoundsplitter.pathprovider;

public class AdditionsKeysoundPathProvider extends BaseKeysoundPathProvider {
	private int sampleType;
	private int samplesetIndex;
	private int additionIndex;

	public AdditionsKeysoundPathProvider(int initialSampleType) {
		sampleType = Math.max(1, initialSampleType);
	}

	@Override
	protected String getNewKeysoundPath(String extension) {
		String path = buildPath(extension);
		incrementPath();
		return path;
	}

	private String buildPath(String extension) {
		return samplesetNames[samplesetIndex] + "-hit"
				+ additionNames[additionIndex]
				+ (sampleType > 1 ? sampleType : "") + "." + extension;
	}

	private void incrementPath() {
		++additionIndex;
		if (additionIndex >= additionNames.length) {
			additionIndex = 0;
			++samplesetIndex;
			if (samplesetIndex >= samplesetNames.length) {
				samplesetIndex = 0;
				++sampleType;
			}
		}
	}

	public static int getSampleType(String path) {
		int endPosition = path.lastIndexOf('.');
		for (int i = endPosition - 1; i >= 0; --i) {
			char c = path.charAt(i);
			if (!(c >= '0' && c <= '9'))
				return Integer.valueOf(path.substring(i + 1, endPosition));

		}
		return 0;
	}

	public static int getAdditionsSampleset(String path) {
		for (int i = 0, size = samplesetNames.length; i < size; ++i) {
			if (path.startsWith(samplesetNames[i]))
				return i + 1;
		}
		return 0;
	}

	public static int getAdditions(String path) {
		for (int i = 0, size = additionNames.length; i < size; ++i) {
			if (path.contains(additionNames[i]))
				return additionCodes[i];
		}
		return 0;
	}

	private static String[] samplesetNames = { "normal", "soft", "drum" };
	private static String[] additionNames = { "whistle", "finish", "clap" };
	private static int[] additionCodes = { 2, 4, 8 };
}
