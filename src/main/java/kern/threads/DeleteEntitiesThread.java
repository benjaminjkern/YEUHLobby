package kern.threads;

import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.ItemFrame;

import kern.Game;
import kern.YEUHLobby;

public class DeleteEntitiesThread implements Runnable {
    DeleteEntitiesThread task;

    public DeleteEntitiesThread() { task = this; }

    public void run() {
        // remove ALL entities in the creative world every 15 minutes.

        long now = new Date().getTime();
        for (Game g : YEUHLobby.getPlugin().getGames()) { if (now - g.lastPing > 15 * 60 * 1000) g.forceKill(); }

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
        if (entities > 0) {
            Bukkit.getOnlinePlayers()
                    .forEach(player -> player.sendMessage(YEUHLobby.PREFIX + "\u00a7cRemoving entites..."));
            Bukkit.getLogger().info("[YEUHLobby] \u00a7eRemoved \u00a7c" + entities + " \u00a7eentites.");
        }

        Bukkit.getScheduler().runTaskLater(YEUHLobby.getPlugin(), () -> {
            int ent = -1;
            // remove all entities
            for (Chunk chunk : Bukkit.getWorld("creative").getLoadedChunks()) {
                for (Entity e : chunk.getEntities()) {
                    if (e instanceof HumanEntity) continue;
                    if ((e instanceof ItemFrame || e instanceof ArmorStand) && Math.abs(e.getLocation().getX()) < 100
                            && Math.abs(e.getLocation().getZ()) < 100)
                        continue;

                    ent++;
                }
            }
            if (ent > 0) Bukkit.getOnlinePlayers().forEach(
                    player -> player.sendMessage(YEUHLobby.PREFIX + "\u00a7cRemoving all entites in 30 seconds."));
        }, 15 * 20 * 60 - 20 * 30);
        Bukkit.getScheduler().runTaskLater(YEUHLobby.getPlugin(), task, 15 * 20 * 60);
    }
}
