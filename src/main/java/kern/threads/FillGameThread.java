package kern.threads;

import java.util.Queue;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import kern.Game;
import kern.YEUHLobby;

public class FillGameThread implements Runnable {

    FillGameThread task;
    Game g;

    FillGameThread(Game g) {
        task = this;
        this.g = g;
    }

    @Override
    public void run() {
        Queue<Player> playerQueue = YEUHLobby.getPlugin().getPlayerQueue();

        if (playerQueue.isEmpty()) {
            Bukkit.getScheduler().runTaskLater(YEUHLobby.getPlugin(), new CheckServersThread(), 20);
            return;
        }

        // if game was cancelled, or enough people drop out of queue to make it not work
        // anymore
        if (!g.acceptingNew || g.currentSize + playerQueue.size() < g.minSize) {
            Bukkit.getScheduler().runTaskLater(YEUHLobby.getPlugin(), new CheckServersThread(), 20);
            return;
        }

        try {
            g.sendPlayerToGame(playerQueue.poll());
            g.currentSize++;
        } catch (Exception e) {
            Bukkit.getLogger().warning(e.toString());
        }

        Bukkit.getScheduler().runTaskLater(YEUHLobby.getPlugin(), task, 1);
    }
}
