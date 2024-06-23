package me.aquaponics.adventureclasses;

import me.aquaponics.adventureclasses.classes.*;
import me.aquaponics.adventureclasses.managers.TurretUpgradeManager;
import me.aquaponics.adventureclasses.commands.ChooseClassExecutor;
import me.aquaponics.adventureclasses.commands.ProgessionTreeExecutor;
import me.aquaponics.adventureclasses.listeners.PlayerJoinListener;
import me.aquaponics.adventureclasses.managers.ClassManager;
import me.aquaponics.adventureclasses.managers.ConfigManager;
import me.aquaponics.adventureclasses.managers.XpManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

public final class AdventureClasses extends JavaPlugin {
    private ClassManager classManager;
    private ConfigManager configManager;
    private XpManager xpManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        classManager = new ClassManager(this);
        configManager = new ConfigManager(this);
        xpManager = new XpManager(this, classManager);

        ChooseClassExecutor chooseClass = new ChooseClassExecutor(this, classManager, xpManager);
        getCommand("chooseclass").setExecutor(chooseClass);
        getServer().getPluginManager().registerEvents(chooseClass, this);

        getCommand("progressiontree").setExecutor(new ProgessionTreeExecutor(classManager, configManager, xpManager));
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(classManager), this);
        getServer().getPluginManager().registerEvents(new Warrior(this, classManager, xpManager), this);
        getServer().getPluginManager().registerEvents(new Archer(this, classManager, xpManager), this);
        getServer().getPluginManager().registerEvents(new Engineer(this, classManager, xpManager), this);
        getServer().getPluginManager().registerEvents(new TurretUpgradeManager(this), this);
        getServer().getPluginManager().registerEvents(new Assassin(this, classManager, xpManager), this);
        getServer().getPluginManager().registerEvents(new Healer(this, classManager, xpManager), this);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    updateScoreboard(p);
                }
            }
        }.runTaskTimer(this, 0L, 20L);
    }
    // Update player's scoreboard with class and XP information
    public void updateScoreboard(Player p) {
        AdventureClass playerClass = classManager.getPlayerClass(p);
        if (playerClass != null) {
            ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
            Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
            Objective objective = scoreboard.registerNewObjective("playerInfo", "dummy", ChatColor.GREEN + "Player Info");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);

            Score score = objective.getScore(ChatColor.YELLOW + "Class: " + playerClass.name());
            score.setScore(3);
            score = objective.getScore(ChatColor.YELLOW + "Current Level: " + xpManager.getLevel(p));
            score.setScore(2);
            score = objective.getScore(ChatColor.YELLOW + "XP for Next Level: " + Math.round((xpManager.getRequiredXp(xpManager.getLevel(p) + 1) - xpManager.getXp(p))));
            score.setScore(1);

            p.setScoreboard(scoreboard);
        }
    }
}
