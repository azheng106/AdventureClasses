package me.aquaponics.adventureclasses.classes;

import me.aquaponics.adventureclasses.AdventureClass;
import me.aquaponics.adventureclasses.AdventureClasses;
import me.aquaponics.adventureclasses.managers.TurretUpgradeManager;
import me.aquaponics.adventureclasses.managers.ClassManager;
import me.aquaponics.adventureclasses.managers.XpManager;
import me.aquaponics.adventureclasses.utils.LocationUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class Engineer implements Listener {
    private final AdventureClasses plugin;
    private final ClassManager classManager;
    private final XpManager xpManager;
    private final HashMap<Location, UUID> turretLocs = new HashMap<>();
    private final TurretUpgradeManager turretUpgradeManager;

    public Engineer(AdventureClasses plugin, ClassManager classManager, XpManager xpManager) {
        this.plugin = plugin;
        this.classManager = classManager;
        this.xpManager = xpManager;
        turretUpgradeManager = new TurretUpgradeManager(plugin);
        startTurretShooting();
    }

    @EventHandler
    public void handleTurretCreation(BlockPlaceEvent e) {
        Player p = e.getPlayer();
        if (classManager.getPlayerClass(p) != AdventureClass.ENGINEER) return;
        Block block = e.getBlockPlaced();
        if (createsTurret(block) != null) { // Create a turret
            Block turretTopBlock = createsTurret(block);
            p.getWorld().spawnParticle(Particle.SLIME, block.getLocation(), 80);
            p.sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "Turret created!");
            assert turretTopBlock != null;
            turretLocs.put(turretTopBlock.getLocation(), p.getUniqueId());

            String path = "player." + p.getUniqueId() + ".turrets." + LocationUtil.serializeLocation(turretTopBlock.getLocation());
            plugin.getConfig().set(path + ".active", true);
            plugin.getConfig().set(path + ".level", 1);
            plugin.saveConfig();
            plugin.reloadConfig();
        }
    }

    @EventHandler
    public void handleTurretDestruction(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (classManager.getPlayerClass(p) != AdventureClass.ENGINEER) return;
        Block block = e.getBlock();
        for (int i = 0; i < 3; i++) {
            Location locToCheck = block.getLocation().add(0, i, 0);
            if (turretLocs.containsKey(locToCheck)) {
                if (turretLocs.get(locToCheck) == p.getUniqueId()) {
                    e.getPlayer().sendMessage(ChatColor.GRAY + "" + ChatColor.BOLD + "Turret destroyed!");

                    UUID playerUUID = turretLocs.get(locToCheck);
                    plugin.getConfig().set("player." + playerUUID + ".turrets." + LocationUtil.serializeLocation(locToCheck), null);
                    plugin.saveConfig();

                    turretLocs.remove(locToCheck);
                } else {
                    e.setCancelled(true);
                    p.sendMessage(ChatColor.RED + "You cannot break someone else's turret!");
                }
            }
        }
    }

    @EventHandler
    public void loadPlayerTurretsUponJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (classManager.getPlayerClass(p) != AdventureClass.ENGINEER) return;
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("player." + p.getUniqueId() + ".turrets");
        if (section != null) {
            Set<String> turretCoords = section.getKeys(false);
            for (String turretCoord : turretCoords) {
                Location location = LocationUtil.deserializeLocation(turretCoord);
                turretLocs.put(location, p.getUniqueId());
            }
        }
    }

    private void startTurretShooting() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Location, UUID> entry : turretLocs.entrySet()) {
                    Location turretLoc = entry.getKey();
                    World world = turretLoc.getWorld();
                    UUID uuid = entry.getValue();
                    Player turretOwner = Bukkit.getPlayer(uuid);
                    if (!Bukkit.getOnlinePlayers().contains(turretOwner)) return;
                    if (!turretUpgradeManager.getStatusBool(turretOwner, turretLoc)) return;

                    for (Player player : world.getPlayers()) {
                        if (player.getLocation().distance(turretLoc) <= 8) {
                            Entity projectile = turretUpgradeManager.getProjectileType(turretOwner, turretLoc);
                            Vector direction = player.getLocation().toVector().subtract(turretLoc.toVector()).normalize();
                            projectile.setVelocity(direction);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    @EventHandler
    public void handleOpeningTurretGui(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (classManager.getPlayerClass(p) != AdventureClass.ENGINEER) return;
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Location clickedBlockLoc = e.getClickedBlock().getLocation();
            // Allow a player to upgrade a turret when they right click it
            if (turretLocs.containsKey(clickedBlockLoc)) {
                if (p.getUniqueId() == turretLocs.get(clickedBlockLoc)) {
                    turretUpgradeManager.openUpgradeGui(p, clickedBlockLoc);
                } else {
                    p.sendMessage(ChatColor.RED + "This is not your turret!");
                }
            }
        }
    }

    /**
     * Check if a placed block creates a turret formation
     * @param block placed block
     * @return Returns the top block of the turret, or null if a turret was not created
     */
    private Block createsTurret(Block block) {
        if (block.getType() == Material.CARVED_PUMPKIN) {
            Block blockBelow = block.getRelative(BlockFace.DOWN);
            if (blockBelow.getType() == Material.GOLD_BLOCK) {
                if (blockBelow.getRelative(BlockFace.DOWN).getType() == Material.GOLD_BLOCK) {
                    return block;
                }
            }
        } else if (block.getType() == Material.GOLD_BLOCK) {
            Block blockAbove = block.getRelative(BlockFace.UP);
            if (blockAbove.getType() == Material.CARVED_PUMPKIN
                && block.getRelative(BlockFace.DOWN).getType() == Material.GOLD_BLOCK) {
                return blockAbove;
            }
            else if (blockAbove.getType() == Material.GOLD_BLOCK) {
                if (blockAbove.getRelative(BlockFace.UP).getType() == Material.CARVED_PUMPKIN) {
                    return blockAbove.getRelative(BlockFace.UP);
                }
            }
        }
        return null;
    }
}
