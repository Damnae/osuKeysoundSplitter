package com.damnae.osukeysoundsplitter.strategy;

import java.io.File;
import java.util.List;

import com.damnae.osukeysoundsplitter.Keysound;
import com.damnae.osukeysoundsplitter.TimingPoint;
import com.damnae.osukeysoundsplitter.audio.encode.AudioEncoder;
import com.damnae.osukeysoundsplitter.pathprovider.KeysoundPathProvider;

public interface KeysoundingStrategy {
	File getMapsetFolder();

	AudioEncoder getAudioEncoder();

	KeysoundPathProvider getKeysoundPathProvider();

	String rewriteKeysoundData(Keysound keysound, String keysoundData,
			int volume);

	List<String> rewriteTimingPoints(List<TimingPoint> timingPoints,
			List<Keysound> keysounds);
}
