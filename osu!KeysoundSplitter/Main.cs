import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.damnae.osukeysoundsplitter.audio.encode.AudioEncoder;
import com.damnae.osukeysoundsplitter.audio.encode.OggAudioEncoder;
import com.damnae.osukeysoundsplitter.audio.encode.WavAudioEncoder;

namespace osuKeysoundSplitter
{
public class Main {

	public static void main(String[] args) throws IOException {
		if (args.length < 3) {
			System.out
					.println("Syntax: java -jar \"osu!KeysoundSplitter.jar\" mapsetPath keysoundsOffsetInMilliseconds encodingFormat");
			return;
		}

		File folder = new File(args[0]);
		int offset = Integer.valueOf(args[1]);
		String format = args[2];

		AudioEncoder audioEncoder = audioEncoders.get(format);
		if (audioEncoder == null)
			audioEncoder = audioEncoders.get("ogg");

		new MapsetProcessor().process(folder, offset, audioEncoder);
	}

	private static Map<String, AudioEncoder> audioEncoders = new HashMap<String, AudioEncoder>();
	static {
		audioEncoders.put("wav", new WavAudioEncoder());
		audioEncoders.put("ogg", new OggAudioEncoder());
	}
}
}