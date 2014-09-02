package com.damnae.osukeysoundsplitter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.kc7bfi.jflac.metadata.StreamInfo;

public abstract class BaseKeysoundWriter implements KeysoundWriter {
	private String mapFolderPath;
	private String keysoundFolderPath;
	private KeysoundPathProvider keysoundPathProvider;

	public BaseKeysoundWriter(String mapFolderPath, String keysoundFolderPath,
			KeysoundPathProvider keysoundPathProvider) {

		this.mapFolderPath = mapFolderPath;
		this.keysoundFolderPath = keysoundFolderPath;
		this.keysoundPathProvider = keysoundPathProvider;

		File folder = new File(mapFolderPath + keysoundFolderPath);
		folder.mkdir();
	}

	@Override
	public String writeKeysound(byte[] data, StreamInfo streamInfo)
			throws IOException {

		String path = keysoundPathProvider.getKeysoundPath(keysoundFolderPath,
				data, getExtension());
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
