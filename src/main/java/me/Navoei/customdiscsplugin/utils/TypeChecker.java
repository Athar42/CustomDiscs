package me.Navoei.customdiscsplugin.utils;

import com.destroystokyo.paper.MaterialTags;
import me.Navoei.customdiscsplugin.CustomDiscs;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.EnumSet;
import java.util.Set;

public class TypeChecker {
    static CustomDiscs customDiscs = CustomDiscs.getInstance();

    // Commented methods are kept for possible future checks usage.

    // MUSIC DISCS

    public static boolean isMusicDisc(ItemStack item) {
        return MaterialTags.MUSIC_DISCS.isTagged(item.getType());
    }

    /*public static boolean isMusicDiscPlayer(Player p) {
        return p.getInventory().getItemInMainHand().getType().toString().contains("MUSIC_DISC");
    }*/

    public static boolean isCustomMusicDisc(ItemStack itemStack) {
        if (itemStack == null) return false;
        if (itemStack.getItemMeta() == null) return false;
        return MaterialTags.MUSIC_DISCS.isTagged(itemStack.getType()) && itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(customDiscs, "customdisc"), PersistentDataType.STRING);
    }

    /*public static boolean isCustomMusicDiscPlayer(Player p) {
        return p.getInventory().getItemInMainHand().getType().toString().contains("MUSIC_DISC") && p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(customDiscs, "customdisc"), PersistentDataType.STRING);
    }*/

    // GOAT HORNS

    /*public static boolean isGoatHorn(ItemStack item) {
        return item.getType().toString().contains("GOAT_HORN");
    }*/

    public static boolean isGoatHornPlayer(Player p) {
        return p.getInventory().getItemInMainHand().getType().equals(Material.GOAT_HORN);
    }
    
    public static boolean isCustomGoatHorn(PlayerInteractEvent e) {
        if (e.getItem()==null) return false;
        return e.getItem().getType().equals(Material.GOAT_HORN) && e.getItem().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(customDiscs, "customhorn"), PersistentDataType.STRING);
    }

    public static boolean isCustomGoatHornPlayer(Player p) {
        return p.getInventory().getItemInMainHand().getType().equals(Material.GOAT_HORN) && p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(customDiscs, "customhorn"), PersistentDataType.STRING);
    }

    // PLAYER HEADS

    private static final Set<Material> HEAD_LIST_MATERIALS = EnumSet.of(
            Material.PLAYER_HEAD,
            Material.WITHER_SKELETON_SKULL,
            Material.SKELETON_SKULL,
            Material.ZOMBIE_HEAD,
            Material.CREEPER_HEAD,
            Material.PIGLIN_HEAD,
            Material.DRAGON_HEAD
    );

    private static final Set<Material> HEAD_WALL_LIST_MATERIALS = EnumSet.of(
            Material.PLAYER_WALL_HEAD,
            Material.WITHER_SKELETON_WALL_SKULL,
            Material.SKELETON_WALL_SKULL,
            Material.ZOMBIE_WALL_HEAD,
            Material.CREEPER_WALL_HEAD,
            Material.PIGLIN_WALL_HEAD,
            Material.DRAGON_WALL_HEAD
    );

    /*public static boolean isHead(ItemStack item) {
        return item.getType().toString().contains("PLAYER_HEAD");
    }*/

    public static boolean isHead(Material material) {
        return HEAD_LIST_MATERIALS.contains(material);
    }

    public static boolean isWallHead(Material material) {
        return HEAD_WALL_LIST_MATERIALS.contains(material);
    }

    public static boolean isHeadPlayer(Player p) {
        return isHead(p.getInventory().getItemInMainHand().getType());
    }

    /*public static boolean isCustomHead(ItemStack itemStack) {
        if (itemStack == null) return false;
        if (itemStack.getItemMeta() == null) return false;
        return itemStack.getType().toString().contains("PLAYER_HEAD") && itemStack.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(customDiscs, "customhead"), PersistentDataType.STRING);
    }*/

    public static boolean isCustomHeadPlayer(Player p) {
        return p.getInventory().getItemInMainHand().getType().equals(Material.PLAYER_HEAD) && p.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().has(new NamespacedKey(customDiscs, "customhead"), PersistentDataType.STRING);
    }

}