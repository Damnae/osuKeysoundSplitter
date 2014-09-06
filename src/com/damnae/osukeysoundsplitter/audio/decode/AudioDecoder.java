package com.damnae.osukeysoundsplitter.audio.decode;

import java.io.IOException;

public interface AudioDecoder {

	void register(TrackProcessor trackProcessor);

	void decode() throws IOException;
}
