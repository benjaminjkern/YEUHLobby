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
        if (!YEUHLobby.getWarden().hasPlayerSigned(player.getName())) {
            player.sendMessage(
                    YEUHLobby.PREFIX + "You must read the \u00a7d/rules \u00a7fbefore you can spectate a game!");
            return true;
        }

        List<Game> playingGames = YEUHLobby.getPlugin().getPlayingGames();

        if (args.length == 0) {

            if (playingGames.isEmpty()) {
                sender.sendMessage("\u00a7cThere aren't any active games for you to spectate!");
                return true;
            }

            playingGames.get((int) (Math.random() * playingGames.size())).sendPlayerToGame((Player) sender);
        } else if (args.length == 1) {
            for (Game g : playingGames) { if (args[0].equals(g.server)) g.sendPlayerToGame((Player) sender); }

            sender.sendMessage(
                    "\u00a7cThat game doesn't exist or isn't currently active! Use /games to list all registered games.");
        }
        return true;
    }

}
