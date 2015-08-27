
namespace osuKeysoundSplitter.PathProvider
{
    public abstract class BaseKeysoundPathProvider : KeysoundPathProvider
    {
        private KeysoundCache keysoundCache;
        private MessageDigest messageDigest;
        private StringBuffer sb = new StringBuffer();

        public BaseKeysoundPathProvider(KeysoundCache keysoundCache)
        {
            this.keysoundCache = keysoundCache;
            try
            {
                messageDigest = MessageDigest.getInstance("SHA1");

            }
            catch (NoSuchAlgorithmException e)
            {
                throw new Exception(e);
            }
        }

        public override string getIdentifier(byte[] data)
        {
            byte[] mdbytes = messageDigest.digest(data);

            foreach (byte mdbyte in mdbytes)
            {
                sb.append(Integer.toString((mdbyte & 0xff) + 0x100, 16)
                        .substring(1));
            }
            string identifier = sb.toString();
            sb.setLength(0);

            return identifier;
        }

        public override bool isGenerated(string keysoundIdentifier)
        {
            return keysoundCache.hasKeysound(keysoundIdentifier);
        }

        public override string getKeysoundPath(string keysoundIdentifier, string extension)
        {
            string keysoundPath = keysoundCache.getKeysoundPath(keysoundIdentifier);

            if (keysoundPath != null && !isPathValid(keysoundPath))
                keysoundPath = null;

            if (keysoundPath == null)
            {
                while (keysoundPath == null
                        || !keysoundCache.isPathAvailable(keysoundPath))
                {

                    keysoundPath = getNewKeysoundPath(extension);
                }
                keysoundCache.register(keysoundIdentifier, keysoundPath);
            }

            return keysoundPath;
        }

        protected bool isPathValid(string keysoundPath)
        {
            return true;
        }

        protected abstract string getNewKeysoundPath(string extension);
    }
}