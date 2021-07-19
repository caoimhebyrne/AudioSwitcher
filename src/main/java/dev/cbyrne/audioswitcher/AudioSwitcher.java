package dev.cbyrne.audioswitcher;

import dev.cbyrne.audioswitcher.alc.ALCHelper;
import dev.cbyrne.audioswitcher.config.AudioSwitcherConfig;
import dev.cbyrne.audioswitcher.util.VersionChecker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreenOptionsSounds;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
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
    @SuppressWarnings("unused")
    @Mod.Instance
    private static AudioSwitcher instance;

    private int buttonYPosition = 0;
    private boolean previousWasOptionsSounds = false;
    private boolean isPatcherInstalled = false;
    private boolean sentUpdateMessage = false;
    private VersionChecker.UpdateJsonResponse updateInfo = null;

    public Logger logger = LogManager.getLogger("AudioSwitcher");
    public VersionChecker versionChecker = new VersionChecker("https://raw.githubusercontent.com/cbyrneee/AudioSwitcher/master/version.json");;

    public ALCHelper alcHelper = new ALCHelper();
    public List<String> devices = new ArrayList<>();

    public static AudioSwitcher getInstance() {
        return instance;
    }

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        logger.info("Initialising AudioSwitcher...");
        isPatcherInstalled = Loader.isModLoaded("patcher");

        AudioSwitcherConfig.loadConfig();

        MinecraftForge.EVENT_BUS.register(this);
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                logger.info("Checking for updates...");
                updateInfo = versionChecker.checkForUpdate();
                if (updateInfo != null) {
                    logger.info("New update available! Version: {}", updateInfo.versionString);
                } else {
                    logger.info("No updates available!");
                }
            } catch (Exception e) {
                logger.error("Failed to check for updates:", e);
            }
        });
    }

    @SubscribeEvent
    public void onEntityJoin(EntityJoinWorldEvent event) {
        if (event.entity instanceof EntityPlayerSP && !sentUpdateMessage && updateInfo != null) {
            sentUpdateMessage = true;
            ChatStyle linkStyle = new ChatStyle().setChatClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, updateInfo.url)).setChatHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentText("Download update")));

            event.entity.addChatMessage(new ChatComponentText(
                EnumChatFormatting.DARK_RED + "[" +
                    EnumChatFormatting.RED + "AudioSwitcher" +
                    EnumChatFormatting.DARK_RED + "]" +
                    EnumChatFormatting.GRAY + " An update is available! (" + updateInfo.versionString + ") Click here for more info")
                .setChatStyle(linkStyle));
        }
    }

    @SubscribeEvent
    public void onGuiInit(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.gui instanceof GuiScreenOptionsSounds) {
            previousWasOptionsSounds = true;
            fetchAvailableDevicesCached();

            String soundDevice = AudioSwitcherConfig.SELECTED_SOUND_DEVICE;
            if (soundDevice == null || soundDevice.isEmpty()) {
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
                fetchAvailableDevicesUncached();
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

    public void fetchAvailableDevicesCached() {
        devices = alcHelper.getAvailableDevices(true);
    }

    public void fetchAvailableDevicesUncached() {
        devices = alcHelper.getAvailableDevices(false);
        logger.info(Arrays.toString(devices.toArray()));
    }
}
