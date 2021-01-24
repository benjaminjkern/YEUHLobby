package kern.commands;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import kern.Game;
import kern.YEUHLobby;

public class JoinCommand implements CommandExecutor {

    private static Set<String> joinTwice = new HashSet<>();

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;

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
                    sender.sendMessage(
                            "\u00a7cThere aren't any available games for you to join at the moment! Please wait for one to be ready!");
                    if (!playingGames.isEmpty()) {
                        sender.sendMessage(
                                "\u00a7cUse \u00a7l/spectate \u00a7cif you would like to spectate an active game!");
                    }
                } else {
                    openGames.get((int) (Math.random() * openGames.size())).sendPlayerToGame(player);
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