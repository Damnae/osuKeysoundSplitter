SET folder=%~dp0
java -jar "osu!KeysoundSplitter.jar" "%folder:~0,-1%" 0 ogg
pause