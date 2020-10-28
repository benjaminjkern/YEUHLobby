package kern.commands;

import org.bukkit.entity.Player;

import kern.YEUHLobby;
import kern.listeners.GUIInventoryListener;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RulesCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) return true;

        ((Player) sender).openInventory(GUIInventoryListener.getRulesInventory((Player) sender));
        return true;
    }
}
