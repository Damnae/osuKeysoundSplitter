package com.damnae.osukeysoundsplitter.writer;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.LittleEndianDataOutput;

import com.damnae.osukeysoundsplitter.strategy.KeysoundingStrategy;

public class WavKeysoundWriter extends BaseKeysoundWriter {

	public WavKeysoundWriter(File mapsetFolder,
			KeysoundingStrategy keysoundingStrategy,
			ExecutorService executorService) {
		super(mapsetFolder, keysoundingStrategy, executorService);
	}

	@Override
	protected void writeKeysound(FileOutputStream os, byte[] data,
			StreamInfo streamInfo) throws IOException {

		DataOutput dataOutput = new DataOutputStream(os);
		LittleEndianDataOutput dataOutputLE = new LittleEndianDataOutput(
				dataOutput);

		int channels = streamInfo.getChannels();
		int bps = streamInfo.getBitsPerSample();
		int sampleRate = streamInfo.getSampleRate();
		long totalSamples = data.length / ((bps / 8) * channels);
		long dataSize = totalSamples * (bps / 8) * channels;

		dataOutput.write("RIFF".getBytes());
		dataOutputLE.writeInt((int) dataSize + 36);
		dataOutput.write("WAVEfmt ".getBytes());
		dataOutput.write(new byte[] { 0x10, 0x00, 0x00, 0x00 });
		dataOutput.write(new byte[] { 0x01, 0x00 });
		dataOutputLE.writeShort(channels);
		dataOutputLE.writeInt(sampleRate);
		dataOutputLE.writeInt(sampleRate * (bps / 8) * channels);
		dataOutputLE.writeShort((bps / 8) * channels);
		dataOutputLE.writeShort(bps);
		dataOutput.write("data".getBytes());
		dataOutputLE.writeInt((int) dataSize);
		dataOutput.write(data);
	}

	@Override
	protected String getExtension() {
		return "wav";
	}
}
