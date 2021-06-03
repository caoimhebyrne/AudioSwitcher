package dev.cbyrne.audioswitcher.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.cbyrne.audioswitcher.Metadata;
import net.minecraft.client.Minecraft;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class VersionChecker {
    private final String minecraftVersion = Minecraft.getMinecraft().getVersion();
    private final URL jsonUrl;

    public VersionChecker(String jsonUrl) throws MalformedURLException {
        this.jsonUrl = new URL(jsonUrl);
    }

    /**
     * Checks for an update through the JSON url provided in the constructor
     *
     * @return An instance of {@link UpdateJsonResponse} if an update is available, otherwise null
     */
    @Nullable
    public UpdateJsonResponse checkForUpdate() throws IOException {
        Type responseType = new TypeToken<Map<String, UpdateJsonResponse>>() {
        }.getType();

        try (InputStreamReader reader = new InputStreamReader(this.jsonUrl.openStream())) {
            Map<String, UpdateJsonResponse> response = new Gson().fromJson(reader, responseType);

            UpdateJsonResponse updateInfo = response.get(minecraftVersion);
            return (updateInfo.version > Metadata.version) ? updateInfo : null;
        }
    }

    public static class UpdateJsonResponse {
        public String versionString;
        public String url;
        public int version;
    }
}
