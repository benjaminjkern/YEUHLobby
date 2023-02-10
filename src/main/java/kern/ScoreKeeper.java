package kern;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiFunction;

import org.bukkit.Bukkit;

public class ScoreKeeper {

    private Map<String, PlayerStats> cache;

    private static final PlayerStats PLAYER_NOT_FOUND = new PlayerStats(null);
    private File statsFolder;

    public ScoreKeeper() {
        cache = new HashMap<>();
        try {
            statsFolder = new File(YEUHLobby.getPlugin().getDataFolder(), "userStats");
            statsFolder.createNewFile();
        } catch (IOException e) {
            Bukkit.getLogger().warning("[YEUHLobby] Something went wrong when creating userStats folder!");
        }
    }
    // try to only keep in cache if they are online or in some top ten list

    private Iterable<PlayerStats> allStats() {

        if (statsFolder == null) return new ArrayList<>();

        Iterator<File> statsFiles = Arrays.asList(statsFolder.listFiles()).iterator();

        return new Iterable<PlayerStats>() {
            public Iterator<PlayerStats> iterator() {
                return new Iterator<PlayerStats>() {
                    public boolean hasNext() { return statsFiles.hasNext(); }

                    public PlayerStats next() {
                        File statsFile = statsFiles.next();
                        try (Scanner s = new Scanner(statsFile)) {
                            if (s.hasNextLine()) return readStatsCache(s.nextLine().split(":")[1]);
                            if (!statsFile.delete())
                                Bukkit.getLogger().warning("Failed to delete empty stats file: " + statsFile.getName());
                        } catch (FileNotFoundException e) {
                            Bukkit.getLogger().warning("File doesn't exist (?): " + statsFile.getName());
                        }
                        return PLAYER_NOT_FOUND;
                    }
                };
            }
        };
    }

    private PlayerStats readStatsCache(String name) {
        if (cache.containsKey(name.toLowerCase())) return cache.get(name.toLowerCase());
        return readStats(name);
    }

    private PlayerStats readStats(String name) {
        try {
            YEUHLobby.getPlugin().getDataFolder().mkdir();
            File f = new File(YEUHLobby.getPlugin().getDataFolder(), "userStats/" + name.toLowerCase() + ".stats");

            try (Scanner s = new Scanner(f)) {
                Map<String, String> statsMap = new HashMap<>();

                while (s.hasNextLine()) {
                    String[] line = s.nextLine().split(":");
                    statsMap.put(line[0], line[1]);
                }

                return PlayerStats.newFromParse(statsMap);
            }
        } catch (IOException e) {}

        return PLAYER_NOT_FOUND;
    }

    public void removeStats(String name) { getStats(name).reset(); }

    public PlayerStats getStats(String name) { return getStats(name, false); }

    public PlayerStats getStats(String name, boolean create) {
        if (cache.containsKey(name.toLowerCase())) return cache.get(name.toLowerCase());

        PlayerStats ps = readStats(name);
        if (ps.player != null) {
            cache.put(ps.player, ps);
            return ps;
        }

        if (create) {
            Bukkit.getLogger().info("[YEUHLobby] Creating user stats for \u00a7d" + name + "\u00a7f.");
            ps = new PlayerStats(name);
            cache.put(name.toLowerCase(), ps);
            return ps;
        }
        return PLAYER_NOT_FOUND;
    }

    // write back to disk, should really only be done when plugin is disabled
    public void storeData() {
        Bukkit.getLogger().info("[YEUHLobby] Storing User Stats data.");
        Bukkit.getLogger()
                .info("[YEUHLobby] \u00a7d" + cache.size() + " \u00a7funique players joined since last storage event.");

        // write everything in the cache
        for (Entry<String, PlayerStats> e : cache.entrySet()) {
            File f = new File(statsFolder, e.getKey() + ".stats");

            try {
                if (!e.getValue().isEmpty()) {
                    f.createNewFile();
                    try (FileWriter myWriter = new FileWriter(f, false)) {
                        myWriter.write(e.getValue().toFileString());
                    }
                } else f.delete();
            } catch (IOException ex) {
                Bukkit.getLogger().warning("[YEUHLobby] Something went wrong when writing user stats");
            }
        }
        cache.clear();
    }

    public void updateBoards() {
        List<List<PlayerStats>> topLists = topMultiple(new String[] { "elo", "wins", "playerKills", "botKills" }, 10,
                1);
        Bukkit.getLogger().warning("NEED TO VERIFY THIS IS WORKING FOR HOLOGRAPHIC DISPLAYS");

        addTopScoreboardLines(topLists.get(0), "topscores", (list, i) -> "&5(&d&l" + i + "&5) &f"
                + list.get(i - 1).player + ": " + list.get(i - 1).ratingString(true));

        addTopScoreboardLines(topLists.get(1), "topwins",
                (list, i) -> "&5(&d&l" + i + "&5) &f" + list.get(i - 1).player + ": &d" + list.get(i - 1).wins);

        addTopScoreboardLines(topLists.get(2), "topkills",
                (list, i) -> "&5(&d&l" + i + "&5) &f" + list.get(i - 1).player + ": &d" + list.get(i - 1).playerKills);

        addTopScoreboardLines(topLists.get(3), "topyeuhs",
                (list, i) -> "&5(&d&l" + i + "&5) &f" + list.get(i - 1).player + ": &d" + list.get(i - 1).botKills);
    }

    private void addTopScoreboardLines(List<PlayerStats> list, String hologramName,
            BiFunction<List<PlayerStats>, Integer, String> lineString) {
        try {
            Class<?> holographicDisplays = Bukkit.getPluginManager().getPlugin("HolographicDisplays").getClass();
            Object commandManager = holographicDisplays.getField("commandManager").get(holographicDisplays);
            Object hologramEditor = commandManager.getClass().getField("hologramEditor").get(commandManager);

            Object hologram = hologramEditor.getClass().getMethod("getExistingHologram", String.class)
                    .invoke(hologramEditor, hologramName);
            for (int i = 1; i <= 10; i++) {
                Object line = hologramEditor.getClass()
                        .getMethod("parseHologramLine", hologram.getClass(), String.class)
                        .invoke(hologramEditor, hologram, lineString.apply(list, i));

                Object lines = hologram.getClass().getMethod("getLines").invoke(hologram);

                getMethod(lines, "set").invoke(lines, i, line);
                getMethod(hologramEditor, "saveChanges").invoke(hologramEditor, hologram, null);
            }
        } catch (NoSuchFieldException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Bukkit.getLogger().warning(e.getMessage());
            e.printStackTrace();
        }
    }

    private Method getMethod(Object obj, String methodName) throws NoSuchMethodException {
        for (Method method : obj.getClass().getMethods()) { if (method.getName().equals(methodName)) return method; }
        throw new NoSuchMethodException();
    }

    public void lowerRatings() {
        long rightNow = new Date().getTime();

        for (PlayerStats ps : allStats()) {
            if (rightNow - ps.lastSeen > 24 * 3600 * 1000) {
                ps.setRating(ps.getRatingFromElo(ps.getEloScore() - 1));
                if (ps.rating < 10) {
                    ps.reset();
                    continue;
                }
            }
            ps.nemesisKills = Math.max(ps.nemesisKills - 1, 0);
            ps.lastKilledByKills = Math.max(ps.lastKilledByKills - 1, 0);
            if (ps.nemesisKills == 0) { ps.nemesis = null; }
            if (ps.lastKilledByKills == 0) { ps.lastKilledBy = null; }
        }
    }

    public List<List<PlayerStats>> topMultiple(String[] fields, int amount, int flip) {
        List<PriorityQueue<PlayerStats>> topLists = new ArrayList<>();

        for (String field : fields) { topLists.add(determineTopList(field, flip)); }

        List<List<PlayerStats>> returnLists = new ArrayList<>();

        for (PriorityQueue<PlayerStats> topList : topLists) {
            List<PlayerStats> returnList = new LinkedList<>();

            for (PlayerStats ps : allStats()) { topList.add(ps); }

            for (int i = 0; i < amount; i++) {
                while (topList.peek() != null && topList.peek().player.contains("-")) topList.poll();
                if (topList.peek() == null) break;
                PlayerStats top = topList.poll();
                returnList.add(top);
            }
            returnLists.add(returnList);
        }

        return returnLists;
    }

    public List<PlayerStats> top(String field, int amount) { return top(field, amount, 1); }

    public List<PlayerStats> bottom(String field, int amount) { return top(field, amount, -1); }

    public List<PlayerStats> top(String field, int amount, int flip) {
        return topMultiple(new String[] { field }, amount, flip).get(0);
    }

    private PriorityQueue<PlayerStats> determineTopList(String field, int flip) {
        if (field.equalsIgnoreCase("level"))
            return new PriorityQueue<>((a, b) -> flip * (a.getLevel() > b.getLevel() ? -1 : 1));
        if (field.equalsIgnoreCase("elo"))
            return new PriorityQueue<>((a, b) -> flip * (a.getEloScore() > b.getEloScore() ? -1 : 1));
        return new PriorityQueue<>((a, b) -> {
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

}