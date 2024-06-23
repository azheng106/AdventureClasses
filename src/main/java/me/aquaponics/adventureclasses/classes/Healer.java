package me.aquaponics.adventureclasses.classes;

import me.aquaponics.adventureclasses.AdventureClass;
import me.aquaponics.adventureclasses.AdventureClasses;
import me.aquaponics.adventureclasses.managers.ClassManager;
import me.aquaponics.adventureclasses.managers.XpManager;
import me.aquaponics.adventureclasses.utils.ItemUtil;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.UUID;

public class Healer implements Listener {
    private final AdventureClasses plugin;
    private final ClassManager classManager;
    private final XpManager xpManager;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();

    public Healer(AdventureClasses plugin, ClassManager classManager, XpManager xpManager) {
        this.plugin = plugin;
        this.classManager = classManager;
        this.xpManager = xpManager;
    }

    @EventHandler
    public void handleHealingWaveActivation(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        ItemStack item = e.getItem();
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (item != null && item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "healerwand"))) {
            if (classManager.getPlayerClass(p) == AdventureClass.HEALER) {
                UUID uuid = p.getUniqueId();
                int HEAL_COOLDOWN_MS = 15 * 1000;
                if (cooldowns.containsKey(uuid) && System.currentTimeMillis() - cooldowns.get(uuid) < HEAL_COOLDOWN_MS) {
                    p.sendMessage(ChatColor.RED + "Healing wave is on cooldown (" +
                            String.format("%.2f", (HEAL_COOLDOWN_MS - (System.currentTimeMillis() - cooldowns.get(uuid))) / 1000D) + "s)");
                } else {
                    cooldowns.put(uuid, System.currentTimeMillis());
                    Location pLoc = p.getLocation();
                    int HEAL_RADIUS = 8;
                    for (Player pl : Bukkit.getOnlinePlayers()) {
                        if (pl.getLocation().distance(pLoc) <= HEAL_RADIUS) {
                            pl.setHealth(Math.min(pl.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue(), pl.getHealth() + 10));
                            pl.getWorld().spawnParticle(Particle.HEART, pl.getLocation(), 10);
                            pl.sendMessage(ChatColor.GREEN + "" + ChatColor.BOLD + "You were healed by " + p.getName() + "!");
                        }
                    }
                }
            } else {
                p.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "Only healers can use this wand!");
            }
        }

    }

}
