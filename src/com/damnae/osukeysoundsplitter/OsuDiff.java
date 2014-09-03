package com.damnae.osukeysoundsplitter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OsuDiff {

	public class DiffEvent {
		public long time;
		public String data;

		public boolean isSplittingPoint() {
			return data == null;
		}

		@Override
		public String toString() {
			return time + " " + data;
		}
	}

	public List<DiffEvent> diffEvents = new ArrayList<DiffEvent>();

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

		Collections.sort(diffEvents, new Comparator<DiffEvent>() {

			@Override
			public int compare(DiffEvent event1, DiffEvent event2) {
				int value = (int) (event1.time - event2.time);
				if (value == 0)
					value = (event1.data != null ? 1 : 0)
							- (event2.data != null ? 1 : 0);
				return value;
			}
		});
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

				for (String bookmark : bookmarks) {
					DiffEvent diffEvent = new DiffEvent();
					diffEvent.time = Integer.valueOf(bookmark);
					diffEvents.add(diffEvent);
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

			boolean isSimultaneous = false;
			for (DiffEvent diffEvent : diffEvents) {
				if (Math.abs(diffEvent.time - startTime) <= 2
						&& diffEvent.data != null) {

					diffEvent.data += "\n" + line;
					isSimultaneous = true;
					break;
				}
			}

			if (!isSimultaneous) {
				DiffEvent diffEvent = new DiffEvent();
				diffEvent.time = startTime;
				diffEvent.data = line;
				diffEvents.add(diffEvent);
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
