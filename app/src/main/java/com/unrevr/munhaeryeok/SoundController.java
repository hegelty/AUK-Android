package com.unrevr.munhaeryeok;

public class SoundController {
    public static boolean playing = false;
    public static void playSound() {
        playing = true;
    }
    public static void stopSound() {
        playing = false;
    }
}
