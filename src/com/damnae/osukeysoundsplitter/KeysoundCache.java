package com.damnae.osukeysoundsplitter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class KeysoundCache {
	private File keysoundCacheFile;
	private Map<String, String> keysoundPaths = new HashMap<String, String>();
	private Set<String> unusedIdentifiers = new HashSet<String>();

	public KeysoundCache(File keysoundCacheFile) throws IOException {
		this.keysoundCacheFile = keysoundCacheFile;

		if (keysoundCacheFile.exists()) {
			FileInputStream is = new FileInputStream(keysoundCacheFile);
			try {
				InputStreamReader inputStreamReader = new InputStreamReader(is,
						Charset.forName("UTF-8"));
				BufferedReader reader = new BufferedReader(inputStreamReader);
				try {
					String line;
					while ((line = reader.readLine()) != null) {
						line = line.trim();

						String[] keyVal = Utils.splitValues(line, '=');
						String identifier = keyVal[0];
						String path = keyVal[1];

						File keysoundFile = new File(
								keysoundCacheFile.getParentFile(), path);

						if (!keysoundFile.exists())
							continue;

						keysoundPaths.put(identifier, path);
						unusedIdentifiers.add(identifier);
					}

				} finally {
					reader.close();
				}

			} finally {
				is.close();
			}
		}
	}

	public boolean isRegistered(String keysoundIdentifier) {
		return keysoundPaths.containsKey(keysoundIdentifier);
	}

	public boolean isPathRegistered(String path) {
		return keysoundPaths.containsValue(path);
	}

	public String getKeysoundPath(String keysoundIdentifier) {
		unusedIdentifiers.remove(keysoundIdentifier);
		return keysoundPaths.get(keysoundIdentifier);
	}

	public void register(String keysoundIdentifier, String keysoundPath) {
		keysoundPaths.put(keysoundIdentifier, keysoundPath);
	}

	public void update() throws IOException {
		for (String unusedIdentifier : unusedIdentifiers) {
			File keysoundFile = new File(keysoundCacheFile.getParentFile(),
					keysoundPaths.get(unusedIdentifier));

			keysoundPaths.remove(unusedIdentifier);
			keysoundFile.delete();
		}

		FileOutputStream os = new FileOutputStream(keysoundCacheFile);
		try {
			OutputStreamWriter inputStreamReader = new OutputStreamWriter(os,
					Charset.forName("UTF-8"));
			BufferedWriter writer = new BufferedWriter(inputStreamReader);
			try {
				for (Entry<String, String> keysoundPathEntry : keysoundPaths
						.entrySet()) {

					writer.write(keysoundPathEntry.getKey());
					writer.write('=');
					writer.write(keysoundPathEntry.getValue());
					writer.write('\n');
				}

			} finally {
				writer.close();
			}

		} finally {
			os.close();
		}
	}
}
