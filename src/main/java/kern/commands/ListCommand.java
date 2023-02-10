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
            String message = "\nLobby: " + Bukkit.getOnlinePlayers().size();
            if (Bukkit.getOnlinePlayers().size() > 0) message += "\n  "
                    + Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.joining(","));
            for (Game g : YEUHLobby.getPlugin().getGames()) {
                message += "\n" + g.server + ": " + g.currentSize;
                if (g.currentSize > 0) {
                    message += "\n  " + g.getPlayers().stream().map((uuid) -> {
                        String name = Bukkit.getOfflinePlayer(uuid).getName();
                        if (g.getAlive().contains(name)) return name;
                        return name + " \u00a77(Spectating)\u00a7f";
                    }).collect(Collectors.joining(", "));
                    if (g.currentSize > g.getPlayers().size())
                        message += " \u00a77(" + (g.currentSize - g.getPlayers().size()) + " untracked players)";
                }
            }
            sender.sendMessage(message);
        }
        return true;
    }

}