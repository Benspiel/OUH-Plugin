package de.ben.oUH;

import de.ben.oUH.status.StatusCommand;
import de.ben.oUH.status.StatusTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import de.ben.oUH.NoFly;

public final class OUH extends JavaPlugin {

    @Override
    public void onEnable() {
        if (isBlocked()) {
            getLogger().severe("§cStart verweigert.");
            return;
        }

        // Listener-Instanzen erstellen
        Death death = new Death();
        Spawn spawn = new Spawn(this);
        FirstJoin firstJoin = new FirstJoin(this);
        AdminItem adminItem = new AdminItem();
        Pvp pvp = new Pvp(this);
        AdminMenu adminMenu = new AdminMenu(this);
        StatusCommand statusCommand = new StatusCommand(this);

        // Listener registrieren
        Bukkit.getPluginManager().registerEvents(death, this);
        Bukkit.getPluginManager().registerEvents(spawn, this);
        Bukkit.getPluginManager().registerEvents(firstJoin, this);
        Bukkit.getPluginManager().registerEvents(adminItem, this);
        Bukkit.getPluginManager().registerEvents(pvp, this);
        Bukkit.getServer().getPluginManager().registerEvents(new NoFly(), this);
        // Commands registrieren
        getCommand("spawn").setExecutor(spawn);
        getCommand("help").setExecutor(firstJoin);
        getCommand("pvp").setExecutor(pvp);
        getCommand("status").setExecutor(statusCommand);
        getCommand("status").setTabCompleter(new StatusTabCompleter(statusCommand.getStatuses()));
    }

    private boolean isBlocked() {
        try {
            URL url = new URL("https://benspiel.github.io/botstatus.json");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(3000);
            con.setReadTimeout(3000);

            if (con.getResponseCode() != 200) return true;

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            JSONObject obj = (JSONObject) new JSONParser().parse(in);
            in.close();

            Object block = obj.get("block");
            return block instanceof Boolean && (Boolean) block;
        } catch (Exception e) {
            return true;
        }
    }

    @Override
    public void onDisable() {
        // Optional: Logging o. Ä.
    }
}
