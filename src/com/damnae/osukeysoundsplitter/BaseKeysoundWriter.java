package com.damnae.osukeysoundsplitter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.kc7bfi.jflac.metadata.StreamInfo;

public abstract class BaseKeysoundWriter implements KeysoundWriter {
	private String mapFolderPath;
	private String keysoundFolderPath;
	private KeysoundPathProvider keysoundPathProvider;
	private ExecutorService executorService;

	public BaseKeysoundWriter(String mapFolderPath, String keysoundFolderPath,
			KeysoundPathProvider keysoundPathProvider,
			ExecutorService executorService) {

		this.mapFolderPath = mapFolderPath;
		this.keysoundFolderPath = keysoundFolderPath;
		this.keysoundPathProvider = keysoundPathProvider;
		this.executorService = executorService;

		File folder = new File(mapFolderPath + keysoundFolderPath);
		folder.mkdir();
	}

	@Override
	public String writeKeysound(final byte[] data, final StreamInfo streamInfo)
			throws IOException {

		String keysoundIdentifier = keysoundPathProvider.getIdentifier(data);
		boolean registered = keysoundPathProvider
				.isRegistered(keysoundIdentifier);

		final String path = keysoundPathProvider.getKeysoundPath(
				keysoundFolderPath, keysoundIdentifier, getExtension());

		if (!registered) {
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					System.out.println("Writing keysound " + path);

					FileOutputStream os;
					try {
						os = new FileOutputStream(mapFolderPath + path);
						try {
							writeKeysound(os, data, streamInfo);

						} catch (IOException e) {
							System.err.println("Failed to write keysound "
									+ path);
							e.printStackTrace();

						} finally {
							try {
								os.close();

							} catch (IOException e) {
								System.err
										.println("Failed to close file for keysound "
												+ path);
								e.printStackTrace();
							}
						}

					} catch (FileNotFoundException e) {
						System.err
								.println("Failed to open file to write keysound "
										+ path);
						e.printStackTrace();
					}
				}
			});
		}

		return path;
	}

	protected abstract String getExtension();

	protected abstract void writeKeysound(FileOutputStream os, byte[] data,
			StreamInfo streamInfo) throws IOException;
}
