package com.dark.force;

public class NativeLibrary
{
    static
    {
        System.loadLibrary("wolve");
    }

    public static native void init();

    public static native void changeToggle(int i);

    public static native String[] getListFT();
}