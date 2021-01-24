package kern.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import kern.YEUHLobby;

public class RatingCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("Your player rating is: \u00a76"
                    + YEUHLobby.getScoreKeeper().getStats(sender.getName()).ratingString());
        } else if (args.length == 1) {
            Bukkit.dispatchCommand(sender, "stats rating " + args[0]);
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            YEUHLobby.getScoreKeeper().getStats(args[1]).setRating(Double.parseDouble(args[2]));
        }
        sender.sendMessage("Use \u00a7d/stats \u00a7ffor more info.");
        return true;
    }

}