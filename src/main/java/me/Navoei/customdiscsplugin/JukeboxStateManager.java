package me.Navoei.customdiscsplugin;

import org.bukkit.Location;
import org.bukkit.block.Jukebox;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class JukeboxStateManager {

    static CustomDiscs plugin = CustomDiscs.getInstance();
    static PlayerManager playerManager = PlayerManager.instance();

    static Set<Location> playingJukeboxLocations = ConcurrentHashMap.newKeySet();

    static Set<Location> pendingJukeboxLocations = ConcurrentHashMap.newKeySet();

    public static void markJukeboxPending(Location jukeboxLocation) {
        pendingJukeboxLocations.add(jukeboxLocation);
    }

    public static boolean isCustomDiscLocation(Location jukeboxLocation) {
        return pendingJukeboxLocations.contains(jukeboxLocation) || playingJukeboxLocations.contains(jukeboxLocation);
    }

    public static void start(Jukebox jukebox) {
        Location jukeboxLocation = jukebox.getLocation();
        pendingJukeboxLocations.remove(jukeboxLocation);

        if (playingJukeboxLocations.contains(jukeboxLocation) || !playerManager.isAudioPlayerPlaying(jukeboxLocation)) return;

        playingJukeboxLocations.add(jukeboxLocation);
        plugin.getServer().getRegionScheduler().runAtFixedRate(plugin, jukeboxLocation, scheduledTask -> {
            if (playerManager.isAudioPlayerPlaying(jukeboxLocation)) {
                if (!jukebox.isPlaying()) {
                    jukebox.startPlaying();
                }
            } else {
                jukebox.stopPlaying();
                playingJukeboxLocations.remove(jukeboxLocation);
                scheduledTask.cancel();
            }
        }, 1, 1);
    }

}