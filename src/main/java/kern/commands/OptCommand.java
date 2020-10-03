package kern.commands;

import java.util.Queue;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kern.YEUHLobby;

public class OptCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1) return false;

        if (!(sender instanceof Player)) return true;

        Player player = (Player) sender;
        if (!YEUHLobby.getWarden().hasPlayerSigned(player.getName())) {
            player.sendMessage(YEUHLobby.PREFIX
                    + "You must read the \u00a7d/rules \u00a7fbefore you can opt in and out of the queue!");
            return true;
        }

        Queue<Player> playerQueue = YEUHLobby.getPlugin().getPlayerQueue();

        switch (args[0]) {
            case "out":
                if (!playerQueue.contains((Player) sender)) {
                    sender.sendMessage("\u00a7cYou aren't in the queue! You can't opt out!");
                    return true;
                }
                sender.sendMessage(YEUHLobby.PREFIX
                        + "You have opted out of the queue. Use \u00a7d/opt in \u00a7fif you would like to rejoin.");
                YEUHLobby.getPlugin().getPlayerQueue().remove((Player) sender);
                return true;

            case "in":
                if (playerQueue.contains((Player) sender)) {
                    sender.sendMessage("\u00a7cYou are already in the queue!");
                } else {
                    sender.sendMessage(YEUHLobby.PREFIX
                            + "You have opted in to the queue. Use \u00a7d/opt out \u00a7fif you would like to stay in the lobby.");
                    YEUHLobby.getPlugin().getPlayerQueue().add((Player) sender);
                }

                if (!YEUHLobby.getPlugin().getPlayingGames().isEmpty()) {
                    player.sendMessage(YEUHLobby.PREFIX
                            + "If you would like to spectate an active game, use \u00a7d/spectate\u00a7f!");
                }
                return true;
            default:
                // nothing
        }

        return false;
    }

}
