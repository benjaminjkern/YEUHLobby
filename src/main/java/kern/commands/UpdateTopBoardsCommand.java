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

public class UpdateTopBoardsCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("yeuhlobby.admin")) return false;
        YEUHLobby.getScoreKeeper().updateBoards();
        return true;

    }
}
