package com.damnae.osukeysoundsplitter.strategy;

import com.damnae.osukeysoundsplitter.pathprovider.AdditionsKeysoundPathProvider;

public class StandardKeysoundingStrategy extends BaseKeysoundingStrategy {

	public StandardKeysoundingStrategy(int initialSampleType) {
		super(new AdditionsKeysoundPathProvider(initialSampleType));
	}

}
