package dev.amber.audioswitcher.asm.impl;

import dev.amber.audioswitcher.AudioSwitcher;
import dev.amber.audioswitcher.config.AudioSwitcherConfig;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;

import java.util.Arrays;
import java.util.List;

public class LibraryLWJGLOpenALImpl {
    public static void createAL() throws LWJGLException {
        try {
            // Create an OpenAL context for searching for devices
            AL.create();

            // Get all devices
            AudioSwitcher.getInstance().getDevices();
            List<String> devices = AudioSwitcher.getInstance().devices;

            // Destroy the old context
            AL.destroy();

            // Create an OpenAL instance with our sound device, or if it doesn't exist just use the default
            if(devices.contains(AudioSwitcherConfig.SELECTED_SOUND_DEVICE)) {
                AudioSwitcher.getInstance().logger.info("Previously used sound device (" + AudioSwitcherConfig.SELECTED_SOUND_DEVICE + ") is available, using it!");
                AL.create(AudioSwitcherConfig.SELECTED_SOUND_DEVICE, 44100, 60, false);
            } else {
                // Fallback to default sound device, selected one isn't available
                AudioSwitcher.getInstance().logger.warn("Failed to find previously selected sound device, using system default...");
                AL.create();
            }
        } catch (Exception e) {
            AL.create();
        }
    }
}
