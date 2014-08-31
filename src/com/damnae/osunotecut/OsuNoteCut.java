package com.damnae.osunotecut;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.PCMProcessor;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteData;
import org.kc7bfi.jflac.util.WavWriter;

import com.damnae.osunotecut.OsuDiff.AudioArea;

public class OsuNoteCut {

	private WavWriter wav;

	public void process(File diffFile, File keysoundsFile) throws IOException {
		OsuDiff osuDiff = new OsuDiff(diffFile);
	}
}
