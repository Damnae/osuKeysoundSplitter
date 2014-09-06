package com.damnae.osukeysoundsplitter;

public class Keysound {
	public enum Type {
		HITOBJECT, LINE, AUTO
	}

	public String filename;
	public long startTime;
	public long endTime;
	public String data;
	public Type type;
}