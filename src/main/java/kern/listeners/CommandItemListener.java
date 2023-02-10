package kern.listeners;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class CommandItemListener implements Listener {

    @EventHandler
    public void onInventory(InventoryClickEvent e) {
        if (canDoWhatever((Player) e.getWhoClicked()))
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (canDoWhatever(e.getPlayer()))
            return;
        e.setCancelled(true);
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player))
            return;
        if (canDoWhatever((Player) e.getEntity()))
            return;
        e.setCancelled(true);
    }

    public static boolean inSpawn(Location l) {
        return Math.abs(l.getBlockX()) < 100 && Math.abs(l.getBlockZ()) <= 100;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInteract(PlayerInteractEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE || e.getAction() == Action.PHYSICAL)
            return;
        if (!e.getPlayer().getInventory().getItemInMainHand().containsEnchantment(Enchantment.DIG_SPEED))
            return;
        e.setCancelled(true);
        switch (e.getPlayer().getInventory().getItemInMainHand().getType()) {
        case NETHERITE_SWORD:
            Bukkit.dispatchCommand(e.getPlayer(), "join");
            break;
        case BOW:
            Bukkit.dispatchCommand(e.getPlayer(), "stats");
            break;
        case DIAMOND:
            if (!e.getPlayer().getOpenInventory().getTitle().toLowerCase().contains("yeuh"))
                Bukkit.dispatchCommand(e.getPlayer(), "help");
            break;
        default:
        }
    }

    private static boolean canDoWhatever(Player p) {
        return p.getGameMode() == GameMode.CREATIVE || p.hasPermission("yeuhLobby.admin")
                || Math.abs(p.getLocation().getBlockX()) > 100 || Math.abs(p.getLocation().getBlockZ()) > 100;
    }

    public static Map<Integer, ItemStack> getCommandItemInventory() {
        Map<Integer, ItemStack> commandItems = new HashMap<>();

        ItemStack item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta im = item.getItemMeta();
        im.setDisplayName("\u00a7dJoin a Battle Royale!");
        im.setLore(Arrays.asList("\u00a77What are you waiting for?"));
        im.addEnchant(Enchantment.DIG_SPEED, 1, true);
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(im);
        commandItems.put(4, item);

        item = new ItemStack(Material.BOW);
        im = item.getItemMeta();
        im.setDisplayName("\u00a76See Your Player Stats");
        im.setLore(Arrays.asList("\u00a77See how you rank up to other players!"));
        im.addEnchant(Enchantment.DIG_SPEED, 1, true);
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(im);
        commandItems.put(2, item);

        item = new ItemStack(Material.DIAMOND);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7bHelp");
        im.setLore(Arrays.asList("\u00a77It's okay, we get it's complicated"));
        im.addEnchant(Enchantment.DIG_SPEED, 1, true);
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        item.setItemMeta(im);
        commandItems.put(6, item);

        return commandItems;
    }
}
