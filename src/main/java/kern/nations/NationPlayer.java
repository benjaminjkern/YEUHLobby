package kern.nations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class NationPlayer {
    public String name;
    public Player player;
    public double money;
    public Set<Nation> nations;

    public Chunk toChunk;
    public String outputText;
    public Runnable promptCallback;
    public Runnable claimCallback;
    public Runnable walkCallback;

    private List<String> news;

    public NationPlayer(Player player) {
        if (!player.isOnline()) return;
        this.name = player.getName();
        this.player = player;

        money = 0;
        nations = new HashSet<>();
        news = new ArrayList<>();

        NationManager.players.add(this);
    }

    public Nation getCurrentNation() { return NationManager.getNationFromChunk(player.getLocation().getChunk()); }

    public City getCurrentCity() { return NationManager.getCityFromChunk(player.getLocation().getChunk()); }

    public Nation nationLeader() {
        for (Nation nation : nations) { if (nation.getLeader() == this) return nation; }
        return null;
    }

    // returns closest
    public Nation getNation(String nationName) {
        Nation closestNation = null;
        for (Nation nation : nations) {
            if (!nation.name.equalsIgnoreCase(nationName)) continue;
            if (closestNation == null || nation.getSpawn().distanceSquared(player.getLocation()) < closestNation
                    .getSpawn().distanceSquared(player.getLocation())) {
                closestNation = nation;
            }
        }
        return closestNation;
    }

    // returns closest
    public City getCity(String cityName) {
        City closestCity = null;
        for (Nation nation : nations) {
            for (City city : nation.cities) {
                if (!city.name.equalsIgnoreCase(cityName) || !city.citizens.contains(this)) continue;
                if (closestCity == null || city.spawn.distanceSquared(player.getLocation()) < closestCity.spawn
                        .distanceSquared(player.getLocation())) {
                    closestCity = city;
                }
            }
        }
        return closestCity;
    }

    // commands that users can do

    private boolean verifyCanCreateNation() {
        Nation nationLeader = nationLeader();
        if (nationLeader != null) {
            player.sendMessage("\u00a7cYou are already the leader of the nation of \u00a7f" + nationLeader.name
                    + "\u00a7c! You cannot found a new nation!");
            return false;
        }

        Nation currentNation = getCurrentNation();
        if (currentNation == null) return true;
        player.sendMessage("\u00a7cYou cannot create a nation here! This land is owned by \u00a7f" + currentNation.name
                + "\u00a7c!");
        return false;
    }

    public void createNation() {
        if (!verifyCanCreateNation()) return;

        player.sendMessage("What would you like your new nation to be called? (Type in chat)");
        promptCallback = () -> createNation(outputText);
    }

    public void createNation(String nationName) {

        if (!verifyCanCreateNation()) return;

        player.sendMessage("\u00a77\u00a7l" + nationName + "\u00a7f? What a weird name for a nation!");
        player.sendMessage("What would you like the capitol of your new nation to be called? (Type in chat)");
        promptCallback = () -> createNation(nationName, outputText);
    }

    public void createNation(String nationName, String cityName) {
        if (!verifyCanCreateNation()) return;

        if (nationName == null || nationName.isEmpty()) {
            player.sendMessage("\u00a7cThe Nation must have a real name!");
            return;
        }
        if (cityName == null || cityName.isEmpty()) {
            player.sendMessage("\u00a7cThe Capitol City must have a real name!");
            return;
        }

        Nation nation = new Nation(this, nationName, cityName);
        player.sendMessage("You have created the great nation of " + nation.getColoredName() + " with the capitol "
                + nation.capitol.getColoredName());
    }

    private boolean verifyCanCreateCity() {
        if (nations.isEmpty()) {
            player.sendMessage(
                    "\u00a7cYou are not a citizen of any nation! You must either join a nation or use \u00a7f/nation create \u00a7c to start your own and create new cities!");
            return false;
        }

        Nation currentNation = getCurrentNation();
        if (currentNation == null) return true;

        if (!currentNation.isPlayerCitizen(this)) {
            player.sendMessage("\u00a7cYou cannot create a city here! This land is owned by \u00a7f"
                    + currentNation.name + "\u00a7f!");
            return false;
        }

        City currentCity = getCurrentCity();
        if (currentCity == null) return true;

        player.sendMessage("\u00a7cYou cannot create a city here! You are already in the city of \u00a7f"
                + currentCity.name + "\u00a7f!");
        return false;
    }

    public void createCity() {
        if (!verifyCanCreateCity()) return;

        Nation currentNation = getCurrentNation();
        if (currentNation == null) {
            if (nations.size() > 1) {
                player.sendMessage("Which nation do you want this city to be a part of? (Type in chat)");
                promptCallback = () -> {
                    Nation nation = getNation(outputText);
                    if (nation == null) {
                        player.sendMessage(
                                "\u00a7cYou are not part of any nation named \u00a7f" + outputText + "\u00a7c.");
                    } else createCity(nation);
                };
            } else {
                createCity(nations.iterator().next());
            }
            return;
        }
        createCity(currentNation);
    }

    public void createCity(Nation cityNation) {
        if (!verifyCanCreateCity()) return;

        player.sendMessage("What would you like your new city to be called? (Type in chat)");
        promptCallback = () -> createCity(outputText, cityNation);
    }

    public void createCity(String cityName) {
        if (!verifyCanCreateCity()) return;

        Nation currentNation = getCurrentNation();
        if (currentNation == null) {
            if (nations.size() > 1) {
                player.sendMessage("Which nation do you want this city to be a part of? (Type in chat)");
                promptCallback = () -> createCity(cityName, outputText);
            } else {
                createCity(cityName, nations.iterator().next());
            }
            return;
        }
        createCity(cityName, currentNation);
    }

    public void createCity(String cityName, String nationName) {
        if (!verifyCanCreateCity()) return;

        Nation nation = getNation(nationName);
        if (nation == null)
            player.sendMessage("\u00a7cYou are not part of any nation named \u00a7f" + nationName + "\u00a7c.");
        else createCity(cityName, nation);
    }

    public void createCity(String cityName, Nation cityNation) {
        if (!verifyCanCreateCity()) return;

        if (cityName == null || cityName.isEmpty()) {
            player.sendMessage("\u00a7cThe city must have a real name!");
            return;
        }
        if (cityNation == null) {
            player.sendMessage("\u00a7cThat's not a real nation!");
            return;
        }
        if (!cityNation.isPlayerCitizen(this)) {
            player.sendMessage("\u00a7cYou are not a citizen of the nation of \u00a7f" + cityNation.name + "\u00a7c!");
            return;
        }

        City city = new City(cityNation, this, cityName);
        player.sendMessage("You have created the great city of " + city.getFullName() + ".");
    }

    private boolean verifyCanClaimChunks() {
        if (nations.isEmpty()) {
            player.sendMessage(
                    "\u00a7cYou are not a citizen of any nation! You cannot claim land unless you are a member of a city within a nation!");
            player.sendMessage(
                    "\u00a7cUse \u00a7f/nation create\u00a7c to create a new nation, or use \u00a7f/nation join \u00a7cto join an existing nation!");
            return false;
        }
        if (nations.stream()
                .allMatch(nation -> nation.cities.stream().allMatch(city -> !city.citizens.contains(this)))) {
            player.sendMessage(
                    "\u00a7cYou are not a member of any city! You cannot claim land unless you are a member of a city!");
            player.sendMessage(
                    "\u00a7cUse \u00a7f/nation createcity\u00a7c to create a new nation, or use \u00a7f/nation joincity \u00a7cto join an existing city!");
            return false;
        }

        Nation currentNation = getCurrentNation();
        if (currentNation == null) return true;
        if (!currentNation.isPlayerCitizen(this)) {
            player.sendMessage("\u00a7cYou cannot claim land here! This land is owned by the nation of \u00a7f"
                    + currentNation.name + "\u00a7c!");
            return false;
        }

        City currentCity = getCurrentCity();
        if (currentCity == null || currentCity.citizens.contains(this)) return true;
        player.sendMessage("\u00a7cYou cannot claim land here! This land is owned by the city of\u00a7f"
                + currentCity.name + "\u00a7c!");
        return false;
    }

    public void claimChunks() {
        if (!verifyCanClaimChunks()) return;
        if (claimCallback != null) {
            claimCallback.run();
            return;
        }

        City currentCity = getCurrentCity();

        if (currentCity != null) {
            claimChunks(currentCity);
            return;
        }

        Chunk currentChunk = player.getLocation().getChunk();
        Set<City> possibleCities = new HashSet<>();
        for (Chunk neighbor : NationManager.getDirectChunkNeighbors(currentChunk)) {
            City neighborCity = NationManager.getCityFromChunk(neighbor);
            if (neighborCity != null && neighborCity.citizens.contains(this)) possibleCities.add(neighborCity);
        }
        if (possibleCities.isEmpty()) {
            player.sendMessage("\u00a7cThere are no cities nearby that you can claim land for!");
            return;
        }
        if (possibleCities.size() == 1) {
            claimChunks(possibleCities.iterator().next());
            return;
        }
        player.sendMessage("\u00a7cThere are multiple cities nearby that you are a citizen of!");
        player.sendMessage("\u00a7cPlease move into the city you wish to claim land for!");

    }

    public void claimChunks(String cityName) {
        if (!verifyCanClaimChunks()) return;

        if (claimCallback != null) {
            claimCallback.run();
            return;
        }

        City city = getCity(cityName);
        if (city == null) {
            player.sendMessage(
                    "\u00a7cYou are not a member of any city by the name of \u00a7f" + cityName + "\u00a7c!");
            return;
        }
        claimChunks(city);
    }

    public void claimChunks(City claimCity) {
        if (!verifyCanClaimChunks()) return;

        if (claimCallback != null) {
            claimCallback.run();
            return;
        }
        if (claimCity == null || !claimCity.citizens.contains(this)) {
            // This should never happen
            player.sendMessage("\u00a7cYou must select a city that you are a member of!");
            return;
        }

        Set<Chunk> claimedChunks = new HashSet<>();
        player.sendMessage("Now claiming land for the city: " + claimCity.getColoredName());
        player.sendMessage(
                "Move into different chunks to mark them, then use \u00a7d/nation claim \u00a7fagain to claim them!");

        if (getCurrentCity() == null) tryToClaimChunk(player.getLocation().getChunk(), claimCity, claimedChunks);
        walkCallback = () -> tryToClaimChunk(toChunk, claimCity, claimedChunks);
        claimCallback = () -> finishClaim(claimCity, claimedChunks);
    }

    public void tryToClaimChunk(Chunk chunk, City claimCity, Set<Chunk> claimedChunks) {
        Nation currentNation = getCurrentNation();
        City currentCity = getCurrentCity();

        if (claimedChunks.stream()
                .allMatch(claimedChunk -> Math.abs(claimedChunk.getX() - chunk.getX())
                        + Math.abs(claimedChunk.getZ() - chunk.getZ()) > 1)
                && claimCity.ownedLand.stream().allMatch(claimedChunk -> Math.abs(claimedChunk.getX() - chunk.getX())
                        + Math.abs(claimedChunk.getZ() - chunk.getZ()) > 1)) {
            player.sendMessage(
                    "\u00a7cThis chunk is not connected to the city or the chunks you are current claiming! Please return to continue claiming chunks for the city of \u00a7f"
                            + claimCity.name + "\u00a7c!");
            player.sendMessage("Use \u00a7d/nation claim \u00a7fto finish claiming!");
            return;
        }
        if (currentNation != null && claimCity.nation != currentNation) {
            player.sendMessage(
                    "\u00a7cThis is the nation of \u00a7f" + currentNation.name + "\u00a7c! You cannot claim here!");
            return;
        }
        if (currentCity == null) {
            if (claimedChunks.contains(chunk)) {
                player.sendMessage("\u00a7cYou already selected this chunk!");
            } else {
                claimedChunks.add(chunk);
                player.sendMessage("Total chunks selected: \u00a7d" + claimedChunks.size());
            }
        } else {
            player.sendMessage("\u00a7cThis is the city of \u00a7f" + currentCity.name
                    + "\u00a7c! You cannot claim new land here!");
        }
    }

    public void finishClaim(City claimCity, Set<Chunk> claimedChunks) {
        claimCallback = null;
        walkCallback = null;

        if (claimCity.mayor != this) {
            player.sendMessage("Sent a request to claim \u00a7d" + claimedChunks.size()
                    + " \u00a7fchunks for the city of " + claimCity.getFullName() + ".");
            claimCity.claimRequests.add(claimedChunks);
            return;
        }

        double price = claimedChunks.size();

        if (claimCity.bank >= price) {
            claimCity.bank -= price;
        } else if (claimCity.bank + money >= price) {
            money -= price - claimCity.bank;
            claimCity.bank = 0;
        } else {
            player.sendMessage("\u00a7cYou do not have enough money to claim \u00a7f" + claimedChunks.size()
                    + " \u00a7cchunks for the city of \u00a7f" + claimCity.name + "\u00a7c.");
            player.sendMessage("  \u00a7cRequired amount: \u00a7f" + NationManager.CURRENCY + price);
            player.sendMessage("  \u00a7cYour money: \u00a7f" + NationManager.CURRENCY + money);
            player.sendMessage(
                    "  \u00a7cCity of \u00a7f" + claimCity.name + "\u00a7c bank: \u00a7f" + claimCity.bankText());
            return;
        }
        player.sendMessage("You claimed \u00a7d" + claimedChunks.size() + " \u00a7fchunks for the city of "
                + claimCity.getColoredName());

        claimCity.ownedLand.addAll(claimedChunks);
        claimCity.nation.updateLand();
    }

    public void setSpawn() {
        City city = getCurrentCity();
        if (city == null) {
            player.sendMessage("\u00a7cMust be standing in a city to set the spawn!");
            return;
        }

        if (city.mayor != this) {
            player.sendMessage("\u00a7cMust be the mayor of the city to set the spawn!");
            return;
        }

        city.spawn = player.getLocation();
        player.sendMessage("Set the spawn point for the city of " + city.getColoredName() + ".");
    }

    public void teleport() {
        if (nations.size() == 1) {
            City city = nations.iterator().next().capitol;
            player.teleport(city.spawn);
            player.sendMessage("You travelled to " + city.getFullName() + ".");
        } else {
            player.sendMessage("Please specify where you wish to travel with \u00a7d/nation tp [Location]\u00a7f!");
        }
    }

    public void teleport(String location) {
        List<City> possibleCities = new ArrayList<>();
        List<City> disallowedCities = new ArrayList<>();

        for (Nation nation : nations) {
            for (City city : nation.cities) {
                if (!city.name.equalsIgnoreCase(location)) continue;
                // this should be redundant here but whatever
                if (nation.canTravel(this)) possibleCities.add(city);
                else disallowedCities.add(city);
            }
        }
        if (possibleCities.isEmpty()) {
            for (Nation nation : NationManager.nations) {
                for (City city : nation.cities) {
                    if (!city.name.equalsIgnoreCase(location)) continue;
                    if (nation.canTravel(this)) possibleCities.add(city);
                    else disallowedCities.add(city);
                }
            }
        }
        if (possibleCities.isEmpty()) {
            for (Nation nation : NationManager.nations) {
                if (nation.name.equalsIgnoreCase(location)) {
                    if (nation.canTravel(this)) possibleCities.add(nation.capitol);
                    else disallowedCities.add(nation.capitol);
                }
            }
        }
        if (possibleCities.isEmpty()) {
            if (disallowedCities.isEmpty()) {
                player.sendMessage(
                        "\u00a7cDid not find any cities or nations by the name of \u00a7f" + location + "\u00a7c!");
            } else {
                player.sendMessage(
                        "\u00a7cDid not find any cities or nations that you are allowed to travel to by the name of \u00a7f"
                                + location + "\u00a7c!");
            }
        } else if (possibleCities.size() == 1) {
            teleport(possibleCities.get(0));
        } else {
            player.sendMessage(
                    "Please specify which city you would like to travel to using \u00a7d/nation teleport [City Name] [Nation Name]\u00a7f.");
            for (City city : possibleCities) { player.sendMessage(" - " + city.getFullName() + "."); }
            for (City city : disallowedCities) {
                player.sendMessage(" \u00a7c- \u00a7f" + city.name + "\u00a7c, \u00a7f" + city.nation.name
                        + "\u00a7c. (You do not have permission to travel here)");
            }
        }
    }

    public void teleport(String cityName, String nationName) {

        City closestCity = null;

        for (Nation nation : NationManager.nations) {
            if (nation.name.equalsIgnoreCase(nationName)) {
                for (City city : nation.cities) {
                    if (!city.name.equalsIgnoreCase(cityName) || !nation.canTravel(this)) continue;
                    if (closestCity == null || city.spawn.distanceSquared(player.getLocation()) < closestCity.spawn
                            .distanceSquared(player.getLocation()))
                        closestCity = city;
                }
            }
        }
        if (closestCity == null) {
            for (Nation nation : NationManager.nations) {
                if (nation.name.equalsIgnoreCase(cityName)) {
                    for (City city : nation.cities) {
                        if (!city.name.equalsIgnoreCase(nationName) || !nation.canTravel(this)) continue;
                        if (closestCity == null || city.spawn.distanceSquared(player.getLocation()) < closestCity.spawn
                                .distanceSquared(player.getLocation()))
                            closestCity = city;
                    }
                }
            }
        }
        if (closestCity == null) {
            player.sendMessage("\u00a7cDid not find \u00a7f" + cityName + "\u00a7c, \u00a7f" + nationName
                    + "\u00a7c that you are allowed to travel to!");
            return;
        }

        teleport(closestCity);

    }

    public void teleport(City city) {
        player.teleport(city.spawn);
        player.sendMessage("You travelled to " + city.getFullName() + ".");
    }

    /*
     * 
     * Joining / Leaving a nation
     * 
     */

    public void joinNation() {
        Nation nation = getCurrentNation();
        if (nation == null) {
            player.sendMessage("\u00a7cPlease specify a nation with \u00a7f/nation join [Nation]");
            return;
        }
        joinNation(nation);
    }

    public void joinNation(String nationName) {
        Nation closestMatch = null;
        for (Nation nation : NationManager.nations) {
            if (!nation.name.equalsIgnoreCase(nationName)) continue;
            if (closestMatch == null || nation.getSpawn().distanceSquared(player.getLocation()) < closestMatch
                    .getSpawn().distanceSquared(player.getLocation()))
                closestMatch = nation;
        }
        if (closestMatch == null) {
            player.sendMessage("\u00a7cDid not find any nation by the name of \u00a7f" + nationName + "\u00a7c!");
            return;
        }
        joinNation(closestMatch);
    }

    public void joinNation(Nation nation) {
        if (nation.isPlayerCitizen(this)) {
            player.sendMessage("\u00a7cYou are already a citizen of the nation of \u00a7f" + nation.name + "\u00a7c!");
            return;
        }
        for (Nation enemy : nation.enemies) {
            if (enemy.isPlayerCitizen(this)) {
                player.sendMessage("You are a citizen of the nation of " + enemy.getColoredName()
                        + ", which is an enemy of " + nation.getColoredName() + "!");
                player.sendMessage("If you would like to leave the nation of " + enemy.getColoredName()
                        + ", use \u00a7d/nation leave " + enemy.name + "\u00a7f!");
                return;
            }
        }
        // nation.joinRequests.add(this);
        player.sendMessage("Sent a request to join " + nation.getColoredName() + "!");
    }

    public void leaveNation() {
        if (nations.size() == 0) {
            player.sendMessage("\u00a7cYou are not a citizen of any nation!");
            return;
        }
        if (nations.size() > 1) {
            player.sendMessage(
                    "\u00a7cYou are a citizen of multiple nations. Please specify a nation with \u00a7f/nation leave [Nation]");
            return;
        }
        leaveNation(nations.iterator().next());
    }

    public void leaveNation(String nationName) {
        for (Nation nation : NationManager.nations) {
            if (nation.name.equalsIgnoreCase(nationName) && nation.isPlayerCitizen(this)) {
                leaveNation(nation);
                return;
            }
        }
        player.sendMessage(
                "\u00a7cYou are not a citizen of any nation by the name of \u00a7f" + nationName + "\u00a7c!");
    }

    public void leaveNation(Nation nation) {
        if (!nation.isPlayerCitizen(this)) {
            player.sendMessage("\u00a7cYou are a not a citizen of \u00a7f" + nation.name + "\u00a7c!");
            return;
        }
        if (nation.getLeader() == this) {
            if (nation.getCitizens().size() == 1) {
                player.sendMessage("\u00a7cYou are the leader of \u00a7f" + nation.name
                        + "\u00a7c! If you leave, the nation will be entirely abandoned!\nUse \u00a7f/nation leaveconfirm \u00a7cif you are sure you want to leave.");
            } else {
                player.sendMessage("\u00a7cYou are the leader of \u00a7f" + nation.name
                        + "\u00a7c! Please abdicate the position to someone else before leaving.");
            }
            return;
        }
        nation.cities.forEach(city -> {
            if (city.mayor == this) {
                player.sendMessage("You left your post as mayor of the city of " + city.getFullName() + ".");

                if (city.citizens.size() <= 1) {
                    city.abandonCity();
                } else {
                    city.citizens.remove(this);
                    city.mayor = city.citizens.iterator().next();
                }
            }
        });
        nations.remove(nation);
        player.sendMessage("You left the nation of " + nation.getColoredName() + ".");
    }

    public void leaveNationConfirm() {
        if (nations.size() == 0) {
            player.sendMessage("\u00a7cYou are not a citizen of any nation!");
            return;
        }
        if (nations.size() > 1) {
            player.sendMessage(
                    "\u00a7cYou are a citizen of multiple nations. Please specify a nation with \u00a7f/nation leave [Nation]");
            return;
        }
        leaveNationConfirm(nations.iterator().next());
    }

    public void leaveNationConfirm(String nationName) {
        for (Nation nation : NationManager.nations) {
            if (nation.name.equalsIgnoreCase(nationName) && nation.isPlayerCitizen(this)) {
                leaveNationConfirm(nation);
                return;
            }
        }
        player.sendMessage(
                "\u00a7cYou are not a citizen of any nation by the name of \u00a7f" + nationName + "\u00a7c!");
    }

    public void leaveNationConfirm(Nation nation) {
        if (nation.getLeader() == this && nation.getCitizens().size() == 1) {
            player.sendMessage("The nation of " + nation.getColoredName() + " has been dissolved.");
            nation.removeNation();
            return;
        }

        leaveNation(nation);
    }

    public void invitePlayer(String playerName) {

    }

    public void invitePlayer(String playerName, String cityName) {

    }

    public void kickPlayer(String playerName) {

    }

    public void kickPlayer(String playerName, String cityName) {

    }

    public void joinCity() {

    }

    public void joinCity(String cityName) {

    }

    public void addNews(String newsInfo) {
        if (player.isOnline()) {
            player.sendMessage(newsInfo);
        } else {
            news.add(newsInfo);
        }
    }

    public void displayInfo() {
        if (news.isEmpty()) player.sendMessage("\u00a7d\u00a7l\u00a7oWelcome!");
        else player.sendMessage("\u00a7d\u00a7l\u00a7oWelcome back! Here's what happened while you were gone:");
        for (String info : news) { player.sendMessage(info); }
        if (nations.isEmpty()) {
            player.sendMessage(
                    "You are not a citizen of any nation! Use \u00a7d/nation create \u00a7fto start a new nation, or \u00a7d/nation join \u00a7fto join an existing one!\nUse \u00a7d/nation list\u00a7f to see all existing nations.");
        }
    }

    private String getDirString() {
        float yaw = (player.getLocation().getYaw() + 360) % 360;
        if (yaw >= 360 - 45 || yaw < 45) return "v";
        else if (yaw >= 45 && yaw < 180 - 45) return "<";
        else if (yaw >= 180 - 45 && yaw < 180 + 45) return "^";
        else if (yaw >= 180 + 45 && yaw < 360 - 45) return ">";

        // shouldnt ever get here
        return "*";
    }

    public void sendNationMap() {
        int radius = 5;
        Chunk currentChunk = player.getLocation().getChunk();

        String dirString = getDirString();

        Set<Nation> seenNations = new HashSet<>();
        for (int z = -radius; z <= radius; z++) {
            String line = "";
            for (int x = -radius; x <= radius; x++) {
                Chunk thisChunk = currentChunk.getWorld().getChunkAt(currentChunk.getX() + x, currentChunk.getZ() + z);
                Nation nationAtChunk = NationManager.getNationFromChunk(thisChunk);
                City cityAtChunk = NationManager.getCityFromChunk(thisChunk);
                if (x == 0 && z == 0) {
                    line += "\u00a7e" + dirString;
                    seenNations.add(nationAtChunk);
                } else if (nationAtChunk != null) {
                    if (cityAtChunk == null) line += nationAtChunk.getMapMarker();
                    else line += cityAtChunk.getMapMarker();
                } else line += "\u00a78/";
                seenNations.add(nationAtChunk);
            }
            player.sendMessage(line);
        }
        Nation standingIn = NationManager.getNationFromChunk(currentChunk);
        City standingInCity = NationManager.getCityFromChunk(currentChunk);
        player.sendMessage("\nKey:");
        if (standingIn == null) {
            player.sendMessage("  \u00a7e" + dirString + "  You \u00a77(You are currently in unclaimed territory.)");
        } else if (standingInCity == null) {
            player.sendMessage("  \u00a7e" + dirString + "  You \u00a77(You are currently in "
                    + standingIn.getColoredName() + "\u00a77 territory.)");
        } else {
            player.sendMessage("  \u00a7e" + dirString + "  You \u00a77(You are currently in the city of "
                    + standingInCity.getColoredName() + "\u00a77.)");
        }
        if (!seenNations.isEmpty()) {
            for (Nation nation : seenNations) {
                if (nation != null)
                    player.sendMessage("  " + nation.getMapMarker() + "  " + nation.getColor(true) + nation.name);
            }
        }
        player.sendMessage("  \u00a78/  Unclaimed Area");
    }
}
