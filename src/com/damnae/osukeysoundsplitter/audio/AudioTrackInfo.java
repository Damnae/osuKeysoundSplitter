package com.damnae.osukeysoundsplitter.audio;

public interface AudioTrackInfo {

	int getSampleRate();

	int getBitsPerSample();

	int getChannels();

	int getBytesPerSample();

	int getBytesPerSecond();
}
