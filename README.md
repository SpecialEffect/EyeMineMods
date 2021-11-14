# EyeMineArchitectury
A version of [EyeMineMods](https://github.com/SpecialEffect/EyeMineMods) that is build upon the [Architectury API](https://github.com/architectury/architectury-api) allowing it to work on either Forge or Fabric.

This repository contains the Minecraft mods used with [EyeMine](https://github.com/SpecialEffect/EyeMine), which provides a gaze-controlled keyboard for in-game actions.

:warning: If you're a user of EyeMine you shouldn't need to view or use this repo, since everything is bundled into an installer at https://github.com/SpecialEffect/EyeMine/releases. This is only intended for developers or expert Minecraft users who are searching for alternate mod versions to manage themselves. 

## Information for users

[**The EyeMine wiki**](https://github.com/SpecialEffect/EyeMine/wiki) contains full information on how to get started, system requirements, a user guide, demo videos, a troubleshooting page, and more.

EyeMine is designed for eye-gaze control, and also supports head-pointer control. If you want to play Minecraft with different alternative inputs such as assistive switches and joystick, please [contact SpecialEffect](https://www.specialeffect.org.uk/contact) with more information about your setup. Some of the functionality in these mods may be helpful. 

## Supporting different versions
The **official** release of EyeMine currently supports Minecraft Forge **v1.14.4**, and the whole bundle (keyboards, mod and installer) are available for download from [**The EyeMine wiki**](https://github.com/SpecialEffect/EyeMine/wiki). If you don't already play Minecraft then you should get started with this one. 

### Other versions
The original "classic" version of EyeMine works with Minecraft Forge **v1.11.2** and can be downloaded as a bundle from the [EyeMine wiki: Classic installation](https://github.com/SpecialEffect/EyeMine/wiki/%5BClassic%5D-Installation).

A version of the mod ported by [Mrbysco](https://github.com/Mrbysco) supports **v1.16.5** and can be downloaded from https://github.com/SpecialEffect/EyeMineMods/releases

## Information for developers

Code for different versions of Minecraft Forge can be found on different branches:
- Minecraft Forge v1.11.2 (via the [eyemineClassic_mc1_11_2 branch](https://github.com/SpecialEffect/EyeMineMods/tree/eyemineClassic_mc1_11_2)) 
- Minecraft Forge v1.14.4 (via the [eyemineV2_mc1_14_4 branch](https://github.com/SpecialEffect/EyeMineMods/tree/eyemineV2_mc1_14_4)).
- Minecraft Forge v1.16.5 (via the [eyemineV2_mc1_16_5 branch](https://github.com/SpecialEffect/EyeMineMods/tree/eyemineV2_mc1_16_5)).

All the code comprising EyeMine is GPL3 licensed. We would love contributions to port to other versions of Minecraft. If you're interested in doing this, please email eyemine@specialeffect.org.uk to let us know what you're working on!

## Dev setup
These instructions are based on Windows 10 and using Eclipse as an IDE. If you are using a different setup, some details may vary.

### Pre-requisites
- Java SDK ("JDK")

### Instructions 
Get the code.
```
git clone git@github.com:SpecialEffect/EyeMineMods.git
cd EyeMineMods
```

Build the code. The built mod file will be found in the `build/libs` directory.
```
gradlew build
```

Optionally set up to build and run from Eclipse:
```
gradlew genEclipseRuns
gradlew eclipse
```
Now you can open the project in Eclipse:
File -> Import -> Gradle -> Existing Gradle project -> Set Project Root Directory to the EyeMineMods folder -> Finish
Once the project is loaded, select the project in Package Explorer, hit the green "Run" or "Debug" button and select "runClient" as the configuration to run.

