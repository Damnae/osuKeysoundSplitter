package com.damnae.osukeysoundsplitter.pathprovider;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class CounterKeysoundPathProvider implements KeysoundPathProvider {
	private MessageDigest messageDigest;
	private StringBuffer sb = new StringBuffer();

	private int fileIndex;
	private Map<String, String> keysoundPaths = new HashMap<String, String>();

	public CounterKeysoundPathProvider() {
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
	public boolean isRegistered(String keysoundIdentifier) {
		return keysoundPaths.containsKey(keysoundIdentifier);
	}

	@Override
	public String getKeysoundPath(String keysoundIdentifier, String extension) {
		String keysoundPath = keysoundPaths.get(keysoundIdentifier);
		if (keysoundPath == null) {
			++fileIndex;
			keysoundPath = fileIndex + "." + extension;
			keysoundPaths.put(keysoundIdentifier, keysoundPath);
		}

		return keysoundPath;
	}
}