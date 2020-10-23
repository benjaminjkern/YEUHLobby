package kern.listeners;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import kern.Game;
import kern.YEUHLobby;
import kern.threads.FillItemFrameThread;

public class PlayerListener implements Listener {

    private static Set<Player> sendingToGame = new HashSet<>();

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        String joinMessage = "";
        boolean canGoBackToGame = false;
        for (Game g : YEUHLobby.getPlugin().getGames()) {
            if (g.getPlayers().contains(player.getName())) {
                g.getPlayers().remove(player.getName());
                joinMessage = "\u00a70(\u00a7d\u00a7l<\u00a70) \u00a77" + player.getDisplayName();
            }

            if (g.getDisconnectedPlayers().contains(player.getName())) {
                player.sendMessage(YEUHLobby.PREFIX
                        + "You disconnected while a game was running! Use \u00a7d/join \u00a7fto go back to the game.");
            }

            if (g.getWinners().contains(player.getName())) {

                String wonMessage = YEUHLobby.PREFIX + "\u00a7d\u00a7l" + player.getName()
                        + " \u00a7fhas won the game!";

                for (Player p : Bukkit.getOnlinePlayers()) { p.sendMessage(wonMessage); }
                player.sendMessage(wonMessage);
                Bukkit.getLogger().info(wonMessage);

                player.sendMessage(YEUHLobby.PREFIX + "\u00a7dCongrats on your win! You now have \u00a7f\u00a7l"
                        + YEUHLobby.getScoreKeeper().getStats(player.getName(), true).wins + "\u00a7d wins!");

                g.getWinners().remove(player.getName());
            }
        }

        if (joinMessage.isEmpty()) joinMessage = "\u00a70(\u00a7a\u00a7l+\u00a70) \u00a77" + player.getDisplayName();
        event.setJoinMessage(joinMessage);

        new BukkitRunnable() { public void run() { Bukkit.dispatchCommand(player, "rating"); } }
                .runTaskLater(YEUHLobby.getPlugin(), 10 * 5);

        new BukkitRunnable() {
            @Override
            public void run() {

                if (!YEUHLobby.getPlugin().getStartingGames().isEmpty()) {
                    player.sendMessage(YEUHLobby.PREFIX
                            + "A game is starting right now! If you would like to join, use \u00a7d/join\u00a7f!");

                } else if (!YEUHLobby.getPlugin().getPlayingGames().isEmpty()) {
                    player.sendMessage(YEUHLobby.PREFIX
                            + "There are active games running! If you would like to spectate one, use \u00a7d/spectate\u00a7f!");
                }
            }
        }.runTaskTimer(YEUHLobby.getPlugin(), 20 * 5, 20 * 60 * 5);

    }

    public static void addToSendingToGame(Player p) { sendingToGame.add(p); }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        String quitMessage = "";

        if (sendingToGame.contains(p)) {
            quitMessage = "\u00a70(\u00a7d\u00a7l>\u00a70) \u00a77" + p.getDisplayName();
            sendingToGame.remove(p);
        }

        if (quitMessage.isEmpty()) quitMessage = "\u00a70(\u00a7c\u00a7l-\u00a70) \u00a77" + p.getDisplayName();
        event.setQuitMessage(quitMessage);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRightClick(PlayerInteractEntityEvent event) {
        Entity entity = event.getRightClicked();
        Player player = event.getPlayer();

        if (entity instanceof ItemFrame && Math.abs(entity.getLocation().getX()) < 100
                && Math.abs(entity.getLocation().getZ()) < 100) {
            ItemFrame itemFrame = (ItemFrame) entity;
            ItemStack item = itemFrame.getItem();
            if (item.getItemMeta() != null && item.getItemMeta().getLore() != null) {
                event.setCancelled(true);
                player.sendMessage("\u00a7d" + item.getItemMeta().getDisplayName() + "\u00a77:");
                for (String lore : item.getItemMeta().getLore()) player.sendMessage("- \u00a77" + lore);
            }

            if (FillItemFrameThread.waitingFor == null && FillItemFrameThread.task != null) {
                FillItemFrameThread.waitingFor = itemFrame;
                FillItemFrameThread.cont();
            }
        }
    }

}