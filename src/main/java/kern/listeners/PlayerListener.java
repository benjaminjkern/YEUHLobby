package kern.listeners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import kern.Game;
import kern.YEUHLobby;
import kern.threads.FillItemFrameThread;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class PlayerListener implements Listener {

    private Set<Player> inSpawn;
    private static Map<String, Runnable> holdMessage = new HashMap<>();
    private static Set<Player> sendingToGame = new HashSet<>();

    public PlayerListener() {
        inSpawn = new HashSet<>();
        new BukkitRunnable() {
            public void run() {
                if (!YEUHLobby.getPlugin().isEnabled()) this.cancel();
                Bukkit.getOnlinePlayers().forEach(player -> {
                    checkHeldMessages(player);
                    player.setFoodLevel(20);
                    if (Math.abs(player.getLocation().getBlockX()) <= 100
                            && Math.abs(player.getLocation().getBlockZ()) <= 100) {
                        if (!player.hasPermission("yeuhLobby.admin") && player.getGameMode() != GameMode.SURVIVAL) {
                            Bukkit.getScheduler().scheduleSyncDelayedTask(YEUHLobby.getPlugin(), () -> {
                                player.setGameMode(GameMode.SURVIVAL);
                                player.getInventory().clear();
                                CommandItemListener.getCommandItemInventory().entrySet().forEach(
                                        entry -> player.getInventory().setItem(entry.getKey(), entry.getValue()));
                            });
                            if (inSpawn.contains(player)) {
                                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                                        "\u00a7cCreative Mode is only permitted outside of the spawn area!"));
                            }
                        }
                        if (!inSpawn.contains(player)) inSpawn.add(player);
                    } else {
                        if (inSpawn.contains(player)) {
                            inSpawn.remove(player);
                            if (player.getGameMode() != GameMode.CREATIVE) {
                                Bukkit.getScheduler().scheduleSyncDelayedTask(YEUHLobby.getPlugin(), () -> {
                                    player.setGameMode(GameMode.CREATIVE);
                                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(
                                            "\u00a7fYou have entered the \u00a7dCreative Zone\u00a7f! Build whatever you like!"));
                                });
                            }
                        }
                    }
                });
            }
        }.runTaskTimerAsynchronously(YEUHLobby.getPlugin(), 0, 10);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        YEUHLobby.getScoreKeeper().getStats(player.getName()).seen();

        String joinMessage = "\u00a70(\u00a7a\u00a7l+\u00a70) \u00a77" + player.getDisplayName();

        for (Game g : YEUHLobby.getPlugin().getGames()) {
            if (g.getPlayers().contains(player.getUniqueId())) {
                g.getPlayers().remove(player.getUniqueId());
                if (g.getPlayers().isEmpty() && (g.gameState.equals("PLAYING") || g.gameState.equals("DEATHMATCH"))) {
                    g.gameState = "ENDED";
                }
                joinMessage = "\u00a70(\u00a7d\u00a7l<\u00a70) \u00a77" + player.getDisplayName();
            }

            if (!g.getPlayers().isEmpty() && g.getAlive().contains(player.getName())) {
                player.sendMessage(YEUHLobby.PREFIX
                        + "You disconnected while a game was running! Use \u00a7d/join \u00a7fto go back to the game.");
            }
        }

        event.setJoinMessage(joinMessage);

        new BukkitRunnable() {
            public void run() {
                checkHeldMessages(player);
                Bukkit.dispatchCommand(player, "rating");
            }
        }.runTaskLater(YEUHLobby.getPlugin(), 10 * 5);

        new BukkitRunnable() {
            @Override
            public void run() {

                if (!YEUHLobby.getPlugin().getStartingGames().isEmpty()) {
                    player.sendMessage(YEUHLobby.PREFIX
                            + "A game is starting right now! If you would like to join, use \u00a7d/join\u00a7f!");

                } else if (!YEUHLobby.getPlugin().getPlayingGames().isEmpty()) {
                    player.sendMessage(YEUHLobby.PREFIX
                            + "There are active games running! If you would like to spectate one, use \u00a7d/spectate\u00a7f!");
                } else {
                    player.sendMessage(YEUHLobby.PREFIX
                            + "Use \u00a7d/join \u00a7fto start playing \u00a7d\u00a7lYEUH BATTLE ROYALE\u00a7f!");
                }
            }
        }.runTaskTimer(YEUHLobby.getPlugin(), 20 * 5, 20 * 60 * 5);

    }

    private void checkHeldMessages(Player player) {
        String name = player.getName().toLowerCase();
        if (holdMessage.containsKey(name)) {
            holdMessage.get(name).run();
            holdMessage.remove(name);
        }
    }

    public static void addToSendingToGame(Player p) { sendingToGame.add(p); }

    public static void upRank(String player) {
        holdMessage.put(player.toLowerCase(), () -> {
            Player p = Bukkit.getPlayerExact(player);
            if (p == null) return;
            String rank = YEUHLobby.getScoreKeeper().getStats(player).rankingColor()
                    + YEUHLobby.getScoreKeeper().getStats(player).getRankString();
            p.sendMessage(YEUHLobby.PREFIX + "\u00a7fYou have been elevated to the rank of " + rank + "\u00a7f!");
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("You are now a " + rank + "\u00a7f!"));
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_AMBIENT, 1, 0);
            p.getWorld().spawnParticle(Particle.END_ROD, p.getEyeLocation(), 20);
            p.getWorld().spawnParticle(Particle.TOTEM, p.getEyeLocation(), 20);
        });
    }

    public static void downRank(String player) {
        holdMessage.put(player.toLowerCase(), () -> {
            Player p = Bukkit.getPlayerExact(player);
            if (p == null) return;
            String rank = YEUHLobby.getScoreKeeper().getStats(player).rankingColor()
                    + YEUHLobby.getScoreKeeper().getStats(player).getRankString();
            p.sendMessage(YEUHLobby.PREFIX + "\u00a7fYou have been downgraded to the rank of " + rank + "\u00a7f");
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("You are now a " + rank + "\u00a7f"));
        });
    }

    public static void sendLevelUpMessage(String player) {
        holdMessage.put(player.toLowerCase(), () -> {
            Player p = Bukkit.getPlayerExact(player);
            if (p == null) return;
            int level = YEUHLobby.getScoreKeeper().getStats(player).getLevel();
            p.sendMessage(
                    YEUHLobby.PREFIX + "\u00a7bYou leveled up! You are now level \u00a7f\u00a7l" + level + "\u00a7b!");
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent("You are now level \u00a7f\u00a7l" + level + "\u00a7b!"));
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 0);
            p.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, p.getEyeLocation(), 20);
            p.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, p.getEyeLocation(), 20);
            p.getWorld().spawnParticle(Particle.NOTE, p.getEyeLocation(), 20);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        String quitMessage = "\u00a70(\u00a7c\u00a7l-\u00a70) \u00a77" + p.getDisplayName();

        if (sendingToGame.contains(p)) {
            quitMessage = "\u00a70(\u00a7d\u00a7l>\u00a70) \u00a77" + p.getDisplayName();
            sendingToGame.remove(p);
        }

        event.setQuitMessage(quitMessage);
        inSpawn.remove(p);
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

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player && Math.abs(event.getEntity().getLocation().getX()) < 100
                && Math.abs(event.getEntity().getLocation().getZ()) < 100) {
            event.getEntity().setFireTicks(0);
            event.setCancelled(true);
        }
    }

}