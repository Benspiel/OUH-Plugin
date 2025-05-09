package de.ben.oUH;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
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
        menu.setItem(11, createItem(Material.PLAYER_HEAD, "§aSpieler anzeigen", List.of("§7Klicke, um alle Spieler zu sehen")));
        menu.setItem(13, createItem(Material.ENDER_PEARL, "§bTeleport zu zufälligem Spieler", null));
        menu.setItem(15, createItem(Material.BOOK, "§cMehr Reports", null));
        player.openInventory(menu);
    }

    private void openPlayerMenu(Player player, Player target) {
        playerTargets.put(player.getUniqueId(), target.getUniqueId());
        Inventory menu = Bukkit.createInventory(null, 54, PLAYER_MENU_TITLE);

        menu.setItem(12, createToggleItem(Material.FEATHER, "§bFly (Rechtsklick)", player.getAllowFlight()));
        menu.setItem(13, createToggleItem(Material.REDSTONE_TORCH, "§eGod Mode (Rechtsklick)", player.isInvulnerable()));
        menu.setItem(14, createItem(Material.DIAMOND_CHESTPLATE, "§6Inventar ansehen", null));
        menu.setItem(21, createItem(Material.PLAYER_HEAD, "§dKopf erhalten", null));
        menu.setItem(22, createItem(Material.BOOK, "§cMehr Reports", null));
        menu.setItem(23, createItem(Material.RED_DYE, "§cSpieler kicken", null));
        menu.setItem(30, createItem(Material.NETHERITE_AXE, "§4Spieler bannen", null));
        menu.setItem(31, createItem(Material.NETHERITE_SWORD, "§cSpieler töten", null));
        menu.setItem(32, createItem(Material.GOLDEN_APPLE, "§aSpieler heilen", null));
        menu.setItem(39, createItem(Material.POTION, "§eSpieler füttern", null));
        menu.setItem(41, createItem(Material.ENDER_PEARL, "§bTeleport zu Spieler", null));
        menu.setItem(49, createItem(Material.BARRIER, "§cZurück", null));

        player.openInventory(menu);
    }

    private ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        if (lore != null) meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createToggleItem(Material material, String name, boolean enabled) {
        return createItem(material, name, List.of("§7Status: " + (enabled ? "§aAktiviert" : "§cDeaktiviert")));
    }

    private boolean hasPermission(Player player, String permission) {
        return player.hasPermission("ouh.menu.bypass") || player.hasPermission(permission);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR || clicked.getItemMeta() == null) return;

        String title = event.getView().getTitle();
        if (title.equals(MAIN_MENU_TITLE) || title.equals(PLAYER_LIST_TITLE) || title.equals(PLAYER_MENU_TITLE)) {
            event.setCancelled(true);
        }

        if (title.equals(MAIN_MENU_TITLE)) {
            if (clicked.getType() == Material.PLAYER_HEAD) {
                openPlayerList(player);
            } else if (clicked.getType() == Material.ENDER_PEARL) {
                List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
                players.remove(player);
                if (!players.isEmpty()) {
                    Player random = players.get((int) (Math.random() * players.size()));
                    player.teleport(random.getLocation());
                    player.sendMessage("§aTeleportiert zu §e" + random.getName());
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
                String targetName = clicked.getItemMeta().getDisplayName().replace("§e", "");
                Player target = Bukkit.getPlayer(targetName);
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

            Material type = clicked.getType();
            ClickType click = event.getClick();

            switch (type) {
                case FEATHER -> {
                    if (!hasPermission(player, "ouh.menu.fly")) return;
                    if (click.isRightClick()) {
                        boolean newState = !player.getAllowFlight();
                        player.setAllowFlight(newState);
                        player.setFlying(newState);
                        openPlayerMenu(player, target);
                    }
                }
                case REDSTONE_TORCH -> {
                    if (!hasPermission(player, "ouh.menu.god")) return;
                    if (click.isRightClick()) {
                        player.setInvulnerable(!player.isInvulnerable());
                        openPlayerMenu(player, target);
                    }
                }
                case DIAMOND_CHESTPLATE -> {
                    if (!hasPermission(player, "ouh.menu.invsee")) return;
                    player.performCommand("invsee " + target.getName());
                    player.closeInventory();
                }
                case PLAYER_HEAD -> {
                    if (!hasPermission(player, "ouh.menu.skull")) return;
                    ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                    SkullMeta meta = (SkullMeta) skull.getItemMeta();
                    meta.setOwningPlayer(target);
                    meta.setDisplayName("§d" + target.getName() + "s Kopf");
                    skull.setItemMeta(meta);
                    player.getInventory().addItem(skull);
                    player.sendMessage("§dKopf von §e" + target.getName() + " §derhalten.");
                    player.closeInventory();
                }
                case BOOK -> {
                    player.performCommand("reports");
                    player.closeInventory();
                }
                case RED_DYE -> {
                    if (!hasPermission(player, "ouh.menu.kick")) return;
                    player.performCommand("kick " + target.getName());
                    player.sendMessage("§c" + target.getName() + " wurde gekickt.");
                    player.closeInventory();
                }
                case NETHERITE_AXE -> {
                    if (!hasPermission(player, "ouh.menu.ban")) return;
                    player.performCommand("ban " + target.getName());
                    player.sendMessage("§4" + target.getName() + " wurde gebannt.");
                    player.closeInventory();
                }
                case NETHERITE_SWORD -> {
                    if (!hasPermission(player, "ouh.menu.kill")) return;
                    target.setHealth(0);
                    player.sendMessage("§c" + target.getName() + " wurde getötet.");
                    player.closeInventory();
                }
                case GOLDEN_APPLE -> {
                    if (!hasPermission(player, "ouh.menu.heal")) return;
                    target.setHealth(target.getMaxHealth());
                    player.sendMessage("§a" + target.getName() + " wurde geheilt.");
                    target.sendMessage("§aDu wurdest geheilt.");
                    player.closeInventory();
                }
                case POTION -> {
                    if (!hasPermission(player, "ouh.menu.feed")) return;
                    target.setFoodLevel(20);
                    target.setSaturation(20f);
                    player.sendMessage("§e" + target.getName() + " wurde gefüttert.");
                    target.sendMessage("§eDu wurdest gefüttert.");
                    player.closeInventory();
                }
                case ENDER_PEARL -> {
                    if (!hasPermission(player, "ouh.menu.tp")) return;
                    player.teleport(target.getLocation());
                    player.sendMessage("§bTeleportiert zu §e" + target.getName());
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
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(target);
            meta.setDisplayName("§e" + target.getName());
            skull.setItemMeta(meta);
            inv.setItem(index++, skull);
        }
        inv.setItem(45, createItem(Material.BARRIER, "§cZurück", null));
        player.openInventory(inv);
    }
}
