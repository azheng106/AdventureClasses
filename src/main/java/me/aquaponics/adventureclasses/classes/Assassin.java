package me.aquaponics.adventureclasses.classes;

import me.aquaponics.adventureclasses.AdventureClass;
import me.aquaponics.adventureclasses.AdventureClasses;
import me.aquaponics.adventureclasses.managers.ClassManager;
import me.aquaponics.adventureclasses.managers.XpManager;
import net.minecraft.world.item.alchemy.Potion;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class Assassin implements Listener {
    private final AdventureClasses plugin;
    private final ClassManager classManager;
    private final XpManager xpManager;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private long lastSneakTime = 0;
    private int sneakCount = 0;
    private HashMap<UUID, Boolean> isInvisible = new HashMap<>();
    public Assassin(AdventureClasses plugin, ClassManager classManager, XpManager xpManager) {
        this.plugin = plugin;
        this.classManager = classManager;
        this.xpManager = xpManager;
    }

    @EventHandler
    public void handleInvisActivation(PlayerToggleSneakEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        int INVIS_COOLDOWN_MS = 30 * 1000;
        if (classManager.getPlayerClass(p) != AdventureClass.ASSASSIN) return;
        if (!e.isSneaking()) return;
        if (System.currentTimeMillis() - lastSneakTime < 1000) {
            sneakCount++;
        } else {
            sneakCount = 1;
        }
        lastSneakTime = System.currentTimeMillis();
        if (sneakCount > 3) {
            sneakCount = 0;
            if (cooldowns.containsKey(uuid) && System.currentTimeMillis() - cooldowns.get(uuid) < INVIS_COOLDOWN_MS) {
                 p.sendMessage(ChatColor.RED + "Cloak is on cooldown (" +
                        String.format("%.2f", (INVIS_COOLDOWN_MS - (System.currentTimeMillis() - cooldowns.get(uuid))) / 1000D) + "s)");
            } else {
                p.sendMessage(ChatColor.GREEN + "Shrouded Cloak activated");
                isInvisible.put(uuid, true);
                cooldowns.put(uuid, System.currentTimeMillis());
                for (Player player : Bukkit.getOnlinePlayers()) { // Make player disappear to every player on server
                    player.hidePlayer(plugin, p);
                }
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 100, 2, false, false));
                p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 100, 9, false, false));
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100, 9, false, false));
                p.getWorld().spawnParticle(Particle.SMOKE_LARGE, p.getLocation(), 100);
                p.getWorld().playSound(p.getLocation(), Sound.BLOCK_LAVA_EXTINGUISH, 1f, 1f);
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        isInvisible.put(uuid, false);
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            player.showPlayer(plugin, p);
                        }
                    }
                }.runTaskLater(plugin, 100L);
            }
        }
    }

    @EventHandler
    public void handleInvisCancellation(EntityDamageByEntityEvent e) {
        if (e.getDamager() instanceof Player p) {
            UUID uuid = p.getUniqueId();
            if (isInvisible.getOrDefault(uuid, false)) {
                p.sendMessage("Cloak removed because you attacked!");
                isInvisible.put(uuid, false);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.showPlayer(plugin, p);
                }
                p.removePotionEffect(PotionEffectType.SPEED);
                p.removePotionEffect(PotionEffectType.NIGHT_VISION);
                p.removePotionEffect(PotionEffectType.INVISIBILITY);
            }
        }
    }
}
