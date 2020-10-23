package kern.commands;

import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import kern.listeners.GUIInventoryListener;

public class ListCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (sender instanceof Player) ((Player) sender).openInventory(GUIInventoryListener.getListInventory());
        else sender.sendMessage("Total: " + Bukkit.getOnlinePlayers().size() + "\n"
                + Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.joining(",")));
        return true;
    }

}