package de.ben.oUH;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Spawn implements Listener, CommandExecutor, TabCompleter {

    private final OUH plugin;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Set<UUID> teleporting = new HashSet<>();

    public Spawn(OUH plugin) {
        this.plugin = plugin;
        plugin.getCommand("spawn").setExecutor(this);
        plugin.getCommand("spawn").setTabCompleter(this);
    }

    private final long TELEPORT_COOLDOWN = 5 * 60 * 1000; // 5 Minuten in Millisekunden

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Dieser Befehl ist nur für Spieler verfügbar.");
            return true;
        }

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        if (cooldowns.containsKey(uuid)) {
            long remaining = (cooldowns.get(uuid) - now);
            if (remaining > 0) {
                long seconds = remaining / 1000;
                player.sendMessage(ChatColor.RED + "Du musst noch " + seconds + " Sekunden warten, bevor du dich erneut teleportieren kannst.");
                return true;
            }
        }

        teleporting.add(uuid);
        player.sendMessage(ChatColor.YELLOW + "Nicht bewegen! Du wirst in 5 Sekunden teleportiert...");

        new BukkitRunnable() {
            int countdown = 5;

            @Override
            public void run() {
                if (!teleporting.contains(uuid)) {
                    cancel();
                    return;
                }

                if (countdown <= 0) {
                    teleporting.remove(uuid);
                    Location location = player.getWorld().getSpawnLocation().clone();
                    location.setY(location.getY() + 1); // 1 Block höher teleportieren
                    player.teleport(location);
                    player.sendMessage(ChatColor.GREEN + "Du wurdest zum Spawn teleportiert!");
                    cooldowns.put(uuid, System.currentTimeMillis() + TELEPORT_COOLDOWN);
                    cancel();
                    return;
                }

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent("Teleport in " + countdown + " Sekunden..."));
                countdown--;
            }
        }.runTaskTimer(plugin, 0L, 20L); // Jede Sekunde

        return true;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!teleporting.contains(uuid)) return;

        if (event.getFrom().getX() != event.getTo().getX()
                || event.getFrom().getY() != event.getTo().getY()
                || event.getFrom().getZ() != event.getTo().getZ()) {

            teleporting.remove(uuid);
            player.sendMessage(ChatColor.RED + "Du hast dich bewegt! Teleport abgebrochen.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return Collections.emptyList(); // Keine Tab-Vervollständigung notwendig
    }
}
