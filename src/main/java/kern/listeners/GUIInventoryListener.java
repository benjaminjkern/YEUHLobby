package kern.listeners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import kern.YEUHLobby;

public class GUIInventoryListener implements Listener {

    private static final String EXIT_NAME = "\u00a7cExit Menu";

    private static void addButton(Inventory inv, Material mat, int slot, String name, String... lore) {
        ItemStack btn = new ItemStack(mat);
        ItemMeta btnMeta = btn.getItemMeta();
        btnMeta.setDisplayName(name);
        if (lore.length > 0) btnMeta.setLore(Arrays.asList(lore));
        btn.setItemMeta(btnMeta);
        addButton(inv, btn, slot);
    }

    private static void addButton(Inventory inv, Material mat, int slot, String name) {
        addButton(inv, mat, slot, name, new String[0]);
    }

    private static void addButton(Inventory inv, ItemStack item, int slot) { inv.setItem(slot, item); }

    public static final String HELP_TITLE = "\u00a7d\u00a7l\u00a7oYEUH \u00a7fHelp:";

    public static Inventory getHelpInventory() {
        Inventory helpInventory = Bukkit.createInventory(null, 27, HELP_TITLE);

        addButton(helpInventory, Material.BARRIER, 8, EXIT_NAME);

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
        ItemMeta im;

        addButton(rulesInventory, Material.DIAMOND_BLOCK, 3, "\u00a75(\u00a7d\u00a7l1\u00a75) \u00a7fDon't cheat!",
                "\u00a77You will (probably) get", "\u00a77banned for doing so!");
        addButton(rulesInventory, Material.DIAMOND_BLOCK, 4, "\u00a75(\u00a7d\u00a7l2\u00a75) \u00a7fOther than that,",
                "\u00a7fwe don't really care!");
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

    public static final String PATRON_TITLE = "\u00a7d\u00a7l\u00a7oYEUH \u00a7fPatron Ranks:";

    public static Inventory getPatronInventory() {

        Inventory patronInventory = Bukkit.createInventory(null, 54, PATRON_TITLE);

        addButton(patronInventory, Material.BARRIER, 8, EXIT_NAME);

        ItemStack item;
        ItemMeta im;

        final String WEBSTORE_NAME = "\u00a77Click to go to our web store!";

        // Member+
        item = new ItemStack(Material.APPLE);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7dMember+ \u00a7f(\u00a7a$2/mo\u00a7f)");
        im.setLore(Arrays.asList());
        item.setAmount(2);
        item.setItemMeta(im);
        addButton(patronInventory, item, 10);
        addButton(patronInventory, Material.WHITE_STAINED_GLASS, 18,
                "\u00a7fThe following perks come with \u00a7dMember+\u00a7f:");
        addButton(patronInventory, Material.WHITE_STAINED_GLASS, 19,
                "\u00a7fThe following perks come with \u00a7dMember+\u00a7f:");
        addButton(patronInventory, Material.WHITE_STAINED_GLASS, 20,
                "\u00a7fThe following perks come with \u00a7dMember+\u00a7f:");

        item = new ItemStack(Material.GOLDEN_SHOVEL);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7eClaim land in the creative world!");
        im.setLore(Arrays.asList("\u00a77Claim up to \u00a7f250 \u00a77blocks!", "\u00a77(Extends vertically)"));
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(im);
        addButton(patronInventory, item, 27);

        item = new ItemStack(Material.PLAYER_HEAD);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7dGet a Rating Boost!");
        im.setLore(
                Arrays.asList("\u00a77Get a new boost every month!", "\u00a77Equivalent to \u00a7f50 \u00a77votes!"));
        item.setItemMeta(im);
        addButton(patronInventory, item, 28);

        item = new ItemStack(Material.BEETROOT);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7cGet a Fancy Discord Rank!");
        item.setItemMeta(im);
        addButton(patronInventory, item, 29);
        // King

        item = new ItemStack(Material.GOLDEN_APPLE);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7dKing \u00a7f(\u00a7a$7/mo\u00a7f)");
        im.setLore(Arrays.asList(WEBSTORE_NAME));
        item.setAmount(7);
        item.setItemMeta(im);
        addButton(patronInventory, item, 13);
        addButton(patronInventory, Material.PINK_STAINED_GLASS, 21,
                "\u00a7fThe following perks come with \u00a7dKing\u00a7f:");
        addButton(patronInventory, Material.PINK_STAINED_GLASS, 22,
                "\u00a7fThe following perks come with \u00a7dKing\u00a7f:");
        addButton(patronInventory, Material.PINK_STAINED_GLASS, 23,
                "\u00a7fThe following perks come with \u00a7dKing\u00a7f:");

        item = new ItemStack(Material.DIAMOND_SHOVEL);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7bClaim more land in the creative world!");
        im.setLore(Arrays.asList("\u00a77Claim up to \u00a7f1,000 \u00a77blocks!", "\u00a77(Extends vertically)"));
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(im);
        addButton(patronInventory, item, 30);

        item = new ItemStack(Material.SKELETON_SKULL);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7fGet a better Rating Boost!");
        im.setLore(
                Arrays.asList("\u00a77Get a new boost every month!", "\u00a77Equivalent to \u00a7f150 \u00a77votes!"));
        item.setItemMeta(im);
        addButton(patronInventory, item, 31);

        item = new ItemStack(Material.COOKED_BEEF);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7cGet a Fancier Discord Rank!");
        item.setItemMeta(im);
        addButton(patronInventory, item, 32);

        item = new ItemStack(Material.DIAMOND_SWORD);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7bYour Scenario Votes count as Double!");
        im.setLore(Arrays.asList("\u00a77Or you can vote for up to", "\u00a77\u00a7f5 \u00a77Scenarios!"));
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(im);
        addButton(patronInventory, item, 39);

        item = new ItemStack(Material.BOW);
        im = item.getItemMeta();
        im.setDisplayName("\u00a76Early Access to New Game Types!");
        item.setItemMeta(im);
        addButton(patronInventory, item, 40);

        item = new ItemStack(Material.GUNPOWDER);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7fPatron-exclusive particles and sounds!");
        im.setLore(Arrays.asList("\u00a77Custom particles & sounds appear as you",
                "\u00a77Run around and when you kill players!"));
        item.setItemMeta(im);
        addButton(patronInventory, item, 41);

        // Beast King
        item = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7dBeast King \u00a7f(\u00a7a$12/mo\u00a7f)");
        im.setLore(Arrays.asList(WEBSTORE_NAME));
        item.setAmount(12);
        item.setItemMeta(im);
        addButton(patronInventory, item, 16);
        addButton(patronInventory, Material.RED_STAINED_GLASS, 24,
                "\u00a7fThe following perks come with \u00a7dBeast King\u00a7f:");
        addButton(patronInventory, Material.RED_STAINED_GLASS, 25,
                "\u00a7fThe following perks come with \u00a7dBeast King\u00a7f:");
        addButton(patronInventory, Material.RED_STAINED_GLASS, 26,
                "\u00a7fThe following perks come with \u00a7dBeast King\u00a7f:");

        item = new ItemStack(Material.NETHERITE_SHOVEL);
        im = item.getItemMeta();
        im.setDisplayName("\u00a76Claim even \u00a7omore \u00a76land in the creative world!");
        im.setLore(Arrays.asList("\u00a77Claim up to \u00a7f4,000 \u00a77blocks!", "\u00a77(Extends vertically)"));
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(im);
        addButton(patronInventory, item, 33);

        item = new ItemStack(Material.WITHER_SKELETON_SKULL);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7fGet an even \u00a7obetter \u00a7fRating Boost!");
        im.setLore(
                Arrays.asList("\u00a77Get a new boost every month!", "\u00a77Equivalent to \u00a7f250 \u00a77votes!"));
        item.setItemMeta(im);
        addButton(patronInventory, item, 34);

        item = new ItemStack(Material.CAKE);
        im = item.getItemMeta();
        im.setDisplayName("\u00a76Get an even \u00a7oFancier \u00a76Discord Rank!");
        item.setItemMeta(im);
        addButton(patronInventory, item, 35);

        item = new ItemStack(Material.NETHERITE_SWORD);
        im = item.getItemMeta();
        im.setDisplayName("\u00a76Your Scenario Votes count as \u00a7oTriple\u00a76!");
        im.setLore(Arrays.asList("\u00a77Or you can vote for up to", "\u00a77\u00a7f7 \u00a77Scenarios!"));
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(im);
        addButton(patronInventory, item, 42);

        item = new ItemStack(Material.TNT);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7cEarly Access to New Game Types!");
        item.setItemMeta(im);
        addButton(patronInventory, item, 43);

        item = new ItemStack(Material.BLAZE_POWDER);
        im = item.getItemMeta();
        im.setDisplayName("\u00a76\u00a7oFully Customized \u00a76particles and sounds!");
        im.setLore(Arrays.asList("\u00a77Custom particles & sounds appear as you",
                "\u00a77Run around and when you kill players!"));
        item.setItemMeta(im);
        addButton(patronInventory, item, 44);

        item = new ItemStack(Material.NETHERITE_HELMET);
        im = item.getItemMeta();
        im.setDisplayName("\u00a76Host your own private games!");
        im.setLore(Arrays.asList("\u00a77Choose the rules and scenarios,", "\u00a77And invite your friends!"));
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(im);
        addButton(patronInventory, item, 51);

        item = new ItemStack(Material.ENDER_CHEST);
        im = item.getItemMeta();
        im.setDisplayName("\u00a79Access your enderchest at any time!");
        im.setLore(Arrays.asList("\u00a77Using the \u00a79/enderchest \u00a77command"));
        item.setItemMeta(im);
        addButton(patronInventory, item, 52);

        item = new ItemStack(Material.PANDA_SPAWN_EGG);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7fHave a pet!");
        im.setLore(Arrays.asList("\u00a77Both in the lobby and in the game!", "\u00a77(Pets cannot fight for you)"));
        item.setItemMeta(im);
        addButton(patronInventory, item, 53);

        return patronInventory;
    }

    public static final String LIST_TITLE = "\u00a7d\u00a7l\u00a7oYEUH \u00a7fGame Queue:";

    public static Inventory getListInventory() {
        List<Player> playerList = new ArrayList<>(Bukkit.getOnlinePlayers());

        Inventory listInventory = Bukkit.createInventory(null, (int) (Math.ceil(playerList.size() / 7.)) * 9,
                LIST_TITLE);

        addButton(listInventory, Material.BARRIER, 8, EXIT_NAME);

        int i = 0;
        for (Player p : playerList) {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta im = (SkullMeta) item.getItemMeta();
            im.setOwningPlayer(Bukkit.getOfflinePlayer(p.getUniqueId()));
            im.setDisplayName("\u00a7d" + p.getDisplayName());
            if (!YEUHLobby.getWarden().hasPlayerSigned(p.getName())) {
                im.setLore(Arrays.asList("\u00a7cHas not signed the \u00a7l/rules\u00a7c!"));
            } else if (PlayerListener.inQueue(p)) {
                im.setLore(Arrays.asList("\u00a7fIn the game queue"));
            } else if (PlayerListener.hasQueueListener(p)) {
                im.setLore(Arrays
                        .asList("\u00a7fWaiting for \u00a7d" + PlayerListener.getQueueListener(p) + " \u00a7fplayers"));
            } else {
                im.setLore(Arrays.asList("\u00a77Opted out of the game queue"));
            }
            item.setItemMeta(im);

            while (i % 9 > 7) i++;

            addButton(listInventory, item, i);
            i++;
        }

        return listInventory;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();

        ItemStack item;

        switch (e.getView().getTitle()) {
            case HELP_TITLE:
                if (e.getRawSlot() >= e.getView().getTopInventory().getSize()) {
                    if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) e.setCancelled(true);
                    return;
                }
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
                        e.getView().close();
                        Bukkit.dispatchCommand(Bukkit.getPlayerExact(player.getName()), "list");
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
                        e.getWhoClicked().openInventory(getPatronInventory());
                        break;
                    default:
                }
                break;

            case RULES_TITLE:
                if (e.getRawSlot() >= e.getView().getTopInventory().getSize()) {
                    if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) e.setCancelled(true);
                    return;
                }
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
                break;

            case LIST_TITLE:
                if (e.getRawSlot() >= e.getView().getTopInventory().getSize()) {
                    if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) e.setCancelled(true);
                    return;
                }

                e.setCancelled(true);
                item = e.getCurrentItem();
                if (item == null) return;
                switch (item.getType()) {
                    case BARRIER:
                        e.getView().close();
                        break;
                    case PLAYER_HEAD:
                        break;
                    default:
                }
                break;

            case PATRON_TITLE:
                if (e.getRawSlot() >= e.getView().getTopInventory().getSize()) {
                    if (e.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) e.setCancelled(true);
                    return;
                }
                e.setCancelled(true);
                item = e.getCurrentItem();
                if (item == null) return;
                switch (item.getType()) {
                    case BARRIER:
                        e.getView().close();
                        break;
                    default:
                        e.getView().close();
                        Bukkit.dispatchCommand(Bukkit.getPlayerExact(player.getName()), "patreon");
                }
                break;
            default:
                // continue

        }

    }
}
