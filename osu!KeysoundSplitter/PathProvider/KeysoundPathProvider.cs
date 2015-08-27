
namespace osuKeysoundSplitter.PathProvider
{
public interface KeysoundPathProvider {

	String getIdentifier(byte[] data);

	boolean isGenerated(String keysoundIdentifier);

	String getKeysoundPath(String keysoundIdentifier, String extension);
}
}