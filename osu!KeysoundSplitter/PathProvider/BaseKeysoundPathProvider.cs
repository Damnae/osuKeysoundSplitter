import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.damnae.osukeysoundsplitter.KeysoundCache;

namespace osuKeysoundSplitter.PathProvider
{
public abstract class BaseKeysoundPathProvider implements KeysoundPathProvider {
	private KeysoundCache keysoundCache;
	private MessageDigest messageDigest;
	private StringBuffer sb = new StringBuffer();

	public BaseKeysoundPathProvider(KeysoundCache keysoundCache) {
		this.keysoundCache = keysoundCache;
		try {
			messageDigest = MessageDigest.getInstance("SHA1");

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getIdentifier(byte[] data) {
		byte[] mdbytes = messageDigest.digest(data);

		for (byte mdbyte : mdbytes) {
			sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16)
					.substring(1));
		}
		String identifier = sb.toString();
		sb.setLength(0);

		return identifier;
	}

	@Override
	public boolean isGenerated(String keysoundIdentifier) {
		return keysoundCache.hasKeysound(keysoundIdentifier);
	}

	@Override
	public String getKeysoundPath(String keysoundIdentifier, String extension) {
		String keysoundPath = keysoundCache.getKeysoundPath(keysoundIdentifier);

		if (keysoundPath != null && !isPathValid(keysoundPath))
			keysoundPath = null;

		if (keysoundPath == null) {
			while (keysoundPath == null
					|| !keysoundCache.isPathAvailable(keysoundPath)) {

				keysoundPath = getNewKeysoundPath(extension);
			}
			keysoundCache.register(keysoundIdentifier, keysoundPath);
		}

		return keysoundPath;
	}

	protected boolean isPathValid(String keysoundPath) {
		return true;
	}

	protected abstract String getNewKeysoundPath(String extension);
}
}