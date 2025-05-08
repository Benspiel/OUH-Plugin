package de.ben.oUH;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class Pvp implements Listener, CommandExecutor {

    private final Set<UUID> pvpDisabled = new HashSet<>();
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final JavaPlugin plugin;

    private final File dataFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public Pvp(OUH ouh) {
        this.plugin = JavaPlugin.getProvidingPlugin(getClass());

        File pluginFolder = new File(Bukkit.getPluginsFolder(), "Ouh");
        if (!pluginFolder.exists()) pluginFolder.mkdir();

        this.dataFile = new File(pluginFolder, "pvp.json");

        loadData();
    }

    private void loadData() {
        if (!dataFile.exists()) return;
        try (Reader reader = new FileReader(dataFile)) {
            Type type = new TypeToken<Set<UUID>>() {}.getType();
            Set<UUID> loaded = gson.fromJson(reader, type);
            if (loaded != null) pvpDisabled.addAll(loaded);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveData() {
        try (Writer writer = new FileWriter(dataFile)) {
            gson.toJson(pvpDisabled, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // PvP-Status wird automatisch aus Datei geladen
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        saveData();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (args.length != 1 || (!args[0].equalsIgnoreCase("on") && !args[0].equalsIgnoreCase("off"))) {
            player.sendMessage("§cBenutzung: /pvp [on|off]");
            return true;
        }

        UUID uuid = player.getUniqueId();

        if (args[0].equalsIgnoreCase("on")) {
            if (!pvpDisabled.contains(uuid)) {
                player.sendMessage("§aPvP ist bereits aktiviert.");
                return true;
            }
            pvpDisabled.remove(uuid);
            saveData();
            player.sendMessage("§aPvP wurde aktiviert. Du kannst jetzt wieder andere Spieler angreifen und angegriffen werden.");
            return true;
        }

        // Cooldown für erneutes Deaktivieren
        if (cooldowns.containsKey(uuid)) {
            long remaining = cooldowns.get(uuid) - System.currentTimeMillis();
            if (remaining > 0) {
                player.sendMessage("§cDu musst noch " + (remaining / 1000) + " Sekunden warten, bevor du PvP wieder deaktivieren kannst.");
                return true;
            }
        }

        player.sendMessage("§ePvP wird in 170 Sekunden deaktiviert. Du kannst bis dahin noch kämpfen!");

        new BukkitRunnable() {
            int seconds = 170;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                if (seconds <= 0) {
                    pvpDisabled.add(uuid);
                    saveData();
                    cooldowns.put(uuid, System.currentTimeMillis() + 300 * 1000);
                    player.sendMessage("§cPvP wurde deaktiviert. Du kannst keine Spieler mehr angreifen und wirst auch nicht angegriffen.");
                    cancel();
                    return;
                }

                player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        new TextComponent("§ePvP wird in §6" + seconds + "s §edeaktiviert..."));
                seconds--;
            }
        }.runTaskTimer(plugin, 0L, 20L);

        return true;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity entity = event.getEntity();

        if (!(damager instanceof Player attacker) || !(entity instanceof Player target)) return;

        if (pvpDisabled.contains(attacker.getUniqueId())) {
            event.setCancelled(true);
            if (!attacker.hasMetadata("pvp_message_sent")) {
                attacker.sendMessage("§cDu hast PvP deaktiviert!");
                attacker.setMetadata("pvp_message_sent", new FixedMetadataValue(plugin, true));
                Bukkit.getScheduler().runTaskLater(plugin, () ->
                        attacker.removeMetadata("pvp_message_sent", plugin), 40L);
            }
            return;
        }

        if (pvpDisabled.contains(target.getUniqueId())) {
            event.setCancelled(true);
            if (!attacker.hasMetadata("pvp_message_sent")) {
                attacker.sendMessage("§cDieser Spieler hat PvP deaktiviert!");
                attacker.setMetadata("pvp_message_sent", new FixedMetadataValue(plugin, true));
                Bukkit.getScheduler().runTaskLater(plugin, () ->
                        attacker.removeMetadata("pvp_message_sent", plugin), 40L);
            }
        }
    }
}
