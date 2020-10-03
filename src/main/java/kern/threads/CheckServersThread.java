package kern.threads;

import java.util.List;
import java.util.Queue;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import kern.Game;
import kern.YEUHLobby;

public class CheckServersThread implements Runnable {

    CheckServersThread task;
    int count;
    Game readyGame;

    public CheckServersThread() {
        task = this;
        count = 30;
        readyGame = null;
    }

    @Override
    public void run() {
        Queue<Player> playerQueue = YEUHLobby.getPlugin().getPlayerQueue();
        List<Game> openGames = YEUHLobby.getPlugin().getOpenGames();

        if (readyGame == null) {
            for (Game g : openGames) {
                if (g.currentSize + playerQueue.size() >= g.minSize) {
                    readyGame = g;
                    break;
                }
            }
            Bukkit.getScheduler().runTaskLater(YEUHLobby.getPlugin(), task, 20);
            return;
        }

        if (readyGame.currentSize + playerQueue.size() < readyGame.minSize) {
            readyGame = null;
            count = 30;
            Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(
                    YEUHLobby.PREFIX + "No longer enough players in queue to start a game, stopping countdown."));
            Bukkit.getScheduler().runTaskLater(YEUHLobby.getPlugin(), task, 20);
            return;
        }

        if (!readyGame.acceptingNew) {
            readyGame = null;
            count = 30;
            Bukkit.getOnlinePlayers().forEach(
                    player -> player.sendMessage(YEUHLobby.PREFIX + "Target lobby halted, stopping countdown."));
            Bukkit.getScheduler().runTaskLater(YEUHLobby.getPlugin(), task, 20);
            return;
        }

        if (count > 0) {
            if (count % 10 == 0 || count <= 5) Bukkit.getOnlinePlayers().forEach(player -> {
                player.sendMessage(
                        YEUHLobby.PREFIX + "Sending players to game in: \u00a7d" + count
                                + (count == 30 ? "\u00a7f seconds.\n" + YEUHLobby.PREFIX
                                        + "If you would like to stay in the Creative Lobby, use \u00a7d/opt out\u00a7f."
                                        : "\u00a7f."));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 1, 3);
            });
            count -= 1;
        } else {
            Bukkit.getScheduler().runTask(YEUHLobby.getPlugin(), new FillGameThread(readyGame));
            return;
        }

        Bukkit.getScheduler().runTaskLater(YEUHLobby.getPlugin(), task, 20);
    }
}