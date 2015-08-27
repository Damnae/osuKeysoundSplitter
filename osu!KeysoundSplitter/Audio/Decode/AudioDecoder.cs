
namespace osuKeysoundSplitter.Audio.Decode
{
    public interface AudioDecoder
    {
        void register(TrackProcessor trackProcessor);
        void decode();
    }
}