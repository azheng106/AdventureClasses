package me.aquaponics.adventureclasses.managers;

import me.aquaponics.adventureclasses.AdventureClass;
import me.aquaponics.adventureclasses.AdventureClasses;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class XpManager {
    private final AdventureClasses plugin;
    private final ClassManager classManager;
    private final HashMap<UUID, Double> xpAmounts = new HashMap<>();
    private final HashMap<Integer, Integer> xpReqs = new HashMap<>();
    private final FileConfiguration config;

    public XpManager(AdventureClasses plugin, ClassManager classManager) {
        this.plugin = plugin;
        this.classManager = classManager;
        config = plugin.getConfig();
        loadConfig();
    }

    public void addXp(Player p, double amount) {
        UUID uuid = p.getUniqueId();
        xpAmounts.merge(uuid, amount, Double::sum);
        config.set("player." + uuid + ".xp", xpAmounts.get(uuid));
        plugin.saveConfig();
    }

    public double getXp(Player p) {
        return xpAmounts.getOrDefault(p.getUniqueId(), 0.0);
    }

    public boolean hasEnoughXp(Player p, double amount) {
        return getXp(p) >= amount;
    }

    public int getRequiredXp(int level) {
        return xpReqs.get(level);
    }

    public int getLevel(Player p) {
        for (int i = 1; i <= xpReqs.size(); i++) {
            if (xpReqs.get(i) > getXp(p)) {
                return (i - 1);
            }
        }
        return xpAmounts.size();
    }

    /**
     * Load player xp info from config.yml into HashMap
     */
    public void loadConfig() {
        ConfigurationSection playerSection = config.getConfigurationSection("player");
        if (playerSection != null) {
            Set<String> playerKeys = playerSection.getKeys(false);
            for (String key : playerKeys) {
                double xpAmount = config.getDouble("player." + key + ".xp");
                xpAmounts.put(UUID.fromString(key), xpAmount);
            }
        }
        ConfigurationSection xpSection = config.getConfigurationSection("xpreq");
        if (xpSection != null) {
            Set<String> xpKeys = xpSection.getKeys(false);
            for (String level : xpKeys) {
                int xpReq = config.getInt("xpreq." + level);
                xpReqs.put(Integer.parseInt(level), xpReq);
            }
        }
    }
}
