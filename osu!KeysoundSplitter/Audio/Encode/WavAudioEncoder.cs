
namespace osuKeysoundSplitter.Audio.Encode
{
    public class WavAudioEncoder : BaseAudioEncoder
    {
        protected override void encode(FileOutputStream os, byte[] data, AudioTrackInfo info)
        {
            DataOutput dataOutput = new DataOutputStream(os);
            LittleEndianDataOutput dataOutputLE = new LittleEndianDataOutput(
                    dataOutput);

            int channels = info.getChannels();
            int bps = info.getBitsPerSample();
            int sampleRate = info.getSampleRate();
            long totalSamples = data.length / ((bps / 8) * channels);
            long dataSize = totalSamples * (bps / 8) * channels;

            dataOutput.write("RIFF".getBytes());
            dataOutputLE.writeInt((int)dataSize + 36);
            dataOutput.write("WAVEfmt ".getBytes());
            dataOutput.write(new byte[] { 0x10, 0x00, 0x00, 0x00 });
            dataOutput.write(new byte[] { 0x01, 0x00 });
            dataOutputLE.writeShort(channels);
            dataOutputLE.writeInt(sampleRate);
            dataOutputLE.writeInt(sampleRate * (bps / 8) * channels);
            dataOutputLE.writeShort((bps / 8) * channels);
            dataOutputLE.writeShort(bps);
            dataOutput.write("data".getBytes());
            dataOutputLE.writeInt((int)dataSize);
            dataOutput.write(data);
        }

        public override string getExtension()
        {
            return "wav";
        }
    }
}