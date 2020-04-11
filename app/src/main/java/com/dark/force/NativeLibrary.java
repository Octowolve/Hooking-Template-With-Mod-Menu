package com.dark.force;

public class NativeLibrary
{
    //this will automatically load the library when this class is called.
    static
    {
        System.loadLibrary("wolve");
    }

    public static native void init();

    public static native void changeToggle(int i);

    public static native void changeSeekBar(int i, int seekBarValue);

    public static native void changeSpinner(int i, String value);

    public static native void changeEditText(int i, String value);

    public static native String[] getListFT();
}
