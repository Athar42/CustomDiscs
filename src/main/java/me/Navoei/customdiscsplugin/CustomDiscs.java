package me.Navoei.customdiscsplugin;

import me.Navoei.customdiscsplugin.command.CustomDiscCommand;
import me.Navoei.customdiscsplugin.event.JukeBox;
import me.Navoei.customdiscsplugin.event.HeadPlay;
import me.Navoei.customdiscsplugin.event.HornPlay;
import me.Navoei.customdiscsplugin.language.Lang;
import me.Navoei.customdiscsplugin.utils.ServerVersionChecker;
import me.Navoei.customdiscsplugin.utils.UpdateChecker;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;

import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEffect;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import org.bukkit.block.Jukebox;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CustomDiscs extends JavaPlugin {
	static CustomDiscs instance;

	@Nullable
	private VoicePlugin voicechatPlugin;
	@Nullable
	private UpdateChecker updateChecker;
	private Logger pluginLogger;
    private static boolean debugMode = false;
    private static Component[] helpMessage;
    private static final LegacyComponentSerializer LegacyComponentAmpersand = LegacyComponentSerializer.legacyAmpersand();
    public static YamlConfiguration LANG;
	public static File LANG_FILE;
	public static boolean musicDiscEnable = true;
	public static boolean musicDiscPlayingEnable = true;
	public float musicDiscDistance;
	public float musicDiscMaxDistance;
	public float musicDiscVolume;
	public static boolean customHornEnable = true;
	public static boolean customHornPlayingEnable = true;
	public float customHornDistance;
	public float customHornMaxDistance;
	public int hornCooldown;
	public int hornMaxCooldown;
	public static boolean customHeadEnable = true;
	public static boolean customHeadPlayingEnable = true;
	public float customHeadDistance;
	public float customHeadMaxDistance;
    public int filename_maximum_length;

	@Override
	public void onLoad() {
		CustomDiscs.instance = this;

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();

        CommandAPI.onLoad(new CommandAPIPaperConfig(this).verboseOutput(true).fallbackToLatestNMS(true));
	}
	
	@Override
	public void onEnable() {
		pluginLogger = getLogger();

		PacketEvents.getAPI().init();

		CommandAPI.onEnable();

        new CustomDiscCommand(this).register("customdiscs");

		BukkitVoicechatService service = getServer().getServicesManager().load(BukkitVoicechatService.class);
		
		this.saveDefaultConfig();
		loadLang();

		// Config initializer section
		debugMode = getConfig().getBoolean("debugMode", false);
		musicDiscEnable = getConfig().getBoolean("music-disc-enable");
		musicDiscPlayingEnable = getConfig().getBoolean("music-disc-playing-enable");
		musicDiscDistance = getConfig().getInt("music-disc-distance");
		musicDiscMaxDistance = getConfig().getInt("music-disc-max-distance");
		musicDiscVolume = Float.parseFloat(Objects.requireNonNull(getConfig().getString("music-disc-volume")));
		customHornEnable = getConfig().getBoolean("custom-horn-enable");
		customHornPlayingEnable = getConfig().getBoolean("custom-horn-playing-enable");
		customHornDistance = getConfig().getInt("custom-horn-distance");
		customHornMaxDistance = getConfig().getInt("custom-horn-max-distance");
		hornCooldown = getConfig().getInt("horn-cooldown");
		hornMaxCooldown = getConfig().getInt("horn-max-cooldown");
		customHeadEnable = getConfig().getBoolean("custom-head-enable");
		customHeadPlayingEnable = getConfig().getBoolean("custom-head-playing-enable");
		customHeadDistance = getConfig().getInt("custom-head-distance");
		customHeadMaxDistance = getConfig().getInt("custom-head-max-distance");
        filename_maximum_length = getConfig().getInt("filename-maximum-length");

		// Checking server version and display console message in case the server is not supported
        ServerVersionChecker serverVersionChecker = new ServerVersionChecker(this);
		serverVersionChecker.checkVersion();

		File musicData = new File(this.getDataFolder(), "musicdata");
		if (!(musicData.exists())) {
			musicData.mkdirs();
		}

        helpMessage = new Component[]{
                LegacyComponentAmpersand.deserialize("&8-[&6CustomDiscs v"+ this.getPluginMeta().getVersion() +" - Help Page&8]-"),
                LegacyComponentAmpersand.deserialize("&aAuthor&7: ")
                        .append(Component.text("Navoei")
                        .color(TextColor.color(0xd9a334))
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl("https://github.com/Navoei"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to open Navoei's GitHub page")))),
                LegacyComponentAmpersand.deserialize("&aContributors&7: ")
                        .append(Component.text("Athar42")
                        .color(TextColor.color(0xd9a334))
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl("https://github.com/Athar42"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to open Athar42's GitHub page"))))
                        .append(Component.text(" / "))
                        .append(Component.text("alfw")
                        .color(TextColor.color(0xd9a334))
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl("https://github.com/alfw"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to open alfw's GitHub page")))),
                LegacyComponentAmpersand.deserialize("&fGit&0Hub&7: ")
                        .append(Component.text("https://github.com/Navoei/CustomDiscs")
                        .color(NamedTextColor.BLUE)
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl("https://github.com/Navoei/CustomDiscs"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to open CustomDiscs' GitHub page")))),
                LegacyComponentAmpersand.deserialize("&aDiscord&7: ")
                        .append(Component.text("https://discord.gg/rJtBRmRFCr")
                        .color(NamedTextColor.BLUE)
                        .decorate(TextDecoration.UNDERLINED)
                        .clickEvent(ClickEvent.openUrl("https://discord.gg/rJtBRmRFCr"))
                        .hoverEvent(HoverEvent.showText(Component.text("Click to join our Discord !"))))
        };
		
		if (service != null) {
			voicechatPlugin = new VoicePlugin();
			service.registerPlugin(voicechatPlugin);
			pluginLogger.info("Successfully registered CustomDiscs plugin");
		} else {
			pluginLogger.info("Failed to register CustomDiscs plugin");
		}

		if (isMusicDiscEnable()) {
			getServer().getPluginManager().registerEvents(new JukeBox(), this);
			getServer().getPluginManager().registerEvents(new HopperManager(), this);
		}
		if (isCustomHeadEnable()) {	getServer().getPluginManager().registerEvents(new HeadPlay(), this); }
		if (isCustomHornEnable()) {	getServer().getPluginManager().registerEvents(new HornPlay(), this); }

		if (getConfig().getBoolean("update-checker.enabled", true)) {
			String channel = getConfig().getString("update-checker.channel", "release");
			updateChecker = new UpdateChecker(this, channel);
			updateChecker.start();

			getServer().getPluginManager().registerEvents(new Listener() {
				@EventHandler
				public void onJoin(PlayerJoinEvent event) {
					if (updateChecker == null) return;
					String latestVersion = updateChecker.getLatestVersion();
					if (latestVersion == null) return;
					Player player = event.getPlayer();
					if (!player.isOp() && !player.hasPermission("customdiscs.update")) return;

					String currentVersion = getPluginMeta().getVersion();
					Component playerUpdateMessage = LegacyComponentAmpersand.deserialize(Lang.PREFIX + Lang.UPDATE_AVAILABLE.toString().replace("%latest_version%", latestVersion).replace("%current_version%", currentVersion));
					Component linkUpdateMessage = LegacyComponentAmpersand.deserialize("&8[&6CustomDiscs&8]&r &7➜ ").append(Component.text(UpdateChecker.MODRINTH_PAGE_URL).color(NamedTextColor.AQUA).decorate(TextDecoration.UNDERLINED).clickEvent(ClickEvent.openUrl(UpdateChecker.MODRINTH_PAGE_URL)).hoverEvent(HoverEvent.showText(Component.text("Click to open the Modrinth page"))));
					player.sendMessage(playerUpdateMessage);
					player.sendMessage(linkUpdateMessage);
				}
			}, this);
		}

		// To avoid any "0" values, set it to 1.
		if (hornCooldown <= 0) {
			hornCooldown = 1;
		}
		if (hornMaxCooldown <= 0) {
			hornMaxCooldown = 1;
		}

		PacketEvents.getAPI().getEventManager().registerListener(new PacketListenerAbstract(PacketListenerPriority.NORMAL) {
			@Override
			public void onPacketSend(PacketSendEvent event) {
				if (event.getPacketType() != PacketType.Play.Server.EFFECT) return;
				try {
					WrapperPlayServerEffect wrapper = new WrapperPlayServerEffect(event);
					if (wrapper.getType() != 1010) return;
					if (!isMusicDiscEnable()) return;
					if (!(event.getPlayer() instanceof Player player)) return;

					Vector3i jukeboxPosition = wrapper.getPosition();
					Location jukeboxLocation = new Location(player.getWorld(), jukeboxPosition.x, jukeboxPosition.y, jukeboxPosition.z);

					if (JukeboxStateManager.isCustomDiscLocation(jukeboxLocation)) {
						event.setCancelled(true);

						getServer().getRegionScheduler().run(CustomDiscs.this, jukeboxLocation, task ->
								JukeboxStateManager.start((Jukebox) jukeboxLocation.getBlock().getState())
						);
					}
				} catch (Exception e) {
					if (isDebugMode()) {
						pluginLogger.warning("Unexpected packet content detected (" + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
					}
				}
			}
		});
		
	}
	
	@Override
	public void onDisable() {
		CommandAPI.onDisable();
		PacketEvents.getAPI().terminate();
		if (updateChecker != null) {
			updateChecker.stop();
		}
		if (voicechatPlugin != null) {
			getServer().getServicesManager().unregister(voicechatPlugin);
			pluginLogger.info("Successfully unregistered CustomDiscs plugin");
		}
	}
	
	public static CustomDiscs getInstance() {
		return instance;
	}
        
	/**
	 * Load the lang.yml file.
	 */
	public void loadLang() {
		File lang = new File(getDataFolder(), "lang.yml");
		if (!lang.exists()) {
			try {
				getDataFolder().mkdir();
				lang.createNewFile();
				InputStream defConfigStream = this.getResource("lang.yml");
				if (defConfigStream != null) {
					copyInputStreamToFile(defConfigStream, lang);
					YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(lang);
					defConfig.save(lang);
					Lang.setFile(defConfig);
				}
			} catch (IOException e) {
				pluginLogger.severe("Failed to create lang.yml for CustomDiscs.");
				pluginLogger.severe("Now disabling...");
				this.setEnabled(false); // Without it loaded, we can't send them messages
				if (isDebugMode()) {
					pluginLogger.log(Level.SEVERE, "Exception output: ", e);
				}
			}
		}
		YamlConfiguration conf = YamlConfiguration.loadConfiguration(lang);
		for (Lang item : Lang.values()) {
			if (conf.getString(item.getPath()) == null) {
				conf.set(item.getPath(), item.getDefault());
			}
		}
		Lang.setFile(conf);
		LANG = conf;
		LANG_FILE = lang;
		try {
			conf.save(getLangFile());
		} catch (IOException e) {
			pluginLogger.warning("Failed to save lang.yml for CustomDiscs");
			pluginLogger.warning("Now disabling...");
			if (isDebugMode()) {
				pluginLogger.log(Level.SEVERE, "Exception output: ", e);
			}
		}
	}
	
	/**
	 * Gets the lang.yml config.
	 *
	 * @return The lang.yml config.
	 */
	public YamlConfiguration getLang() {
		return LANG;
	}
	
	/**
	 * Get the lang.yml file.
	 *
	 * @return The lang.yml file.
	 */
	public File getLangFile() {
		return LANG_FILE;
	}
	
	public static void copyInputStreamToFile(InputStream input, File file) {
		
		try (OutputStream output = new FileOutputStream(file)) {
			input.transferTo(output);
		} catch (IOException ioException) {
			if (isDebugMode()) {
				CustomDiscs.getInstance().getLogger().log(Level.SEVERE, "Exception output: ", ioException);
			}
		}
		
	}

	/**
	 * Get the debugMode configuration.
	 *
	 * @return The boolean value of debugMode.
	 */
	public static boolean isDebugMode() { return debugMode; }

	/**
	 * Get the musicDiscPlayingEnable configuration.
	 *
	 * @return The boolean value of musicDiscPlayingEnable.
	 */
	public static boolean isMusicDiscEnable() { return musicDiscEnable; }

	/**
	 * Get the customHornPlayingEnable configuration.
	 *
	 * @return The boolean value of customHornPlayingEnable.
	 */
	public static boolean isCustomHornEnable() { return customHornEnable; }

	/**
	 * Get the customHeadPlayingEnable configuration.
	 *
	 * @return The boolean value of customHeadPlayingEnable.
	 */
	public static boolean isCustomHeadEnable() { return customHeadEnable; }

	/**
	 * Get the musicDiscPlayingEnable configuration.
	 *
	 * @return The boolean value of musicDiscPlayingEnable.
	 */
	public static boolean isMusicDiscPlayingEnable() { return musicDiscPlayingEnable; }

	/**
	 * Get the customHornPlayingEnable configuration.
	 *
	 * @return The boolean value of customHornPlayingEnable.
	 */
	public static boolean isCustomHornPlayingEnable() { return customHornPlayingEnable; }

	/**
	 * Get the customHeadPlayingEnable configuration.
	 *
	 * @return The boolean value of customHeadPlayingEnable.
	 */
	public static boolean isCustomHeadPlayingEnable() { return customHeadPlayingEnable; }

    /**
     * Get the help message.
     *
     * @return The text component for the help message.
     */
    public static Component[] getHelpMessage() { return helpMessage; }
}