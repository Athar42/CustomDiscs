package me.Navoei.customdiscsplugin.language;

import org.bukkit.configuration.file.YamlConfiguration;

public enum Lang {
    PREFIX("prefix", "&8[&6CustomDiscs&8]&r"),
    INVALID_FILENAME("invalid-filename", "&rThis is an invalid filename!"),
    INVALID_FORMAT("invalid-format", "&rFile must be in wav, flac, or mp3 format!"),
    FILE_NOT_FOUND("file-not-found", "&rFile not found!"),
    NOT_HOLDING_CORRECT_ITEM("not-holding-correct-item", "&rYou must either hold a disc, goat horn or player head in your main hand."),
    CREATE_FILENAME("create-filename", "&7Your filename is: &a\"%filename%\"."),
    CREATE_CUSTOM_NAME("create-custom-name", "&7Your custom name is: &a\"%custom_name%\"."),
    DOWNLOADING_FILE("downloading-file", "&7Downloading file..."),
    FILE_TOO_LARGE("file-too-large", "&rThe file is larger than %max_download_size%MB."),
    SUCCESSFUL_DOWNLOAD("successful-download", "&aFile successfully downloaded to &7%file_path%&a."),
    CREATE_DISC("create-disc", "&aCreate a disc by doing &7/cd create filename.extension \"Custom Lore\"&a."),
    DOWNLOAD_ERROR("download-error", "&rAn error has occurred while downloading."),
    NOW_PLAYING("now-playing","&6Now playing: %song_name%"),
    DISC_CONVERTED("disc-converted", "&aConverted disc to new format! &fThis is due to changes in newer Minecraft versions which introduced &ToolTipDisplay&f."),
    INVALID_RANGE("invalid-range","&rYou need to chose a range between 1 and %range_value%"),
    CREATE_CUSTOM_RANGE("create-custom-range", "&7Your range is set to: &a\"%custom_range%\"."),
    NOT_HOLDING_CUSTOM_GOAT_HORN("not-holding-custom-goathorn", "&cYou must hold a custom goat horn in your main hand."),
    INVALID_COOLDOWN("invalid-cooldown","&cYou need to chose a cooldown between 1 and %cooldown_value% (in ticks)."),
    CREATE_CUSTOM_GOAT_COOLDOWN("create-custom-goat-cooldown", "&7Your goat horn cooldown is set to: &a\"%custom_goat_cooldown%\" ticks."),
    CUSTOM_MUSIC_DISABLED("custom-music-disabled", "&7Custom music discs are disabled in the configuration."),
    CUSTOM_HEAD_DISABLED("custom-head-disabled", "&7Custom player heads are disabled in the configuration."),
    CUSTOM_HORN_DISABLED("custom-horn-disabled", "&7Custom goat horns are disabled in the configuration.");

    private final String path;
    private final String def;
    private static YamlConfiguration LANG;

    /**
     * Lang enum constructor.
     * @param path The string path.
     * @param start The default string.
     */
    Lang(String path, String start) {
        this.path = path;
        this.def = start;
    }

    /**
     * Set the {@code YamlConfiguration} to use.
     * @param config The config to set.
     */
    public static void setFile(YamlConfiguration config) {
        LANG = config;
    }

    @Override
    public String toString() {
        if (this == PREFIX)
            return LANG.getString(this.path, def) + " ";
        return LANG.getString(this.path, def);
    }

    /**
     * Get the default value of the path.
     * @return The default value of the path.
     */
    public String getDefault() {
        return this.def;
    }

    /**
     * Get the path to the string.
     * @return The path to the string.
     */
    public String getPath() {
        return this.path;
    }

}