package com.damnae.osukeysoundsplitter.audio.encode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.damnae.osukeysoundsplitter.audio.AudioTrackInfo;

public abstract class BaseAudioEncoder implements AudioEncoder {

	@Override
	public void encode(File toFile, byte[] data, AudioTrackInfo info)
			throws IOException {

		toFile.getParentFile().mkdirs();

		FileOutputStream os = new FileOutputStream(toFile);
		try {
			encode(os, data, info);

		} finally {
			os.close();
		}
	}

	protected abstract void encode(FileOutputStream os, byte[] data,
			AudioTrackInfo info) throws IOException;
}
