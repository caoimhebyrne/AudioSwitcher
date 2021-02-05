package dev.amber.audioswitcher.launch.asm.impl;

import dev.amber.audioswitcher.AudioSwitcher;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;

public class LibraryLWJGLOpenALImpl {
    public static void createAL() throws LWJGLException {
        try {
            AL.create("Realtek HD Audio 2nd output (Realtek(R) Audio)", 44100, 60, false);
            AudioSwitcher.getInstance().getDevices();
        } catch(Exception e) {
            // Fallback to default sound device
            AL.create();
        }
    }
}
