package kern.threads;

import org.bukkit.Bukkit;

import kern.YEUHLobby;

public class LowerRatingsThread implements Runnable {

    private LowerRatingsThread task = this;

    public void run() {
        Bukkit.getLogger().info("[YEUHLobby] Updating Ratings...");
        YEUHLobby.getScoreKeeper().lowerRatings();
        YEUHLobby.getScoreKeeper().storeData();

        Bukkit.getScheduler().runTaskLater(YEUHLobby.getPlugin(), task, 20 * 24 * 3600);

    }

}
