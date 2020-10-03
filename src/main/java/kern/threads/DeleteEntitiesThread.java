package kern.threads;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.ItemFrame;

import kern.YEUHLobby;

public class DeleteEntitiesThread implements Runnable {
    DeleteEntitiesThread task;

    public DeleteEntitiesThread() { task = this; }

    public void run() {
        // remove ALL entities in the creative world every 15 minutes.
        Bukkit.getOnlinePlayers()
                .forEach(player -> player.sendMessage(YEUHLobby.PREFIX + "\u00a7cRemoving entites..."));

        int entities = 0;
        // remove all entities
        for (Chunk chunk : Bukkit.getWorld("creative").getLoadedChunks()) {
            for (Entity e : chunk.getEntities()) {
                if (e instanceof HumanEntity) continue;
                if (e instanceof ItemFrame && Math.abs(e.getLocation().getX()) < 100
                        && Math.abs(e.getLocation().getZ()) < 100)
                    continue;
                e.remove();
                entities++;
            }
        }
        if (entities > 0) Bukkit.getLogger().info("[YEUHLobby] \u00a7eRemoved \u00a7c" + entities + " \u00a7eentites.");

        Bukkit.getScheduler().runTaskLater(YEUHLobby.getPlugin(),
                () -> Bukkit.getOnlinePlayers().forEach(
                        player -> player.sendMessage(YEUHLobby.PREFIX + "\u00a7cRemoving all entites in 30 seconds.")),
                15 * 20 * 60 - 20 * 30);
        Bukkit.getScheduler().runTaskLater(YEUHLobby.getPlugin(), task, 15 * 20 * 60);
    }
}
