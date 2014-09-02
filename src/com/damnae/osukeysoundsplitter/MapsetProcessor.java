package com.damnae.osukeysoundsplitter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MapsetProcessor {

	private class DiffContext {
		public KeysoundProcessor keysoundProcessor = new KeysoundProcessor();
	}

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

		Map<String, DiffContext> diffContexts = new HashMap<String, DiffContext>();

		for (File keysoundTrackFile : keysoundTrackFiles) {
			String keysoundTrackName = getKeysoundTrackName(keysoundTrackFile);
			for (File diffFile : diffFiles) {
				String diffName = getDiffName(diffFile);
				String suffix = "-" + keysoundTrackName;
				if (diffName == null || !diffName.endsWith(suffix))
					continue;

				String contextName = diffName.substring(0,
						diffName.length() - suffix.length()).trim();
				DiffContext context = diffContexts.get(contextName);
				if (context == null) {
					context = new DiffContext();
					diffContexts.put(contextName, context);
				}

				System.out.println("Processing diff \"" + diffName
						+ "\" with keysound track \"" + keysoundTrackName
						+ "\" for diff \"" + contextName + "\"");
				context.keysoundProcessor.process(diffFile, keysoundTrackFile,
						offset);
			}
		}

		for (Entry<String, DiffContext> diffContextEntry : diffContexts
				.entrySet()) {

			String contextName = diffContextEntry.getKey();
			DiffContext context = diffContextEntry.getValue();

			for (File diffFile : diffFiles) {
				String diffName = getDiffName(diffFile);
				if (diffName == null || !diffName.equals(contextName))
					continue;

				System.out.println("Inserting keysounds in " + contextName);
				context.keysoundProcessor.insertKeysounds(diffFile);
				break;
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
