package com.damnae.osukeysoundsplitter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class KeysoundPathProvider {
	private MessageDigest messageDigest;
	private StringBuffer sb = new StringBuffer();

	private int fileIndex;
	private Map<String, String> existingKeysounds = new HashMap<String, String>();

	public KeysoundPathProvider() {
		try {
			messageDigest = MessageDigest.getInstance("SHA1");

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

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

	public boolean isRegistered(String keysoundIdentifier) {
		return existingKeysounds.containsKey(keysoundIdentifier);
	}

	public String getKeysoundPath(String folderPath, String keysoundIdentifier,
			String extension) {

		String path = existingKeysounds.get(keysoundIdentifier);
		if (path == null) {
			path = folderPath + fileIndex++ + "." + extension;
			existingKeysounds.put(keysoundIdentifier, path);
		}

		return path;
	}
}
