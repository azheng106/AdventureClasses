package me.aquaponics.adventureclasses.classes;

import me.aquaponics.adventureclasses.AdventureClass;
import me.aquaponics.adventureclasses.AdventureClasses;
import me.aquaponics.adventureclasses.managers.ClassManager;
import me.aquaponics.adventureclasses.managers.XpManager;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class Archer implements Listener {
    private final AdventureClasses plugin;
    private final ClassManager classManager;
    private final XpManager xpManager;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final HashMap<UUID, Boolean> burstActive = new HashMap<>();

    public Archer(AdventureClasses plugin, ClassManager classManager, XpManager xpManager) {
        this.plugin = plugin;
        this.classManager = classManager;
        this.xpManager = xpManager;
    }

    @EventHandler
    public void handleBurstShotActivation(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        if (classManager.getPlayerClass(p) != AdventureClass.ARCHER) return;
        if (e.getAction() != Action.LEFT_CLICK_AIR) return;

        if (e.getItem() != null && e.getItem().getType() == Material.BOW) {
            long time = System.currentTimeMillis();
            int COOLDOWN_MS = 20 * 1000;
            if (cooldowns.containsKey(uuid) && time - cooldowns.get(uuid) < COOLDOWN_MS) {
                p.sendMessage(ChatColor.RED + "Burst shot is on cooldown (" +
                        String.format("%.2f", (COOLDOWN_MS + cooldowns.get(uuid) - System.currentTimeMillis()) / 1000D) + "s)");
            } else {
                p.sendMessage(ChatColor.GREEN + "Activated burst shot");
                p.getWorld().spawnParticle(Particle.CRIT, p.getLocation(), 50);
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1f, 1f);
                cooldowns.put(p.getUniqueId(), System.currentTimeMillis());
                burstActive.put(uuid, true);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        burstActive.put(uuid, false);
                    }
                }.runTaskLater(plugin, 70L);
            }
        }
    }
    @EventHandler
    public void handleBurstShot(EntityShootBowEvent e) {
        if (e.getEntity() instanceof Player p) {
            if (!burstActive.get(p.getUniqueId())) return;
            if (classManager.getPlayerClass(p) != AdventureClass.ARCHER) return;
            e.setCancelled(true);
            Location pEyeLoc = p.getEyeLocation();
            World world = p.getWorld();
            Arrow arrow = (Arrow) world.spawnEntity(pEyeLoc, EntityType.ARROW);
            arrow.setVelocity(pEyeLoc.getDirection().normalize().multiply(3));

            if (!(e.getBow().containsEnchantment(Enchantment.ARROW_INFINITE)
                  || p.getGameMode() == GameMode.CREATIVE)) { // Simulate normal arrow usage
                p.getInventory().removeItem(new ItemStack(Material.ARROW, 1));
            }
        }
    }
}
