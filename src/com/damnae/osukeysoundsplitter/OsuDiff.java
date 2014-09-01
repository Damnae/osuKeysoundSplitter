package com.damnae.osukeysoundsplitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class OsuDiff {

	public class AudioArea {
		public long startTime;
		public long endTime;
		public List<Long> noteTimes = new ArrayList<Long>();
	}

	public List<AudioArea> audioAreas = new ArrayList<AudioArea>();

	public OsuDiff(File file) throws IOException {
		FileInputStream is = new FileInputStream(file);
		try {
			InputStreamReader inputStreamReader = new InputStreamReader(is,
					Charset.forName("UTF-8"));
			BufferedReader reader = new BufferedReader(inputStreamReader);

			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("[") && line.endsWith("]")) {
					String sectionName = line.substring(1, line.length() - 1);

					if (sectionName.equals("Editor")) {
						parseOsuEditorSection(reader);

					} else if (sectionName.equals("HitObjects")) {
						parseOsuHitObjectsSection(reader);
					}
				}
			}

		} finally {
			is.close();
		}
	}

	private void parseOsuEditorSection(BufferedReader reader)
			throws IOException {

		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty())
				break;

			String key = parseKeyValueKey(line);
			String value = parseKeyValueValue(line);

			if (key.equals("Bookmarks")) {
				String[] bookmarks = value.split(",");

				for (int i = 0, size = bookmarks.length; i < size; i += 2) {
					AudioArea audioArea = new AudioArea();
					audioArea.startTime = Integer.valueOf(bookmarks[i]);
					audioArea.endTime = Integer.valueOf(bookmarks[i + 1]);
					audioAreas.add(audioArea);
				}
			}
		}
	}

	private void parseOsuHitObjectsSection(BufferedReader reader)
			throws IOException {

		String line;
		while ((line = reader.readLine()) != null) {
			line = line.trim();
			if (line.isEmpty())
				break;

			String[] values = line.split(",");
			final long startTime = Integer.parseInt(values[2]);

			for (AudioArea audioArea : audioAreas) {
				if (audioArea.startTime <= startTime
						&& startTime < audioArea.endTime) {

					audioArea.noteTimes.add(startTime);
					break;
				}
			}
		}
	}

	private static String parseKeyValueKey(String line) {
		return line.substring(0, line.indexOf(":")).trim();
	}

	private static String parseKeyValueValue(String line) {
		return line.substring(line.indexOf(":") + 1, line.length()).trim();
	}
}
