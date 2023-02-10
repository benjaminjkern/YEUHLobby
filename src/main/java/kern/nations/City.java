package kern.nations;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;

public class City {
    public Nation nation;
    NationPlayer mayor;
    public String name;
    double bank;

    double happiness;

    Set<Set<Chunk>> claimRequests;

    Set<Chunk> ownedLand;
    Set<NationPlayer> citizens;
    public Location spawn;

    boolean abandoned;

    public City(Nation nation, NationPlayer mayor, String name) {
        this.name = name;
        this.nation = nation;
        this.mayor = mayor;

        ownedLand = new HashSet<>();
        spawn = mayor.player.getLocation();
        ownedLand.add(spawn.getChunk());

        citizens = new HashSet<>();
        citizens.add(mayor);
        happiness = 0;

        nation.cities.add(this);
        nation.updateLand();
    }

    public String getColoredName() { return "\u00a7" + nation.getColor(true) + name + "\u00a7r"; }

    public String bankText() { return NationManager.CURRENCY + bank; }

    public Set<Chunk> getOutsideChunks() {
        Set<Chunk> outsideChunks = new HashSet<>();
        for (Chunk chunk : ownedLand) {
            if (NationManager.getDirectChunkNeighbors(chunk).stream()
                    .anyMatch(neighbor -> !ownedLand.contains(neighbor)))
                outsideChunks.add(chunk);
        }
        return outsideChunks;
    }

    public String getMapMarker() { return nation.getColor(true) + name.substring(0, 1); }

    public double neededTaxes() { return ownedLand.size() + citizens.size(); }

    public void dealWithTaxes() {
        double neededTaxes = neededTaxes();
        if (bank >= neededTaxes) {
            bank -= neededTaxes;
            happiness++;
            return;
        }
        happiness--;
        double keepChance = bank / neededTaxes;
        bank = 0;

        if (happiness < 0) {
            int lostChunks = 0;
            for (Chunk outsideChunk : getOutsideChunks()) {
                if (Math.random() > keepChance) {
                    ownedLand.remove(outsideChunk);
                    lostChunks++;
                }
            }

            mayor.addNews("Your city, " + getFullName() + ", has lost " + lostChunks + " chunks!");

            if (ownedLand.isEmpty()) { abandonCity(); }
        }
    }

    public void abandonCity() {
        ownedLand.clear();
        citizens.forEach(
                citizen -> citizen.addNews("The city of " + getFullName() + " has been completely abandoned."));
        nation.bank += bank;
        nation.cities.remove(this);
        nation.updateLand();
    }

    public void removeChunk(Chunk chunk) {
        ownedLand.remove(chunk);
        if (ownedLand.isEmpty()) { remove(); }
    }

    public boolean touchingCity(Chunk chunk) {
        return NationManager.getDirectChunkNeighbors(chunk).stream().anyMatch(neighbor -> ownedLand.contains(neighbor));
    }

    public void remove() {
        if (nation != null) nation.cities.remove(this);
        nation = null;
    }

    public String getFullName() { return getColoredName() + ", " + nation.getColoredName(); }

}
