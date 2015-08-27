using System.IO;

namespace osuKeysoundSplitter.Audio.Encode
{
    public interface AudioEncoder
    {
        void encode(File toFile, byte[] data, AudioTrackInfo info);
        void encodeSilence(File toFile);
        string getExtension();
    }
}