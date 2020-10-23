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

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("unsign")) {
                sender.sendMessage("\u00a7cYou have unsigned the rules! Oh noes!");
                YEUHLobby.getWarden().unsign(sender.getName());
                return true;
            }
            if (args[0].equalsIgnoreCase("sign")) {
                sender.sendMessage("You have signed the rules! You can now use \u00a7d\u00a7l\u00a7oYEUH\u00a7f!");
                YEUHLobby.getWarden().sign(sender.getName());
                return true;
            }
        }

        ((Player) sender).openInventory(GUIInventoryListener.getRulesInventory((Player) sender));
        return true;
    }
}
