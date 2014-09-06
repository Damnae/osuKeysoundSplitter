package com.damnae.osukeysoundsplitter;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import com.damnae.osukeysoundsplitter.audio.AudioTrackInfo;
import com.damnae.osukeysoundsplitter.audio.encode.AudioEncoder;
import com.damnae.osukeysoundsplitter.pathprovider.KeysoundPathProvider;
import com.damnae.osukeysoundsplitter.strategy.KeysoundingStrategy;

public class KeysoundWriter {
	private KeysoundingStrategy keysoundingStrategy;
	private ExecutorService executorService;

	public KeysoundWriter(KeysoundingStrategy keysoundingStrategy,
			ExecutorService executorService) {

		this.keysoundingStrategy = keysoundingStrategy;
		this.executorService = executorService;
	}

	public String writeKeysound(final byte[] data, final AudioTrackInfo info)
			throws IOException {

		final KeysoundPathProvider keysoundPathProvider = keysoundingStrategy
				.getKeysoundPathProvider();
		final AudioEncoder audioEncoder = keysoundingStrategy.getAudioEncoder();

		String keysoundIdentifier = keysoundPathProvider.getIdentifier(data);
		boolean isGenerated = keysoundPathProvider
				.isGenerated(keysoundIdentifier);

		final String keysoundPath = keysoundPathProvider.getKeysoundPath(
				keysoundIdentifier, audioEncoder.getExtension());

		if (!isGenerated) {
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					File keysoundFile = new File(keysoundingStrategy
							.getMapsetFolder(), keysoundPath);

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
