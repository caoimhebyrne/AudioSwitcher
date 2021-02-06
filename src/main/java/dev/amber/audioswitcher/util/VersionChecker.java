package dev.amber.audioswitcher.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Map;

public class VersionChecker {
    private final String jsonURL = "https://raw.githubusercontent.com/dreamhopping/AudioSwitcher/master/version.json";
    public UpdateJsonResponse updateInfo;

    public String minecraftVersion = Minecraft.getMinecraft().getVersion();
    public int modVersion = 1;

    public boolean checkForUpdate() {
        try {
            URL url = new URL(jsonURL);
            Type responseType = new TypeToken<Map<String, UpdateJsonResponse>>() {}.getType();

            InputStreamReader reader = new InputStreamReader(url.openStream());
            Map<String, UpdateJsonResponse> response = new Gson().fromJson(reader, responseType);

            reader.close();

            updateInfo = response.get(minecraftVersion);
            return updateInfo.version > modVersion;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static class UpdateJsonResponse {
        public String versionString;
        public String url;
        public int version;
    }
}
