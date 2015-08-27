using System;

namespace osuKeysoundSplitter.PathProvider
{
    public class AdditionsKeysoundPathProvider : BaseKeysoundPathProvider
    {
        private int sampleSet;
        private int sampleTypeIndex;
        private int additionIndex;

        public AdditionsKeysoundPathProvider(KeysoundCache keysoundCache,
                int initialSampleType)
            : base(keysoundCache)
        {
            sampleSet = Math.Max(1, initialSampleType);
        }

        protected override string getNewKeysoundPath(string extension)
        {
            string path = buildPath(extension);
            incrementPath();
            return path;
        }

        private String buildPath(String extension)
        {
            return sampleTypeNames[sampleTypeIndex] + "-hit"
                    + additionNames[additionIndex]
                    + (sampleSet > 1 ? sampleSet.ToString() : "") + "." + extension;
        }

        private void incrementPath()
        {
            ++additionIndex;
            if (additionIndex >= additionNames.Length)
            {
                additionIndex = 0;
                ++sampleTypeIndex;
                if (sampleTypeIndex >= sampleTypeNames.Length)
                {
                    sampleTypeIndex = 0;
                    ++sampleSet;
                }
            }
        }

        protected override bool isPathValid(String keysoundPath)
        {
            return !keysoundPath.Contains("/") && !keysoundPath.Contains("\\")
                    && keysoundPath.Contains("-hit")
                    && getSampleType(keysoundPath) != 0
                    && getAdditions(keysoundPath) != 0;
        }

        public static int getSampleSet(String path)
        {
            int endPosition = path.LastIndexOf('.');
            for (int i = endPosition - 1; i >= 0; --i)
            {
                char c = path[i];
                if (!(c >= '0' && c <= '9'))
                    return int.Parse(path.Substring(i + 1, endPosition));
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

        public static int getAdditions(String path)
        {
            for (int i = 0, size = additionNames.Length; i < size; ++i)
            {
                if (path.Contains(additionNames[i]))
                    return additionCodes[i];
            }
            return 0;
        }

        private static String[] sampleTypeNames = { "normal", "soft", "drum" };
        private static String[] additionNames = { "whistle", "finish", "clap" };
        private static int[] additionCodes = { 2, 4, 8 };
    }
}