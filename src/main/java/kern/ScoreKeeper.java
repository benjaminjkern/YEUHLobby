package kern;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ScoreKeeper {

    private Map<String, PlayerStats> cache;

    private static final PlayerStats PLAYER_NOT_FOUND = new PlayerStats(null);

    private Scanner s;
    private File f;

    public ScoreKeeper() {
        cache = new HashMap<>();
        s = null;
        resetScanner();
    }

    private void resetScanner() {
        try {
            YEUHLobby.getPlugin().getDataFolder().mkdir();
            f = new File(YEUHLobby.getPlugin().getDataFolder(), "userStats.txt");
            f.createNewFile();
            s = new Scanner(f);
        } catch (IOException e) {
            Bukkit.getLogger().warning("[YEUHLobby] Something went wrong when opening userStats.txt!");
        }
    }

    public void removeStats(String name) {
        if (cache.containsKey(name.toLowerCase())) cache.remove(name.toLowerCase());

        while (s.hasNextLine()) {
            String line = s.nextLine();

            PlayerStats ps = PlayerStats.newFromParse(line);

            // if it finds other players while looking for one in specifics, it adds them to
            // the cache anyways
            if (!cache.containsKey(ps.player)) cache.put(ps.player, ps);

            if (ps.player.equals(name.toLowerCase())) {
                // don't add it
            }
        }

        // if it gets here, it wasn't in the queue so its fine
    }

    public PlayerStats getStats(String name) { return getStats(name, false); }

    public PlayerStats getStats(String name, boolean create) {

        if (cache.containsKey(name.toLowerCase())) return cache.get(name.toLowerCase());

        while (s.hasNextLine()) {
            String line = s.nextLine();

            PlayerStats ps = PlayerStats.newFromParse(line);

            // if it finds other players while looking for one in specifics, it adds them to
            // the cache anyways
            if (!cache.containsKey(ps.player)) cache.put(ps.player, ps);

            if (ps.player.equals(name.toLowerCase())) return ps;
        }

        if (create) {
            Bukkit.getLogger().info("[YEUHLobby] Creating user stats for " + name + ".");
            PlayerStats ps = new PlayerStats(name);
            cache.put(name.toLowerCase(), ps);
            return ps;
        }
        return PLAYER_NOT_FOUND;
    }

    // write back to disk, should really only be done when plugin is disabled
    public void storeData() {
        Bukkit.getLogger().info("[YEUHLobby] Storing User Stats data.");

        // finish the scanner
        while (s.hasNextLine()) {
            String line = s.nextLine();
            PlayerStats ps = PlayerStats.newFromParse(line);
            if (!cache.containsKey(ps.player)) cache.put(ps.player, ps);
        }
        s.close();

        // write everything in the cache
        try (FileWriter myWriter = new FileWriter(f, false)) {
            for (Entry<String, PlayerStats> e : cache.entrySet()) {
                if (!e.getValue().isEmpty()) myWriter.write(e.getValue().toString() + "\n");
            }
            myWriter.close();
        } catch (IOException e) {
            Bukkit.getLogger().warning("[YEUHLobby] Something went wrong when writing to userStats.txt!");
        }

        resetScanner();
        cache.clear();
    }

    public void kill(String killer, String killed) {
        PlayerStats killerStats = getStats(killer);
        PlayerStats killedStats = getStats(killed);

        double killerRating = killerStats.rating;
        double killedRating = killedStats.rating;

        killerStats.winTo(killedRating, killed);
        killedStats.loseTo(killerRating, killer);
    }

    public void lowerRatings() {
        // finish the scanner
        while (s.hasNextLine()) {
            String line = s.nextLine();
            PlayerStats ps = PlayerStats.newFromParse(line);
            if (!cache.containsKey(ps.player)) cache.put(ps.player, ps);
        }
        long rightNow = new Date().getTime();

        for (PlayerStats ps : cache.values()) {
            if (rightNow - ps.lastSeen > 24 * 3600 * 1000 && ps.rating > 50) {
                ps.setRating(PlayerStats.getRatingFromElo(ps.getEloScore() - 1));
                if (ps.rating < 50) ps.setRating(50);
            }
        }
    }

    public List<PlayerStats> top(String field, int amount) {
        PriorityQueue<PlayerStats> topList = new PriorityQueue<>((a, b) -> {
            try {
                Object A = PlayerStats.class.getField(field).get(a);
                Object B = PlayerStats.class.getField(field).get(b);

                if (A instanceof Integer) { return ((Integer) A).intValue() > ((Integer) B).intValue() ? -1 : 1; }
                if (A instanceof Double) { return ((Double) A).doubleValue() > ((Double) B).doubleValue() ? -1 : 1; }
            } catch (Exception e) {}
            throw new IllegalArgumentException("That field cannot be compared!");
        });

        List<PlayerStats> returnList = new LinkedList<>();

        for (PlayerStats ps : cache.values()) { topList.add(ps); }

        for (int i = 0; i < amount; i++) {
            while (topList.peek().player.contains("-")) topList.poll();
            returnList.add(topList.poll());
        }

        return returnList;
    }
}