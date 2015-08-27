
namespace osuKeysoundSplitter.PathProvider
{
    public interface KeysoundPathProvider
    {
        string getIdentifier(byte[] data);
        bool isGenerated(string keysoundIdentifier);
        string getKeysoundPath(string keysoundIdentifier, string extension);
    }
}