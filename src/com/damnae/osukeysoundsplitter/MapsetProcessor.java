package com.damnae.osukeysoundsplitter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

public class MapsetProcessor {
	private static final String KEYSOUND_TRACK_EXTENSION = ".flac";
	private static final String DIFF_FILE_EXTENSION = ".osu";

	public void process(String folderPath, int offset) throws IOException {
		File folder = new File(folderPath);

		File[] keysoundTrackFiles = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(KEYSOUND_TRACK_EXTENSION);
			}
		});

		File[] diffFiles = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(DIFF_FILE_EXTENSION);
			}
		});

		for (File keysoundTrackFile : keysoundTrackFiles) {
			String keysoundTrackName = getKeysoundTrackName(keysoundTrackFile);
			for (File diffFile : diffFiles) {
				String diffName = getDiffName(diffFile);
				if (diffName == null || !diffName.startsWith(keysoundTrackName))
					continue;

				System.out.println("Processing diff " + diffName
						+ " with keysound track " + keysoundTrackName);
				new KeysoundProcessor().process(diffFile, keysoundTrackFile,
						offset);
			}
		}
	}

	private String getKeysoundTrackName(File keysoundTrackFile) {
		return getName(keysoundTrackFile);
	}

	private String getDiffName(File diffFile) {
		String name = getName(diffFile);
		int beginIndex = name.lastIndexOf('[');
		if (beginIndex == -1)
			return null;
		int endIndex = name.lastIndexOf(']');
		if (endIndex == -1)
			return null;
		return name.substring(beginIndex + 1, endIndex);
	}

	private String getName(File file) {
		String name = file.getName();
		int dotIndex = name.lastIndexOf('.');
		if (dotIndex == -1)
			return name;
		return name.substring(0, dotIndex);
	}
}
