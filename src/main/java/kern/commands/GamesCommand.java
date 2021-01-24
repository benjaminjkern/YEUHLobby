package kern.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import kern.Game;
import kern.YEUHLobby;

public class GamesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (YEUHLobby.getPlugin().getGames().isEmpty()) {
                sender.sendMessage("\u00a7cThere aren't any currently registered games!");
                return true;
            }
            sender.sendMessage(YEUHLobby.PREFIX + "All currently registered games:");
            for (Game g : YEUHLobby.getPlugin().getGames()) {
                sender.sendMessage(g.toString());
                if (sender.hasPermission("yeuhlobby.admin")) sender.sendMessage("Last ping: " + g.getLastPing());
            }
            return true;
        } else if (args.length == 1) {
            for (Game g : YEUHLobby.getPlugin().getGames()) {
                if (g.server.equalsIgnoreCase(args[0])) {
                    sender.sendMessage(g.toString());
                    if (sender.hasPermission("yeuhlobby.admin")) sender.sendMessage("Last ping: " + g.getLastPing());
                    return true;
                }
            }
            sender.sendMessage("\u00a7cThat game doesn't exist!");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("kill") && sender.hasPermission("yeuhlobby.admin")) {
            for (Game g : YEUHLobby.getPlugin().getGames()) {
                if (g.server.equalsIgnoreCase(args[1])) {
                    if (g.forceKill()) sender.sendMessage("\u00a7eSuccessfully killed " + g.server);
                    else sender.sendMessage("\u00a7cFailed to kill " + g.server);
                    return true;
                }
            }
            sender.sendMessage("\u00a7cThat game doesn't exist!");
        }
        return true;
    }

}
