using osuKeysoundSplitter.Audio.Encode;
using System.Collections.Generic;

namespace osuKeysoundSplitter
{
public class Main {

	public static void main(string[] args) {
		if (args.Length < 3) {
			System.out
					.println("Syntax: java -jar \"osu!KeysoundSplitter.jar\" mapsetPath keysoundsOffsetInMilliseconds encodingFormat");
			return;
		}

		File folder = new File(args[0]);
		int offset = Integer.valueOf(args[1]);
		String format = args[2];

		AudioEncoder audioEncoder = audioEncoders[format];
		if (audioEncoder == null)
			audioEncoder = audioEncoders["ogg"];

		new MapsetProcessor().process(folder, offset, audioEncoder);
	}

	private static Dictionary<string, AudioEncoder> audioEncoders = new Dictionary<string, AudioEncoder>();
	static {
		audioEncoders.put("wav", new WavAudioEncoder());
		audioEncoders.put("ogg", new OggAudioEncoder());
	}
}
}