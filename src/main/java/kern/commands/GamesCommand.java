package kern.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import kern.Game;
import kern.YEUHLobby;

public class GamesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (YEUHLobby.getPlugin().getGames().isEmpty()) {
            sender.sendMessage("\u00a7cThere aren't any currently registered games!");
            return true;
        }
        sender.sendMessage(YEUHLobby.PREFIX + "All currently registered games:");
        for (Game g : YEUHLobby.getPlugin().getGames()) { sender.sendMessage(g.toString()); }
        return true;
    }

}
