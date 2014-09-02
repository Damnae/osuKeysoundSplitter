package com.damnae.osukeysoundsplitter;

import java.io.IOException;

public class Main {

	public static final String FOLDER_PATH = "C:/Games/osu!/Songs/egoizminstr/";
	public static final int OFFSET = 10;

	public static void main(String[] args) throws IOException {
		new MapsetProcessor().process(FOLDER_PATH, OFFSET);
		System.out.println("Done");
	}
}
