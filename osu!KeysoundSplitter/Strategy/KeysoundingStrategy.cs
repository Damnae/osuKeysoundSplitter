using osuKeysoundSplitter.Audio.Encode;
using osuKeysoundSplitter.PathProvider;
using System.Collections.Generic;
using System.IO;

namespace osuKeysoundSplitter.Strategy
{
    public interface KeysoundingStrategy
    {
        File getMapsetFolder();
        AudioEncoder getAudioEncoder();
        KeysoundPathProvider getKeysoundPathProvider();
        string rewriteKeysoundData(Keysound keysound, string keysoundData, int volume);
        List<string> rewriteTimingPoints(List<TimingPoint> timingPoints, List<Keysound> keysounds);
    }
}