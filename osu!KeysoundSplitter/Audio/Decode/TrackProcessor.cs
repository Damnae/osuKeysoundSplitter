
namespace osuKeysoundSplitter.Audio.Decode
{
    public interface TrackProcessor
    {
        void processTrackInfo(AudioTrackInfo info);
        void processPCM(byte[] data, int length);
        void decodingCompleted();
    }
}