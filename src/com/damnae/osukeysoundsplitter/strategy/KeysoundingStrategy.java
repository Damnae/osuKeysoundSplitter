package com.damnae.osukeysoundsplitter.strategy;

import java.util.List;

import com.damnae.osukeysoundsplitter.Keysound;
import com.damnae.osukeysoundsplitter.pathprovider.KeysoundPathProvider;

public interface KeysoundingStrategy {
	KeysoundPathProvider getKeysoundPathProvider();

	String rewriteKeysoundData(Keysound keysound, String keysoundData,
			int volume);

	List<String> rewriteTimingPoints(List<String> timingPointLines,
			List<Keysound> keysounds);
}
