package kern.listeners;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.scheduler.BukkitRunnable;

import kern.YEUHLobby;

public class CreativeListener implements Listener {

    private static Set<Material> potionBits;
    static {
        potionBits = new HashSet<>();
        potionBits.add(Material.TIPPED_ARROW);
        potionBits.add(Material.POTION);
        potionBits.add(Material.SPLASH_POTION);
        potionBits.add(Material.LINGERING_POTION);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onCreativeGive(InventoryCreativeEvent e) {
        ItemStack item = e.getCursor().clone();
        if (potionBits.contains(item.getType())) {
            if (item.hasItemMeta()) {
                PotionMeta pm = (PotionMeta) item.getItemMeta();
                if (pm.hasCustomEffects()) return;
            }

            new BukkitRunnable() {
                @Override
                public void run() { e.getWhoClicked().getInventory().setItem(e.getSlot(), item); }
            }.runTaskLater(YEUHLobby.getPlugin(), 1);
            return;
        }

        if (item.getType() == Material.ENCHANTED_BOOK) {
            if (item.hasItemMeta()) {
                Map<Enchantment, Integer> enchants = ((EnchantmentStorageMeta) item.getItemMeta()).getStoredEnchants();
                // Bukkit.getLogger().info(enchants + "");
                if (enchants.size() > 1) return;
                for (Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                    Bukkit.getLogger().info(entry.getValue() + ", " + entry.getKey().getMaxLevel());
                    if (entry.getValue() > entry.getKey().getMaxLevel()) return;
                }
            }

            new BukkitRunnable() {
                @Override
                public void run() { e.getWhoClicked().getInventory().setItem(e.getSlot(), item); }
            }.runTaskLater(YEUHLobby.getPlugin(), 1);
            return;
        }
    }
}
