package kern.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import kern.Game;
import kern.YEUHLobby;
import kern.threads.KillServerThread;

public class GamesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0) {
            if (YEUHLobby.getPlugin().getGames().isEmpty()) {
                sender.sendMessage("\u00a7cThere aren't any currently registered games!");
            } else {
                sender.sendMessage(YEUHLobby.PREFIX + "All currently registered games:");
                for (Game g : YEUHLobby.getPlugin().getGames()) {
                    sender.sendMessage(g.toString());
                    if (sender.hasPermission("yeuhlobby.admin")) {
                        sender.sendMessage("Last ping: " + g.getLastPing());
                        if (g.gameState.equals("ENDED"))
                            sender.sendMessage("Ended: " + g.getLastPing(g.endTime));
                    }
                }
            }
            if (sender.hasPermission("yeuhlobby.admin")) {
                if (YEUHLobby.getPlugin().getGames().size() < KillServerThread.lastSeenGame.size())
                    sender.sendMessage("\u00a77("
                            + (KillServerThread.lastSeenGame.size() - YEUHLobby.getPlugin().getGames().size())
                            + " untracked games)");
                sender.sendMessage("\u00a7d" + YEUHLobby.gamesPlayedToday + " \u00a77games have been played today.");
                sender.sendMessage(
                        "\u00a7d" + YEUHLobby.gamesPlayedTotal + " \u00a77games have been played since last reset.");
            }
            return true;
        } else if (args.length == 1) {
            for (Game g : YEUHLobby.getPlugin().getGames()) {
                if (g.server.equalsIgnoreCase(args[0])) {
                    sender.sendMessage(g.toString());
                    if (sender.hasPermission("yeuhlobby.admin")) {
                        sender.sendMessage("Last ping: " + g.getLastPing());
                        if (g.gameState.equals("ENDED"))
                            sender.sendMessage("Ended: " + g.getLastPing(g.endTime));
                    }
                    return true;
                }
            }
            sender.sendMessage("\u00a7cThat game doesn't exist!");
        } else if (args.length == 2 && args[0].equalsIgnoreCase("kill") && sender.hasPermission("yeuhlobby.admin")) {
            for (Game g : YEUHLobby.getPlugin().getGames()) {
                if (g.server.equalsIgnoreCase(args[1])) {
                    if (g.forceKill())
                        sender.sendMessage("\u00a7eSuccessfully killed " + g.server);
                    else
                        sender.sendMessage("\u00a7cFailed to kill " + g.server);
                    return true;
                }
            }
            if (Game.forceKill(args[1]))
                sender.sendMessage("\u00a7eSuccessfully killed " + args[1]);
            else
                sender.sendMessage("\u00a7cThat game doesn't exist or failed to kill game!");
        }
        return true;
    }

}
