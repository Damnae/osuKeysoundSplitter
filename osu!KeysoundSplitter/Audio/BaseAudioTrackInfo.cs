
namespace osuKeysoundSplitter.Audio
{
    public abstract class BaseAudioTrackInfo : AudioTrackInfo
    {
        public override int getBytesPerSample()
        {
            return (getBitsPerSample() / 8) * getChannels();
        }

        public override int getBytesPerSecond()
        {
            return getSampleRate() * getBytesPerSample();
        }

        public override string toString()
        {
            return "SampleRate=" + getSampleRate() + " Channels=" + getChannels()
                    + " BPS=" + getBitsPerSample();
        }
    }
}