# SpecialEffectMinecraftMods
A collection of small minecraft modifications to support the use of an eye-gaze controlled keyboard as input. Designed to accompany OptiKeyMinecraft.
Functionality would also be applicable for switch control.

## Dev setup
If you want to contribute, you need to install minecraft forge to build the mods. 
These instructions are based on Windows 8.1, Minecraft 1.8.8 and using Eclipse as an IDE.

### Setting up forge
1) Download the source ("MDK") for Forge from http://files.minecraftforge.net/. Extract it to your favourite folder. For the sake of these instructions, it is assumed you extract it to C:\code\forge-1.8.8\

2) Open a command prompt in C:\code\forge-1.8.8\ and run the following commands. This will set up everything for development.
> gradlew setupDecompWorkspace

> gradlew eclipse

3) Load the forge project in Eclipse, by launching Eclipse and selecting C:\code\forge-1.8.8\eclipse as the workspace path. 

If all has gone well, Minecraft will be launched when you hit "Run", including an ExampleMod which comes bundled with the Forge code.

### Hooking up mod code
1) Clone this repository into (e.g.) C:\code\SpecialEffectMinecraftMods

2) Remove the directory containing the sample "ExampleMod" in the forge source code. We'll replace this with a soft link to our own mod code.
> rm C:\code\forge-1.8.8\src\main\java\com

3) Create a soft link to the mod code from within your forge source folder:
- Launch a command prompt as an administrator. On Win8.1, you can do this by right-clicking on the Windows button in the bottom left and selecting "Command Prompt (Admin)"
- Create the softlink (change the paths as appropriate to match your setup):

> mklink /D C:\code\forge-1.8.8\src\main\java C:\code\mods 

Now, if all has gone well, Minecraft will be launched when you hit "Run", and the SpecialEffect mod will be loaded. Verify that:
- Under "Mods" there is now an entry called "SpecialEffectEyeGaze"
- Under Options -> Controls, there's now a section labelled "SpecialEffect"

### Building a mod for distribution
Take a look at the contents of release.py to see the required steps.
