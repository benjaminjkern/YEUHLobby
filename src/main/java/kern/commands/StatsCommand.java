package kern.commands;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import kern.ScoreKeeper;

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
            default:
        }
        return showStats(sender, args);
    }

    private boolean rating(CommandSender sender, String... args) {
        if (args.length == 0) return rating(sender, sender.getName());
        sender.sendMessage(
                args[0] + "'s player rating is: \u00a76" + String.format("%.2f", sk.getStats(args[0]).rating));
        return true;
    }

    private boolean reset(CommandSender sender, String... args) {
        if (args.length == 0) return reset(sender, sender.getName());

        if (!sender.hasPermission("YEUHLobby.stats.reset")) { return noPerms(sender); }
        sk.getStats(args[0]).reset();
        sender.sendMessage(args[0] + "'s stats have been reset!");
        return true;
    }

    private boolean showStats(CommandSender sender, String... args) {
        sender.sendMessage(sk.getStats(args[0]) + "");
        return true;
    }

    private boolean noPerms(CommandSender sender) {
        sender.sendMessage("\u00a7cYou don't have permission to do that!");
        return true;
    }

}
