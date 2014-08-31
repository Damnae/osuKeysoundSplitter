package com.damnae.osunotecut;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.kc7bfi.jflac.FLACDecoder;

import com.damnae.osunotecut.OsuDiff.AudioArea;

public class OsuNoteCut {

	class Keysound {
		public long startTime;
		public long endTime;
		public boolean isEvent;
	}

	public List<Keysound> keysounds = new ArrayList<Keysound>();

	public void process(File diffFile, File keysoundsFile) throws IOException {
		OsuDiff osuDiff = new OsuDiff(diffFile);
		List<Keysound> keysounds = getKeysounds(osuDiff);
		extractKeysounds(keysoundsFile, keysounds);
	}

	private List<Keysound> getKeysounds(OsuDiff osuDiff) {
		// This assumes audioArea.noteTimes is sorted,
		// which is as long as the .osu is

		List<Keysound> keysounds = new ArrayList<Keysound>();
		for (AudioArea audioArea : osuDiff.audioAreas) {
			if (audioArea.noteTimes.isEmpty()) {
				Keysound keysound = new Keysound();
				keysound.startTime = audioArea.startTime;
				keysound.endTime = audioArea.endTime;
				keysound.isEvent = true;
				keysounds.add(keysound);

			} else {
				Keysound keysound = new Keysound();
				keysound.startTime = audioArea.startTime;
				keysound.endTime = audioArea.noteTimes.get(0);
				keysound.isEvent = true;
				keysounds.add(keysound);
			}

			for (int i = 0, size = audioArea.noteTimes.size(); i < size; ++i) {
				long startTime = audioArea.noteTimes.get(i);
				long endTime = i < size - 1 ? audioArea.noteTimes.get(i + 1)
						: audioArea.endTime;

				Keysound keysound = new Keysound();
				keysound.startTime = startTime;
				keysound.endTime = endTime;
				keysound.isEvent = false;
				keysounds.add(keysound);
			}
		}

		return keysounds;
	}

	private void extractKeysounds(File keysoundsFile, List<Keysound> keysounds)
			throws IOException {

		KeysoundExtractor keysoundExtractor = new KeysoundExtractor(keysounds,
				new WavKeysoundWriter(getKeysoundFolderPath(keysoundsFile)));

		FileInputStream is = new FileInputStream(keysoundsFile);
		FLACDecoder decoder = new FLACDecoder(is);
		decoder.addPCMProcessor(keysoundExtractor);
		decoder.decode();

		keysoundExtractor.complete();
	}

	private String getKeysoundFolderPath(File keysoundsFile) throws IOException {
		String keysoundsPath = keysoundsFile.getCanonicalPath();
		int extensionPos = keysoundsPath.lastIndexOf('.');
		if (extensionPos < 0)
			throw new InvalidParameterException(keysoundsPath);

		return keysoundsPath.substring(0, extensionPos) + "/";
	}
}
