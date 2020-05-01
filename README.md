# Substrate-Template-With-Mod-Menu

This is a simple template for the usage of Cydia Substrate with a Mod Menu written in Java.

### Installation:
* Download Android NDK, Revision 16b from: https://developer.android.com/ndk/downloads/older_releases
* Download this github files somewhere to your PC.

### Android Studio

* You need to configure Android Studio so it uses the old ndk
* File -> Project Structure -> SDK Location set the NDK Location path to the path whereever you downloaded the android-ndk 16b too

### How to implement this mod menu 
* https://guidedhacking.com/threads/android-mod-menu-tutorial-very-hard-works-on-il2cpp-and-native-games-too.13795/
* Read this tutorial carefully 

### Tutorial on how to use this mod menu
* https://guidedhacking.com/threads/function-pointers-and-a-tutorial-for-my-hooking-template.14771/#post-90490

### Layout
This is how the menu looks like when you simply build and run it 

![Mod Menu Layout](Images/VideoShowcase.gif)


### AARCH64
This template should support x64 hooking now thanks to this repo:
https://github.com/Rprop/And64InlineHook
