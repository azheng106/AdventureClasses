package me.aquaponics.adventureclasses.managers;

import me.aquaponics.adventureclasses.AdventureClass;
import me.aquaponics.adventureclasses.AdventureClasses;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ConfigManager {
    private final AdventureClasses plugin;
    private HashMap<AdventureClass, HashMap<Integer, LevelInfo>> classLevels = new HashMap<>();

    public ConfigManager(AdventureClasses plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    /**
     * Load class leveling info from config into HashMap
     */
    private void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        Set<String> classNames = config.getConfigurationSection("classes").getKeys(false);
        for (String className : classNames) {
            HashMap<Integer, LevelInfo> levels = new HashMap<>();
            for (String levelStr : config.getConfigurationSection("classes." + className + ".levels").getKeys(false)) {
                int level = Integer.parseInt(levelStr);
                String name = config.getString("classes." + className + ".levels." + levelStr + ".name");
                List<String> lore = config.getStringList("classes." + className + ".levels." + levelStr + ".lore");
                levels.put(level, new LevelInfo(name, lore));
            }
            classLevels.put(AdventureClass.valueOf(className), levels);
        }
    }

    public LevelInfo getLevelInfo(String className, int level) {
        return classLevels.getOrDefault(AdventureClass.valueOf(className), new HashMap<>()).get(level);
    }

    public record LevelInfo(String name, List<String> lore) {
    }
}
