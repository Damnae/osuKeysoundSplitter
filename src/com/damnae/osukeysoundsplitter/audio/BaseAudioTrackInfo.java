package com.damnae.osukeysoundsplitter.audio;

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
