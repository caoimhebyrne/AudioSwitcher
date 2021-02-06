package dev.amber.audioswitcher.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Map;

public class VersionChecker {
    private final String jsonURL = "https://raw.githubusercontent.com/dreamhopping/AudioSwitcher/master/version.json";
    public String minecraftVersion = Minecraft.getMinecraft().getVersion();
    public int modVersion = 1;

    @Nullable
    public UpdateJsonResponse checkForUpdate() {
        try {
            URL url = new URL(jsonURL);
            Type responseType = new TypeToken<Map<String, UpdateJsonResponse>>() {}.getType();

            InputStreamReader reader = new InputStreamReader(url.openStream());
            Map<String, UpdateJsonResponse> response = new Gson().fromJson(reader, responseType);

            reader.close();

            UpdateJsonResponse updateInfo = response.get(minecraftVersion);
            return updateInfo.version > modVersion ? updateInfo : null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class UpdateJsonResponse {
        public String versionString;
        public String url;
        public int version;
    }
}
