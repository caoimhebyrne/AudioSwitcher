package dev.amber.audioswitcher;

import dev.amber.audioswitcher.config.AudioSwitcherConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Mod(modid = "audioswitcher")
public class AudioSwitcher {
    @Mod.Instance
    private static AudioSwitcher instance;

    public Logger logger = LogManager.getLogger("AudioSwitcher");
    public final List<String> devices = new ArrayList<>();

    public static AudioSwitcher getInstance() {
        return instance;
    }

    public void getDevices() {
        devices.clear();

        try {
            String[] availableDevices = ALC10.alcGetString(null, ALC11.ALC_ALL_DEVICES_SPECIFIER).split("\0");
            devices.addAll(Arrays.asList(availableDevices));
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }
    }

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent e) {
        logger.info("Initialising AudioSwitcher...");
        AudioSwitcherConfig.loadConfig();
    }
}
