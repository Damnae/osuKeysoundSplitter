using osuKeysoundSplitter.Audio.Encode;
using osuKeysoundSplitter.PathProvider;
using System.Collections.Generic;
using System.IO;

namespace osuKeysoundSplitter.Strategy
{
    public abstract class BaseKeysoundingStrategy : KeysoundingStrategy
    {
        private File mapsetFolder;
        private KeysoundPathProvider keysoundPathProvider;
        private AudioEncoder audioEncoder;

        public BaseKeysoundingStrategy(File mapsetFolder,
                KeysoundPathProvider keysoundPathProvider, AudioEncoder audioEncoder)
        {
            this.mapsetFolder = mapsetFolder;
            this.keysoundPathProvider = keysoundPathProvider;
            this.audioEncoder = audioEncoder;
        }

        public override File getMapsetFolder()
        {
            return mapsetFolder;
        }

        public override KeysoundPathProvider getKeysoundPathProvider()
        {
            return keysoundPathProvider;
        }

        public override AudioEncoder getAudioEncoder()
        {
            return audioEncoder;
        }

        public override List<string> rewriteTimingPoints(List<TimingPoint> timingPoints, List<Keysound> keysounds)
        {
            return TimingPoint.buildTimingPointLines(timingPoints);
        }
    }
}