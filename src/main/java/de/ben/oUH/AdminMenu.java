package de.ben.oUH;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class AdminMenu implements CommandExecutor, Listener {

    private final JavaPlugin plugin;

    private final String MAIN_MENU_TITLE = "§cAdmin-Menü";
    private final String PLAYER_LIST_TITLE = "§aOnline-Spieler";
    private final String PLAYER_MENU_TITLE = "§aSpieler-Details";

    public AdminMenu(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.getCommand("admin").setExecutor(this);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;
        openMainMenu(player);
        return true;
    }

    private void openMainMenu(Player player) {
        Inventory menu = Bukkit.createInventory(null, 27, MAIN_MENU_TITLE);

        // Slot 11 – Spielerliste öffnen
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = playerHead.getItemMeta();
        meta.setDisplayName("§aSpieler anzeigen");
        meta.setLore(List.of("§7Klicke, um alle Spieler zu sehen"));
        playerHead.setItemMeta(meta);
        menu.setItem(11, playerHead);

        // Slot 13 – Random Teleport
        ItemStack randomTp = new ItemStack(Material.ENDER_PEARL);
        ItemMeta tpMeta = randomTp.getItemMeta();
        tpMeta.setDisplayName("§bTeleport zu zufälligem Spieler");
        randomTp.setItemMeta(tpMeta);
        menu.setItem(13, randomTp);

        // Slot 15 – Mehr Reports Button
        ItemStack reportsButton = new ItemStack(Material.BOOK);
        ItemMeta reportsMeta = reportsButton.getItemMeta();
        reportsMeta.setDisplayName("§cMehr Reports");
        reportsButton.setItemMeta(reportsMeta);
        menu.setItem(15, reportsButton);

        player.openInventory(menu);
    }

    private void openPlayerMenu(Player player, Player target) {
        Inventory menu = Bukkit.createInventory(null, 9, PLAYER_MENU_TITLE);

        // Slot 8 – Back Button
        ItemStack backButton = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§cZurück");
        backButton.setItemMeta(backMeta);
        menu.setItem(8, backButton);

        player.openInventory(menu);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        // Nur blockieren, wenn es sich um unsere Menüs handelt
        if (title.equals(MAIN_MENU_TITLE) || title.equals(PLAYER_LIST_TITLE) || title.equals(PLAYER_MENU_TITLE)) {
            event.setCancelled(true);
        } else {
            return; // Bei anderen Inventaren nichts machen
        }

        String displayName = clicked.getItemMeta() != null ? clicked.getItemMeta().getDisplayName() : "";

        // MAIN MENU
        if (title.equals(MAIN_MENU_TITLE)) {
            if (clicked.getType() == Material.PLAYER_HEAD) {
                openPlayerList(player);
            } else if (clicked.getType() == Material.ENDER_PEARL) {
                List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                players.remove(player); // sich selbst ausnehmen
                if (!players.isEmpty()) {
                    Player random = players.get((int) (Math.random() * players.size()));
                    player.teleport(random.getLocation());
                    player.sendMessage("§aDu wurdest zu §e" + random.getName() + " §ateleportiert.");
                } else {
                    player.sendMessage("§cKeine anderen Spieler online.");
                }
            } else if (clicked.getType() == Material.BOOK) {
                // Mehr Reports Befehl ausführen
                player.performCommand("reports");
                player.closeInventory(); // Menü schließen
            }
        }

        // PLAYER LIST MENU
        else if (title.equals(PLAYER_LIST_TITLE)) {
            if (clicked.getType() == Material.BARRIER) {
                openMainMenu(player);
            } else if (clicked.getType() == Material.PLAYER_HEAD) {
                Player target = Bukkit.getPlayer(displayName.replace("§e", ""));
                if (target != null) {
                    openPlayerMenu(player, target);
                } else {
                    player.sendMessage("§cSpieler nicht gefunden oder offline.");
                }
            }
        }

        // PLAYER MENU
        else if (title.equals(PLAYER_MENU_TITLE)) {
            if (clicked.getType() == Material.BARRIER) {
                openMainMenu(player);
            }
        }
    }

    private void openPlayerList(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, PLAYER_LIST_TITLE);

        // Obere 5 Reihen = Platz für Spielerköpfe (0–44)
        int index = 0;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (index >= 45) break; // max 45 Köpfe
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            skullMeta.setOwningPlayer(target);
            skullMeta.setDisplayName("§e" + target.getName());
            skull.setItemMeta(skullMeta);
            inv.setItem(index, skull);
            index++;
        }

        // Slot 45: Zurück (Barriere)
        ItemStack back = new ItemStack(Material.BARRIER);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§cZurück");
        back.setItemMeta(backMeta);
        inv.setItem(45, back);

        player.openInventory(inv);
    }
}
