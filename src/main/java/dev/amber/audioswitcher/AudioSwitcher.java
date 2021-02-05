package dev.amber.audioswitcher;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
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
    private List<String> devices = new ArrayList<>();

    public static AudioSwitcher getInstance() {
        return instance;
    }

    public void getDevices() {
        devices.clear();

        try {
            String[] availableDevices = ALC10.alcGetString(null, ALC11.ALC_ALL_DEVICES_SPECIFIER).split("\0");
            System.out.println(Arrays.toString(availableDevices));
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent e) {
        logger.info("Initialising AudioSwitcher...");
    }
}
