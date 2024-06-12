# Custom Discs v2.6 for Paper 1.20.4 (1.20.6 Not Tested Yet)

A Paper fork of henkelmax's Audio Player.
- Play custom music discs using the Simple Voice Chat API. (The voice chat mod is required on the client and server.)
- Use ```/customdisc``` or ```/cd``` to view available commands.
- Music files should go into ```plugins/CustomDiscs/musicdata/```
- Music files must be in the ```.wav```, ```.flac```, or ```.mp3``` format.

Downloading Files:
- To download a file use the command ```/cd download <url> <filename.extension>```. The link used to download a file must be a direct link (meaning the file must automatically begin downloading when accessing the link). Files must have the correct extension specified. An UnsupportedAudioFileException will be thrown in the server's console if the file extension is not correct (for example when giving a wav file the mp3 extension). Below is an example of how to use the command and a link to get direct downloads from Google Drive.
- Example: ```/cd download https://example.com/mysong mysong.mp3```
- Direct Google Drive links: https://lonedev6.github.io/gddl/

Permission Nodes (Required to run the commands. Playing discs does not require a permission.):
- ```customdiscs.create``` to create a disc
- ```customdiscs.download``` to download a file

Dependencies:
- This plugin depends on the latest version of ProtocolLib for 1.20.x and SimpleVoiceChatBukkit version 2.4.25. 


https://user-images.githubusercontent.com/64107368/178426026-c454ac66-5133-4f3a-9af9-7f674e022423.mp4

Default Config:
```
# [Music Disc Config]

# The distance from which music discs can be heard in blocks.
music-disc-distance: 16

# The master volume of music discs from 0-1. (You can set values like 0.5 for 50% volume).
music-disc-volume: 1

#The maximum download size in megabytes.
max-download-size: 50
```


