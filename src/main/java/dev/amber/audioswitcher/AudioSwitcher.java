package dev.amber.audioswitcher;

import dev.amber.audioswitcher.config.AudioSwitcherConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Mod(modid = "audioswitcher")
public class AudioSwitcher {
    @Mod.Instance
    private static AudioSwitcher instance;
    public List<String> devices = new ArrayList<>();
    public Logger logger = LogManager.getLogger("AudioSwitcher");
    int buttonYPosition = 0;
    boolean previousWasOptionsSounds = false;
    boolean isPatcherInstalled = false;

    public static AudioSwitcher getInstance() {
        return instance;
    }

    public void getDevices() {
        try {
            String[] availableDevices = ALC10.alcGetString(null, ALC11.ALC_ALL_DEVICES_SPECIFIER).split("\0");
            List<String> availableDevicesList = Arrays.asList(availableDevices);

            devices = availableDevicesList.stream().distinct().collect(Collectors.toList());
            logger.info("Found devices: {}", Arrays.toString(devices.toArray()));
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }
    }

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        logger.info("Initialising AudioSwitcher...");

        AudioSwitcherConfig.loadConfig();
        MinecraftForge.EVENT_BUS.register(this);

        isPatcherInstalled = Loader.isModLoaded("patcher");
    }

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.gui instanceof GuiScreenOptionsSounds) {
            previousWasOptionsSounds = true;

            // Refresh devices when the menu is opened
            getDevices();

            // The sounds gui was just initialised, add our button
            String soundDevice = AudioSwitcherConfig.SELECTED_SOUND_DEVICE;

            if (soundDevice == null || soundDevice.isEmpty()) {
                refreshDevices();
                soundDevice = devices.isEmpty() ? "Default Sound Device" : devices.get(0);
            }

            for (GuiButton button : event.buttonList) {
                if (button.id == 200) {
                    String buttonText = soundDevice;
                    int stringWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(buttonText);
                    if (stringWidth >= 175) {
                        buttonText = Minecraft.getMinecraft().fontRendererObj.trimStringToWidth(buttonText, 170) + "...";
                    }

                    buttonYPosition = isPatcherInstalled ? button.yPosition - 45 : button.yPosition - 25;
                    event.buttonList.add(new GuiButton(19238, (event.gui.width / 2) - 100, buttonYPosition, buttonText));
                    break;
                }
            }
        } else if (previousWasOptionsSounds) {
            previousWasOptionsSounds = false;

            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                AudioSwitcherConfig.saveConfig();
                return null;
            }, 0, TimeUnit.MILLISECONDS);

            try {
                Minecraft.getMinecraft().getSoundHandler().sndManager.reloadSoundSystem();
            } catch (Exception e) {
                logger.error("Failed to reinitialize OpenAL: ", e);
            }
        }
    }

    @SubscribeEvent
    public void onGuiDraw(GuiScreenEvent.DrawScreenEvent event) {
        if (event.gui instanceof GuiScreenOptionsSounds) {
            if (buttonYPosition != 0) {
                event.gui.drawCenteredString(Minecraft.getMinecraft().fontRendererObj, "Sound Device (click to change)", event.gui.width / 2, isPatcherInstalled ? buttonYPosition - 14 : buttonYPosition - 15, -1);
            }
        }
    }

    @SubscribeEvent
    public void onGuiAction(GuiScreenEvent.ActionPerformedEvent event) {
        if (event.gui instanceof GuiScreenOptionsSounds) {
            // Check if the button pressed was our button
            if (event.button.id == 19238) {
                refreshDevices();
                if (devices.isEmpty()) return;

                String soundDevice = AudioSwitcherConfig.SELECTED_SOUND_DEVICE;
                if (soundDevice != null && !soundDevice.isEmpty()) {
                    // The index of the current sound device
                    int index = devices.indexOf(soundDevice);
                    if (index + 1 >= devices.size()) {
                        soundDevice = devices.get(0);
                    } else {
                        soundDevice = devices.get(index + 1);
                    }
                } else {
                    soundDevice = devices.get(0);
                }

                logger.info("Setting sound device to: " + soundDevice);

                // Readjust the button's text
                String buttonText = soundDevice;
                int stringWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(buttonText);
                if (stringWidth >= 175) {
                    buttonText = Minecraft.getMinecraft().fontRendererObj.trimStringToWidth(buttonText, 170) + "...";
                }

                AudioSwitcherConfig.SELECTED_SOUND_DEVICE = soundDevice;
                event.button.displayString = buttonText;

                event.setCanceled(true);
            }
        }
    }

    private void refreshDevices() {
        if (devices.isEmpty()) {
            getDevices();
        }
    }
}
