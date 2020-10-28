package kern;

import java.util.Date;

import org.bukkit.Bukkit;

public class PlayerStats {
    public String player;
    public double rating;
    public int playerKills;
    public int botKills;
    public int mobKills;
    public int animalKills;
    public int playerDeaths;
    public int envDeaths;
    public int wins;
    public int games;
    public String nemesis;
    String lastKilledBy;
    public int nemesisKills;
    int lastKilledByKills;
    long lastSeen;

    private static int K = 40;

    public PlayerStats(String player) {
        if (player != null) this.player = player.toLowerCase();
        else this.player = null;
        rating = 50;
        playerKills = botKills = mobKills = animalKills = playerDeaths = envDeaths = wins = games = nemesisKills = lastKilledByKills = 0;
        nemesis = lastKilledBy = null;
        seen();
    }

    public void reset() {
        rating = 50;
        playerKills = botKills = mobKills = animalKills = playerDeaths = envDeaths = wins = games = nemesisKills = lastKilledByKills = 0;
        nemesis = lastKilledBy = null;
    }

    public boolean isEmpty() {
        return rating == 50 && playerKills + botKills + mobKills + animalKills + playerDeaths + envDeaths + wins + games
                + nemesisKills + lastKilledByKills == 0;
    }

    public void seen() { lastSeen = new Date().getTime(); }

    public static PlayerStats newFromParse(String input) {
        String[] fields = input.split(":");
        if (fields.length < 2) throw new IllegalArgumentException("Requires at least 2 inputs!");

        PlayerStats ps = new PlayerStats(fields[0]);
        ps.rating = Double.parseDouble(fields[1]);
        if (fields.length == 2) return ps;
        if (fields.length < 14) throw new IllegalArgumentException("Requires at least 14 inputs!");

        ps.playerKills = Integer.parseInt(fields[2]);
        ps.botKills = Integer.parseInt(fields[3]);
        ps.mobKills = Integer.parseInt(fields[4]);
        ps.animalKills = Integer.parseInt(fields[5]);
        ps.playerDeaths = Integer.parseInt(fields[6]);
        ps.envDeaths = Integer.parseInt(fields[7]);
        ps.wins = Integer.parseInt(fields[8]);
        ps.games = Integer.parseInt(fields[9]);
        ps.nemesis = fields[10];
        ps.lastKilledBy = fields[11];
        ps.nemesisKills = Integer.parseInt(fields[12]);
        ps.lastKilledByKills = Integer.parseInt(fields[13]);
        if (fields.length == 14) return ps;
        // if (fields.length < 15) throw new IllegalArgumentException("Requires at least
        // 14 inputs!");

        ps.lastSeen = Long.parseLong(fields[14]);
        if (fields.length == 15) return ps;
        throw new IllegalArgumentException("Wrong amount of inputs");
    }

    public void loseTo(double opponentScore, String opponent) {

        if (opponent != null && !opponent.equalsIgnoreCase("YEUH-ANIMAL")
                && !opponent.equalsIgnoreCase("YEUH-MONSTER")) {
            if (lastKilledBy != null && lastKilledBy.equals(opponent)) lastKilledByKills++;
            else lastKilledByKills = 1;
            if (!opponent.equalsIgnoreCase("YEUH-BOT")) lastKilledBy = opponent;

            if ((nemesis == null || lastKilledByKills >= nemesisKills) && !opponent.equalsIgnoreCase("YEUH-BOT")) {
                nemesis = opponent;
                nemesisKills = lastKilledByKills;
            } else if (nemesis != null && nemesis.equals(opponent)) { nemesisKills++; }

            playerDeaths++;
        } else envDeaths++;

        games++;

        setRating(getRatingFromElo(getEloScore() - K * expected(opponentScore)));
    }

    public void winTo(double opponentScore, String opponent) {

        double multiplier = 1;
        if (opponent != null) {
            if (nemesis != null && opponent.equals(nemesis)) {
                multiplier *= 2;
                nemesisKills--;
                if (nemesisKills == 0) nemesis = null;
            }
            if (lastKilledBy != null && opponent.equals(lastKilledBy)) {
                lastKilledBy = null;
                lastKilledByKills = 0;
            }
            if (opponent.equalsIgnoreCase("YEUH-BOT")) botKills++;
            else if (opponent.equalsIgnoreCase("YEUH-MONSTER")) {
                mobKills++;
            } else if (opponent.equalsIgnoreCase("YEUH-ANIMAL")) {
                animalKills++;
            } else playerKills++;
        } else {
            wins++;
            games++;
        }

        setRating(getRatingFromElo(getEloScore() + multiplier * K * (1 - expected(opponentScore))));
    }

    private double expected(double opponentScore) {
        return (100 * rating - rating * opponentScore)
                / (100 * rating + 100 * opponentScore - 2 * rating * opponentScore);

    }

    public void environmentDeath() { loseTo(50, null); }

    public void winGame() { winTo(50, null); }

    public static double getRatingFromElo(double eloScore) { return 100 / (1 + Math.pow(10, (1000 - eloScore) / 400)); }

    public double getEloScore() { return 1000 - 400 * Math.log10(100 / rating - 1); }

    public double setRating(double newRating) {
        if (player == null) return 50;

        double oldScore = rating;
        rating = Math.max(0, Math.min(100, newRating));

        if (oldScore != rating) {
            try {
                Bukkit.getPlayerExact(player)
                        .sendMessage("Your Player Rating has been updated to \u00a76" + String.format("%.2f", rating));
            } catch (NullPointerException e) {}

            // Bukkit.getLogger().info(player + "'s new score is " + rating);
        }
        return rating;
    }

    public double addRating(double toAdd) { return setRating(rating + toAdd); }

    public String toString() {
        return player + ":" + rating + ":" + playerKills + ":" + botKills + ":" + mobKills + ":" + animalKills + ":"
                + playerDeaths + ":" + envDeaths + ":" + wins + ":" + games + ":" + nemesis + ":" + lastKilledBy + ":"
                + nemesisKills + ":" + lastKilledByKills + ":" + lastSeen;
    }

}