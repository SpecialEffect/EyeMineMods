# EyeMineMods
This repository contains the Minecraft mods used with [EyeMine](https://github.com/SpecialEffect/EyeMine), which provides a gaze-controlled keyboard for in-game actions.

## Information for users

[**The EyeMine wiki**](https://github.com/SpecialEffect/EyeMine/wiki) contains full information on how to get started, system requirements, a user guide, demo videos, a troubleshooting page, and more.

EyeMine is designed for eye-gaze control, and also supports head-pointer control. If you want to play Minecraft with different alternative inputs such as assistive switches and joystick, please [contact SpecialEffect](https://www.specialeffect.org.uk/contact) with more information about your setup. Some of the functionality in these mods may be helpful. 

## Information for developers

These EyeMine mods currently support Minecraft Forge v1.11.2 (via the [eyemineClassic_mc1_11_2 branch](https://github.com/SpecialEffect/EyeMineMods/tree/eyemineClassic_mc1_11_2) and Minecraft Forge v1.14.4 (via the [eyemineV2_mc1_14_4 branch](https://github.com/SpecialEffect/EyeMineMods/tree/eyemineV2_mc1_14_4). We would love contributions to port to other versions of Minecraft. 

All the code comprising EyeMine is GPL3 licensed.

## Dev setup
If you want to contribute, you need to install minecraft forge to build the mods. 
These instructions are based on Windows 8.1, Minecraft 1.11.2 and using Eclipse as an IDE.

### Pre-requisites
- An IDE, such as Eclipse 
- Java SDK ("JDK")

### Setting up forge
1) Download the source ("MDK") for Forge from http://files.minecraftforge.net/. Extract it to your favourite folder. For the sake of these instructions, it is assumed you extract it to C:\code\forge-1.11.2\

2) Open a command prompt in C:\code\forge-1.11.2\ and run the following commands. This will set up everything for development.
> gradlew setupDecompWorkspace

> gradlew eclipse

3) Load the forge project in Eclipse, by launching Eclipse and selecting C:\code\forge-1.11.2\eclipse as the workspace path. 

If all has gone well, Minecraft will be launched when you hit the green "Run" button, including an ExampleMod which comes bundled with the Forge code.

If it doesn't work, check out the troubleshooting tips here:
http://www.minecraftforge.net/forum/index.php/topic,14048.0.html#post_initial_setup

### Hooking up mod code
It's entirely possible to just check out the mod repository inside your forge source folder. If you want to do it, just replace C:\code\forge-1.11.2\src\main with the mod repo (and call it 'main').
However, as soon as you want to build the mod for more than one version of minecraft, you will find that this strategy doesn't scale. My preferred workflow is to use softlinks from each forge directory to point at a separate mods directory. Instructions for this are below.

1) Clone this repository into (e.g.) C:\code\SpecialEffectMinecraftMods

2) Remove the 'src\main' directory containing the sample "ExampleMod" in the forge source code. We'll replace this with a soft link to our own mod code.
> rmdir C:\code\forge-1.11.2\src\main

3) Create a soft link to the mod code from within your forge source folder:
- Launch a command prompt as an administrator. On Win8.1, you can do this by right-clicking on the Windows button in the bottom left and selecting "Command Prompt (Admin)"
- Create the softlink (change the paths as appropriate to match your setup):

> mklink /D C:\code\forge-1.11.2\src\main C:\code\SpecialEffectMinecraftMods 

Now, if all has gone well, Minecraft will be launched when you hit "Run", and the SpecialEffect mod will be loaded. Verify that:
- Under "Mods" there is now an entry called "EyeGaze"
- Under Options -> Controls, there are now sections labelled "EyeGaze: ..."

### Building a mod for distribution
You should be able to use (or adapt) the script *release.py* to build a mod for release. 
