package kern.listeners;

import java.util.List;
import java.util.Queue;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import kern.Game;
import kern.YEUHLobby;

public class PlayerListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Queue<Player> playerQueue = YEUHLobby.getPlugin().getPlayerQueue();

        new BukkitRunnable() {
            public void run() {
                if (YEUHLobby.getWarden().hasPlayerSigned(player.getName())) {
                    player.sendMessage(YEUHLobby.PREFIX
                            + "You have opted in to the queue. Use \u00a7d/opt out \u00a7fif you would like to stay in the lobby.");
                    playerQueue.add(player);
                }

                Bukkit.dispatchCommand(Bukkit.getPlayerExact(player.getName()), "rating");
            }
        }.runTaskLater(YEUHLobby.getPlugin(), 20 * 5);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!YEUHLobby.getWarden().hasPlayerSigned(player.getName())) {
                    player.sendMessage(YEUHLobby.PREFIX + "Read the \u00a7d/rules \u00a7fto opt in to the game queue!");
                    return;
                }

                int minSize = Integer.MAX_VALUE;

                List<Game> openGames = YEUHLobby.getPlugin().getOpenGames();

                if (!playerQueue.contains(player)) {
                    player.sendMessage("");
                    player.sendMessage(YEUHLobby.PREFIX
                            + "You are not in the queue at the moment! Use \u00a7d/opt in \u00a7fif you would like to play \u00a75\u00a7lBattle Royale!");
                } else if (!openGames.isEmpty()) {
                    for (Game g : openGames) if (g.minSize < minSize) minSize = g.minSize;
                    int lobbySize = YEUHLobby.getPlugin().getPlayerQueue().size();
                    if (lobbySize < minSize) {
                        player.sendMessage("");
                        player.sendMessage(YEUHLobby.PREFIX + "There " + (lobbySize == 1 ? "is" : "are")
                                + " only \u00a7d" + lobbySize + " \u00a7fplayer" + (lobbySize == 1 ? "" : "s")
                                + " in the queue! You need at least \u00a7d" + minSize + " \u00a7fto start a game.");
                        player.sendMessage(YEUHLobby.PREFIX
                                + "Tell a friend to join!\n\u00a78\u00a7oOr if you don't have any friends, just wait for someone else to join and become friends with them!");
                    }
                }

                if (!YEUHLobby.getPlugin().getPlayingGames().isEmpty()) {
                    player.sendMessage(YEUHLobby.PREFIX
                            + "If you would like to spectate an active game, use \u00a7d/spectate\u00a7f!");

                }
            }
        }.runTaskTimer(YEUHLobby.getPlugin(), 20 * 5, 20 * 60 * 5);

    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) { YEUHLobby.getPlugin().getPlayerQueue().remove(event.getPlayer()); }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRightClick(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();

        if (entity instanceof ItemFrame && Math.abs(entity.getLocation().getX()) < 100
                && Math.abs(entity.getLocation().getZ()) < 100) {
            ItemFrame itemFrame = (ItemFrame) entity;
            ItemStack item = itemFrame.getItem();
            if (item != null) {
                event.setCancelled(true);
                player.sendMessage("\u00a7d" + item.getItemMeta().getDisplayName() + "\u00a77:");
                for (String lore : item.getItemMeta().getLore()) player.sendMessage("- \u00a77" + lore);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (Math.abs(e.getBlock().getLocation().getX()) < 100 && Math.abs(e.getBlock().getLocation().getZ()) < 100)
            return;
        if (!YEUHLobby.getWarden().hasPlayerSigned(e.getPlayer().getName())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("\u00a7cRead the &l/rules \u00a7cbefore you can build here!");
        }
    }

    @EventHandler
    public void onBlockBreak(BlockPlaceEvent e) {
        if (Math.abs(e.getBlock().getLocation().getX()) < 100 && Math.abs(e.getBlock().getLocation().getZ()) < 100)
            return;
        if (!YEUHLobby.getWarden().hasPlayerSigned(e.getPlayer().getName())) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("\u00a7cRead the &l/rules \u00a7cbefore you can build here!");
        }
    }

}