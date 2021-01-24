package kern.commands;

import org.bukkit.entity.Player;

import kern.listeners.GUIInventoryListener;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class HelpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) return true;

        if (args.length >= 1 && args[0].equalsIgnoreCase("patron")) {
            ((Player) sender).openInventory(GUIInventoryListener.getPatronInventory());
            return true;
        }
        if (args.length >= 1 && args[0].equalsIgnoreCase("member")) {
            Bukkit.dispatchCommand(sender, "member");
            return true;
        }
        if (args.length >= 1 && args[0].equalsIgnoreCase("discord")) {
            Bukkit.dispatchCommand(sender, "discord");
            return true;
        }

        ((Player) sender).openInventory(GUIInventoryListener.getHelpInventory());
        return true;
    }
}
