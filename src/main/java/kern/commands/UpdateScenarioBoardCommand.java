package kern.commands;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import kern.YEUHLobby;
import kern.threads.FillItemFrameThread;

public class UpdateScenarioBoardCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("yeuhlobby.admin")) return false;

        YamlConfiguration yf = YamlConfiguration
                .loadConfiguration(new File("/home/benkern/Desktop/soloMain/plugins/UhcCore/lang.yml"));

        if (!(sender instanceof Player)) return true;

        Scanner scan;
        try {
            YEUHLobby.getPlugin().getDataFolder().mkdir();
            File f = new File(YEUHLobby.getPlugin().getDataFolder(), "activeScenarios.txt");
            f.createNewFile();
            scan = new Scanner(f);
        } catch (IOException e) {
            sender.sendMessage("\u00a7cSomething went wrong when opening activeScenarios.txt!");
            return true;
        }

        Bukkit.getScheduler().runTaskAsynchronously(YEUHLobby.getPlugin(),
                new FillItemFrameThread((Player) sender, yf, scan));
        return true;

    }
}
