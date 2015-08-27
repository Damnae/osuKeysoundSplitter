import com.damnae.osukeysoundsplitter.KeysoundCache;

namespace osuKeysoundSplitter.PathProvider
{
public class CounterKeysoundPathProvider extends BaseKeysoundPathProvider {
	private String keysoundsFolderName;
	private int fileIndex;

	public CounterKeysoundPathProvider(KeysoundCache keysoundCache) {
		super(keysoundCache);
	}

	public CounterKeysoundPathProvider(KeysoundCache keysoundCache,
			String keysoundsFolderName) {

		super(keysoundCache);
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
}