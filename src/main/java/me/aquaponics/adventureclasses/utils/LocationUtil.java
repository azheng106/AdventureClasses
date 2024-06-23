package me.aquaponics.adventureclasses.utils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.persistence.PersistentDataHolder;

public class LocationUtil {
    /**
     * Turn location into string to use in config.yml or PersistentDataHolder
     */

    public static String serializeLocation(Location location) {
        String world = location.getWorld().getName();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        return world + ";" + x + ";" + y + ";" + z;
    }

    /**
     * Turn serialized location string back into location
     */
    public static Location deserializeLocation(String locationString) {
        String[] parts = locationString.split(";");
        World world = Bukkit.getWorld(parts[0]);
        int x = Integer.parseInt(parts[1]);
        int y = Integer.parseInt(parts[2]);
        int z = Integer.parseInt(parts[3]);
        return new Location(world, x, y, z);
    }
}
