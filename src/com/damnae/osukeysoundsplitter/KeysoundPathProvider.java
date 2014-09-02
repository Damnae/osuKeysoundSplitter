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

	public String getKeysoundPath(String folderPath, byte[] data,
			String extension) {

		byte[] mdbytes = messageDigest.digest(data);

		for (byte mdbyte : mdbytes) {
			sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16)
					.substring(1));
		}
		String hash = sb.toString();
		sb.setLength(0);

		String path = existingKeysounds.get(hash);
		if (path == null) {
			path = folderPath + fileIndex++ + "." + extension;
			existingKeysounds.put(hash, path);
		}

		return path;
	}
}
