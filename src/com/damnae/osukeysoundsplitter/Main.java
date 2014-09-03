package com.damnae.osukeysoundsplitter;

import java.io.File;
import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {
		if (args.length < 2) {
			System.out
					.println("Syntax: java -jar \"osu!KeysoundSplitter.jar\" mapsetPath keysoundsOffsetInMilliseconds");
			return;
		}

		File folder = new File(args[0]);
		int offset = Integer.valueOf(args[1]);

		new MapsetProcessor().process(folder, offset);
	}
}
