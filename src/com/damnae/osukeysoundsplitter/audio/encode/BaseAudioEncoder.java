package com.damnae.osukeysoundsplitter.audio.encode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.damnae.osukeysoundsplitter.audio.AudioTrackInfo;
import com.damnae.osukeysoundsplitter.audio.BaseAudioTrackInfo;

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

	@Override
	public void encodeSilence(File toFile) throws IOException {
		AudioTrackInfo info = new BaseAudioTrackInfo() {

			@Override
			public int getSampleRate() {
				return 44100;
			}

			@Override
			public int getChannels() {
				return 2;
			}

			@Override
			public int getBitsPerSample() {
				return 16;
			}
		};
		encode(toFile, new byte[0], info);
	}

	protected abstract void encode(FileOutputStream os, byte[] data,
			AudioTrackInfo info) throws IOException;
}
