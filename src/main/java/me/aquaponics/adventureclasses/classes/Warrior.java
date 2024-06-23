package me.aquaponics.adventureclasses.classes;

import me.aquaponics.adventureclasses.AdventureClass;
import me.aquaponics.adventureclasses.AdventureClasses;
import me.aquaponics.adventureclasses.managers.ClassManager;
import me.aquaponics.adventureclasses.managers.XpManager;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public class Warrior implements Listener {
    private final AdventureClasses plugin;
    private final ClassManager classManager;
    private final XpManager xpManager;
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final HashSet<Material> weapons = new HashSet<>(Arrays.asList(
            Material.WOODEN_SWORD,
            Material.WOODEN_AXE,
            Material.STONE_SWORD,
            Material.STONE_AXE,
            Material.GOLDEN_SWORD,
            Material.GOLDEN_AXE,
            Material.IRON_SWORD,
            Material.IRON_AXE,
            Material.DIAMOND_SWORD,
            Material.DIAMOND_AXE,
            Material.NETHERITE_SWORD,
            Material.NETHERITE_AXE
    ));

    public Warrior(AdventureClasses plugin, ClassManager classManager, XpManager xpManager) {
        this.plugin = plugin;
        this.classManager = classManager;
        this.xpManager = xpManager;
    }

    @EventHandler
    public void handleXpGain(EntityDamageByEntityEvent e) {
        Entity damager = e.getDamager();
        if (damager instanceof Player p) {
            if (classManager.getPlayerClass(p) != AdventureClass.WARRIOR) return;
            double damage = e.getDamage();
            xpManager.addXp(p, damage);
        }
    }

    @EventHandler
    public void handleBerserkActivation(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        if (classManager.getPlayerClass(p) != AdventureClass.WARRIOR) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        if (e.getItem() != null && weapons.contains(e.getItem().getType())) {
            long time = System.currentTimeMillis();
            int COOLDOWN_MS = 23 * 1000;
            int DURATION_TICKS = 100;
            if (cooldowns.containsKey(uuid) && time - cooldowns.get(uuid) < COOLDOWN_MS) {
                p.sendMessage(ChatColor.RED + "Berserk is on cooldown (" +
                        String.format("%.2f", (COOLDOWN_MS  - (System.currentTimeMillis() - cooldowns.get(uuid))) / 1000D) + "s)");
            } else {
                p.sendMessage(ChatColor.GREEN + "Activated berserk");
                cooldowns.put(p.getUniqueId(), System.currentTimeMillis());
                AttributeInstance attackSpeed = p.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
                attackSpeed.setBaseValue(attackSpeed.getBaseValue() + 4);

                p.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, DURATION_TICKS, 0));
                p.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, DURATION_TICKS, 0));
                p.getWorld().spawnParticle(Particle.FLAME, p.getLocation(), 50);
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_BLAZE_HURT, 1f, 1f);
                new BukkitRunnable() {
                    int ticks = 0;
                    @Override
                    public void run() {
                        if (ticks >= DURATION_TICKS) {
                            attackSpeed.setBaseValue(attackSpeed.getBaseValue() - 4);
                            this.cancel();
                        }
                        ticks++;
                    }
                }.runTaskTimer(plugin, 0L, 1L);
            }
        }
    }

    @EventHandler
    public void handleAttackSpeedReset(PlayerJoinEvent e) {
        // Stops people from disconnecting mid-berserk and keeping the extra attack speed
        Player p  = e.getPlayer();
        AttributeInstance attackSpeed = p.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (attackSpeed != null) {
            attackSpeed.setBaseValue(4);
        }
    }
}
