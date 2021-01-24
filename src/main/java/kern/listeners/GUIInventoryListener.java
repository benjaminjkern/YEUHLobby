package kern.listeners;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
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
import kern.Game;
import kern.PlayerStats;

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
        addButton(helpInventory, Material.LAVA_BUCKET, 12, "\u00a76Player List");
        addButton(helpInventory, Material.APPLE, 13, "\u00a7cBecome a Member!");

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

        addButton(rulesInventory, Material.BARRIER, 8, EXIT_NAME);

        addButton(rulesInventory, Material.DIAMOND_BLOCK, 3, "\u00a75(\u00a7d\u00a7l1\u00a75) \u00a7fDon't cheat!",
                "\u00a77You will (probably) get", "\u00a77banned for doing so!");
        addButton(rulesInventory, Material.DIAMOND_BLOCK, 4, "\u00a75(\u00a7d\u00a7l2\u00a75) \u00a7fOther than that,",
                "\u00a7fwe don't really care!");
        addButton(rulesInventory, Material.DIAMOND_BLOCK, 5, "\u00a75(\u00a7d\u00a7l3\u00a75) \u00a7fJust have fun!");

        return rulesInventory;
    }

    public static final String STATS_TITLE = "\u00a7d\u00a7l\u00a7oYEUH \u00a7fStats:";

    public static Inventory getStatsInventory(String player) {
        Inventory statsInventory = Bukkit.createInventory(null, 45, "\u00a75" + player + "\u00a7f's " + STATS_TITLE);
        addButton(statsInventory, Material.BARRIER, 8, EXIT_NAME);

        PlayerStats ps = YEUHLobby.getScoreKeeper().getStats(player);

        ItemStack item;
        ItemMeta im;

        item = new ItemStack(Material.DIAMOND_SWORD);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7d\u00a7lYEUH \u00a7fLevel:");
        im.setLore(Arrays.asList("\u00a7b\u00a7l" + String.format("%d", ps.getLevel())));
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(im);
        addButton(statsInventory, item, 2);

        item = new ItemStack(Material.NETHERITE_SWORD);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7fPlayer Rating:");
        im.setLore(Arrays.asList("\u00a7l" + ps.ratingString()));
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(im);
        addButton(statsInventory, item, 4);

        item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skim = (SkullMeta) item.getItemMeta();
        skim.setDisplayName("\u00a7fRank:");
        skim.setOwningPlayer(Bukkit.getOfflinePlayer(player));
        im.setLore(Arrays.asList(ps.rankingColor() + ps.getRankString()));
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(im);
        addButton(statsInventory, item, 6);

        item = new ItemStack(Material.APPLE);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7fGames Played:");
        im.setLore(Arrays.asList("\u00a7c\u00a7l" + ps.games));
        item.setItemMeta(im);
        addButton(statsInventory, item, 12);
        item = new ItemStack(Material.ENCHANTED_GOLDEN_APPLE);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7fGames Won:");
        im.setLore(Arrays.asList("\u00a7e\u00a7l" + ps.wins));
        item.setItemMeta(im);
        addButton(statsInventory, item, 14);

        item = new ItemStack(Material.DIAMOND_SWORD);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7fPlayers Killed:");
        im.setLore(Arrays.asList("\u00a7b\u00a7l" + ps.playerKills));
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(im);
        addButton(statsInventory, item, 19);
        item = new ItemStack(Material.IRON_SWORD);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7fBots Killed:");
        im.setLore(Arrays.asList("\u00a77\u00a7l" + ps.botKills));
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(im);
        addButton(statsInventory, item, 21);
        item = new ItemStack(Material.GOLDEN_SWORD);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7fMonsters Killed:");
        im.setLore(Arrays.asList("\u00a76\u00a7l" + ps.mobKills));
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(im);
        addButton(statsInventory, item, 23);
        item = new ItemStack(Material.WOODEN_SWORD);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7fAnimals Killed:");
        im.setLore(Arrays.asList("\u00a76\u00a7l" + ps.animalKills));
        im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(im);
        addButton(statsInventory, item, 25);

        item = new ItemStack(Material.BOW);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7fTimes Killed by a Player:");
        im.setLore(Arrays.asList("\u00a76\u00a7l" + ps.playerDeaths));
        item.setItemMeta(im);
        addButton(statsInventory, item, 30);
        item = new ItemStack(Material.RED_DYE);
        im = item.getItemMeta();
        im.setDisplayName("\u00a7fTimes Killed by Environment:");
        im.setLore(Arrays.asList("\u00a7c\u00a7l" + ps.envDeaths));
        item.setItemMeta(im);
        addButton(statsInventory, item, 32);

        item = new ItemStack(Material.PLAYER_HEAD);
        skim = (SkullMeta) item.getItemMeta();
        skim.setDisplayName("\u00a7fNemesis:");
        if (ps.nemesis != null && !ps.nemesis.equalsIgnoreCase("null")) {
            skim.setOwningPlayer(Bukkit.getOfflinePlayer(ps.nemesis));
            skim.setLore(Arrays.asList("\u00a7c\u00a7l" + ps.nemesis, "\u00a7fTimes Killed:",
                    "\u00a7c\u00a7l" + ps.nemesisKills));
        } else {
            skim.setLore(Arrays.asList("\u00a77\u00a7l" + player + " \u00a77doesn't have a nemesis!"));
        }
        item.setItemMeta(skim);
        addButton(statsInventory, item, 40);

        return statsInventory;

    }

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

    public static final String LIST_TITLE = "\u00a7d\u00a7l\u00a7oYEUH \u00a7fPlayer List:";

    public static Inventory getListInventory() {
        Map<OfflinePlayer, String> playerList = new HashMap<>();

        for (Player p : Bukkit.getOnlinePlayers()) { playerList.put(p, "\u00a7fIn the creative lobby"); }
        for (Game g : YEUHLobby.getPlugin().getGames()) {
            for (UUID uuid : g.getPlayers()) {
                boolean spectating = !g.getAlive().contains(Bukkit.getOfflinePlayer(uuid).getName());
                playerList.put(Bukkit.getOfflinePlayer(uuid),
                        "\u00a7fIn game: \u00a7d\u00a7l" + g.server + (spectating ? "\n\u00a77(Spectating)" : ""));
            }
        }

        Inventory listInventory = Bukkit.createInventory(null, (int) (Math.ceil(playerList.size() / 7.)) * 9,
                LIST_TITLE);

        addButton(listInventory, Material.BARRIER, 8, EXIT_NAME);

        int i = 0;
        for (Entry<OfflinePlayer, String> p : playerList.entrySet()) {
            ItemStack item = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta im = (SkullMeta) item.getItemMeta();
            im.setOwningPlayer(p.getKey());
            if (p.getKey() instanceof Player) {
                im.setDisplayName("\u00a7d" + ((Player) p.getKey()).getDisplayName());
            } else {
                im.setDisplayName("\u00a7d" + p.getKey().getName());
            }
            im.setLore(Arrays.asList(p.getValue().split("\n")));
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
                    case APPLE:
                        e.getView().close();
                        Bukkit.dispatchCommand(Bukkit.getPlayerExact(player.getName()), "member");
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
                    case BARRIER:
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

        if (e.getView().getTitle().endsWith(STATS_TITLE)) {
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
        }

    }
}
