 osuKeysoundSplitter
===================

## How to

* [Generate a voice-only 16bit flac from a song and its instrumental version.](http://www.howtogeek.com/61250/how-to-isolate-and-save-vocals-from-music-tracks-using-audacity/)
* Also export both the synchronized instrumental and complete song separately (@192kb/s).
* Import the instrumental in osu! and time it. The diff name must be called the same as the flac: `voice.flac` + `SNoW - NightmaRe (Damnae) [voice].osu`.
* Add the voice flac inside the map's folder.
* Make a copy of the diff and make it use the complete song's mp3.
* Place notes over the voice.
* Place bookmarks where the voice parts start and end, each pair of bookmarks describe a section that will have keysounds. Bookmarks can also be used to cut breathing sounds from the notes, see how they are placed in [this map](https://osu.ppy.sh/s/208734).
* Copy the bookmarks and notes to the instrumental diff.
* Edit the path in Main.java:
```
public static final String FOLDER_PATH = "C:/Games/osu!/Songs/208734 SNoW - NightmaRe/";
```
`FOLDER_PATH` is the absolute path to the mapset and `DIFF_FILE` the relative path to the instrumental diff that will get keysounded.
* Run!
* Once done you can remove the complete mp3, its diff and the voice flac.
* 

## FAQTS

 * Yes, it works with any audio track, it doesn't have to be voice.
 * No, it doesn't separate different instruments from a complete song for you.
 * No, there's no user interface and no .jar yet. You have to compile it and run it.
 * No, it's only for .osu files, so no .bme.
 * No, it doesn't work with sliders yet; osu! doesn't support the way I insert hitsounds in an .osu when it's a slider.
 