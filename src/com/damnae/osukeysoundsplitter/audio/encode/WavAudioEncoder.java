package com.damnae.osukeysoundsplitter.audio.encode;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.kc7bfi.jflac.util.LittleEndianDataOutput;

import com.damnae.osukeysoundsplitter.audio.AudioTrackInfo;

public class WavAudioEncoder extends BaseAudioEncoder {

	@Override
	protected void encode(FileOutputStream os, byte[] data, AudioTrackInfo info)
			throws IOException {
		DataOutput dataOutput = new DataOutputStream(os);
		LittleEndianDataOutput dataOutputLE = new LittleEndianDataOutput(
				dataOutput);

		int channels = info.getChannels();
		int bps = info.getBitsPerSample();
		int sampleRate = info.getSampleRate();
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
	public String getExtension() {
		return "wav";
	}
}
