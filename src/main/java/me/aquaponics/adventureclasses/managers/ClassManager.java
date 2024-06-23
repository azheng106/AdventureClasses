package me.aquaponics.adventureclasses.managers;

import me.aquaponics.adventureclasses.AdventureClass;
import me.aquaponics.adventureclasses.AdventureClasses;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ClassManager {
    private final AdventureClasses plugin;
    private final Map<UUID, AdventureClass> playerClasses = new HashMap<>();

    public ClassManager(AdventureClasses plugin) {
        this.plugin = plugin;
        loadPlayerClasses();
    }

    public void chooseClass(Player player, AdventureClass clazz) {
        playerClasses.put(player.getUniqueId(), clazz);
        plugin.getConfig().set("player." + player.getUniqueId() + ".class", String.valueOf(clazz));
        plugin.saveConfig();
    }

    public AdventureClass getPlayerClass(Player player) {
        return playerClasses.get(player.getUniqueId());
    }

    public void loadPlayerClasses() {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection section = config.getConfigurationSection("player");
        if (section != null) {
            Set<String> keys = section.getKeys(false);
            for (String key : keys) {
                String className = config.getString("player." + key + ".class");
                UUID playerUUID = UUID.fromString(key);
                playerClasses.put(playerUUID, AdventureClass.valueOf(className));
            }
        }

    }
}
