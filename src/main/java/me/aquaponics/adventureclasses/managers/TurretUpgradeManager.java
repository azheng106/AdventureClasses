package me.aquaponics.adventureclasses.managers;

import me.aquaponics.adventureclasses.AdventureClasses;
import me.aquaponics.adventureclasses.utils.LocationUtil;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.List;

// Upgrade GUI for Engineer's turret
public class TurretUpgradeManager implements Listener {
    private final AdventureClasses plugin;

    public TurretUpgradeManager(AdventureClasses plugin) {
        this.plugin = plugin;
    }

    public void openUpgradeGui(Player p, Location turretLoc) {
        Inventory upgradeGui = Bukkit.createInventory(null, 45, ChatColor.BLUE + "Turret Upgrade");
        String path = getPath(p, turretLoc);
        int level = getUpgradeLevel(path);
        upgradeGui.setItem(11, createGuiItem(Material.EMERALD,
                ChatColor.GREEN + "Turret Upgrade",
                Arrays.asList(ChatColor.GRAY + "Level: " + level + ((level == 4) ? ChatColor.AQUA + "" + ChatColor.BOLD + " MAX" : ""),
                        ChatColor.GRAY + "Ammo Type: " + getProjectileTypeStr(path),
                        ChatColor.GRAY + "Range: 8 blocks",
                        (level != 4) ? ChatColor.GRAY + "Price: " + getPrice(level) : ""), turretLoc));
        upgradeGui.setItem(13, createGuiItem(Material.REDSTONE,
                ChatColor.RED + "Attack Players",
                Arrays.asList(ChatColor.GRAY + "Status: " + getStatusStr(path)), turretLoc));
        p.openInventory(upgradeGui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (ChatColor.stripColor(e.getView().getTitle()).equals("Turret Upgrade")) {
            e.setCancelled(true);
            Player p = (Player) e.getWhoClicked();
            ItemStack clickedItem = e.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().isAir()) return;
            if (clickedItem.getType() == Material.EMERALD) {
                upgradeTurret(p, clickedItem);
                openUpgradeGui(p, getTurretLocationFromItem(clickedItem)); // Refresh the item lores and stuff in GUI
            } else if (clickedItem.getType() == Material.REDSTONE) {
                toggleStatus(p, clickedItem);
                openUpgradeGui(p, getTurretLocationFromItem(clickedItem));
            }

        }
    }

    private void toggleStatus(Player p, ItemStack clickedItem) {
        Location turretLoc = getTurretLocationFromItem(clickedItem);
        String path = getPath(p, turretLoc);
        plugin.getConfig().set(path + ".active", !getStatusBool(path));
        plugin.saveConfig();
        if (getStatusBool(path)) {
            p.sendMessage(ChatColor.GREEN + "Turret is now enabled!");
        } else {
            p.sendMessage(ChatColor.RED + "Turret is now disabled!");
        }
    }

    private String getPath(Player p, Location turretLoc) {
        return "player." + p.getUniqueId() + ".turrets." + LocationUtil.serializeLocation(turretLoc);
    }

    private void upgradeTurret(Player p, ItemStack clickedItem) {
        Location turretLoc = getTurretLocationFromItem(clickedItem);
        if (turretLoc != null) {
            String path = getPath(p, turretLoc);
            int level = getUpgradeLevel(path);
            String price = getPrice(level);
            if (level == 4) {
                p.sendMessage(ChatColor.AQUA + "Max level reached!");
                return;
            }
            if (parsePrice(p, price, level)) {
                level++;
                plugin.getConfig().set(path + ".level", level);
                plugin.saveConfig();
            }
        }
    }

    private int getUpgradeLevel(String path) {
        return plugin.getConfig().getInt(path + ".level");
    }

    public Entity getProjectileType(Player p, Location turretLoc) {
        String path = getPath(p, turretLoc);
        int level = getUpgradeLevel(path);
        Location spawnLoc = turretLoc.clone().add(0.5, 1.5, 0.5);
        switch (level) {
            case 1:
                Arrow arrow = (Arrow) turretLoc.getWorld().spawnEntity(spawnLoc, EntityType.ARROW);
                return arrow;
            case 2:
                Arrow arrow1 = (Arrow) turretLoc.getWorld().spawnEntity(spawnLoc, EntityType.ARROW);
                arrow1.setBasePotionType(PotionType.SLOWNESS);
                arrow1.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                return arrow1;
            case 3:
                Arrow arrow2 = (Arrow) turretLoc.getWorld().spawnEntity(spawnLoc, EntityType.ARROW);
                arrow2.setBasePotionType(PotionType.INSTANT_DAMAGE);
                arrow2.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                return arrow2;
            case 4:
                Arrow arrow3 = (Arrow) turretLoc.getWorld().spawnEntity(spawnLoc, EntityType.ARROW);
                arrow3.setBasePotionType(PotionType.STRONG_HARMING);
                arrow3.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
                return arrow3;
            default:
                return null;
        }
    }

    private String getProjectileTypeStr(String path) {
        return switch (getUpgradeLevel(path)) {
            case 1 -> "Arrow";
            case 2 -> "Slowness I Arrow";
            case 3 -> "Damage I Arrow";
            case 4 -> "Damage II Arrow";
            default -> null;
        };
    }

    /**
     * Get price of upgrade as String, to be displayed in GUI
     * @param level level that you are upgrading FROM
     */
    private String getPrice(int level) {
        return (int) Math.pow(4, level + 1) + " Diamonds";
    }

    /**
     * Check if player has a certain number of a material in their inventory
     */
    private boolean hasRequiredItems(Player p, Material material, int amount) {
        int count = 0;
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count+= item.getAmount()    ;
                if (count >= amount) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Remove a certain amount of an item from player's inventory
     */
    private void removeRequiredItems(Player p, Material material, int amount) {
        int remaining = amount;
        for (ItemStack item : p.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                if (item.getAmount() >= remaining) {
                    item.setAmount(item.getAmount() - remaining);
                    return;
                } else {
                    remaining -= item.getAmount();
                    item.setAmount(0);
                }
            }
        }
    }

    /**
     * Check if a player can afford the price
     * If yes, remove the required items from the player's inventory
     * Sends feedback message to player too
     */
    private boolean parsePrice(Player p, String priceStr, int fromLevel) {
        String[] parts = priceStr.split(" ");
        int requiredAmount = Integer.parseInt(parts[0]);
        Material requiredMaterial = parseMaterial(parts[1]);
        if (requiredMaterial == null) {
            p.sendMessage("requiredMaterial in parsePrice is null, report this");
            return false;
        }

        if (hasRequiredItems(p, requiredMaterial, requiredAmount)) {
            removeRequiredItems(p, requiredMaterial, requiredAmount);
            p.sendMessage(ChatColor.GREEN + "Upgraded turret level " + fromLevel + " -> " + (fromLevel+1) + "!");
            return true;
        } else {
            p.sendMessage(ChatColor.RED + "You need " + requiredAmount + " " + requiredMaterial + "!");
            return false;
        }
    }

    private Material parseMaterial(String str) {
        str = str.toUpperCase();
        try {
            return Material.valueOf(str);
        } catch (Exception e1) {
            str = str.substring(0, str.length() - 1); // Strip the "s" from the end
            try {
                return Material.valueOf(str);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private String getStatusStr(String path) {
        return plugin.getConfig().getBoolean(path + ".active") ? "ENABLED" : "DISABLED";
    }

    public boolean getStatusBool(String path) {
        return getStatusStr(path).equals("ENABLED");
    }

    public boolean getStatusBool(Player p, Location turretLoc) {
        String path = getPath(p, turretLoc);
        return getStatusBool(path);
    }

    private ItemStack createGuiItem(Material material, String name, List<String> lore, Location location) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);

        PersistentDataContainer data = meta.getPersistentDataContainer();
        data.set(new NamespacedKey(plugin, "turretloc"),
                PersistentDataType.STRING,
                LocationUtil.serializeLocation(location));

        item.setItemMeta(meta);
        return item;
    }


    /**
     * Gets the location that is attached to an item's PersistentDataContainer, or null if none attached
     */
    private Location getTurretLocationFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String locationString = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "turretloc"), PersistentDataType.STRING);
            if (locationString != null) return LocationUtil.deserializeLocation(locationString);
        }
        return null;
    }
}
