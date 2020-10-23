package kern.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kern.Game;
import kern.YEUHLobby;

public class SpectateCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        List<Game> playingGames = YEUHLobby.getPlugin().getPlayingGames();
        List<Game> openGames = YEUHLobby.getPlugin().getOpenGames();

        if (args.length == 0) {

            if (playingGames.isEmpty()) {
                sender.sendMessage("\u00a7cThere aren't any active games for you to spectate at the moment!");
                return true;
            }

            playingGames.get((int) (Math.random() * playingGames.size())).sendPlayerToGame(player);
        } else if (args.length == 1) {
            for (Game g : playingGames) {
                if (args[0].equalsIgnoreCase(g.server)) {
                    g.sendPlayerToGame(player);
                    return true;
                }
            }

            for (Game g : openGames) {
                if (args[0].equalsIgnoreCase(g.server)) {
                    sender.sendMessage(
                            "\u00a7cThat game isn't currently active, but sure I'll send you there anyways!");
                    g.sendPlayerToGame(player);
                    return true;
                }
            }

            sender.sendMessage(
                    "\u00a7cThat game doesn't exist or isn't currently active! Use /games to list all registered games.");
        }
        return true;
    }

}
