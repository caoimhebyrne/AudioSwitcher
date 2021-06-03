package dev.cbyrne.audioswitcher.asm.impl;

import dev.cbyrne.audioswitcher.AudioSwitcher;
import dev.cbyrne.audioswitcher.config.AudioSwitcherConfig;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;

import java.util.List;

@SuppressWarnings("unused")
public class LibraryLWJGLOpenALImpl {
    public static void createAL() throws LWJGLException {
        try {
            // Used when reloading, we can't create an AL if one already exists
            if (AL.isCreated()) AL.destroy();
            List<String> devices = AudioSwitcher.getInstance().devices;

            // Only scan for new devices if there is none found, it's already reloaded by the GUI
            if (devices.isEmpty()) {
                AL.create();

                AudioSwitcher.getInstance().getDevices();
                devices = AudioSwitcher.getInstance().devices;

                AL.destroy();
            }

            String soundDevice = AudioSwitcherConfig.SELECTED_SOUND_DEVICE;
            AudioSwitcher.getInstance().logger.info("Switching to sound device: " + soundDevice);

            // Create an OpenAL instance with our sound device. If it isn't available, just use the default
            if (devices.contains(soundDevice)) {
                AudioSwitcher.getInstance().logger.info("Sound device {} is available", soundDevice);
                AL.create(soundDevice, 44100, 60, false);
            } else {
                AudioSwitcher.getInstance().logger.warn("Failed to find sound device {}, using system default", soundDevice);
                AL.create();
            }
        } catch (Exception e) {
            AudioSwitcher.getInstance().logger.warn("Failed to find create device!, using system default. Error: ", e);
            AL.create();
        }
    }
}
