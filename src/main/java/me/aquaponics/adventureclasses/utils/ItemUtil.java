package me.aquaponics.adventureclasses.utils;

import me.aquaponics.adventureclasses.AdventureClasses;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class ItemUtil {
    /**
     * Utility function that returns the Healer's Wand item
     */
    public static ItemStack healerWand() {
        ItemStack wand = new ItemStack(Material.STICK, 1);
        ItemMeta meta = wand.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "" + ChatColor.BOLD + "Healer's Wand");
        meta.getPersistentDataContainer().set(new NamespacedKey(AdventureClasses.getPlugin(AdventureClasses.class),
                "healerwand"), PersistentDataType.INTEGER, 0);
        meta.setLore(List.of(ChatColor.RED + "Right click to use healer's ability"));
        wand.setItemMeta(meta);
        return wand;
    }
}
