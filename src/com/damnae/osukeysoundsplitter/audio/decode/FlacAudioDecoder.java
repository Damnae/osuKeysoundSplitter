package com.damnae.osukeysoundsplitter.audio.decode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.PCMProcessor;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.ByteData;

import com.damnae.osukeysoundsplitter.audio.BaseAudioTrackInfo;

public class FlacAudioDecoder implements AudioDecoder {
	private File audioFile;
	private List<TrackProcessor> audioProcessors = new ArrayList<TrackProcessor>();

	public FlacAudioDecoder(File audioFile) {
		this.audioFile = audioFile;
	}

	@Override
	public void register(TrackProcessor audioProcessor) {
		if (audioProcessor == null)
			throw new InvalidParameterException(
					"audioProcessor must not be null");

		audioProcessors.add(audioProcessor);
	}

	@Override
	public void decode() throws IOException {
		FileInputStream is = new FileInputStream(audioFile);
		FLACDecoder decoder = new FLACDecoder(is);
		for (final TrackProcessor trackProcessor : audioProcessors)
			decoder.addPCMProcessor(new PCMProcessor() {

				@Override
				public void processStreamInfo(final StreamInfo info) {
					trackProcessor.processTrackInfo(new BaseAudioTrackInfo() {

						@Override
						public int getSampleRate() {
							return info.getSampleRate();
						}

						@Override
						public int getChannels() {
							return info.getChannels();
						}

						@Override
						public int getBitsPerSample() {
							return info.getBitsPerSample();
						}

						@Override
						public String toString() {
							return info.toString();
						}
					});
				}

				@Override
				public void processPCM(ByteData byteData) {
					trackProcessor.processPCM(byteData.getData(),
							byteData.getLen());
				}
			});

		decoder.decode();

		for (TrackProcessor trackProcessor : audioProcessors)
			trackProcessor.decodingCompleted();
	}
}
