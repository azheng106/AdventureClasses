package me.aquaponics.adventureclasses.commands;

import me.aquaponics.adventureclasses.AdventureClass;
import me.aquaponics.adventureclasses.managers.ClassManager;
import me.aquaponics.adventureclasses.managers.ConfigManager;
import me.aquaponics.adventureclasses.managers.XpManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ProgessionTreeExecutor implements CommandExecutor {
    private final ClassManager classManager;
    private final ConfigManager configManager;
    private final XpManager xpManager;

    public ProgessionTreeExecutor(ClassManager classManager, ConfigManager configManager, XpManager xpManager) {
        this.classManager = classManager;
        this.configManager = configManager;
        this.xpManager = xpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player p) {
            AdventureClass playerClass = classManager.getPlayerClass(p);
            if (playerClass != null) {
                String playerClassStr = playerClass.name();
                Inventory gui = Bukkit.createInventory(null, 27, ChatColor.valueOf(playerClass.color) + "" + ChatColor.BOLD + "Progression Tree");
                for (int i = 0; i < 27; i++) {
                    ConfigManager.LevelInfo levelInfo = configManager.getLevelInfo(playerClassStr, i + 1);
                    if (levelInfo != null) {
                        double currentLevelXpReq = xpManager.getRequiredXp(i+1);
                        boolean hasEnoughXpForCurrentLvl = xpManager.hasEnoughXp(p, currentLevelXpReq);
                        Material paneColor = hasEnoughXpForCurrentLvl ? Material.GREEN_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
                        gui.setItem(i, createGuiItem(paneColor, levelInfo.name(), levelInfo.lore(), ChatColor.RESET + "" + ChatColor.DARK_AQUA + Math.round(xpManager.getXp(p)) + "/" + currentLevelXpReq));
                    }
                }
                p.openInventory(gui);
            } else {
                p.sendMessage(ChatColor.BOLD + "Choose a class first!");
            }
        }
        return true;
    }

    public static ItemStack createGuiItem(Material material, String name, List<String> lore, String... moreLore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        name = ChatColor.translateAlternateColorCodes('&', name);
        meta.setDisplayName(name);
        List<String> combinedLore = new ArrayList<>(lore);
        combinedLore.addAll(Arrays.asList(moreLore));
        combinedLore = combinedLore.stream()
                .map(x -> ChatColor.translateAlternateColorCodes('&', x))
                .collect(Collectors.toList());
        meta.setLore(combinedLore);
        item.setItemMeta(meta);
        return item;
    }
}
