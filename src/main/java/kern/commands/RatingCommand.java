package kern.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import kern.YEUHLobby;

public class RatingCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        sender.sendMessage("Your player rating is: \u00a76"
                + String.format("%.2f", YEUHLobby.getScoreKeeper().getStats(sender.getName()).rating));
        return true;
    }

}