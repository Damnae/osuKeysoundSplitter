
using System.IO;
namespace osuKeysoundSplitter.Audio.Encode
{
public abstract class BaseAudioEncoder : AudioEncoder {

	public override void encode(File toFile, byte[] data, AudioTrackInfo info)
			 {

		toFile.getParentFile().mkdirs();

		FileOutputStream os = new FileOutputStream(toFile);
		try {
			encode(os, data, info);

		} finally {
			os.close();
		}
	}

	public override void encodeSilence(File toFile)  {
		AudioTrackInfo info = new BaseAudioTrackInfo() {

			public override int getSampleRate() {
				return 44100;
			}

			public override int getChannels() {
				return 2;
			}

			public override int getBitsPerSample() {
				return 16;
			}
		};
		encode(toFile, new byte[0], info);
	}

	protected abstract void encode(FileOutputStream os, byte[] data,
			AudioTrackInfo info) throws IOException;
}
}