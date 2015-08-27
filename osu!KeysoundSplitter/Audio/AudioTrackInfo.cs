
namespace osuKeysoundSplitter.Audio
{
    public interface AudioTrackInfo
    {
        int getSampleRate();
        int getBitsPerSample();
        int getChannels();
        int getBytesPerSample();
        int getBytesPerSecond();
    }
}