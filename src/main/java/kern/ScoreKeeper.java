package kern;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ScoreKeeper {

    private Map<String, Double> cache;
    private final double defaultValue = 50;
    private final double scale = 10;

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
            f = new File(YEUHLobby.getPlugin().getDataFolder(), "userScores.txt");
            f.createNewFile();
            s = new Scanner(f);
        } catch (IOException e) {
            Bukkit.getLogger().warning("[YEUHLobby] Something went wrong when opening userScores.txt!");
        }
    }

    public double getScore(String name, boolean create) throws PlayerDoesntExistException {

        if (cache.containsKey(name)) return cache.get(name);

        if (s == null) {
            cache.put(name, defaultValue);
            return defaultValue;
        }

        while (s.hasNextLine()) {
            String[] player = s.nextLine().split(":");

            double score = Double.parseDouble(player[1]);

            if (!cache.containsKey(player[0]))
                // if it finds other players while looking for one in specifics, it adds them to
                // the cache anyways
                cache.put(player[0], score);

            if (player[0].equals(name)) {
                cache.put(name, score);
                return score;
            }
        }

        if (create) {
            Bukkit.getLogger()
                    .info("[UhcCore] Creating user score for " + name + ". Default value: " + defaultValue + ".");
            cache.put(name, defaultValue);
            return defaultValue;
        }
        throw new PlayerDoesntExistException(name);
    }

    public double getScore(Player p) {
        try {
            return getScore(p.getName(), true);
        } catch (PlayerDoesntExistException e) {
            // shouldnt ever happen
            return -1;
        }
    }

    public double getScore(String name) throws PlayerDoesntExistException { return getScore(name, false); }

    public double setScore(String name, double score) throws PlayerDoesntExistException {
        // make sure it's in the cache first
        double oldScore = getScore(name);
        double filteredScore = Math.max(0, Math.min(100, score));

        cache.put(name, filteredScore);
        try {
            if (oldScore != filteredScore) Bukkit.getPlayerExact(name).sendMessage(
                    "Your Player Rating has been updated to \u00a76" + String.format("%.2f", filteredScore));
        } catch (NullPointerException e) {}
        Bukkit.getLogger().info(name + "'s new score is " + filteredScore);
        return filteredScore;
    }

    public double setScore(Player p, double score) {
        try {
            return setScore(p.getName(), score);
        } catch (PlayerDoesntExistException e) {
            return -1;
        }
    }

    public double addScore(String name, double score) throws PlayerDoesntExistException {
        return setScore(name, score + getScore(name));
    }

    public double addScore(Player p, double score) {
        try {
            return addScore(p.getName(), score);
        } catch (PlayerDoesntExistException e) {
            return -1;
        }
    }

    public double getScoreI(String name) throws PlayerDoesntExistException { return inverse(getScore(name)); }

    public double getScoreI(Player p) { return inverse(getScore(p)); }

    public double setScoreI(String name, double scoreI) throws PlayerDoesntExistException {
        return setScore(name, forward(scoreI));
    }

    public double setScoreI(Player p, double scoreI) { return setScore(p, forward(scoreI)); }

    public double addScoreI(String name, double scoreI) throws PlayerDoesntExistException {
        return setScore(name, forward(getScoreI(name) + scoreI));
    }

    public double addScoreI(Player p, double scoreI) { return setScore(p, forward(getScoreI(p) + scoreI)); }

    // write back to disk, should really only be done when plugin is disabled
    public void storeData() {
        Bukkit.getLogger().info("[UhcCore] Storing User Score data.");
        // finish the scanner
        while (s.hasNextLine()) {
            String[] player = s.nextLine().split(":");
            double score = Double.parseDouble(player[1]);

            if (!cache.containsKey(player[0])) cache.put(player[0], score);
        }
        s.close();

        // write everything in the cache
        try (FileWriter myWriter = new FileWriter(f, false)) {
            for (Entry<String, Double> e : cache.entrySet()) {
                if (e.getValue() != defaultValue) myWriter.write(e.getKey() + ":" + e.getValue() + "\n");
            }
            myWriter.close();
        } catch (IOException e) {
            Bukkit.getLogger().warning("[UhcCore] Something went wrong when writing to userScores.txt!");
        }

        resetScanner();
        cache.clear();
    }

    private double Prob(double x) { return 1 / (1 + Math.pow(10, x / 400)); }

    private double forward(double x) { return 100 * Prob(1000 - x); }

    private double inverse(double x) { return 1000 - 400 * Math.log10(100 / x - 1); }

    public void updateScores(Player winner, Player loser) {
        double A = getScoreI(winner);
        double B = getScoreI(loser);

        double P = Prob(B - A);

        double diff = scale * (P - 1);

        setScore(winner, forward(A + diff));
        setScore(loser, forward(B - diff));
    }

    public void envDie(Player loser) { setScore(loser, forward(getScoreI(loser) - scale * getScore(loser) / 100)); }

    public void envWin(Player winner) { setScore(winner, forward(getScoreI(winner) + scale * getScore(winner) / 100)); }

}