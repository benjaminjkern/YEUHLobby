package kern.listeners;

import java.util.Arrays;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import kern.YEUHLobby;

public class GUIInventoryListener implements Listener {

    private static void addButton(Inventory inv, Material mat, int slot, String name) {
        ItemStack btn = new ItemStack(mat);
        ItemMeta btnMeta = btn.getItemMeta();
        btnMeta.setDisplayName(name);
        btn.setItemMeta(btnMeta);
        addButton(inv, btn, slot);
    }

    private static void addButton(Inventory inv, ItemStack item, int slot) { inv.setItem(slot, item); }

    public static final String HELP_TITLE = "\u00a7d\u00a7l\u00a7oYEUH \u00a7fHelp:";

    public static Inventory getHelpInventory() {
        Inventory helpInventory = Bukkit.createInventory(null, 27, HELP_TITLE);

        addButton(helpInventory, Material.BARRIER, 8, "\u00a7cExit Menu");

        addButton(helpInventory, Material.DIAMOND, 11, "\u00a7bServer Rules");
        addButton(helpInventory, Material.LAVA_BUCKET, 12, "\u00a76Game Queue");
        addButton(helpInventory, Material.BOW, 13, "\u00a7aPlayer Rating");

        ItemStack item;

        item = new ItemStack(Material.NETHERITE_SWORD);
        ItemMeta im = item.getItemMeta();
        im.setDisplayName("\u00a79Join our Discord!");
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(im);

        addButton(helpInventory, item, 14);
        addButton(helpInventory, Material.ENCHANTED_GOLDEN_APPLE, 15, "\u00a7dBecome a Patron!");

        return helpInventory;
    }

    public static final String RULES_TITLE = "\u00a7d\u00a7l\u00a7oYEUH \u00a7fRules:";

    public static Inventory getRulesInventory(Player player) {
        Inventory rulesInventory = Bukkit.createInventory(null, 9, RULES_TITLE);

        ItemStack item;

        item = new ItemStack(Material.DIAMOND_BLOCK);
        ItemMeta im = item.getItemMeta();
        im.setDisplayName("\u00a75(\u00a7d\u00a7l1\u00a75) \u00a7fDon't cheat!");
        im.setLore(Arrays.asList("\u00a77You will (probably) get", "\u00a77banned for doing so!"));
        item.setItemMeta(im);

        addButton(rulesInventory, item, 3);

        item = new ItemStack(Material.DIAMOND_BLOCK);
        im = item.getItemMeta();
        im.setDisplayName("\u00a75(\u00a7d\u00a7l2\u00a75) \u00a7fOther than that,");
        im.setLore(Arrays.asList("\u00a7fwe don't really care!"));
        item.setItemMeta(im);
        addButton(rulesInventory, item, 4);
        addButton(rulesInventory, Material.DIAMOND_BLOCK, 5, "\u00a75(\u00a7d\u00a7l3\u00a75) \u00a7fJust have fun!");

        if (!YEUHLobby.getWarden().hasPlayerSigned(player.getName())) {
            item = new ItemStack(Material.WRITABLE_BOOK);
            im = item.getItemMeta();
            im.setDisplayName("\u00a7fSign and \u00a7d/opt in");
            im.setLore(Arrays.asList("\u00a7fto the game queue!"));
            item.setItemMeta(im);
        } else {
            item = new ItemStack(Material.ENCHANTED_BOOK);
            im = item.getItemMeta();
            im.setDisplayName("\u00a7aYou have already signed!");
            im.setLore(Arrays.asList("\u00a77Click to close menu."));
            item.setItemMeta(im);
        }
        addButton(rulesInventory, item, 8);

        return rulesInventory;
    }

    public static final String RATING_TITLE = "Your Player Rating";

    public static Inventory getRatingInventory(Player player) { return Bukkit.createInventory(null, 45, RATING_TITLE); }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();

        ItemStack item;

        switch (e.getView().getTitle()) {

            case HELP_TITLE:
                if (e.getRawSlot() > 26) return;
                e.setCancelled(true);
                item = e.getCurrentItem();
                if (item == null) return;
                switch (item.getType()) {
                    case BARRIER:
                        e.getView().close();
                        break;
                    case DIAMOND:
                        e.getWhoClicked().openInventory(getRulesInventory(player));
                        break;
                    case LAVA_BUCKET:
                        e.getWhoClicked().openInventory(getRulesInventory(player));
                        break;
                    case BOW:
                        e.getView().close();
                        Bukkit.dispatchCommand(Bukkit.getPlayerExact(player.getName()), "rating");
                        break;
                    case NETHERITE_SWORD:
                        e.getView().close();
                        Bukkit.dispatchCommand(Bukkit.getPlayerExact(player.getName()), "discord");
                        break;
                    case ENCHANTED_GOLDEN_APPLE:
                        e.getWhoClicked().openInventory(getRulesInventory(player));
                        break;
                    default:
                }
                break;

            case RULES_TITLE:
                if (e.getRawSlot() > 8) return;
                e.setCancelled(true);
                item = e.getCurrentItem();
                if (item == null) return;
                switch (item.getType()) {
                    case WRITABLE_BOOK:
                        YEUHLobby.getWarden().sign(player.getName());
                        player.openInventory(getRulesInventory(player));
                        break;
                    case ENCHANTED_BOOK:
                        e.getView().close();
                        break;
                    default:
                }
            default:
                // continue

        }

    }
}
