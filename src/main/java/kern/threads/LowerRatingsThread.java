package kern.threads;

import org.bukkit.Bukkit;

import kern.YEUHLobby;

public class LowerRatingsThread implements Runnable {

    private LowerRatingsThread task = this;

    public void run() {
        try {
            Bukkit.getLogger().info("[YEUHLobby] Updating Ratings...");
            YEUHLobby.setGamesPlayedToday(0);
            YEUHLobby.getScoreKeeper().lowerRatings();
            YEUHLobby.getScoreKeeper().storeData();
            YEUHLobby.getScoreKeeper().updateBoards();
            KillServerThread.lastSeenGame.clear();
        } catch (Exception e) {
            Bukkit.getLogger().warning(e.toString());
        }

        Bukkit.getScheduler().runTaskLater(YEUHLobby.getPlugin(), task, 20 * 24 * 60 * 60);
    }

}
