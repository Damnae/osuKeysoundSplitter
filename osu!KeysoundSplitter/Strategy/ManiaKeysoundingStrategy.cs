import java.io.File;

import com.damnae.osukeysoundsplitter.Keysound;
import com.damnae.osukeysoundsplitter.KeysoundCache;
import com.damnae.osukeysoundsplitter.audio.encode.AudioEncoder;
import com.damnae.osukeysoundsplitter.pathprovider.CounterKeysoundPathProvider;

namespace osuKeysoundSplitter.Strategy
{
public class ManiaKeysoundingStrategy extends BaseKeysoundingStrategy {

	public ManiaKeysoundingStrategy(File mapsetFolder,
			KeysoundCache keysoundCache, AudioEncoder audioEncoder) {

		super(mapsetFolder, new CounterKeysoundPathProvider(keysoundCache),
				audioEncoder);
	}

	public ManiaKeysoundingStrategy(File mapsetFolder,
			KeysoundCache keysoundCache, AudioEncoder audioEncoder,
			String keysoundsFolderName) {

		super(mapsetFolder, new CounterKeysoundPathProvider(keysoundCache,
				keysoundsFolderName), audioEncoder);
	}

	@Override
	public String rewriteKeysoundData(Keysound keysound, String keysoundData,
			int volume) {

		int colonPos = keysoundData.lastIndexOf(":");
		if (colonPos > -1) {
			keysoundData = keysoundData.substring(0, colonPos) + ":"
					+ keysound.filename;
		}

		return keysoundData;
	}
}
}