package com.damnae.osunotecut;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.LittleEndianDataOutput;

public class WavKeysoundWriter extends BaseKeysoundWriter {

	public WavKeysoundWriter(String mapFolderPath, String keysoundFolderPath) {
		super(mapFolderPath, keysoundFolderPath);
	}

	protected void writeKeysound(FileOutputStream os, byte[] data,
			StreamInfo streamInfo) throws IOException {

		DataOutput dataOutput = new DataOutputStream(os);
		LittleEndianDataOutput dataOutputLE = new LittleEndianDataOutput(
				dataOutput);

		long totalSamples = data.length
				/ ((streamInfo.getBitsPerSample() / 8) * streamInfo
						.getChannels());
		int channels = streamInfo.getChannels();
		int bps = streamInfo.getBitsPerSample();
		int sampleRate = streamInfo.getSampleRate();

		long dataSize = totalSamples * channels * ((bps + 7) / 8);
		dataOutput.write("RIFF".getBytes());
		// filesize-8
		dataOutputLE.writeInt((int) dataSize + 36);
		dataOutput.write("WAVEfmt ".getBytes());
		// chunk size = 16
		dataOutput.write(new byte[] { 0x10, 0x00, 0x00, 0x00 });
		// compression code == 1
		dataOutput.write(new byte[] { 0x01, 0x00 });
		dataOutputLE.writeShort(channels);
		dataOutputLE.writeInt(sampleRate);
		// or is it (sample_rate*channels*bps) / 8
		dataOutputLE.writeInt(sampleRate * channels * ((bps + 7) / 8));
		// block align
		dataOutputLE.writeShort(channels * ((bps + 7) / 8));
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
