package com.damnae.osukeysoundsplitter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.kc7bfi.jflac.PCMProcessor;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteData;

import com.damnae.osukeysoundsplitter.KeysoundProcessor.Keysound;
import com.damnae.osukeysoundsplitter.writer.KeysoundWriter;

public class KeysoundExtractor implements PCMProcessor {
	private List<Keysound> keysounds;
	private KeysoundWriter keysoundWriter;
	private int offset;

	private StreamInfo info;
	private int bytesPerSample;
	private int bytesPerSecond;

	private int keysoundIndex = -1;
	private long pcmPosition;

	private ByteArrayOutputStream bos;

	public KeysoundExtractor(List<Keysound> keysounds,
			KeysoundWriter keysoundWriter, int offset) {

		this.keysoundWriter = keysoundWriter;
		this.keysounds = keysounds;
		this.offset = offset;
	}

	public void complete() throws IOException {
		writeKeysound(keysoundIndex);
		bos.close();
	}

	@Override
	public void processStreamInfo(StreamInfo info) {
		System.out.println(info);

		this.info = info;
		this.bytesPerSample = (info.getBitsPerSample() / 8)
				* info.getChannels();
		this.bytesPerSecond = (info.getSampleRate() * bytesPerSample);

		bos = new ByteArrayOutputStream(bytesPerSecond);
	}

	@Override
	public void processPCM(ByteData pcm) {
		double startTime = (pcmPosition * 1000.0) / bytesPerSecond - offset;
		double endTime = ((pcmPosition + pcm.getLen()) * 1000.0)
				/ bytesPerSecond - offset;
		double duration = (pcm.getLen() * 1000.0) / bytesPerSecond;

		int samples = pcm.getLen() / bytesPerSample;
		int consumedSamples = 0;

		// Check to move to the next keysound
		if (keysoundIndex < keysounds.size() - 1) {
			Keysound nextKeysound = keysounds.get(keysoundIndex + 1);

			if (nextKeysound.startTime <= endTime) {
				++keysoundIndex;
				try {
					double timePercentage = (nextKeysound.startTime - startTime)
							/ duration;
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
			writeKeysound(keysoundIndex - 1);
			bos.reset();
		}
	}

	private void writePCM(ByteData pcm, int sampletOffset, int samples)
			throws IOException {

		int offset = sampletOffset * bytesPerSample;
		int length = samples * bytesPerSample;

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

	private void writeKeysound(int index) throws IOException {
		System.out.println("Extracted keysound " + (index + 1) + " / "
				+ keysounds.size());
		String keysoundPath = keysoundWriter.writeKeysound(bos.toByteArray(),
				info);

		keysounds.get(index).filename = keysoundPath;
	}
}