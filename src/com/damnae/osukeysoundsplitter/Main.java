package com.damnae.osukeysoundsplitter;

import java.io.File;
import java.io.IOException;

public class Main {

	public static final String FOLDER_PATH = "C:/Games/osu!/Songs/egoizminstr/";
	public static final String DIFF_FILE = "la la larks - ego-izm -Instrumental- (Damnae) [voice].osu";
	public static final String KEYSOUNDS_FILE = "voice.flac";

	public static void main(String[] args) throws IOException {
		File diffFile = new File(FOLDER_PATH + DIFF_FILE);
		File keysoundsFile = new File(FOLDER_PATH + KEYSOUNDS_FILE);

		new KeysoundProcessor().process(diffFile, keysoundsFile);
		System.out.println("Done");
	}
}
