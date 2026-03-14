package me.Navoei.customdiscsplugin.command.SubCommands;

import me.Navoei.customdiscsplugin.CustomDiscs;
import me.Navoei.customdiscsplugin.language.Lang;
import me.Navoei.customdiscsplugin.utils.FilebinUtils;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.arguments.TextArgument;
import dev.jorel.commandapi.executors.CommandArguments;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DownloadSubCommand extends CommandAPICommand {
	private final CustomDiscs plugin;
	private final boolean debugModeResult = CustomDiscs.isDebugMode();

	public DownloadSubCommand(CustomDiscs plugin) {
		super("download");
		this.plugin = plugin;

		this.withFullDescription(NamedTextColor.GRAY + "Downloads a file from a given URL.");
		this.withUsage("/customdisc download <url> <filename.extension>");
		this.withPermission("customdiscs.download");

		this.withArguments(new TextArgument("url"));
		this.withArguments(new StringArgument("filename"));

		this.executesPlayer(this::onCommandPlayer);
		this.executesConsole(this::onCommandConsole);
	}

	private int onCommandPlayer(Player player, CommandArguments arguments) {
		final Logger pluginLogger = plugin.getLogger();

        plugin.getServer().getAsyncScheduler().runNow(this.plugin, scheduledTask ->  {
			try {
				try {
					String urlString = Objects.requireNonNull(arguments.getByClass("url", String.class));
					URI uri = new URI(urlString);
					if (uri.getScheme() == null) return;
                    if (!uri.getScheme().equalsIgnoreCase("http") && !uri.getScheme().equalsIgnoreCase("https")) {
                        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_PROTOCOL.toString()));
                        return;
                    }
					URL fileURL = uri.toURL();

                    String filename = Objects.requireNonNull(arguments.getByClass("filename", String.class));
                    if (filename.length() > this.plugin.filename_maximum_length) {
                        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_FILENAME_LENGTH.toString().replace("%filename_length_value%", Integer.toString(this.plugin.filename_maximum_length))));
                        return;
                    }

					if(debugModeResult) {
						pluginLogger.info("DEBUG - Download File URL: " + fileURL);
						pluginLogger.info("DEBUG - File name: " + filename);
					}

					if (filename.contains("../")) {
						player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_FILENAME.toString()));
						return;
					}

					String fileExtension = getFileExtension(filename);
					if (!fileExtension.equals("wav") && !fileExtension.equals("mp3") && !fileExtension.equals("flac")) {
						player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.INVALID_FORMAT.toString()));
						return;
					}

					String finalFilename = filename;
					File downloadFileLocation = Path.of(this.plugin.getDataFolder().getPath(), "musicdata", finalFilename).toFile();
					if (downloadFileLocation.exists()) {
						finalFilename = getAvailableFilename(filename);
						downloadFileLocation = Path.of(this.plugin.getDataFolder().getPath(), "musicdata", finalFilename).toFile();
						player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.FILE_ALREADY_EXISTS.toString().replace("%filename%", filename).replace("%new_filename%", finalFilename)));
					}

					player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.DOWNLOADING_FILE.toString()));

					int maxDownloadSize = this.plugin.getConfig().getInt("max-download-size", 50);

					if (FilebinUtils.isFilebinUrl(urlString)) {
						try {
							if (FilebinUtils.isFilebinDirectUrl(urlString)) {
								if (debugModeResult) {
									pluginLogger.info("DEBUG - Detected Filebin direct file URL, downloading...");
								}
								FilebinUtils.downloadFilebinFile(fileURL, downloadFileLocation, maxDownloadSize);
							} else {
								if (debugModeResult) {
									pluginLogger.info("DEBUG - Detected Filebin bin URL, querying API...");
								}
								FilebinUtils.FilebinFileInfo fileInfo = FilebinUtils.getFirstAudioFile(urlString, maxDownloadSize);
								if (debugModeResult) {
									pluginLogger.info("DEBUG - Filebin file found: " + fileInfo.filename() + " (" + fileInfo.bytes() + " bytes)");
								}
								FilebinUtils.downloadFilebinFile(fileInfo.downloadUrl(), downloadFileLocation, maxDownloadSize);
							}
						} catch (FilebinUtils.FileTooLargeException e) {
							player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.FILE_TOO_LARGE.toString().replace("%max_download_size%", String.valueOf(maxDownloadSize))));
							return;
						} catch (FilebinUtils.NoAudioFilesException e) {
							player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.FILEBIN_NO_AUDIO.toString()));
							pluginLogger.warning("No valid audio file found in Filebin bin: " + urlString);
							if (debugModeResult) {
								pluginLogger.severe("Exception output: " + e);
							}
							return;
						} catch (IOException e) {
							player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.FILEBIN_API_ERROR.toString()));
							pluginLogger.warning("Filebin download failed for: " + urlString);
							if (debugModeResult) {
								pluginLogger.severe("Exception output: " + e);
							}
							return;
						}
					} else {
						URLConnection connection = fileURL.openConnection();
						if (connection != null) {
							long size = connection.getContentLengthLong() / 1048576;
							if (size > maxDownloadSize) {
								player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.FILE_TOO_LARGE.toString().replace("%max_download_size%", String.valueOf(maxDownloadSize))));
								return;
							}
						}
						FileUtils.copyURLToFile(fileURL, downloadFileLocation);
					}

					player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.SUCCESSFUL_DOWNLOAD.toString().replace("%file_path%", "plugins/CustomDiscs/musicdata/" + finalFilename)));
					player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.CREATE_DISC.toString().replace("%filename%", finalFilename)));
				} catch (URISyntaxException | MalformedURLException e) {
					player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.DOWNLOAD_ERROR.toString()));
					pluginLogger.warning("A download error occurred.");
					if(debugModeResult) {
						pluginLogger.log(Level.SEVERE, "Exception output: ", e);
					}
				}
			} catch (IOException e) {
				player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(Lang.PREFIX + Lang.DOWNLOAD_ERROR.toString()));
				pluginLogger.warning("A download error occurred.");
				if(debugModeResult) {
					pluginLogger.log(Level.SEVERE, "Exception output: ", e);
				}
			}
		});

		return 1;
	}

	private int onCommandConsole(ConsoleCommandSender executor, CommandArguments arguments) {
		executor.sendMessage(NamedTextColor.RED + "Only players can use this command : '"+arguments+"'!");
		return 1;
	}

	private String getAvailableFilename(String filename) {
		int lastDotLocation = filename.lastIndexOf('.');
		String base = lastDotLocation > 0 ? filename.substring(0, lastDotLocation) : filename;
		String ext  = lastDotLocation > 0 ? filename.substring(lastDotLocation) : "";
		int i = 1;
		String lookupNextAvailableFilename;
		do {
			lookupNextAvailableFilename = base + "_" + i + ext;
			i++;
		} while (Path.of(this.plugin.getDataFolder().getPath(), "musicdata", lookupNextAvailableFilename).toFile().exists());
		return lookupNextAvailableFilename;
	}

	private String getFileExtension(String s) {
		int index = s.lastIndexOf(".");
		if (index > 0) {
			return s.substring(index + 1);
		} else {
			return "";
		}
	}

}