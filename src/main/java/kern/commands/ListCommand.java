package kern.commands;

import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kern.Game;
import kern.YEUHLobby;
import kern.listeners.GUIInventoryListener;

public class ListCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) ((Player) sender).openInventory(GUIInventoryListener.getListInventory());
        else {
            sender.sendMessage("Lobby: " + Bukkit.getOnlinePlayers().size());
            if (Bukkit.getOnlinePlayers().size() > 0) sender.sendMessage(
                    Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.joining(",")));
            for (Game g : YEUHLobby.getPlugin().getGames()) {
                sender.sendMessage(g.server + ": " + g.currentSize);
                if (g.currentSize > 0) sender.sendMessage(g.getPlayers().stream()
                        .map((uuid) -> Bukkit.getOfflinePlayer(uuid).getName()).collect(Collectors.joining(",")));
            }
        }
        return true;
    }

}