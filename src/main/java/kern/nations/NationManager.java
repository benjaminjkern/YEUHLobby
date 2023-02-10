package kern.nations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NationManager {
    public static Set<Nation> nations = new HashSet<>();
    public static Set<NationPlayer> players = new HashSet<>();

    public static String NATION_WORLD = "survival";
    public static String CURRENCY = "♧";
    public static String NATION_PREFIX = "\u00a7d[\u00a7fNations\u00a7d]\u00a7f ";

    // utilities

    public static NationPlayer getPlayer(Player player) {
        for (NationPlayer nationPlayer : players) {
            if (nationPlayer.player == player) return nationPlayer;
            if (nationPlayer.name.equals(player.getName())) {
                nationPlayer.player = player;
                return nationPlayer;
            }
        }
        Bukkit.getLogger().info(NATION_PREFIX + "Creating new NationPlayer for " + player.getName());
        return new NationPlayer(player);
    }

    public static Nation getNationFromChunk(Chunk chunk) {
        for (Nation nation : nations) { if (nation.ownedLand.contains(chunk)) return nation; }
        return null;
    }

    public static City getCityFromChunk(Chunk chunk) {
        for (Nation nation : nations) {
            if (nation.ownedLand.contains(chunk)) {
                for (City city : nation.cities) { if (city.ownedLand.contains(chunk)) return city; }
            }
        }
        return null;
    }

    public static Set<Chunk> getDirectChunkNeighbors(Chunk chunk) {
        World world = chunk.getWorld();
        return new HashSet<>(Arrays.asList(world.getChunkAt(chunk.getX() + 1, chunk.getZ()),
                world.getChunkAt(chunk.getX() - 1, chunk.getZ()), world.getChunkAt(chunk.getX(), chunk.getZ() + 1),
                world.getChunkAt(chunk.getX(), chunk.getZ() - 1)));
    }

    public static Set<Chunk> getAllChunkNeighbors(Chunk chunk) {
        World world = chunk.getWorld();
        return new HashSet<>(Arrays.asList(world.getChunkAt(chunk.getX() + 1, chunk.getZ()),
                world.getChunkAt(chunk.getX() + 1, chunk.getZ() + 1), world.getChunkAt(chunk.getX(), chunk.getZ() + 1),
                world.getChunkAt(chunk.getX() - 1, chunk.getZ() + 1), world.getChunkAt(chunk.getX() - 1, chunk.getZ()),
                world.getChunkAt(chunk.getX() - 1, chunk.getZ() - 1), world.getChunkAt(chunk.getX(), chunk.getZ() - 1),
                world.getChunkAt(chunk.getX() + 1, chunk.getZ() - 1)));
    }

    public static void dayTick() { for (Nation nation : nations) { nation.distributeTaxesToCities(); } }

    // commands
    public static void sendNationInfo(CommandSender p) {
        for (Nation nation : nations) {
            p.sendMessage(nation.getColoredName());
            p.sendMessage("  Bank: " + nation.bankText());
            p.sendMessage("  Happiness: " + nation.happiness());
            p.sendMessage("  Claimed Land: " + nation.ownedLand.size());
            p.sendMessage("  Citizens: ");
            for (NationPlayer nationPlayer : nation.getCitizens()) {
                p.sendMessage("    " + nationPlayer.name + (nationPlayer == nation.getLeader() ? " *" : ""));
            }
            p.sendMessage("  Cities:");
            for (City city : nation.cities) {
                p.sendMessage("    " + city.getColoredName() + (city == nation.capitol ? " *" : ""));
                p.sendMessage("      Bank: " + city.bankText());
                p.sendMessage("      Happiness: " + city.happiness);
                p.sendMessage("      Claimed Land: " + city.ownedLand.size());
                p.sendMessage("      Citizens: ");
                for (NationPlayer nationPlayer : city.citizens) {
                    p.sendMessage("        " + nationPlayer.name + (nationPlayer == city.mayor ? " *" : ""));
                }
            }
        }
    }
}
