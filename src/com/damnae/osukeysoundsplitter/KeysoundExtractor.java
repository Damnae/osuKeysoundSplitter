package com.damnae.osukeysoundsplitter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.kc7bfi.jflac.PCMProcessor;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteData;

import com.damnae.osukeysoundsplitter.KeysoundProcessor.Keysound;

public class KeysoundExtractor implements PCMProcessor {
	private List<Keysound> keysounds;
	private KeysoundWriter keysoundWriter;

	private StreamInfo info;

	private int keysoundIndex = -1;
	private long pcmPosition;

	private ByteArrayOutputStream bos;

	public KeysoundExtractor(List<Keysound> keysounds,
			KeysoundWriter keysoundWriter) {

		this.keysoundWriter = keysoundWriter;
		this.keysounds = keysounds;
	}

	public void complete() throws IOException {
		System.out.println("Writing keysound " + (keysoundIndex));
		keysounds.get(keysoundIndex).filename = keysoundWriter.writeKeysound(
				String.valueOf(keysoundIndex), bos.toByteArray(), info);

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
		long bytePerSecond = (info.getSampleRate()
				* (info.getBitsPerSample() / 8) * info.getChannels());

		double startTime = (pcmPosition * 1000.0) / bytePerSecond;
		double endTime = ((pcmPosition + pcm.getLen()) * 1000.0)
				/ bytePerSecond;
		double duration = (pcm.getLen() * 1000.0) / bytePerSecond;

		int samples = pcm.getLen()
				/ ((info.getBitsPerSample() / 8) * info.getChannels());
		int consumedSamples = 0;

		// Check to move to the next keysound
		if (keysoundIndex < keysounds.size() - 1) {
			Keysound nextKeysound = keysounds.get(keysoundIndex + 1);

			if (nextKeysound.startTime <= endTime) {
				++keysoundIndex;
				try {
					double timePercentage = (nextKeysound.startTime - startTime)
							/ (double) duration;
					if (timePercentage < 0) {
						// XXX why?

						System.err.println("negative timePercentage "
								+ timePercentage);
						timePercentage = 0;
					}

					consumedSamples = (int) (timePercentage * samples);

					if (keysoundIndex > 0)
						writePCM(pcm, 0, consumedSamples);
					startKeysound();

				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		if (keysoundIndex > -1
				&& startTime < keysounds.get(keysoundIndex).endTime) {

			try {
				writePCM(pcm, consumedSamples, samples - consumedSamples);

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		pcmPosition += pcm.getLen();
	}

	private void startKeysound() throws IOException {
		if (keysoundIndex > 0) {
			System.out.println("Writing keysound " + (keysoundIndex - 1));
			keysounds.get(keysoundIndex - 1).filename = keysoundWriter
					.writeKeysound(String.valueOf(keysoundIndex - 1),
							bos.toByteArray(), info);

			bos.reset();
		}
	}

	private void writePCM(ByteData pcm, int sampletOffset, int samples)
			throws IOException {

		int offset = sampletOffset * (info.getBitsPerSample() / 8)
				* info.getChannels();
		int length = samples * (info.getBitsPerSample() / 8)
				* info.getChannels();

		try {
			bos.write(pcm.getData(), offset, length);

		} catch (IndexOutOfBoundsException e) {
			System.err.println("index out of bounds writing samples from "
					+ sampletOffset + " to " + (sampletOffset + samples)
					+ ", bytes from " + offset + " to " + (offset + length)
					+ " of " + pcm.getLen());

			throw e;
		}
	}
}