package com.damnae.osukeysoundsplitter.audio.encode;

import java.io.File;
import java.io.IOException;

import com.damnae.osukeysoundsplitter.audio.AudioTrackInfo;

public interface AudioEncoder {

	void encode(File toFile, byte[] data, AudioTrackInfo info)
			throws IOException;

	void encodeSilence(File toFile) throws IOException;

	String getExtension();
}
