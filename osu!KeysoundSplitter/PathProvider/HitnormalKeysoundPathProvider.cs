using System;

namespace osuKeysoundSplitter.PathProvider
{
    public class HitnormalKeysoundPathProvider : BaseKeysoundPathProvider
    {
        private int sampleSetIndex;
        private int sampleTypeIndex;

        public HitnormalKeysoundPathProvider(KeysoundCache keysoundCache,
                int initialSampleType)
            : base(keysoundCache)
        {

            sampleSetIndex = Math.Max(1, initialSampleType);
        }

        protected override String getNewKeysoundPath(String extension)
        {
            String path = buildPath(extension);
            incrementPath();
            return path;
        }

        private String buildPath(String extension)
        {
            return sampleTypeNames[sampleTypeIndex] + "-hitnormal"
                    + (sampleSetIndex > 1 ? sampleSetIndex.ToString() : "") + "." + extension;
        }

        private void incrementPath()
        {
            ++sampleTypeIndex;
            if (sampleTypeIndex >= sampleTypeNames.Length)
            {
                sampleTypeIndex = 0;
                ++sampleSetIndex;
            }
        }

        protected override bool isPathValid(String keysoundPath)
        {
            return !keysoundPath.Contains("/") && !keysoundPath.Contains("\\")
                    && keysoundPath.Contains("-hitnormal")
                    && getSampleType(keysoundPath) != 0;
        }

        public static int getSampleSet(String path)
        {
            int endPosition = path.LastIndexOf('.');
            for (int i = endPosition - 1; i >= 0; --i)
            {
                char c = path[i];
                if (!(c >= '0' && c <= '9'))
                    return Integer.valueOf(path.Substring(i + 1, endPosition));

            }
            return 0;
        }

        public static int getSampleType(String path)
        {
            for (int i = 0, size = sampleTypeNames.Length; i < size; ++i)
            {
                if (path.StartsWith(sampleTypeNames[i]))
                    return i + 1;
            }
            return 0;
        }

        public static String getSampleTypeName(int sampleType)
        {
            if (sampleType < 1 || sampleType > sampleTypeNames.Length)
                throw new InvalidParameterException(String.ValueOf(sampleType));

            return sampleTypeNames[sampleType - 1];
        }

        private static String[] sampleTypeNames = { "normal", "soft", "drum" };
    }
}