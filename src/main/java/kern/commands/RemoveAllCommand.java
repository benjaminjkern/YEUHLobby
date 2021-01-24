package kern.commands;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.ItemFrame;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RemoveAllCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("yeuhlobby.admin")) return true;

        // offset since the item at spawn will need to be despawned
        int entities = -1;
        // remove all entities
        for (Chunk chunk : Bukkit.getWorld("creative").getLoadedChunks()) {
            for (Entity e : chunk.getEntities()) {
                if (e instanceof HumanEntity) continue;
                if ((e instanceof ItemFrame || e instanceof ArmorStand) && Math.abs(e.getLocation().getX()) < 100
                        && Math.abs(e.getLocation().getZ()) < 100)
                    continue;

                e.remove();
                entities++;
            }
        }
        if (entities > 0) Bukkit.getLogger().info("[YEUHLobby] \u00a7eRemoved \u00a7c" + entities + " \u00a7eentites.");
        return true;
    }
}