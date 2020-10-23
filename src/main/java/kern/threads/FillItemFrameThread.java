package kern.threads;

import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Rotation;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import kern.YEUHLobby;

public class FillItemFrameThread implements Runnable {

    YamlConfiguration yf;
    Scanner s;
    Player executor;
    String scenario;
    private int count;
    public static FillItemFrameThread task;
    public static ItemFrame waitingFor;

    public FillItemFrameThread(Player executor, YamlConfiguration yf, Scanner s) {
        task = this;
        this.yf = yf;
        this.s = s;
        this.executor = executor;
        waitingFor = null;
        count = 0;
    }

    public void run() {
        if (yf == null || s == null) {
            executor.sendMessage("Something went wrong!");
            return;
        }

        if (task == null) return;

        if (waitingFor != null) {
            // fill item frame
            ItemStack item = new ItemStack(Material.valueOf(yf.getString(scenario + ".item", "dirt").toUpperCase()));
            ItemMeta im = item.getItemMeta();
            im.setDisplayName((count < 24 ? "\u00a7d" : "\u00a76NEW: ")
                    + yf.getString(scenario + ".name", "Couldn't find name!"));
            im.setLore(yf.getStringList(scenario + ".description"));
            item.setItemMeta(im);
            waitingFor.setItem(item, true);
            waitingFor.setVisible(false);
            waitingFor.setRotation(Rotation.NONE);
            count++;
        }

        if (s.hasNextLine()) {
            scenario = "scenarios." + s.nextLine().toLowerCase();
            executor.sendMessage("Right click item frame for " + scenario);
            waitingFor = null;
        } else {
            executor.sendMessage("That should be the last of them!");
            task = null;
        }
    }

    public static void cont() { Bukkit.getScheduler().runTask(YEUHLobby.getPlugin(), task); }

}
