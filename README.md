osuKeysoundSplitter
===================

## How to

* [Generate a voice-only 16bit flac from a song and its intrumental version.](http://www.howtogeek.com/61250/how-to-isolate-and-save-vocals-from-music-tracks-using-audacity/)
* Also export both the synchronized instrumental and complete song separately (@192kb/s).
* Import the instrumental in osu! and time it.
* Add the voice flac inside the map's folder.
* Make a copy of the diff and make it use the complete song's mp3.
* Place notes over the voice.
* Place bookmarks where the voice parts start and end, each pair of bookmarks describe a section that will have keysounds. Bookmarks can also be used to cut breathing sounds from the notes, see how they are placed in [this map](https://osu.ppy.sh/s/208734).
* Copy the bookmarks and notes to the instrument diff.
* Edit the paths in Main.java:
```
public static final String FOLDER_PATH = "C:/Games/osu!/Songs/208734 SNoW - NightmaRe/";
public static final String DIFF_FILE = "SNoW - NightmaRe (Damnae) [NM].osu";
```
`FOLDER_PATH` is the absolute path to the mapset and `DIFF_FILE` the relative path to the instrumental diff that will get keysounded.
* Run!
* Once done you can remove the complete mp3, its diff and the voice flac.
