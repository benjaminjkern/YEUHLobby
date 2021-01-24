package kern;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.bukkit.Bukkit;

import kern.listeners.PlayerListener;

import com.gmail.filoghost.holographicdisplays.commands.CommandValidator;
import com.gmail.filoghost.holographicdisplays.disk.HologramDatabase;
import com.gmail.filoghost.holographicdisplays.event.NamedHologramEditedEvent;
import com.gmail.filoghost.holographicdisplays.exception.CommandException;
import com.gmail.filoghost.holographicdisplays.object.NamedHologram;
import com.gmail.filoghost.holographicdisplays.object.line.CraftHologramLine;

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

    public void loseGame(int total, String loser, String... left) {
        PlayerStats loserStats = getStats(loser);
        double elo = loserStats.getEloScore();

        // dont lose to the same player twice (Bots shouldnt be given multiple points
        // every time a single player dies)
        Set<String> seen = new HashSet<>();
        for (String winner : left) {
            if (winner.equals(loser) || seen.contains(winner)) continue;
            seen.add(winner);
            PlayerStats winnerStats = getStats(winner);
            elo -= PlayerStats.K(total) * loserStats.expected(winnerStats.getEloScore());
            winnerStats.winTo(loserStats.getEloScore(), null, PlayerStats.K(total));
        }
        loserStats.setRating(loserStats.getRatingFromElo(elo));
    }

    public void winGame(int total, String winner, String... losers) {
        PlayerStats winnerStats = getStats(winner);
        double startElo = winnerStats.getEloScore();
        double elo = startElo;

        winnerStats.wins++;

        // dont lose to the same player twice (Bots shouldnt be given multiple points
        // every time a single player dies)
        Set<String> seen = new HashSet<>();
        for (String loser : losers) {
            if (winner.equals(loser) || seen.contains(loser)) continue;
            seen.add(loser);
            PlayerStats loserStats = getStats(loser);
            double diff = PlayerStats.K(total) * loserStats.expected(startElo);
            loserStats.setRating(loserStats.getRatingFromElo(loserStats.getEloScore() - diff));
            elo += diff;
        }

        winnerStats.setRating(winnerStats.getRatingFromElo(elo));
    }

    public void kill(String killer, String killed) {
        PlayerStats killerStats = getStats(killer);
        PlayerStats killedStats = getStats(killed);

        double killerElo = killerStats.getEloScore();
        double killedElo = killedStats.getEloScore();

        killerStats.winTo(killedElo, killed, PlayerStats.K_KILL);
        killedStats.loseTo(killerElo, killer, PlayerStats.K_KILL);
    }

    public void updateBoards() {

        try {
            List<PlayerStats> topRatings = top("rating", 10);
            NamedHologram hologram = CommandValidator.getNamedHologram("topscores");
            for (int i = 1; i <= 10; i++) {
                CraftHologramLine line = CommandValidator.parseHologramLine(hologram, "&5(&d&l" + i + "&5) &f"
                        + topRatings.get(i - 1).player + ": " + topRatings.get(i - 1).ratingString(), true);

                hologram.getLinesUnsafe().get(i).despawn();
                hologram.getLinesUnsafe().set(i, line);
                hologram.refreshAll();

                HologramDatabase.saveHologram(hologram);
                HologramDatabase.trySaveToDisk();
                Bukkit.getPluginManager().callEvent(new NamedHologramEditedEvent(hologram));
            }

            List<PlayerStats> topWins = top("wins", 10);
            hologram = CommandValidator.getNamedHologram("topwins");
            for (int i = 1; i <= 10; i++) {
                CraftHologramLine line = CommandValidator.parseHologramLine(hologram,
                        "&5(&d&l" + i + "&5) &f" + topWins.get(i - 1).player + ": &d" + topWins.get(i - 1).wins, true);

                hologram.getLinesUnsafe().get(i).despawn();
                hologram.getLinesUnsafe().set(i, line);
                hologram.refreshAll();

                HologramDatabase.saveHologram(hologram);
                HologramDatabase.trySaveToDisk();
                Bukkit.getPluginManager().callEvent(new NamedHologramEditedEvent(hologram));
            }

            List<PlayerStats> topKills = top("playerKills", 10);
            hologram = CommandValidator.getNamedHologram("topkills");
            for (int i = 1; i <= 10; i++) {
                CraftHologramLine line = CommandValidator.parseHologramLine(hologram, "&5(&d&l" + i + "&5) &f"
                        + topKills.get(i - 1).player + ": &d" + topKills.get(i - 1).playerKills, true);

                hologram.getLinesUnsafe().get(i).despawn();
                hologram.getLinesUnsafe().set(i, line);
                hologram.refreshAll();

                HologramDatabase.saveHologram(hologram);
                HologramDatabase.trySaveToDisk();
                Bukkit.getPluginManager().callEvent(new NamedHologramEditedEvent(hologram));
            }

            List<PlayerStats> topBotKills = top("botKills", 10);
            hologram = CommandValidator.getNamedHologram("topyeuhs");
            for (int i = 1; i <= 10; i++) {
                CraftHologramLine line = CommandValidator.parseHologramLine(hologram, "&5(&d&l" + i + "&5) &f"
                        + topBotKills.get(i - 1).player + ": &d" + topBotKills.get(i - 1).botKills, true);

                hologram.getLinesUnsafe().get(i).despawn();
                hologram.getLinesUnsafe().set(i, line);
                hologram.refreshAll();

                HologramDatabase.saveHologram(hologram);
                HologramDatabase.trySaveToDisk();
                Bukkit.getPluginManager().callEvent(new NamedHologramEditedEvent(hologram));
            }
        } catch (CommandException e) {
            Bukkit.getLogger().info("OOPSIEDAISIES");
        }
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
                ps.setRating(ps.getRatingFromElo(ps.getEloScore() - 1));
                if (ps.rating < 50) ps.setRating(50);
            }
        }
    }

    public List<PlayerStats> top(String field, int amount) { return top(field, amount, 1); }

    public List<PlayerStats> bottom(String field, int amount) { return top(field, amount, -1); }

    public List<PlayerStats> top(String field, int amount, int flip) {

        // finish the scanner
        while (s.hasNextLine()) {
            String line = s.nextLine();
            PlayerStats ps = PlayerStats.newFromParse(line);
            if (!cache.containsKey(ps.player)) cache.put(ps.player, ps);
        }
        Map<String, Integer> nemesisCount = new HashMap<>();

        PriorityQueue<PlayerStats> topList;

        if (field.equalsIgnoreCase("nemesis")) {
            try {
                for (PlayerStats ps : cache.values()) {
                    if (ps.nemesis == null) continue;
                    if (nemesisCount.containsKey(ps.nemesis))
                        nemesisCount.put(ps.nemesis, nemesisCount.get(ps.nemesis) + 1);
                    else nemesisCount.put(ps.nemesis, 1);
                }

                topList = new PriorityQueue<>((a,
                        b) -> flip * ((nemesisCount.get(a.player) > nemesisCount.get(b.player)
                                || (nemesisCount.get(b.player) == null && nemesisCount.get(a.player) != null)) ? -1
                                        : 1));
            } catch (Exception exception) {
                Arrays.asList(exception.getStackTrace())
                        .forEach(stackTrace -> Bukkit.getLogger().warning(stackTrace.toString()));
                throw exception;
            }
        } else if (field.equalsIgnoreCase("level")) {

            topList = new PriorityQueue<>((a, b) -> flip * (a.getLevel() > b.getLevel() ? -1 : 1));

        } else {
            topList = new PriorityQueue<>((a, b) -> {
                try {
                    Object A = PlayerStats.class.getField(field).get(a);
                    Object B = PlayerStats.class.getField(field).get(b);

                    if (A instanceof Integer) {
                        return flip * (((Integer) A).intValue() > ((Integer) B).intValue() ? -1 : 1);
                    }
                    if (A instanceof Double) {
                        return flip * (((Double) A).doubleValue() > ((Double) B).doubleValue() ? -1 : 1);
                    }
                } catch (Exception e) {}
                throw new IllegalArgumentException("That field cannot be compared!");
            });
        }

        List<PlayerStats> returnList = new LinkedList<>();

        for (PlayerStats ps : cache.values()) { topList.add(ps); }

        for (int i = 0; i < amount; i++) {
            while (topList.peek() != null && topList.peek().player.contains("-")) topList.poll();
            if (topList.peek() == null) break; // shouldn't ever happen but if there are less than the amount required
                                               // then the queue will be empty and it shouldnt return anymore
            returnList.add(topList.poll());
        }

        return returnList;
    }
}