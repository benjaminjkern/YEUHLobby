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
                    + String.format("%.2f", YEUHLobby.getScoreKeeper().getStats(sender.getName()).rating));
        } else {
            Bukkit.dispatchCommand(sender, "stats rating " + args[0]);
        }
        sender.sendMessage("Use \u00a7d/stats \u00a7ffor more info.");
        return true;
    }

}