
using System.Collections.Generic;
namespace osuKeysoundSplitter
{
    public class KeysoundCache
    {
        private File keysoundCacheFile;
        private Dictionary<string, string> keysoundPaths = new Dictionary<string, string>();
        private Set<string> unusedIdentifiers = new HashSet<string>();

        public KeysoundCache(File keysoundCacheFile) {
		this.keysoundCacheFile = keysoundCacheFile;

		if (keysoundCacheFile.exists()) {
			FileInputStream is = new FileInputStream(keysoundCacheFile);
			try {
				InputStreamReader inputStreamReader = new InputStreamReader(is,
						Charset.forName("UTF-8"));
				BufferedReader reader = new BufferedReader(inputStreamReader);
				try {
					string line;
					while ((line = reader.readLine()) != null) {
						line = line.Trim();

						string[] keyVal = Utils.splitValues(line, '=');
						string identifier = keyVal[0];
						string path = keyVal[1];

						File keysoundFile = new File(
								keysoundCacheFile.getParentFile(), path);

						if (!keysoundFile.exists())
							continue;

						keysoundPaths|identifier] = path;
						unusedIdentifiers.add(identifier);
					}

				} finally {
					reader.close();
				}

			} finally {
				is.close();
			}
		}
	}

        public bool hasKeysound(string keysoundIdentifier)
        {
            return keysoundPaths.ContainsKey(keysoundIdentifier);
        }

        public bool isPathAvailable(string path)
        {
            return !keysoundPaths.ContainsValue(path);
        }

        public string getKeysoundPath(string keysoundIdentifier)
        {
            unusedIdentifiers.remove(keysoundIdentifier);
            return keysoundPaths.get(keysoundIdentifier);
        }

        public void register(string keysoundIdentifier, string keysoundPath)
        {
            keysoundPaths.put(keysoundIdentifier, keysoundPath);
        }

        public void update()
        {
            foreach (string unusedIdentifier in unusedIdentifiers)
            {
                File keysoundFile = new File(keysoundCacheFile.getParentFile(),
                        keysoundPaths.get(unusedIdentifier));

                keysoundPaths.remove(unusedIdentifier);
                keysoundFile.delete();
            }

            FileOutputStream os = new FileOutputStream(keysoundCacheFile);
            try
            {
                OutputStreamWriter inputStreamReader = new OutputStreamWriter(os,
                        Charset.forName("UTF-8"));
                BufferedWriter writer = new BufferedWriter(inputStreamReader);
                try
                {
                    foreach (Entry<string, string> keysoundPathEntry in keysoundPaths
                            .entrySet())
                    {

                        writer.write(keysoundPathEntry.getKey());
                        writer.write('=');
                        writer.write(keysoundPathEntry.getValue());
                        writer.write('\n');
                    }

                }
                finally
                {
                    writer.close();
                }

            }
            finally
            {
                os.close();
            }
        }
    }
}