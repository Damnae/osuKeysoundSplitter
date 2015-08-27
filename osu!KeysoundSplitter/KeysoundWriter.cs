import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import com.damnae.osukeysoundsplitter.audio.AudioTrackInfo;
import com.damnae.osukeysoundsplitter.audio.encode.AudioEncoder;
import com.damnae.osukeysoundsplitter.pathprovider.KeysoundPathProvider;
import com.damnae.osukeysoundsplitter.strategy.KeysoundingStrategy;

namespace osuKeysoundSplitter
{
public class KeysoundWriter {
	private static final int FADE_IN_DURATION = 2; // ms
	private static final int FADE_OUT_DURATION = 2; // ms

	private KeysoundingStrategy keysoundingStrategy;
	private ExecutorService executorService;

	public KeysoundWriter(KeysoundingStrategy keysoundingStrategy,
			ExecutorService executorService) {

		this.keysoundingStrategy = keysoundingStrategy;
		this.executorService = executorService;
	}

	public String writeKeysound(final byte[] data, final AudioTrackInfo info)
			throws IOException {

		final KeysoundPathProvider keysoundPathProvider = keysoundingStrategy
				.getKeysoundPathProvider();
		final AudioEncoder audioEncoder = keysoundingStrategy.getAudioEncoder();

		fadeInOut(data, info);

		String keysoundIdentifier = keysoundPathProvider.getIdentifier(data);
		boolean isGenerated = keysoundPathProvider
				.isGenerated(keysoundIdentifier);

		final String keysoundPath = keysoundPathProvider.getKeysoundPath(
				keysoundIdentifier, audioEncoder.getExtension());

		if (!isGenerated) {
			executorService.execute(new Runnable() {

				@Override
				public void run() {
					File keysoundFile = new File(keysoundingStrategy
							.getMapsetFolder(), keysoundPath);

					System.out.println("Writing keysound "
							+ keysoundFile.getPath());
					try {
						audioEncoder.encode(keysoundFile, data, info);

					} catch (IOException e) {
						System.err.println("Failed to write keysound "
								+ keysoundFile.getPath());
						e.printStackTrace();
					}
				}
			});
		}

		return keysoundPath;
	}

	private void fadeInOut(byte[] data, AudioTrackInfo info) {
		if (info.getBitsPerSample() != 16)
			return;

		int bytesPerSample = info.getBytesPerSample();
		int sampleCount = data.length / bytesPerSample;

		int fadeInSamples = Math.min(sampleCount / 2,
				FADE_IN_DURATION * info.getSampleRate() / 1000);
		int fadeOutSamples = Math.min(sampleCount / 2,
				FADE_OUT_DURATION * info.getSampleRate() / 1000);

		for (int offset = 0; offset < data.length; offset += bytesPerSample) {
			int sample = offset / bytesPerSample;

			double volume;
			if (sample < fadeInSamples) {
				volume = (double) sample / fadeInSamples;

			} else if (sample > sampleCount - fadeOutSamples) {
				volume = (double) (sampleCount - sample) / fadeOutSamples;

			} else {
				continue;
			}

			short left = (short) ((data[offset] & 0x00ff) | (data[offset + 1] << 8));
			short right = (short) ((data[offset + 2] & 0x00ff) | (data[offset + 3] << 8));

			left = (short) (left * volume);
			right = (short) (right * volume);

			data[offset] = (byte) (left & 0x00ff);
			data[offset + 1] = (byte) ((left >> 8) & 0x00ff);
			data[offset + 2] = (byte) (right & 0x00ff);
			data[offset + 3] = (byte) ((right >> 8) & 0x00ff);
		}
	}
}
}