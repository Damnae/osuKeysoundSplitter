using System.Collections.Generic;
using System.IO;

namespace osuKeysoundSplitter.Audio.Decode
{
public class FlacAudioDecoder : AudioDecoder {
	private File audioFile;
	private List<TrackProcessor> audioProcessors = new List<TrackProcessor>();

	public FlacAudioDecoder(File audioFile) {
		this.audioFile = audioFile;
	}

	public override void register(TrackProcessor audioProcessor) {
		if (audioProcessor == null)
			throw new InvalidParameterException(
					"audioProcessor must not be null");

		audioProcessors.Add(audioProcessor);
	}

	public override void decode() {
		FileInputStream is = new FileInputStream(audioFile);
		FLACDecoder decoder = new FLACDecoder(is);
		for (TrackProcessor trackProcessor : audioProcessors)
			decoder.addPCMProcessor(new PCMProcessor() {

				public override void processStreamInfo(StreamInfo info) {
					trackProcessor.processTrackInfo(new BaseAudioTrackInfo() {

						public override int getSampleRate() {
							return info.getSampleRate();
						}

						public override int getChannels() {
							return info.getChannels();
						}

						public override int getBitsPerSample() {
							return info.getBitsPerSample();
						}

						public override string toString() {
							return info.toString();
						}
					});
				}

				public override void processPCM(ByteData byteData) {
					trackProcessor.processPCM(byteData.getData(),
							byteData.getLen());
				}
			});

		decoder.decode();

		for (TrackProcessor trackProcessor : audioProcessors)
			trackProcessor.decodingCompleted();
	}
}
}