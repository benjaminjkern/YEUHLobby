package kern;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandException;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ElapsedTimeThread implements Runnable{
    
	private ElapsedTimeThread task;
	
	public ElapsedTimeThread() {
	}
	
	@Override
	public void run() {
        checkServers();

		Bukkit.getScheduler().runTaskLater(YEUHLobby.getPlugin(), task, 20);
    }
    
    private void checkServers() {
        Queue<Player> playerQueue = YEUHLobby.getPlugin().getPlayerQueue();
        
        if (playerQueue.isEmpty()) return;

				for (Game g : games) {
					// if there aren't enough available players to even have a game, don't worry about it
					if (playerQueue.size() + g.currentSize < g.minSize || !g.ready) continue;
					while (g.currentSize < g.maxSize && !playerQueue.isEmpty()) {
						g.sendPlayerToGame(playerQueue.poll());
						g.currentSize++;
					}
					return;
				}
    }

}