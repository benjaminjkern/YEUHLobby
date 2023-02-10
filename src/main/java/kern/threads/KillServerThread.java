package kern.threads;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.bukkit.Bukkit;

import kern.Game;
import kern.YEUHLobby;

public class KillServerThread implements Runnable {

    private KillServerThread task = this;

    public static Map<String, Long> lastSeenGame = new HashMap<>();

    public void run() {
        try {
            long now = new Date().getTime();
            List<Game> games = YEUHLobby.getPlugin().getGames();
            for (Entry<String, Long> g : lastSeenGame.entrySet()) {
                if (g.getValue() == Long.MAX_VALUE)
                    lastSeenGame.put(g.getKey(), now);
                else if (now - g.getValue() > 5 * 60 * 1000) {
                    if (Game.forceKill(g.getKey()))
                        Bukkit.getLogger().info("Killed " + g.getKey() + " because game was not detected!");
                    else
                        Bukkit.getLogger().warning("Failed to kill " + g.getKey() + "!");
                    lastSeenGame.remove(g.getKey());
                }
            }
            for (Game g : games) {
                if (g == null) {
                    games.remove(null);
                    continue;
                }

                if (g.server != null)
                    lastSeenGame.put(g.server, Long.MAX_VALUE);

                if (now - g.lastPing > 2 * 60 * 1000 || ((g.gameState.equals("ENDED") || g.gameState.equals("STARTING"))
                        && now - g.endTime > 2 * 60 * 1000)) {
                    if (g.forceKill())
                        Bukkit.getLogger().info("Killed " + g.server + " for stalling");
                    else
                        Bukkit.getLogger().warning("Failed to kill " + g.server + "!");
                }
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning(e.toString());
        }

        Bukkit.getScheduler().runTaskLater(YEUHLobby.getPlugin(), task, 20 * 60);

    }

}
