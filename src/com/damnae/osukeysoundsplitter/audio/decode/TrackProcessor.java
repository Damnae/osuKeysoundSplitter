package com.damnae.osukeysoundsplitter.audio.decode;

import com.damnae.osukeysoundsplitter.audio.AudioTrackInfo;

public interface TrackProcessor {

	void processTrackInfo(final AudioTrackInfo info);

	void processPCM(byte[] data, int length);

	void decodingCompleted();
}