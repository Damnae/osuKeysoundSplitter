package com.damnae.osukeysoundsplitter;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import com.damnae.osukeysoundsplitter.audio.AudioTrackInfo;
import com.damnae.osukeysoundsplitter.audio.encode.AudioEncoder;
import com.damnae.osukeysoundsplitter.pathprovider.KeysoundPathProvider;
import com.damnae.osukeysoundsplitter.strategy.KeysoundingStrategy;

public class KeysoundWriter {
	private File mapsetFolder;
	private KeysoundingStrategy keysoundingStrategy;
	private AudioEncoder audioEncoder;
	private ExecutorService executorService;

	public KeysoundWriter(File mapsetFolder,
			KeysoundingStrategy keysoundingStrategy, AudioEncoder audioEncoder,
			ExecutorService executorService) {

		this.mapsetFolder = mapsetFolder;
		this.keysoundingStrategy = keysoundingStrategy;
		this.audioEncoder = audioEncoder;
		this.executorService = executorService;
	}

	public String writeKeysound(final byte[] data, final AudioTrackInfo info)
			throws IOException {

		KeysoundPathProvider keysoundPathProvider = keysoundingStrategy
				.getKeysoundPathProvider();

		String keysoundIdentifier = keysoundPathProvider.getIdentifier(data);
		boolean isGenerated = keysoundPathProvider
				.isGenerated(keysoundIdentifier);

		final String keysoundPath = keysoundPathProvider.getKeysoundPath(
				keysoundIdentifier, audioEncoder.getExtension());

		if (!isGenerated) {
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					File keysoundFile = new File(mapsetFolder, keysoundPath);

					System.out.println("Writing keysound "
							+ keysoundFile.getPath());
					try {
						audioEncoder.encode(keysoundFile, data, info);

					} catch (IOException e) {
						System.err.println("Failed to write keysound "
								+ keysoundFile.getPath());
						e.printStackTrace();
					}
				}
			});
		}

		return keysoundPath;
	}
}
