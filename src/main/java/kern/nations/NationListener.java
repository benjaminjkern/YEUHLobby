package kern.nations;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.projectiles.ProjectileSource;

public class NationListener implements Listener {
    @EventHandler
    public void onChat(PlayerChatEvent event) {
        NationPlayer player = NationManager.getPlayer(event.getPlayer());
        if (player.promptCallback == null) return;

        player.outputText = event.getMessage();
        event.setCancelled(true);

        Runnable promptCallback = player.promptCallback;
        player.promptCallback = null;
        promptCallback.run();
    }

    @EventHandler
    public void onChangeChunk(PlayerMoveEvent event) {
        Chunk fromChunk = event.getFrom().getChunk();
        Chunk toChunk = event.getTo().getChunk();
        if (fromChunk == toChunk) return;

        NationPlayer player = NationManager.getPlayer(event.getPlayer());

        Nation fromNation = NationManager.getNationFromChunk(fromChunk);
        Nation toNation = NationManager.getNationFromChunk(toChunk);
        City fromCity = NationManager.getCityFromChunk(fromChunk);
        City toCity = NationManager.getCityFromChunk(toChunk);

        if (player.walkCallback != null) {
            if (toCity != null && toCity == fromCity) return;
            player.toChunk = toChunk;
            player.walkCallback.run();
            return;
        }

        if (fromNation != null && fromNation != toNation)
            event.getPlayer().sendMessage("Now leaving the nation of " + fromNation.getColoredName() + ".");
        if (toNation != null && fromNation != toNation)
            event.getPlayer().sendMessage("Welcome to the nation of " + toNation.getColoredName() + "!");

        if (fromCity == toCity) return;
        if (fromCity != null)
            event.getPlayer().sendMessage("Now leaving the city of " + fromCity.getColoredName() + ".");
        if (toCity != null) event.getPlayer().sendMessage("Welcome to the city of " + toCity.getColoredName() + "!");
    }

    @EventHandler
    public void onBuild(BlockPlaceEvent event) {
        Chunk thisChunk = event.getBlock().getChunk();
        Nation nation = NationManager.getNationFromChunk(thisChunk);
        if (nation == null) return;

        City city = NationManager.getCityFromChunk(thisChunk);
        NationPlayer player = NationManager.getPlayer(event.getPlayer());
        if (city == null && nation.isPlayerCitizen(player)) return;
        if (city != null && city.citizens.contains(player)) return;

        event.setCancelled(true);
        player.player.sendMessage("\u00a7cYou can't build here!");
    }

    @EventHandler
    public void onBuild(BlockBreakEvent event) {
        Chunk thisChunk = event.getBlock().getChunk();
        Nation nation = NationManager.getNationFromChunk(thisChunk);
        if (nation == null) return;

        City city = NationManager.getCityFromChunk(thisChunk);
        NationPlayer player = NationManager.getPlayer(event.getPlayer());
        if (city == null && nation.isPlayerCitizen(player)) return;
        if (city != null && city.citizens.contains(player)) return;

        event.setCancelled(true);
        player.player.sendMessage("\u00a7cYou can't break blocks here!");
    }

    @EventHandler
    public void onFire(BlockIgniteEvent event) {
        Chunk thisChunk = event.getBlock().getChunk();
        Nation nation = NationManager.getNationFromChunk(thisChunk);
        if (nation == null) return;

        City city = NationManager.getCityFromChunk(thisChunk);

        if (event.getPlayer() == null) {
            event.setCancelled(true);
            return;
        }
        NationPlayer player = NationManager.getPlayer(event.getPlayer());
        if (city == null && nation.isPlayerCitizen(player)) return;
        if (city != null && city.citizens.contains(player)) return;

        event.setCancelled(true);
        player.player.sendMessage("\u00a7cYou can't ignite here!");
    }

    @EventHandler
    public void onFireSpread(BlockSpreadEvent event) {
        Chunk lastChunk = event.getSource().getChunk();
        Chunk thisChunk = event.getBlock().getChunk();

        Nation lastNation = NationManager.getNationFromChunk(lastChunk);
        Nation thisNation = NationManager.getNationFromChunk(thisChunk);

        if (thisNation != null && lastNation != thisNation) {
            event.setCancelled(true);
            return;
        }

        City lastCity = NationManager.getCityFromChunk(lastChunk);
        City thisCity = NationManager.getCityFromChunk(thisChunk);

        if (thisCity != null && lastCity != thisCity) {
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler
    public void onPiston(BlockPistonExtendEvent event) {
        for (Block pushedBlock : event.getBlocks()) {
            Chunk thisChunk = pushedBlock.getChunk();
            Chunk nextChunk = pushedBlock.getRelative(event.getDirection()).getChunk();
            Nation thisNation = NationManager.getNationFromChunk(thisChunk);
            Nation nextNation = NationManager.getNationFromChunk(nextChunk);
            if (nextNation != thisNation) {
                event.setCancelled(true);
                return;
            }

            City thisCity = NationManager.getCityFromChunk(thisChunk);
            City nextCity = NationManager.getCityFromChunk(nextChunk);
            if (nextCity != thisCity) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onPiston(BlockPistonRetractEvent event) {
        for (Block pushedBlock : event.getBlocks()) {
            Chunk thisChunk = pushedBlock.getChunk();
            Chunk nextChunk = pushedBlock.getRelative(event.getDirection()).getChunk();
            Nation thisNation = NationManager.getNationFromChunk(thisChunk);
            Nation nextNation = NationManager.getNationFromChunk(nextChunk);
            if (nextNation != thisNation) {
                event.setCancelled(true);
                return;
            }

            City thisCity = NationManager.getCityFromChunk(thisChunk);
            City nextCity = NationManager.getCityFromChunk(nextChunk);
            if (nextCity != thisCity) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> {
            Chunk thisChunk = block.getChunk();
            City thisCity = NationManager.getCityFromChunk(thisChunk);
            return thisCity != null;
        });
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> {
            Chunk thisChunk = block.getChunk();
            City thisCity = NationManager.getCityFromChunk(thisChunk);
            return thisCity != null;
        });
    }

    @EventHandler
    public void onBurn(BlockBurnEvent event) {
        Chunk thisChunk = event.getBlock().getChunk();
        City thisCity = NationManager.getCityFromChunk(thisChunk);
        if (thisCity == null) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onMobSpawn(EntitySpawnEvent event) {
        Entity thisEntity = event.getEntity();
        Chunk thisChunk = thisEntity.getLocation().getChunk();
        City thisCity = NationManager.getCityFromChunk(thisChunk);
        if (thisCity == null) return;
        if (thisEntity instanceof Monster) event.setCancelled(true);
    }

    @EventHandler
    public void onMobAttack(EntityDamageByEntityEvent event) {
        Entity thisEntity = event.getEntity();

        if (!(thisEntity instanceof Breedable) && !(thisEntity instanceof Player)) return;

        Entity damager = event.getDamager();
        if (damager instanceof Projectile) {
            ProjectileSource source = ((Projectile) damager).getShooter();
            if (!(source instanceof LivingEntity)) return;
            damager = (Entity) source;
        } else if (damager instanceof TNTPrimed) { damager = ((TNTPrimed) damager).getSource(); }

        Chunk thisChunk = thisEntity.getLocation().getChunk();
        City thisCity = NationManager.getCityFromChunk(thisChunk);
        if (thisCity == null) return;
        if (damager instanceof Player) {
            NationPlayer player = NationManager.getPlayer((Player) damager);
            if (thisCity.citizens.contains(player)) return;
            player.player.sendMessage("\u00a7cYou cannot hurt players or animals here!");
        }
        event.setCancelled(true);
    }

    // @EventHandler(priority = EventPriority.LOWEST)
    // public void onInteract(PlayerInteractEvent event) {
    // NationPlayer player = NationManager.getPlayer(event.getPlayer());

    // Chunk thisChunk = event.getPlayer().getLocation().getChunk();
    // Nation nation = NationManager.getNationFromChunk(thisChunk);
    // if (nation == null) return;

    // City city = NationManager.getCityFromChunk(thisChunk);
    // if (city == null && nation.citizens.contains(player)) return;
    // if (city != null && city.citizens.contains(player)) return;

    // event.setCancelled(true);
    // player.player.sendMessage("\u00a7cYou can't do that here!");
    // }
}
