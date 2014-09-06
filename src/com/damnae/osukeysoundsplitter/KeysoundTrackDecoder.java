package com.damnae.osukeysoundsplitter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.kc7bfi.jflac.FLACDecoder;

public class KeysoundTrackDecoder {
	private File keysoundTrackFile;
	private List<KeysoundExtractor> keysoundExtractors = new ArrayList<KeysoundExtractor>();

	public KeysoundTrackDecoder(File keysoundTrackFile) {
		this.keysoundTrackFile = keysoundTrackFile;
	}

	public void register(KeysoundExtractor keysoundExtractor) {
		if (keysoundExtractor == null)
			throw new InvalidParameterException(
					"keysoundExtractor must not be null");

		keysoundExtractors.add(keysoundExtractor);
	}

	public void decode() throws IOException {
		FileInputStream is = new FileInputStream(keysoundTrackFile);
		FLACDecoder decoder = new FLACDecoder(is);
		for (KeysoundExtractor keysoundExtractor : keysoundExtractors)
			decoder.addPCMProcessor(keysoundExtractor);

		decoder.decode();

		for (KeysoundExtractor keysoundExtractor : keysoundExtractors)
			keysoundExtractor.complete();
	}
}
