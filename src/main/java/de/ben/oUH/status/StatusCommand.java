package de.ben.oUH.status;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class StatusCommand implements CommandExecutor {

    private final JavaPlugin plugin;
    private final File statusFile;
    private final File playerStatusFile;
    private final Gson gson = new Gson();
    private final Map<String, String> statuses = new HashMap<>();
    private final Map<UUID, String> playerStatuses = new HashMap<>();

    public StatusCommand(JavaPlugin plugin) {
        this.plugin = plugin;

        File pluginFolder = new File(Bukkit.getPluginsFolder(), "Ouh");
        if (!pluginFolder.exists()) pluginFolder.mkdirs();

        this.statusFile = new File(pluginFolder, "status.json");
        this.playerStatusFile = new File(pluginFolder, "player_status.json");

        loadStatuses();
        loadPlayerStatuses();

        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new StatusPlaceholder(playerStatuses, statuses).register();
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                // Nur für mögliche spätere Erweiterung
            }
        }, 20L);
    }

    private void loadStatuses() {
        if (!statusFile.exists()) {
            List<Map<String, String>> example = Arrays.asList(
                    Map.of("name", "afk", "display", "§7[AFK]"),
                    Map.of("name", "builder", "display", "§b[Builder]")
            );
            try (Writer writer = new OutputStreamWriter(new FileOutputStream(statusFile), StandardCharsets.UTF_8)) {
                gson.toJson(example, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(statusFile), StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<Map<String, String>>>() {}.getType();
            List<Map<String, String>> statusList = gson.fromJson(reader, listType);
            for (Map<String, String> entry : statusList) {
                statuses.put(entry.get("name"), ChatColor.translateAlternateColorCodes('&', entry.get("display")));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPlayerStatuses() {
        if (!playerStatusFile.exists()) return;
        try (Reader reader = new InputStreamReader(new FileInputStream(playerStatusFile), StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> raw = gson.fromJson(reader, type);
            for (Map.Entry<String, String> entry : raw.entrySet()) {
                playerStatuses.put(UUID.fromString(entry.getKey()), entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void savePlayerStatuses() {
        Map<String, String> raw = new HashMap<>();
        for (Map.Entry<UUID, String> entry : playerStatuses.entrySet()) {
            raw.put(entry.getKey().toString(), entry.getValue());
        }
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(playerStatusFile), StandardCharsets.UTF_8)) {
            gson.toJson(raw, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Nur Spieler dürfen diesen Befehl verwenden.");
            return true;
        }

        UUID uuid = player.getUniqueId();

        if (args.length == 0) {
            playerStatuses.remove(uuid);
            savePlayerStatuses();
            player.sendMessage(ChatColor.GREEN + "Dein Status wurde entfernt.");
            return true;
        }

        String input = args[0].toLowerCase();
        if (!statuses.containsKey(input)) {
            player.sendMessage(ChatColor.RED + "Unbekannter Status. Verfügbare: " + String.join(", ", statuses.keySet()));
            return true;
        }

        playerStatuses.put(uuid, input);
        savePlayerStatuses();
        player.sendMessage(ChatColor.GREEN + "Status gesetzt auf: " + statuses.get(input));
        return true;
    }

    public Map<String, String> getStatuses() {
        return statuses;
    }
}
