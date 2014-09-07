package com.damnae.osukeysoundsplitter.strategy;

import java.io.File;
import java.util.List;

import com.damnae.osukeysoundsplitter.Keysound;
import com.damnae.osukeysoundsplitter.TimingPoint;
import com.damnae.osukeysoundsplitter.audio.encode.AudioEncoder;
import com.damnae.osukeysoundsplitter.pathprovider.KeysoundPathProvider;

public abstract class BaseKeysoundingStrategy implements KeysoundingStrategy {
	private File mapsetFolder;
	private KeysoundPathProvider keysoundPathProvider;
	private AudioEncoder audioEncoder;

	public BaseKeysoundingStrategy(File mapsetFolder,
			KeysoundPathProvider keysoundPathProvider, AudioEncoder audioEncoder) {

		this.mapsetFolder = mapsetFolder;
		this.keysoundPathProvider = keysoundPathProvider;
		this.audioEncoder = audioEncoder;
	}

	@Override
	public File getMapsetFolder() {
		return mapsetFolder;
	}

	@Override
	public KeysoundPathProvider getKeysoundPathProvider() {
		return keysoundPathProvider;
	}

	@Override
	public AudioEncoder getAudioEncoder() {
		return audioEncoder;
	}

	@Override
	public List<String> rewriteTimingPoints(List<TimingPoint> timingPoints,
			List<Keysound> keysounds) {

		return TimingPoint.buildTimingPointLines(timingPoints);
	}
}
