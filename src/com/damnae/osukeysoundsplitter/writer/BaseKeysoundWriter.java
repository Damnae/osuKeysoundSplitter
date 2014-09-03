package com.damnae.osukeysoundsplitter.writer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.kc7bfi.jflac.metadata.StreamInfo;

import com.damnae.osukeysoundsplitter.pathprovider.KeysoundPathProvider;

public abstract class BaseKeysoundWriter implements KeysoundWriter {
	private File mapsetFolder;
	private KeysoundPathProvider keysoundPathProvider;
	private ExecutorService executorService;

	public BaseKeysoundWriter(File mapsetFolder,
			KeysoundPathProvider keysoundPathProvider,
			ExecutorService executorService) {

		this.mapsetFolder = mapsetFolder;
		this.keysoundPathProvider = keysoundPathProvider;
		this.executorService = executorService;
	}

	@Override
	public String writeKeysound(final byte[] data, final StreamInfo streamInfo)
			throws IOException {

		String keysoundIdentifier = keysoundPathProvider.getIdentifier(data);
		boolean registered = keysoundPathProvider
				.isRegistered(keysoundIdentifier);

		final String keysoundPath = keysoundPathProvider.getKeysoundPath(
				keysoundIdentifier, getExtension());

		if (!registered) {
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					File keysoundFile = new File(mapsetFolder, keysoundPath);
					System.out.println("Writing keysound "
							+ keysoundFile.getPath());
					try {

						FileOutputStream os;
						os = new FileOutputStream(keysoundFile);
						try {
							writeKeysound(os, data, streamInfo);

						} catch (IOException e) {

						} finally {
							os.close();
						}

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

	protected abstract String getExtension();

	protected abstract void writeKeysound(FileOutputStream os, byte[] data,
			StreamInfo streamInfo) throws IOException;
}
