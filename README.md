 osuKeysoundSplitter
===================

## How to

* [Generate a voice-only 16bit flac from a song and its instrumental version.](http://www.howtogeek.com/61250/how-to-isolate-and-save-vocals-from-music-tracks-using-audacity/)
* Also export both the synchronized instrumental and complete song separately (@192kb/s).
* Import the instrumental in osu! and time it.
* Add the voice flac inside the map's folder.
* Make a copy of the diff and make it use the complete song's mp3. The name of diff should be suffixed with the flac name: if your diff is called `NM`, and the flac `voice.flac`, call this diff `NM -voice`.
* Place notes over the voice on this diff.
* Place bookmarks where the voice parts start and end, each pair of bookmarks describe a section that will have keysounds. Bookmarks can also be used to cut breathing sounds from the notes, see how they are placed in [this map](https://osu.ppy.sh/s/208734).
* Place keysound.bat and the jar in the mapset's folder and run keysound.bat. You can also use this command:
```
java -jar "osu!KeysoundSplitter.jar" mapsetPath keysoundsOffsetInMilliseconds
```

## FAQTS

 * Yes, it works with any audio track, it doesn't have to be voice.
 * Yes, you can have multiple keysound sources in the same map by having multiple flac and multiple diffs, each suffixed by one of the flac name: Diffs named `NM -voice`, `NM -guitar` with `voice.flac` and `guitar.flac` will be combined into `NM`.
 * It works with osu!, ctb and osu!mania.
 * No, it doesn't separate different instruments from a complete song for you.
 * No, there's no user interface yet.
 * No, it's only for .osu files, so no .bme.
