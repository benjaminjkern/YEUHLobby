package kern.commands;

import java.util.Arrays;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kern.Game;
import kern.PlayerStats;
import kern.ScoreKeeper;
import kern.YEUHLobby;
import kern.listeners.GUIInventoryListener;

public class StatsCommand implements CommandExecutor {

    private ScoreKeeper sk;

    public StatsCommand(ScoreKeeper sk) { this.sk = sk; }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 0) { return showStats(sender, sender.getName()); }
        switch (args[0].toLowerCase()) {
            case "rating":
                return rating(sender, Arrays.copyOfRange(args, 1, args.length));
            case "reset":
                return reset(sender, Arrays.copyOfRange(args, 1, args.length));
            case "set":
                return set(sender, Arrays.copyOfRange(args, 1, args.length));
            case "top":
                return top(sender, Arrays.copyOfRange(args, 1, args.length));
            default:
        }
        return showStats(sender, args);
    }

    private boolean rating(CommandSender sender, String... args) {
        if (args.length == 0) return rating(sender, sender.getName());
        if (args.length == 1) sender.sendMessage(
                args[0] + "'s player rating is: \u00a76" + String.format("%.2f", sk.getStats(args[0]).rating));
        switch (args[0].toLowerCase()) {
            case "addelo":
                return addEloPlayerRating(sender, Arrays.copyOfRange(args, 1, args.length));
            default:
        }
        return false;
    }

    private boolean reset(CommandSender sender, String... args) {
        if (args.length == 0) return reset(sender, sender.getName());

        if (!sender.hasPermission("YEUHLobby.stats.reset")) { return noPerms(sender); }
        sk.removeStats(args[0]);
        sender.sendMessage(args[0] + "'s stats have been reset!");
        return true;
    }

    private boolean set(CommandSender sender, String... args) {
        if (!sender.hasPermission("YEUHLobby.stats.set")) return noPerms(sender);

        if (args.length < 2) return false;
        if (args.length == 2) return set(sender, sender.getName(), args[0], args[1]);

        if (args.length != 3) return false;
        PlayerStats ps = sk.getStats(args[0]);
        try {
            Object value = PlayerStats.class.getField(args[1]).get(ps);

            if (value instanceof Double) PlayerStats.class.getField(args[1]).setDouble(ps, Double.parseDouble(args[2]));
            else if (value instanceof Integer)
                PlayerStats.class.getField(args[1]).setInt(ps, Integer.parseInt(args[2]));
            else if (value instanceof String) PlayerStats.class.getField(args[1]).set(ps, args[2]);
            else {
                sender.sendMessage("\u00a7cField \u00a7l" + args[1] + " \u00a7cdoes not exist!");
                return true;
            }

        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            sender.sendMessage("\u00a7cField \u00a7l" + args[1] + " \u00a7cdoes not exist!");
            return true;
        }

        boolean found = false;
        for (Game g : YEUHLobby.getPlugin().getGames()) {
            if (found) break;
            for (UUID uuid : g.getPlayers()) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
                if (args[0].equalsIgnoreCase(op.getName())) {
                    g.outPrint.println("STATS:" + ps);
                    found = true;
                    break;
                }
            }
        }

        sender.sendMessage(
                "\u00a7d" + args[0] + "\u00a7f's \u00a7d" + args[1] + " \u00a7fhas been set to \u00a7d" + args[2]);
        return true;
    }

    private boolean addEloPlayerRating(CommandSender sender, String... args) {
        if (!sender.hasPermission("YEUHLobby.stats.set")) return noPerms(sender);
        if (args.length != 2) return false;
        PlayerStats ps = sk.getStats(args[0]);
        ps.setRating(PlayerStats.getRatingFromElo(ps.getEloScore() + Double.parseDouble(args[1])));

        boolean found = false;
        for (Game g : YEUHLobby.getPlugin().getGames()) {
            if (found) break;
            for (UUID uuid : g.getPlayers()) {
                OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
                if (args[0].equalsIgnoreCase(op.getName())) {
                    g.outPrint.println("STATS:" + ps);
                    found = true;
                    break;
                }
            }
        }

        sender.sendMessage("\u00a7d" + args[0] + "\u00a7f's rating has been set to \u00a76" + ps.rating);
        return true;
    }

    private boolean showStats(CommandSender sender, String... args) {
        if (args.length == 0) return true;
        if (!(sender instanceof Player)) {
            sender.sendMessage(sk.getStats(args[0]).toString());
            return true;
        }
        ((Player) sender).openInventory(GUIInventoryListener.getStatsInventory(args[0]));
        return true;
    }

    private boolean top(CommandSender sender, String... args) {
        String field = "rating";
        if (args.length > 0) field = args[0];

        sender.sendMessage(YEUHLobby.PREFIX + "Top " + field + (field.endsWith("s") ? "" : "s") + ":");

        int i = 1;
        try {
            for (PlayerStats ps : sk.top(field, 10)) {
                Object value = PlayerStats.class.getField(field).get(ps);
                if (value instanceof Double) value = "\u00a76" + String.format("%.2f", value);
                else value = "\u00a7d" + value;
                sender.sendMessage("\u00a75(\u00a7d\u00a7l" + i + "\u00a75) \u00a7f" + ps.player + ": " + value);
                i++;
            }
        } catch (Exception e) {
            sender.sendMessage("\u00a7cField \u00a7l" + field + " \u00a7ccannot be ranked!");
        }
        return true;
    }

    private boolean noPerms(CommandSender sender) {
        sender.sendMessage("\u00a7cYou don't have permission to do that!");
        return true;
    }

}
