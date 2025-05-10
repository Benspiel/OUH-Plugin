package de.ben.oUH;

import de.ben.oUH.status.StatusCommand;
import de.ben.oUH.status.StatusTabCompleter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class OUH extends JavaPlugin {

    @Override
    public void onEnable() {
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
        // AdminMenu registriert sich selbst im Konstruktor

        // Commands registrieren
        getCommand("spawn").setExecutor(spawn);
        getCommand("help").setExecutor(firstJoin);
        getCommand("pvp").setExecutor(pvp);
        getCommand("status").setExecutor(statusCommand);
        getCommand("status").setTabCompleter(new StatusTabCompleter(statusCommand.getStatuses()));
    }

    @Override
    public void onDisable() {
        // Plugin Shutdown Logic (wenn nötig)
    }
}
