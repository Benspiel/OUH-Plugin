package de.ben.oUH;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AdminItem implements Listener {

    private final ItemStack adminStar;
    private final ItemStack vanishBarrier;

    public AdminItem() {
        adminStar = new ItemStack(Material.NETHER_STAR);
        ItemMeta metaStar = adminStar.getItemMeta();
        metaStar.setDisplayName("§cAdmin-Menü");
        adminStar.setItemMeta(metaStar);

        vanishBarrier = new ItemStack(Material.BARRIER);
        ItemMeta metaBarrier = vanishBarrier.getItemMeta();
        metaBarrier.setDisplayName("§bVanish");
        vanishBarrier.setItemMeta(metaBarrier);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        if (!p.hasPermission("ouh.admin")) return;

        // Slot 7 = Barrier, Slot 8 = Nether Star
        if (!isSameItem(p.getInventory().getItem(8), adminStar)) {
            p.getInventory().setItem(8, adminStar);
        }
        if (!isSameItem(p.getInventory().getItem(7), vanishBarrier)) {
            p.getInventory().setItem(7, vanishBarrier);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        ItemStack dropped = event.getItemDrop().getItemStack();
        if (isSameItem(dropped, adminStar) || isSameItem(dropped, vanishBarrier)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        event.getDrops().removeIf(item -> isSameItem(item, adminStar) || isSameItem(item, vanishBarrier));
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (!event.hasItem()) return;

        Player p = event.getPlayer();
        ItemStack item = event.getItem();

        if (isSameItem(item, adminStar)) {
            p.performCommand("admin");
            event.setCancelled(true);
        } else if (isSameItem(item, vanishBarrier)) {
            p.performCommand("vanish");
            event.setCancelled(true);
        }
    }

    private boolean isSameItem(ItemStack a, ItemStack b) {
        if (a == null || b == null) return false;
        if (!a.hasItemMeta() || !b.hasItemMeta()) return false;
        return a.getType() == b.getType()
                && a.getItemMeta().getDisplayName().equals(b.getItemMeta().getDisplayName());
    }
}
