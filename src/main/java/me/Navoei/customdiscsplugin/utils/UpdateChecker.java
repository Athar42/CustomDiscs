package me.Navoei.customdiscsplugin.utils;

import me.Navoei.customdiscsplugin.CustomDiscs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class UpdateChecker {

    private final CustomDiscs plugin;
    private final Logger pluginLogger;
    private final String versionChannel;

    private static final String MODRINTH_API_URL = "https://api.modrinth.com/v2/project/customdiscs-plugin/version";
    public static final String MODRINTH_PAGE_URL = "https://modrinth.com/plugin/customdiscs-plugin";

    private static final long CHECK_INTERVAL_HOURS = 24;

    private volatile String latestVersion = null;
    private ScheduledTask hourlyTask = null;

    public UpdateChecker(CustomDiscs plugin, String versionChannel) {
        this.plugin = plugin;
        this.pluginLogger = plugin.getLogger();
        this.versionChannel = versionChannel;
    }

    public void start() {
        plugin.getServer().getAsyncScheduler().runNow(plugin, t -> runCheck());
        hourlyTask = plugin.getServer().getAsyncScheduler().runAtFixedRate(plugin, t -> runCheck(), CHECK_INTERVAL_HOURS, CHECK_INTERVAL_HOURS, TimeUnit.HOURS);
    }

    public void stop() {
        if (hourlyTask != null) {
            hourlyTask.cancel();
        }
    }

    private void runCheck() {
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

            String apiUrl = versionChannel.equals("release") ? MODRINTH_API_URL + "?version_type=release" : MODRINTH_API_URL;

            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(apiUrl)).header("User-Agent", "CustomDiscs/" + plugin.getPluginMeta().getVersion() + " (github.com/Navoei/CustomDiscs)").timeout(Duration.ofSeconds(10)).GET().build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                if (CustomDiscs.isDebugMode()) {
                    pluginLogger.warning("Modrinth API returned HTTP " + response.statusCode());
                }
                return;
            }

            JsonArray versions = JsonParser.parseString(response.body()).getAsJsonArray();
            if (versions.isEmpty()) return;

            String retrievedVersion = null;
            for (JsonElement jsonElement : versions) {
                JsonObject readVersion = jsonElement.getAsJsonObject();
                String readVersionType = readVersion.get("version_type").getAsString();
                if (readVersionType.equals("release") || (versionChannel.equals("beta") && readVersionType.equals("beta"))) {
                    retrievedVersion = readVersion.get("version_number").getAsString();
                    break;
                }
            }

            if (retrievedVersion == null) return;

            String currentVersion = plugin.getPluginMeta().getVersion();

            if (isNewer(retrievedVersion, currentVersion)) {
                latestVersion = retrievedVersion;
                pluginLogger.info("Update available: " + retrievedVersion + " (current: " + currentVersion + ") — " + MODRINTH_PAGE_URL);
            } else {
                latestVersion = null;
                if (CustomDiscs.isDebugMode()) {
                    pluginLogger.info("Plugin is up to date (" + currentVersion + ").");
                }
            }

        } catch (Exception e) {
            if (CustomDiscs.isDebugMode()) {
                pluginLogger.warning("Check failed: " + e.getMessage());
            }
        }
    }

    public String getLatestVersion() {
        return latestVersion;
    }

    private record VersionInfo(int[] base, boolean isBeta, int betaNumber) {}

    private VersionInfo parseVersion(String version) {
        boolean isBeta = false;
        int betaNumber = 0;

        int separatorIndex = version.indexOf('-');
        if (separatorIndex != -1) {
            isBeta = true;
            String suffix = version.substring(separatorIndex + 1);
            int numberSeparatorIndex = suffix.lastIndexOf('.');
            if (numberSeparatorIndex != -1) {
                try { betaNumber = Integer.parseInt(suffix.substring(numberSeparatorIndex + 1)); } catch (NumberFormatException ignored) {}
            }
            version = version.substring(0, separatorIndex);
        }

        String[] baseParts = version.split("\\.");
        int[] baseVersion = new int[baseParts.length];
        for (int i = 0; i < baseParts.length; i++) {
            baseVersion[i] = Integer.parseInt(baseParts[i].replaceAll("[^0-9].*", ""));
        }

        return new VersionInfo(baseVersion, isBeta, betaNumber);
    }

    private boolean isNewer(String remoteVersion, String currentVersion) {
        try {
            VersionInfo retrievedVersionInfo = parseVersion(remoteVersion);
            VersionInfo currentVersionInfo = parseVersion(currentVersion);

            int maxLen = Math.max(retrievedVersionInfo.base().length, currentVersionInfo.base().length);
            for (int i = 0; i < maxLen; i++) {
                int retrievedVersionDigit = i < retrievedVersionInfo.base().length ? retrievedVersionInfo.base()[i] : 0;
                int currentVersionDigit = i < currentVersionInfo.base().length ? currentVersionInfo.base()[i] : 0;
                if (retrievedVersionDigit != currentVersionDigit) return retrievedVersionDigit > currentVersionDigit;
            }

            if (!retrievedVersionInfo.isBeta() && currentVersionInfo.isBeta()) return true;
            if (retrievedVersionInfo.isBeta() && !currentVersionInfo.isBeta()) return false;
            if (retrievedVersionInfo.isBeta()) return retrievedVersionInfo.betaNumber() > currentVersionInfo.betaNumber();

            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
