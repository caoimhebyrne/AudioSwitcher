package dev.cbyrne.audioswitcher.config;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.FMLClientHandler;

import java.io.File;

public class AudioSwitcherConfig {
    private static final File configFile = new File(FMLClientHandler.instance().getClient().mcDataDir, "config/audioswitcher.config");

    public static String SELECTED_SOUND_DEVICE = "";

    public static void saveConfig() {
        Configuration configuration = new Configuration(configFile);
        updateConfig(configuration, false);

        configuration.save();
    }

    public static void loadConfig() {
        Configuration configuration = new Configuration(configFile);
        configuration.load();

        updateConfig(configuration, true);
    }

    private static void updateConfig(Configuration configuration, boolean load) {
        Property selectedSoundDeviceProperty = configuration.get("General", "selectedSoundDevice", "");

        if (load) SELECTED_SOUND_DEVICE = selectedSoundDeviceProperty.getString();
        else selectedSoundDeviceProperty.setValue(SELECTED_SOUND_DEVICE);
    }
}
