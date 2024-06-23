package me.aquaponics.adventureclasses.listeners;

import me.aquaponics.adventureclasses.managers.ClassManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    private ClassManager classManager;

    public PlayerJoinListener(ClassManager classManager) {
        this.classManager = classManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        if (classManager.getPlayerClass(p) == null) {
            TextComponent message = new TextComponent("Click here to choose your class!");
            message.setBold(true);
            message.setColor(ChatColor.GREEN);
            message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/chooseclass"));
            message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Choose your class").create()));
            p.spigot().sendMessage(message);
        }
    }
}
