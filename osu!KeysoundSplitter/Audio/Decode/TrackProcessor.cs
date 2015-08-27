import com.damnae.osukeysoundsplitter.audio.AudioTrackInfo;

namespace osuKeysoundSplitter.Audio.Decode
{
public interface TrackProcessor {

	void processTrackInfo(final AudioTrackInfo info);

	void processPCM(byte[] data, int length);

	void decodingCompleted();
}
}