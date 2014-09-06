package com.damnae.osukeysoundsplitter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import com.damnae.osukeysoundsplitter.audio.AudioTrackInfo;
import com.damnae.osukeysoundsplitter.audio.decode.TrackProcessor;

public class KeysoundExtractor implements TrackProcessor {
	private List<Keysound> keysounds;
	private KeysoundWriter keysoundWriter;
	private int offset;

	private AudioTrackInfo info;
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

	@Override
	public void decodingCompleted() {
		try {
			writeKeysound(keysoundIndex);
			bos.close();

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void processTrackInfo(AudioTrackInfo info) {
		System.out.println(info);

		this.info = info;
		this.bytesPerSample = (info.getBitsPerSample() / 8)
				* info.getChannels();
		this.bytesPerSecond = (info.getSampleRate() * bytesPerSample);

		bos = new ByteArrayOutputStream(bytesPerSecond);
	}

	@Override
	public void processPCM(byte[] data, int length) {
		double startTime = (pcmPosition * 1000.0) / bytesPerSecond - offset;
		double endTime = ((pcmPosition + length) * 1000.0) / bytesPerSecond
				- offset;
		double duration = (length * 1000.0) / bytesPerSecond;

		int samples = length / bytesPerSample;
		int consumedSamples = 0;

		// Check to move to the next keysound
		while (keysoundIndex < keysounds.size() - 1) {
			Keysound nextKeysound = keysounds.get(keysoundIndex + 1);

			if (nextKeysound.startTime > endTime)
				break;

			++keysoundIndex;
			try {
				double timePercentage = (nextKeysound.startTime - startTime)
						/ duration;

				int toSamples = (int) (timePercentage * samples);

				if (keysoundIndex > 0)
					writePCM(data, consumedSamples, toSamples - consumedSamples);
				startKeysound();

				consumedSamples = toSamples;

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		if (keysoundIndex > -1
				&& startTime < keysounds.get(keysoundIndex).endTime) {

			try {
				writePCM(data, consumedSamples, samples - consumedSamples);

			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		pcmPosition += length;
	}

	private void startKeysound() throws IOException {
		if (keysoundIndex > 0) {
			writeKeysound(keysoundIndex - 1);
			bos.reset();
		}
	}

	private void writePCM(byte[] data, int sampletOffset, int samples)
			throws IOException {

		int offset = sampletOffset * bytesPerSample;
		int length = samples * bytesPerSample;

		try {
			bos.write(data, offset, length);

		} catch (IndexOutOfBoundsException e) {
			System.err.println("index out of bounds writing samples from "
					+ sampletOffset + " to " + (sampletOffset + samples)
					+ ", bytes from " + offset + " to " + (offset + length)
					+ " of " + data.length);

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