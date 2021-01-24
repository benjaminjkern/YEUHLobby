package kern;

import java.util.Date;

import org.bukkit.Bukkit;

import kern.listeners.PlayerListener;

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
    public double experience;
    public int rank;

    // private static int K = 40;
    public static final double K_KILL = 13.899276855538119;
    public static final double K_GAME = 7.414368802344478;

    private static final double RANK_CONST = Math.log10(9) * 400;

    public static double K(int total) { return 800 * Math.log10(1.5) / (double) (total - 1); }

    public PlayerStats(String player) {
        if (player != null) this.player = player.toLowerCase();
        else this.player = null;
        rating = 50;
        playerKills = botKills = mobKills = animalKills = playerDeaths = envDeaths = wins = games = nemesisKills = lastKilledByKills = 0;
        nemesis = lastKilledBy = null;
        experience = 0;
        rank = 0;
        seen();
    }

    public void reset() {
        rating = 50;
        playerKills = botKills = mobKills = animalKills = playerDeaths = envDeaths = wins = games = nemesisKills = lastKilledByKills = 0;
        nemesis = lastKilledBy = null;
        experience = 0;
        rank = 0;
    }

    public boolean isEmpty() {
        return rating == 50 && playerKills + botKills + mobKills + animalKills + playerDeaths + envDeaths + wins + games
                + nemesisKills + lastKilledByKills + experience == 0;
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

        ps.lastSeen = Long.parseLong(fields[14]);
        if (fields.length == 15) return ps;

        ps.experience = Double.parseDouble(fields[15]);
        if (fields.length == 16) return ps;

        ps.rank = Integer.parseInt(fields[16]);
        if (fields.length == 17) return ps;

        throw new IllegalArgumentException("Wrong amount of inputs");
    }

    public void loseTo(double opponentElo, String opponent, double K) {
        // shouldn't ever call with yeuh-animal or monster
        if (opponent != null && !opponent.equalsIgnoreCase("YEUH-ANIMAL")
                && !opponent.equalsIgnoreCase("YEUH-MONSTER")) {
            if (!opponent.equalsIgnoreCase("YEUH-BOT")) {
                if (lastKilledBy != null && lastKilledBy.equals(opponent)) lastKilledByKills++;
                else lastKilledByKills = 1;

                lastKilledBy = opponent;

                if (nemesis == null || lastKilledByKills >= nemesisKills) {
                    nemesis = opponent;
                    nemesisKills = lastKilledByKills;
                } else if (nemesis.equals(opponent)) nemesisKills++;
            }
            playerDeaths++;
        } else envDeaths++;

        setRating(getRatingFromElo(getEloScore() - K * expected(opponentElo)));
    }

    public void winTo(double opponentElo, String opponent, double K) {

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
                return;
            } else if (opponent.equalsIgnoreCase("YEUH-ANIMAL")) {
                animalKills++;
                return;
            } else {
                playerKills++;
                experience += YEUHLobby.getScoreKeeper().getStats(opponent).getLevel() * 10;
            }
        }

        setRating(getRatingFromElo(getEloScore() + multiplier * K * (1 - expected(opponentElo))));
    }

    public void setRank(int rank) {
        if (this.rank == rank) return;
        if (rank > this.rank) PlayerListener.upRank(player);
        else PlayerListener.downRank(player);
        setRating(getRatingFromElo(getEloScore(), rank));
        this.rank = rank;
    }

    public int getLevel() { return (int) Math.pow(experience / 50, 1 / 3.) + 1; }

    public double expected(double opponentElo) { return 1 / (1 + Math.pow(10, (opponentElo - getEloScore()) / 400.)); }

    public double getRatingFromElo(double eloScore) { return getRatingFromElo(eloScore, rank); }

    public static double getRatingFromElo(double eloScore, int sampleRank) {
        return 100 / (1 + Math.pow(10, ((1000 + sampleRank * RANK_CONST) - eloScore) / 400));
    }

    public double getEloScore() { return (1000 + rank * RANK_CONST) - 400 * Math.log10(100 / rating - 1); }

    public double setRating(double newRating) {
        if (player == null) return 50;

        double oldScore = rating;
        rating = Math.max(0, Math.min(100, newRating));

        int oldLevel = getLevel();
        if (rating > oldScore) experience += rating - oldScore;
        if (getLevel() > oldLevel) PlayerListener.sendLevelUpMessage(player);

        if (rating < 10 && rank > 0) setRank(rank - 1);
        if (rating >= 90) setRank(rank + 1);

        if (oldScore != rating) {
            try {
                Bukkit.getPlayerExact(player)
                        .sendMessage("Your Player Rating has been updated to \u00a76" + ratingString());
            } catch (NullPointerException e) {}
        }
        return rating;
    }

    public double addRating(double toAdd) { return setRating(rating + toAdd); }

    public String rankingColor() {
        switch (rank) {
            case 1:
                return "\u00a7c";
            case 2:
                return "\u00a7a";
            case 3:
                return "\u00a7b";
            case 4:
                return "\u00a75";
            case 5:
                return "\u00a7d\u00a7l";
            default:
                return "\u00a76";
        }
    }

    public String getRankString() {
        switch (rank) {
            case 1:
                return "Veteran";
            case 2:
                return "Elite";
            case 3:
                return "Supreme";
            case 4:
                return "Master";
            case 5:
                return "Legendary";
            default:
                return "Regular";
        }

    }

    public String ratingString() { return String.format(rankingColor() + "%.2f", rating); }

    public String toString() {
        return player + ":" + rating + ":" + playerKills + ":" + botKills + ":" + mobKills + ":" + animalKills + ":"
                + playerDeaths + ":" + envDeaths + ":" + wins + ":" + games + ":" + nemesis + ":" + lastKilledBy + ":"
                + nemesisKills + ":" + lastKilledByKills + ":" + lastSeen + ":" + experience + ":" + rank;
    }

}