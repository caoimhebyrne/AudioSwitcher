package dev.amber.audioswitcher;

import dev.amber.audioswitcher.asm.impl.LibraryLWJGLOpenALImpl;
import dev.amber.audioswitcher.config.AudioSwitcherConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALC11;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Mod(modid = "audioswitcher")
public class AudioSwitcher {
    @Mod.Instance
    private static AudioSwitcher instance;
    public final List<String> devices = new ArrayList<>();
    public Logger logger = LogManager.getLogger("AudioSwitcher");
    int buttonYPosition = 0;
    boolean previousWasOptionsSounds = false;

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
    public void onPreInit(FMLPreInitializationEvent event) {
        logger.info("Initialising AudioSwitcher...");

        AudioSwitcherConfig.loadConfig();
        MinecraftForge.EVENT_BUS.register(this);
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

                    buttonYPosition = button.yPosition + 45;
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
                event.gui.drawCenteredString(Minecraft.getMinecraft().fontRendererObj, "Sound Device", event.gui.width / 2, buttonYPosition - 15, -1);
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
                    int index = devices.indexOf(soundDevice) + 1;

                    if (index >= devices.size()) {
                        // Go to the start of the array
                        AudioSwitcherConfig.SELECTED_SOUND_DEVICE = devices.get(0);
                    } else {
                        AudioSwitcherConfig.SELECTED_SOUND_DEVICE = devices.get(index);
                    }
                } else {
                    AudioSwitcherConfig.SELECTED_SOUND_DEVICE = devices.get(0);
                }

                logger.info("Setting sound device to: " + AudioSwitcherConfig.SELECTED_SOUND_DEVICE);

                // Readjust the button's text
                String buttonText = AudioSwitcherConfig.SELECTED_SOUND_DEVICE;
                int stringWidth = Minecraft.getMinecraft().fontRendererObj.getStringWidth(buttonText);
                if (stringWidth >= 175) {
                    buttonText = Minecraft.getMinecraft().fontRendererObj.trimStringToWidth(buttonText, 170) + "...";
                }

                event.button.displayString = buttonText;
            }
        }
    }

    private void refreshDevices() {
        if (devices.isEmpty()) {
            getDevices();
        }
    }
}
