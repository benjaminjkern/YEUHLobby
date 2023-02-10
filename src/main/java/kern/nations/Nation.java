package kern.nations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.bukkit.Chunk;
import org.bukkit.Location;

import kern.nations.perks.NationPerk;

public class Nation {
    String color;
    public String name;
    double bank;
    double taxRate;

    public Set<Nation> allies;
    public Set<Nation> enemies;
    public Set<NationPerk> perks;

    public City capitol;

    Set<NationPlayer> citizens;
    public Set<City> cities;
    Set<Chunk> ownedLand;

    private static String randomColor() {
        return (new String[] { "1", "2", "3", "4", "5", "6", "7" })[(int) (Math.random() * 7)];
    }

    public String getColor() { return getColor(false); }

    public String getColor(boolean light) {
        if (!light) return color;
        return "\u00a7" + Integer.toHexString(Integer.parseInt(color.substring(1, 2)) + 8);
    }

    public Location getSpawn() { return capitol.spawn; }

    public City getCity(String cityName) {
        for (City city : cities) { if (city.name.equalsIgnoreCase(cityName)) return city; }
        return null;
    }

    public Nation(NationPlayer leader, String name, String capitolName) {
        this.name = name;

        leader.nations.add(this);

        color = "\u00a7" + randomColor();
        bank = 0;
        taxRate = 0.1;

        ownedLand = new HashSet<>();

        citizens = new HashSet<>();
        citizens.add(leader);

        cities = new HashSet<>();
        capitol = new City(this, leader, capitolName);
        cities.add(capitol);

        allies = new HashSet<>();
        enemies = new HashSet<>();
        perks = new HashSet<>();

        NationManager.nations.add(this);

        updateLand();
    }

    // public void annexNation(Nation otherNation, String newName) {
    // if (newName != null) name = newName;

    // // allies.addAll(otherNation.allies);
    // // enemies.addAll(otherNation.enemies);

    // allies.remove(otherNation);
    // enemies.remove(otherNation);
    // NationManager.nations.remove(otherNation);

    // perks.addAll(otherNation.perks);

    // cities.addAll(otherNation.cities);
    // for (City city : otherNation.cities) { city.nation = this; }

    // citizens.addAll(otherNation.citizens);
    // for (NationPlayer citizen : citizens) {
    // citizen.nations.remove(otherNation);
    // citizen.nations.add(this);
    // citizen.addNews(getColoredName() + " has annexed " +
    // otherNation.getColoredName() + "!");
    // citizen.addNews(getColoredName() + " gained " + otherNation.ownedLand.size()
    // + " new chunks!");
    // citizen.addNews(getColoredName() + " gained " + otherNation.cities.size() + "
    // new cities!");
    // for (City city : otherNation.cities) { citizen.addNews(" - " +
    // city.getColoredName()); }
    // }

    // updateLand();
    // }

    public void declareEnemy(Nation otherNation) {
        if (enemies.contains(otherNation)) return;
        enemies.add(otherNation);
        otherNation.enemies.add(this);

        Set<NationPlayer> sentDeclareMessage = new HashSet<>();

        for (NationPlayer citizen : getCitizens()) {
            citizen.addNews(getColoredName() + " has declared war on " + otherNation.getColoredName() + "!");
            sentDeclareMessage.add(citizen);
            if (citizen.nations.contains(otherNation)) {
                citizen.addNews("You are a citizen of both nations! You may want to sort that out soon!");
            }
        }

        for (Nation ally : allies) {
            if (!ally.allies.contains(otherNation)) {
                for (NationPlayer allyCitizen : ally.getCitizens()) {
                    if (!sentDeclareMessage.contains(allyCitizen)) allyCitizen.addNews("Our ally, " + getColoredName()
                            + ", has declared war on " + (ally.enemies.contains(otherNation) ? "our enemy, " : "")
                            + otherNation.getColoredName() + "!");
                    sentDeclareMessage.add(allyCitizen);
                }
            } else {
                for (NationPlayer allyCitizen : ally.getCitizens()) {
                    allyCitizen.addNews("Our ally, " + getColoredName() + ", has declared war on our other ally, "
                            + otherNation.getColoredName() + "!");
                    allyCitizen.addNews("As a result, our allyship has been halted for both nations.");
                }
                ally.allies.remove(otherNation);
            }
        }
        allies.removeIf(ally -> ally.allies.contains(otherNation));

        for (Nation enemyAlly : otherNation.allies) {
            for (NationPlayer enemyAllyCitizen : enemyAlly.getCitizens()) {
                if (!sentDeclareMessage.contains(enemyAllyCitizen)) enemyAllyCitizen.addNews(
                        getColoredName() + " has declared war on our ally, " + otherNation.getColoredName() + "!");
                sentDeclareMessage.add(enemyAllyCitizen);
            }
        }

        for (NationPlayer citizen : otherNation.getCitizens()) {
            if (!sentDeclareMessage.contains(citizen))
                citizen.addNews(getColoredName() + " has declared war on " + otherNation.getColoredName() + "!");
            // sentDeclareMessage.add(enemyAllyCitizen);
            if (allies.contains(otherNation)) citizen.addNews(getColoredName() + " broke the treaty!");
        }

        allies.remove(otherNation);
    }

    public void distributeTaxesToCities() {
        double neededTaxes = 0;
        for (City city : cities) { neededTaxes += city.neededTaxes(); }
        if (neededTaxes <= bank) {
            bank -= neededTaxes;
            return;
        }

        double taxAmount = bank / cities.size();
        bank = 0;

        for (City city : cities) {
            city.bank += taxAmount;
            city.dealWithTaxes();
        }
    }

    public boolean touchingNation(Chunk chunk) {
        return NationManager.getDirectChunkNeighbors(chunk).stream().anyMatch(neighbor -> ownedLand.contains(neighbor));
    }

    public String getMapMarker() { return getMapMarker(false); }

    public String getMapMarker(boolean light) { return getColor(light) + name.substring(0, 1); }

    public String getColoredName() { return getColor() + name + "\u00a7r"; }

    public String bankText() { return NationManager.CURRENCY + bank; }

    public int distToClosestCity(Chunk chunk) {
        int dist = Integer.MAX_VALUE;
        for (City city : cities) {
            for (Chunk cityChunk : city.ownedLand) {
                int chunkDist = Math.max(Math.abs(chunk.getX() - cityChunk.getX()),
                        Math.abs(chunk.getZ() - cityChunk.getZ()));
                if (chunkDist < dist) dist = chunkDist;
                if (dist == 0) return 0;
            }
        }
        return dist;
    }

    public double distRatio(Chunk chunk) {
        double ratio = Double.MAX_VALUE;
        for (City city : cities) {
            for (Chunk cityChunk : city.ownedLand) {
                int dist = Math.max(Math.abs(chunk.getX() - cityChunk.getX()),
                        Math.abs(chunk.getZ() - cityChunk.getZ()));
                if (dist == 0) return Double.MAX_VALUE;
                double chunkRatio = (double) city.ownedLand.size() / dist;
                if (chunkRatio > ratio) ratio = chunkRatio;
            }
        }
        return ratio;
    }

    public double happiness() {
        if (cities.isEmpty()) return 0;
        double sum = 0;
        for (City city : cities) { sum += city.happiness; }
        return sum / cities.size();
    }

    public void updateLand() {
        if (cities.isEmpty()) {
            removeNation();
            return;
        }
        ownedLand = new HashSet<>();

        Map<Chunk, Set<Chunk>> distMap = new HashMap<>();
        for (City city : cities) {
            for (Chunk chunk : city.ownedLand) {
                distMap.put(chunk, new HashSet<>());
                distMap.get(chunk).add(chunk);
            }
        }

        Set<Chunk> changes = new HashSet<>();
        Map<Chunk, Set<Chunk>> otherMap = new HashMap<>();

        for (int i = 1; i <= 3; i++) {
            otherMap.clear();
            for (Entry<Chunk, Set<Chunk>> entry : distMap.entrySet()) {
                otherMap.put(entry.getKey(), new HashSet<>(entry.getValue()));
            }
            for (Entry<Chunk, Set<Chunk>> entry : otherMap.entrySet()) {
                for (Chunk neighbor : NationManager.getAllChunkNeighbors(entry.getKey())) {
                    if (!distMap.containsKey(neighbor)) { distMap.put(neighbor, new HashSet<>()); }

                    distMap.get(neighbor).addAll(entry.getValue());
                }
            }
            for (Entry<Chunk, Set<Chunk>> entry : distMap.entrySet()) {
                if (entry.getValue().size() >= i * (i + 1) / 2) changes.add(entry.getKey());
            }
        }

        distMap = new HashMap<>();
        while (!changes.isEmpty()) {
            changes.forEach(chunk -> {
                Nation nation = NationManager.getNationFromChunk(chunk);
                if (nation == null) {
                    ownedLand.add(chunk);
                    return;
                }
                if (distRatio(chunk) > nation.distRatio(chunk)) {
                    ownedLand.add(chunk);
                    nation.ownedLand.remove(chunk);
                }
            });
            changes = new HashSet<>();

            for (int i = 1; i <= 3; i++) {
                for (Chunk chunk : ownedLand) {
                    for (Chunk neighbor : NationManager.getAllChunkNeighbors(chunk)) {
                        if (ownedLand.contains(neighbor)) continue;

                        if (!distMap.containsKey(neighbor)) distMap.put(neighbor, new HashSet<>());

                        distMap.get(neighbor).add(chunk);
                    }
                }

                for (Entry<Chunk, Set<Chunk>> entry : distMap.entrySet()) {
                    if (entry.getValue().size() >= 2 * i * (i + 1) + 1) { changes.add(entry.getKey()); }
                }
            }
        }
    }

    public void removeNation() {
        for (Nation ally : allies) { ally.allies.remove(this); }
        for (Nation enemy : enemies) { enemy.enemies.remove(this); }
        for (NationPlayer citizen : getCitizens()) {
            citizen.nations.remove(this);
            citizen.addNews("The nation of " + getColoredName() + " has fallen.");
        }
        for (City city : cities) { city.remove(); }
        NationManager.nations.remove(this);
    }

    public boolean canTravel(NationPlayer nationPlayer) {
        return isPlayerCitizen(nationPlayer) || allies.stream().anyMatch(ally -> ally.isPlayerCitizen(nationPlayer));
    }

    public Set<NationPlayer> getCitizens() {
        Set<NationPlayer> allCitizens = new HashSet<>();
        for (City city : cities) { allCitizens.addAll(city.citizens); }
        return allCitizens;
    }

    public boolean isPlayerCitizen(NationPlayer nationPlayer) { return citizens.contains(nationPlayer); }

    public NationPlayer getLeader() { return capitol.mayor; }

}
