package de.ben.oUH;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class NoFly implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Überprüft, ob der Spieler im Nether ist
        if (player.getWorld().getEnvironment() == Environment.NETHER) {

            // Prüft, ob der Spieler fliegt und nicht im Kreativmodus ist
            if (player.isFlying() && player.getGameMode() == GameMode.SURVIVAL) {
                player.setFlying(false);
                player.setAllowFlight(false);
                player.sendMessage("> Nope");
            }
        }
    }
}
