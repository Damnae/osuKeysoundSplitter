using osuKeysoundSplitter.Audio.Encode;
using osuKeysoundSplitter.PathProvider;
using System.IO;

namespace osuKeysoundSplitter.Strategy
{
    public class ManiaKeysoundingStrategy : BaseKeysoundingStrategy
    {

        public ManiaKeysoundingStrategy(File mapsetFolder,
                KeysoundCache keysoundCache, AudioEncoder audioEncoder)
            : base(mapsetFolder, new CounterKeysoundPathProvider(keysoundCache),
                audioEncoder)
        {
        }

        public ManiaKeysoundingStrategy(File mapsetFolder,
                KeysoundCache keysoundCache, AudioEncoder audioEncoder,
                string keysoundsFolderName)
            : base(mapsetFolder, new CounterKeysoundPathProvider(keysoundCache,
                keysoundsFolderName), audioEncoder)
        {
        }

        public override string rewriteKeysoundData(Keysound keysound, string keysoundData,
                int volume)
        {

            int colonPos = keysoundData.LastIndexOf(":");
            if (colonPos > -1)
            {
                keysoundData = keysoundData.Substring(0, colonPos) + ":"
                        + keysound.filename;
            }

            return keysoundData;
        }
    }
}