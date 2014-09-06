package com.damnae.osukeysoundsplitter;

import com.damnae.osukeysoundsplitter.audio.AudioTrackInfo;

public abstract class BaseAudioTrackInfo implements AudioTrackInfo {

	@Override
	public int getBytesPerSample() {
		return (getBitsPerSample() / 8) * getChannels();
	}

	@Override
	public int getBytesPerSecond() {
		return getSampleRate() * getBytesPerSample();
	}

	@Override
	public String toString() {
		return "SampleRate=" + getSampleRate() + " Channels=" + getChannels()
				+ " BPS=" + getBitsPerSample();
	}
}
