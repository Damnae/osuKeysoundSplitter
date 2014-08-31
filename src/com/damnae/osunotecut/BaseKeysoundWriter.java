package com.damnae.osunotecut;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.kc7bfi.jflac.metadata.StreamInfo;

public abstract class BaseKeysoundWriter implements KeysoundWriter {
	private String mapFolderPath;
	private String keysoundFolderPath;

	public BaseKeysoundWriter(String mapFolderPath, String keysoundFolderPath) {
		this.mapFolderPath = mapFolderPath;
		this.keysoundFolderPath = keysoundFolderPath;

		File folder = new File(mapFolderPath + keysoundFolderPath);
		folder.mkdir();
	}

	@Override
	public String writeKeysound(String filename, byte[] data,
			StreamInfo streamInfo) throws IOException {

		String path = keysoundFolderPath + filename + "." + getExtension();
		FileOutputStream os = new FileOutputStream(mapFolderPath + path);
		try {
			writeKeysound(os, data, streamInfo);

		} finally {
			os.close();
		}

		return path;
	}

	protected abstract String getExtension();

	protected abstract void writeKeysound(FileOutputStream os, byte[] data,
			StreamInfo streamInfo) throws IOException;
}
