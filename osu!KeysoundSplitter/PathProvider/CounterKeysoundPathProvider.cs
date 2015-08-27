
namespace osuKeysoundSplitter.PathProvider
{
    public class CounterKeysoundPathProvider : BaseKeysoundPathProvider
    {
        private string keysoundsFolderName;
        private int fileIndex;

        public CounterKeysoundPathProvider(KeysoundCache keysoundCache)
            : base(keysoundCache)
        {
        }

        public CounterKeysoundPathProvider(KeysoundCache keysoundCache,
                string keysoundsFolderName)
            : base(keysoundCache)
        {
            this.keysoundsFolderName = keysoundsFolderName;
        }

        protected override string getNewKeysoundPath(string extension)
        {
            ++fileIndex;

            if (keysoundsFolderName == null)
                return fileIndex + "." + extension;

            return keysoundsFolderName + "/" + fileIndex + "." + extension;
        }
    }
}