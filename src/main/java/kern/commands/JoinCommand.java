package kern.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import kern.Game;
import kern.YEUHLobby;
import kern.threads.KillServerThread;

public class JoinCommand implements CommandExecutor {

    private static Map<String, Integer> joinTwice = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player))
            return true;

        Player player = (Player) sender;

        List<Game> startingGames = YEUHLobby.getPlugin().getStartingGames();
        List<Game> openGames = YEUHLobby.getPlugin().getOpenGames();
        List<Game> playingGames = YEUHLobby.getPlugin().getPlayingGames();

        if (args.length == 0) {

            // bad code, but before doing anything else see if the player is supposed to be
            // part of a game and send them to that game
            for (Game g : playingGames) {
                if (g.getAlive().contains(sender.getName())) {
                    g.sendPlayerToGame(player);
                    return true;
                }
            }

            if (startingGames.isEmpty()) {

                if (openGames.isEmpty()) {
                    if (playingGames.isEmpty()) {
                        sender.sendMessage(
                                "\u00a7cThere aren't any available games for you to join at the moment! One should be ready in \u00a7e1 - 2 minutes\u00a7c!");
                    } else if (playingGames.size() == KillServerThread.lastSeenGame.size()) {
                        sender.sendMessage(
                                "\u00a7cNo games were available for you to join, so you are spectating this game.");
                        playingGames.get((int) (Math.random() * playingGames.size())).sendPlayerToGame(player);
                    } else {
                        sender.sendMessage(YEUHLobby.PREFIX
                                + "There aren't any available games for you to join at the moment! One should be ready in \u00a7e1 - 2 minutes\u00a7f!");
                        sender.sendMessage(YEUHLobby.PREFIX
                                + "Or you can spectate an existing game by using \u00a7d/spectate\u00a7f!");
                    }
                } else {
                    openGames.get(0).sendPlayerToGame(player);
                }
                return true;

            }

            startingGames.get((int) (Math.random() * startingGames.size())).sendPlayerToGame(player);
        } else if (args.length == 1) {
            for (Game g : startingGames) {
                if (args[0].equalsIgnoreCase(g.server)) {
                    g.sendPlayerToGame(player);
                    return true;
                }
            }

            for (Game g : openGames) {
                if (args[0].equalsIgnoreCase(g.server)) {
                    g.sendPlayerToGame(player);
                    return true;
                }
            }

            for (Game g : playingGames) {
                if (args[0].equalsIgnoreCase(g.server)) {
                    sender.sendMessage(
                            "\u00a7cThat game is currently active, so you cannot join it! If you would like, you can \u00a7l/spectate \u00a7cit!");
                    return true;
                }
            }

            sender.sendMessage(
                    "\u00a7cThat game doesn't exist or isn't currently available for you to join! Use /games to list all registered games.");
        }
        return true;
    }

}