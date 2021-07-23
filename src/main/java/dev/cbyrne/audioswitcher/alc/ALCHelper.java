package dev.cbyrne.audioswitcher.alc;

import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * A class to help with interacting with OpenAL
 */
public class ALCHelper {
    private List<String> devices = new ArrayList<>();

    public List<String> getAvailableDevices(boolean useCache) {
        if (!useCache || devices.isEmpty()) {
            String[] available = getAvailableDevices();

            if (available == null) {
                devices = new ArrayList<>();
            } else {
                devices = Arrays.stream(available).distinct().collect(Collectors.toList());
            }
        }

        return devices;
    }

    @Nullable
    public String[] getAvailableDevices() {
        try {
            return ALC10.alcGetString(null, ALC11.ALC_ALL_DEVICES_SPECIFIER).split("\0");
        } catch (Exception ignored) {
            return ALC10.alcGetString(null, ALC10.ALC_DEVICE_SPECIFIER).split("\0");
        }
    }
}
