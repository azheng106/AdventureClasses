package me.aquaponics.adventureclasses.commands;

import me.aquaponics.adventureclasses.AdventureClass;
import me.aquaponics.adventureclasses.AdventureClasses;
import me.aquaponics.adventureclasses.managers.ClassManager;
import me.aquaponics.adventureclasses.managers.XpManager;
import me.aquaponics.adventureclasses.utils.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class ChooseClassExecutor implements CommandExecutor, Listener {
    private final AdventureClasses plugin;
    private final ClassManager classManager;
    private final XpManager xpManager;

    public ChooseClassExecutor(AdventureClasses plugin, ClassManager classManager, XpManager xpManager) {
        this.plugin = plugin;
        this.classManager = classManager;
        this.xpManager = xpManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player p) {
            Inventory gui = Bukkit.createInventory(null, 9, ChatColor.BLUE + "" + ChatColor.BOLD + "Choose Class");
            gui.setItem(0, createGuiItem(Material.IRON_SWORD,
                    ChatColor.RED + "" + ChatColor.BOLD + "WARRIOR",
                    "Excels at hand-to-hand combat"));
            gui.setItem(1, createGuiItem(Material.BOW,
                    ChatColor.GREEN + "" + ChatColor.BOLD + "ARCHER",
                    "Take ranged engagements"));
            gui.setItem(2, createGuiItem(Material.NETHER_STAR,
                    ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "ASSASSIN",
                    "Use stealth to destroy your enemies"));
            gui.setItem(3, createGuiItem(Material.GOLDEN_APPLE,
                    ChatColor.AQUA + "" + ChatColor.BOLD + "HEALER",
                    "Heal yourself and allies"));
            gui.setItem(4, createGuiItem(Material.REDSTONE,
                    ChatColor.GOLD + "" + ChatColor.BOLD + "ENGINEER",
                    "Build cool contraptions"));
            p.openInventory(gui);
        } else {
            sender.sendMessage("This command can only be used by players.");
        }
        return true;
    }

    private AdventureClass classClicked = null;

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player p = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();
        String invTitle = ChatColor.stripColor(e.getView().getTitle());
        // Listen for clicks in the Choose Class GUI
        if (invTitle.equals("Choose Class")) {
            e.setCancelled(true);
            if (clickedItem == null || clickedItem.getType().isAir()) return;

            classClicked = AdventureClass.valueOf(ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName().toUpperCase()));
            if (classManager.getPlayerClass(p) == null) { // Choosing class for first time
                classManager.chooseClass(p, classClicked);
                p.sendMessage(ChatColor.valueOf(classClicked.color) + "You chose " + classClicked + ". Use /tree to open Progession Tree (in development)");
                if (classClicked == AdventureClass.HEALER) {
                    if (!p.getInventory().contains(ItemUtil.healerWand())) {
                        p.getInventory().addItem(ItemUtil.healerWand());
                    }
                }
            } else { // Player already has a class
                if (classClicked == classManager.getPlayerClass(p)) {
                    p.sendMessage(ChatColor.valueOf(classClicked.color) + "You are already " + classClicked + "!");
                } else { // Notify player they will lose all XP when switching classes
                    Inventory confirmSwitchGui = Bukkit.createInventory(null, 27, ChatColor.RED + "" + ChatColor.BOLD + "XP WILL BE LOST UPON SWITCHING CLASSES");
                    confirmSwitchGui.setItem(11, createGuiItem(Material.GREEN_TERRACOTTA, ChatColor.GREEN + "Confirm Class Switch", "XP will be reset to 0 upon switching"));
                    confirmSwitchGui.setItem(15, createGuiItem(Material.RED_TERRACOTTA, ChatColor.RED + "Cancel Class Switch"));
                    p.openInventory(confirmSwitchGui);
                }
            }
        } else if (invTitle.equals("XP WILL BE LOST UPON SWITCHING CLASSES")) {
            e.setCancelled(true);
            if (clickedItem == null || clickedItem.getType().isAir()) return;
            if (clickedItem.getType() == Material.GREEN_TERRACOTTA) {
                classManager.chooseClass(p, classClicked);
                p.sendMessage(ChatColor.valueOf(classClicked.color) + "You switched to " + classClicked + ". XP has been reset.");
                p.closeInventory();
                plugin.getConfig().set("player." + p.getUniqueId() + ".xp", 0);
                plugin.saveConfig();
                xpManager.loadConfig();
                if (classClicked == AdventureClass.HEALER) {
                    if (!p.getInventory().contains(ItemUtil.healerWand())) {
                        p.getInventory().addItem(ItemUtil.healerWand());
                    }
                }
            } else if (clickedItem.getType() == Material.RED_TERRACOTTA) {
                p.sendMessage(ChatColor.RED + "Class switch cancelled");
                p.closeInventory();
            }
        }
    }

    private ItemStack createGuiItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}
