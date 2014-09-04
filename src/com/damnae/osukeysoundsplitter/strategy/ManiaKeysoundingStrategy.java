package com.damnae.osukeysoundsplitter.strategy;

import com.damnae.osukeysoundsplitter.pathprovider.CounterKeysoundPathProvider;

public class ManiaKeysoundingStrategy extends BaseKeysoundingStrategy {

	public ManiaKeysoundingStrategy() {
		super(new CounterKeysoundPathProvider());
	}
}
