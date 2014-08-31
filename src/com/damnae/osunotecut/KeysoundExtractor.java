package com.damnae.osunotecut;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.kc7bfi.jflac.PCMProcessor;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteData;

import com.damnae.osunotecut.OsuNoteCut.Keysound;

public class KeysoundExtractor implements PCMProcessor {
	private List<Keysound> keysounds;
	private KeysoundWriter keysoundWriter;

	private StreamInfo info;

	private int keysoundIndex = -1;
	private long pcmPosition;

	private ByteArrayOutputStream bos;
	private long offset;

	public KeysoundExtractor(List<Keysound> keysounds,
			KeysoundWriter keysoundWriter) {

		this.keysoundWriter = keysoundWriter;
		this.keysounds = keysounds;
		offset = 0;
	}

	public void complete() throws IOException {
		keysoundWriter.writeKeysound(String.valueOf(keysoundIndex),
				bos.toByteArray(), info);

		bos.close();
	}

	@Override
	public void processStreamInfo(StreamInfo info) {
		System.out.println(info);
		System.out.println(info.getSampleRate() * (info.getBitsPerSample() / 8)
				* info.getChannels());

		this.info = info;

		bos = new ByteArrayOutputStream(info.getSampleRate()
				* (info.getBitsPerSample() / 8) * info.getChannels());
	}

	@Override
	public void processPCM(ByteData pcm) {
		long sampleTime = (pcmPosition * 1000)
				/ (info.getSampleRate() * (info.getBitsPerSample() / 8) * info
						.getChannels());

		System.out.println("PCM: " + sampleTime);

		// Check to move to the next keysound
		if (keysoundIndex < keysounds.size() - 2) {
			Keysound nextKeysound = keysounds.get(keysoundIndex + 1);

			if (nextKeysound.startTime - offset <= sampleTime) {
				++keysoundIndex;
				try {
					startKeysound();

				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		if (keysoundIndex > -1
				&& sampleTime < keysounds.get(keysoundIndex).endTime - offset) {

			try {
				writePCM(pcm);

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		pcmPosition += pcm.getLen();
	}

	private void startKeysound() throws IOException {
		if (keysoundIndex > 0) {
			System.out.println("Writing keysound " + (keysoundIndex - 1));
			keysoundWriter.writeKeysound(String.valueOf(keysoundIndex - 1),
					bos.toByteArray(), info);
		}

		bos.reset();
	}

	private void writePCM(ByteData pcm) throws IOException {
		bos.write(pcm.getData(), 0, pcm.getLen());
	}
}