package dev.cbyrne.audioswitcher.alc;

import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;

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
        if (!useCache || devices.isEmpty())
            devices = Arrays.stream(getAvailableDevices()).distinct().collect(Collectors.toList());

        return devices;
    }

    public String[] getAvailableDevices() {
        return ALC10.alcGetString(null, ALC11.ALC_ALL_DEVICES_SPECIFIER).split("\0");
    }
}
