package com.damnae.osunotecut;

import java.io.IOException;

import org.kc7bfi.jflac.metadata.StreamInfo;

public interface KeysoundWriter {

	String writeKeysound(String filename, byte[] data, StreamInfo streamInfo)
			throws IOException;
}
