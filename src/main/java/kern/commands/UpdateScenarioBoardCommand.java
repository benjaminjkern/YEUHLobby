package kern.commands;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import kern.Game;
import kern.YEUHLobby;
import kern.threads.FillItemFrameThread;

public class UpdateScenarioBoardCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("yeuhlobby.admin")) return false;

        if (!(sender instanceof Player)) return true;

        Queue<String> activeScenarios = null;
        YamlConfiguration yf = null;
        for (Game g : YEUHLobby.getPlugin().getGames()) {
            if (g.activeScenarios != null) {
                activeScenarios = new LinkedList<>(Arrays.asList(g.activeScenarios.split(",")));
                yf = YamlConfiguration
                        .loadConfiguration(new File("/home/benkern/Desktop/" + g.server + "/plugins/UhcCore/lang.yml"));
                break;
            }
        }

        if (yf == null || activeScenarios == null) { sender.sendMessage("\u00a7cSomething went wrong!"); }

        Bukkit.getScheduler().runTaskAsynchronously(YEUHLobby.getPlugin(),
                new FillItemFrameThread((Player) sender, yf, activeScenarios));
        return true;

    }
}
