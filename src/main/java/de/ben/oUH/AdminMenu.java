// Paket ggf. anpassen
package de.ben.oUH;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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

import java.util.*;

public class AdminMenu implements CommandExecutor, Listener {

    private final JavaPlugin plugin;
    private final Map<UUID, UUID> playerTargets = new HashMap<>();

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

        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = playerHead.getItemMeta();
        meta.setDisplayName("§aSpieler anzeigen");
        meta.setLore(List.of("§7Klicke, um alle Spieler zu sehen"));
        playerHead.setItemMeta(meta);
        menu.setItem(11, playerHead);

        ItemStack randomTp = new ItemStack(Material.ENDER_PEARL);
        ItemMeta tpMeta = randomTp.getItemMeta();
        tpMeta.setDisplayName("§bTeleport zu zufälligem Spieler");
        randomTp.setItemMeta(tpMeta);
        menu.setItem(13, randomTp);

        ItemStack reportsButton = new ItemStack(Material.BOOK);
        ItemMeta reportsMeta = reportsButton.getItemMeta();
        reportsMeta.setDisplayName("§cMehr Reports");
        reportsButton.setItemMeta(reportsMeta);
        menu.setItem(15, reportsButton);

        player.openInventory(menu);
    }

    private void openPlayerMenu(Player player, Player target) {
        playerTargets.put(player.getUniqueId(), target.getUniqueId());

        Inventory menu = Bukkit.createInventory(null, 9 * 2, PLAYER_MENU_TITLE);

        // Slot 0 – Kick
        menu.setItem(0, createItem(Material.RED_DYE, "§cSpieler kicken"));

        // Slot 1 – Ban
        menu.setItem(1, createItem(Material.IRON_SWORD, "§4Spieler bannen"));

        // Slot 2 – Tp to Player
        menu.setItem(2, createItem(Material.ENDER_PEARL, "§bTeleport zu Spieler"));

        // Slot 3 – Invsee
        menu.setItem(3, createItem(Material.CHEST, "§6Inventar ansehen (/invsee)"));

        // Slot 4 – Walk
        menu.setItem(4, createItem(Material.LEATHER_BOOTS, "§eIn Spieler laufen (Walk)"));

        // Slot 17 – Zurück
        menu.setItem(17, createItem(Material.BARRIER, "§cZurück"));

        player.openInventory(menu);
    }

    private ItemStack createItem(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || clicked.getItemMeta() == null) return;

        String title = event.getView().getTitle();
        String displayName = clicked.getItemMeta().getDisplayName();
        event.setCancelled(true);

        if (title.equals(MAIN_MENU_TITLE)) {
            if (clicked.getType() == Material.PLAYER_HEAD) {
                openPlayerList(player);
            } else if (clicked.getType() == Material.ENDER_PEARL) {
                List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                players.remove(player);
                if (!players.isEmpty()) {
                    Player random = players.get((int) (Math.random() * players.size()));
                    player.teleport(random.getLocation());
                    player.sendMessage("§aDu wurdest zu §e" + random.getName() + " §ateleportiert.");
                } else {
                    player.sendMessage("§cKeine anderen Spieler online.");
                }
            } else if (clicked.getType() == Material.BOOK) {
                player.performCommand("reports");
                player.closeInventory();
            }
        }

        else if (title.equals(PLAYER_LIST_TITLE)) {
            if (clicked.getType() == Material.BARRIER) {
                openMainMenu(player);
            } else if (clicked.getType() == Material.PLAYER_HEAD) {
                Player target = Bukkit.getPlayer(displayName.replace("§e", ""));
                if (target != null) openPlayerMenu(player, target);
            }
        }

        else if (title.equals(PLAYER_MENU_TITLE)) {
            UUID targetUUID = playerTargets.get(player.getUniqueId());
            if (targetUUID == null) return;
            Player target = Bukkit.getPlayer(targetUUID);
            if (target == null) {
                player.sendMessage("§cZielspieler nicht gefunden.");
                return;
            }

            switch (clicked.getType()) {
                case RED_DYE -> {
                    player.performCommand("kick " + target.getName());
                    player.sendMessage("§a" + target.getName() + " wurde gekickt.");
                    player.closeInventory();
                }
                case IRON_SWORD -> {
                    player.performCommand("ban " + target.getName());
                    player.sendMessage("§c" + target.getName() + " wurde gebannt.");
                    player.closeInventory();
                }
                case ENDER_PEARL -> {
                    player.teleport(target.getLocation());
                    player.sendMessage("§bTeleportiert zu §e" + target.getName());
                    player.closeInventory();
                }
                case CHEST -> {
                    player.performCommand("invsee " + target.getName());
                    player.closeInventory();
                }
                case LEATHER_BOOTS -> {
                    player.setGameMode(GameMode.SPECTATOR);
                    player.setSpectatorTarget(target);
                    player.sendMessage("§eDu beobachtest jetzt §6" + target.getName());
                    player.closeInventory();
                }
                case BARRIER -> openMainMenu(player);
            }
        }
    }

    private void openPlayerList(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, PLAYER_LIST_TITLE);

        int index = 0;
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (index >= 45) break;
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            skullMeta.setOwningPlayer(target);
            skullMeta.setDisplayName("§e" + target.getName());
            skull.setItemMeta(skullMeta);
            inv.setItem(index, skull);
            index++;
        }

        inv.setItem(45, createItem(Material.BARRIER, "§cZurück"));
        player.openInventory(inv);
    }
}
