using osuKeysoundSplitter.Audio.Encode;
using System;
using System.Collections.Generic;

namespace osuKeysoundSplitter
{
    class Program
    {
        static void Main()
        {
            Dictionary<string, AudioEncoder> audioEncoders = new Dictionary<string, AudioEncoder>();
            audioEncoders["wav"] = new WavAudioEncoder();
            audioEncoders["ogg"] = new OggAudioEncoder();

            string[] args = Environment.GetCommandLineArgs();
            if (args.Length < 3)
            {
                Console.WriteLine("Syntax: java -jar \"osu!KeysoundSplitter.jar\" mapsetPath keysoundsOffsetInMilliseconds encodingFormat");
                return;
            }

            string folder = args[0];
            int offset = int.Parse(args[1]);
            string format = args[2];

            AudioEncoder audioEncoder = audioEncoders[format];

            new MapsetProcessor().process(folder, offset, audioEncoder);
        }
    }

}