import com.damnae.osukeysoundsplitter.KeysoundCache;

namespace osuKeysoundSplitter.PathProvider
{
public class AdditionsKeysoundPathProvider extends BaseKeysoundPathProvider {
	private int sampleSet;
	private int sampleTypeIndex;
	private int additionIndex;

	public AdditionsKeysoundPathProvider(KeysoundCache keysoundCache,
			int initialSampleType) {

		super(keysoundCache);
		sampleSet = Math.max(1, initialSampleType);
	}

	@Override
	protected String getNewKeysoundPath(String extension) {
		String path = buildPath(extension);
		incrementPath();
		return path;
	}

	private String buildPath(String extension) {
		return sampleTypeNames[sampleTypeIndex] + "-hit"
				+ additionNames[additionIndex]
				+ (sampleSet > 1 ? sampleSet : "") + "." + extension;
	}

	private void incrementPath() {
		++additionIndex;
		if (additionIndex >= additionNames.length) {
			additionIndex = 0;
			++sampleTypeIndex;
			if (sampleTypeIndex >= sampleTypeNames.length) {
				sampleTypeIndex = 0;
				++sampleSet;
			}
		}
	}

	@Override
	protected boolean isPathValid(String keysoundPath) {
		return !keysoundPath.contains("/") && !keysoundPath.contains("\\")
				&& keysoundPath.contains("-hit")
				&& getSampleType(keysoundPath) != 0
				&& getAdditions(keysoundPath) != 0;
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

	public static int getAdditions(String path) {
		for (int i = 0, size = additionNames.length; i < size; ++i) {
			if (path.contains(additionNames[i]))
				return additionCodes[i];
		}
		return 0;
	}

	private static String[] sampleTypeNames = { "normal", "soft", "drum" };
	private static String[] additionNames = { "whistle", "finish", "clap" };
	private static int[] additionCodes = { 2, 4, 8 };

}
}