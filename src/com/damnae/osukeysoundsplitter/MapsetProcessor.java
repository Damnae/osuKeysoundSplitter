package com.damnae.osukeysoundsplitter;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.damnae.osukeysoundsplitter.strategy.KeysoundingStrategy;
import com.damnae.osukeysoundsplitter.strategy.StandardKeysoundingStrategy;

public class MapsetProcessor {

	private static final String KEYSOUND_TRACK_EXTENSION = ".flac";
	private static final String DIFF_FILE_EXTENSION = ".osu";

	private class DiffContext {
		public KeysoundProcessor keysoundProcessor;

		public DiffContext(KeysoundingStrategy keysoundingStrategy) {
			keysoundProcessor = new KeysoundProcessor(keysoundingStrategy);
		}
	}

	public void process(File folder, int offset) throws IOException {
		long startTime = System.nanoTime();

		System.out.println("Processing mapset in " + folder.getCanonicalPath());

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

		ExecutorService executorService = Executors.newFixedThreadPool(Math
				.max(1, Runtime.getRuntime().availableProcessors() - 1));

		Map<String, DiffContext> diffContexts = new HashMap<String, DiffContext>();

		KeysoundingStrategy keysoundingStrategy = new StandardKeysoundingStrategy(
				100);
		// KeysoundingStrategy keysoundingStrategy = new
		// ManiaKeysoundingStrategy(
		// "ks");

		for (File keysoundTrackFile : keysoundTrackFiles) {
			String keysoundTrackName = getKeysoundTrackName(keysoundTrackFile);
			KeysoundTrackDecoder keysoundTrackDecoder = new KeysoundTrackDecoder(
					keysoundTrackFile);

			for (File diffFile : diffFiles) {
				String diffName = getDiffName(diffFile);
				String suffix = "-" + keysoundTrackName;
				if (diffName == null || !diffName.endsWith(suffix))
					continue;

				String contextName = diffName.substring(0,
						diffName.length() - suffix.length()).trim();
				DiffContext context = diffContexts.get(contextName);
				if (context == null) {
					context = new DiffContext(keysoundingStrategy);
					diffContexts.put(contextName, context);
				}

				System.out.println("Processing diff \"" + diffName
						+ "\" with keysound track \"" + keysoundTrackName
						+ "\" for diff \"" + contextName + "\"");

				KeysoundExtractor keysoundExtractor = context.keysoundProcessor
						.process(diffFile, keysoundTrackFile, offset,
								executorService);

				keysoundTrackDecoder.register(keysoundExtractor);
			}

			keysoundTrackDecoder.decode();
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

		executorService.shutdown();
		try {
			while (!executorService.awaitTermination(10, TimeUnit.SECONDS))
				;

		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		long duration = System.nanoTime() - startTime;
		System.out.println("Processed mapset in " + duration / 1000000000.0
				+ "s");
	}

	private String getKeysoundTrackName(File keysoundTrackFile) {
		return Utils.getFileNameWithoutExtension(keysoundTrackFile);
	}

	private String getDiffName(File diffFile) {
		String name = Utils.getFileNameWithoutExtension(diffFile);
		int beginIndex = name.lastIndexOf('[');
		if (beginIndex == -1)
			return null;
		int endIndex = name.lastIndexOf(']');
		if (endIndex == -1)
			return null;
		return name.substring(beginIndex + 1, endIndex);
	}
}
