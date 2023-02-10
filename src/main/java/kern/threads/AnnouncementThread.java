package kern.threads;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;

import kern.Game;
import kern.YEUHLobby;

public class AnnouncementThread implements Runnable {

    private static List<String> announcements;
    private static List<String> funFacts;
    static {
        announcements = new ArrayList<>();
        announcements.add(
                "Want to become a member of \u00a7d\u00a7l\u00a7oYEUH\u00a7f? It's \u00a75\u00a7oeasy \u00a7fand \u00a75\u00a7ofree\u00a7f!\n        Use \u00a7d/member \u00a7ffor more information.");
        announcements.add(
                "Like the server? Consider becoming a \u00a7bPatron\u00a7f!\nPatrons get lots of \u00a7ecool perks\u00a7f, and it really helps with keep the server up and running!\n        Use \u00a7b/help patron \u00a7ffor more info!");
        announcements.add(
                "We have a \u00a79Discord \u00a7fwhere you can meet our \u00a7bcommunity\u00a7f, participate in \u00a7bmassive games\u00a7f, and \u00a7bsuggest new ideas\u00a7f for our team to add to the server!\n        Use \u00a79/discord \u00a7fto join!");
        announcements.add("Fun fact! %funFact%");

        funFacts = new ArrayList<>();
    }

    private AnnouncementThread task = this;

    private int counter = 0;

    public void run() {
        if (counter > 2) counter = 0;
        String message = announcements.get(counter);
        YEUHLobby.broadcastInfoMessage(message);
        for (Game g : YEUHLobby.getPlugin().getGames()) { g.broadcastMessage(message); }
        counter++;

        Bukkit.getScheduler().runTaskLater(YEUHLobby.getPlugin(), task, 20 * 60 * 5);
    }

}
