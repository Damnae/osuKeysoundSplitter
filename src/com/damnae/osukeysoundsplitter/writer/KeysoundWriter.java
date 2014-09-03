package com.damnae.osukeysoundsplitter.writer;

import java.io.IOException;

import org.kc7bfi.jflac.metadata.StreamInfo;

public interface KeysoundWriter {

	String writeKeysound(byte[] data, StreamInfo streamInfo) throws IOException;
}