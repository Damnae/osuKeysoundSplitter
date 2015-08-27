using System;

namespace osuKeysoundSplitter.Audio.Encode
{
    public class OggAudioEncoder : BaseAudioEncoder
    {
        private static const int SAMPLE_COUNT = 1024;

        protected override void encode(FileOutputStream fos, byte[] data, AudioTrackInfo info)
        {
            int bitsPerSample = info.getBitsPerSample();
            int channels = info.getChannels();

            if (bitsPerSample != 16)
                throw new UnsupportedOperationException(
                        "expecting 16 bits per sample, got " + bitsPerSample);

            if (channels != 2)
                throw new UnsupportedOperationException(
                        "expecting 2 channels, got " + channels);

            int bytesPerSample = bitsPerSample / 8 * channels;
            byte[] readbuffer = new byte[SAMPLE_COUNT * bytesPerSample + 44];

            vorbis_info vorbisInfo = new vorbis_info();
            vorbisenc encoder = new vorbisenc();

            if (!encoder.vorbis_encode_init_vbr(vorbisInfo, 2,
                    info.getSampleRate(), 0.3f))
                throw new IOException("Failed to Initialize vorbisenc");

            vorbis_comment comment = new vorbis_comment();
            comment.vorbis_comment_add_tag("ENCODER", "osu!KeysoundSplitter");

            vorbis_dsp_state dspState = new vorbis_dsp_state();

            if (!dspState.vorbis_analysis_init(vorbisInfo))
                throw new IOException("Failed to Initialize vorbis_dsp_state");

            vorbis_block block = new vorbis_block(dspState);

            Random generator = new Random();
            ogg_stream_state streamState = new ogg_stream_state(
                    generator.nextInt(256));

            ogg_packet header = new ogg_packet();
            ogg_packet headerComment = new ogg_packet();
            ogg_packet headerCode = new ogg_packet();

            dspState.vorbis_analysis_headerout(comment, header, headerComment,
                    headerCode);

            streamState.ogg_stream_packetin(header);
            streamState.ogg_stream_packetin(headerComment);
            streamState.ogg_stream_packetin(headerCode);

            ogg_page page = new ogg_page();
            ogg_packet packet = new ogg_packet();

            // Headers
            bool eos = false;
            while (!eos)
            {
                if (!streamState.ogg_stream_flush(page))
                    break;

                fos.write(page.header, 0, page.header_len);
                fos.write(page.body, 0, page.body_len);
            }

            // Encoding
            int dataOffset = 0;
            while (!eos)
            {

                int bytes = Math.Min(data.Length - dataOffset, SAMPLE_COUNT
                        * bytesPerSample);
                System.arraycopy(data, dataOffset, readbuffer, 0, bytes);
                dataOffset += bytes;

                if (bytes == 0)
                {
                    dspState.vorbis_analysis_wrote(0);

                }
                else
                {
                    float[][] buffer = dspState
                            .vorbis_analysis_buffer(SAMPLE_COUNT);

                    int i = 0;
                    while (i < bytes / bytesPerSample)
                    {
                        buffer[0][dspState.pcm_current + i] = ((readbuffer[i * 4 + 1] << 8) | (0x00ff & readbuffer[i * 4])) / 32768.f;
                        buffer[1][dspState.pcm_current + i] = ((readbuffer[i * 4 + 3] << 8) | (0x00ff & readbuffer[i * 4 + 2])) / 32768.f;
                        ++i;
                    }

                    dspState.vorbis_analysis_wrote(i);
                }

                // Analysis
                while (block.vorbis_analysis_blockout(dspState))
                {
                    block.vorbis_analysis(null);
                    block.vorbis_bitrate_addblock();

                    while (dspState.vorbis_bitrate_flushpacket(packet))
                    {

                        streamState.ogg_stream_packetin(packet);

                        while (!eos)
                        {
                            if (!streamState.ogg_stream_pageout(page))
                                break;

                            fos.write(page.header, 0, page.header_len);
                            fos.write(page.body, 0, page.body_len);

                            if (page.ogg_page_eos() > 0)
                                eos = true;
                        }
                    }
                }
            }
        }

        public override String getExtension()
        {
            return "ogg";
        }
    }
}