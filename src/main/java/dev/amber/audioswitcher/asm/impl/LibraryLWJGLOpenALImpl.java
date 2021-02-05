package dev.amber.audioswitcher.asm.impl;

import dev.amber.audioswitcher.AudioSwitcher;
import dev.amber.audioswitcher.config.AudioSwitcherConfig;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;

import java.util.List;

public class LibraryLWJGLOpenALImpl {
    public static void createAL() throws LWJGLException {
        try {
            // For reloading, we can't create an AL if one already exists
            if (AL.isCreated()) {
                AL.destroy();
            }

            // Create an OpenAL context for searching for devices
            AL.create();

            // Get all devices
            List<String> devices = AudioSwitcher.getInstance().devices;
            if (devices.isEmpty()) AudioSwitcher.getInstance().getDevices();
            devices = AudioSwitcher.getInstance().devices;

            // Destroy the old context
            AL.destroy();

            String soundDevice = AudioSwitcherConfig.SELECTED_SOUND_DEVICE;
            AudioSwitcher.getInstance().logger.info("Switching to sound device: " + soundDevice);

            // Create an OpenAL instance with our sound device, or if it doesn't exist just use the default
            if (devices.contains(soundDevice)) {
                AudioSwitcher.getInstance().logger.info("Sound device {} is available", soundDevice);
                AL.create(soundDevice, 44100, 60, false);
            } else {
                // Fallback to default sound device, selected one isn't available
                AudioSwitcher.getInstance().logger.warn("Failed to find sound device {}, using system default", soundDevice);
                AL.create();
            }
        } catch (Exception e) {
            AudioSwitcher.getInstance().logger.warn("Failed to find create device!, using system default. Error: ", e);
            AL.create();
        }
    }
}
