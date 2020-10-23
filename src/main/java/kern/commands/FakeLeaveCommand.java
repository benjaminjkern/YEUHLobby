package kern.commands;

import org.bukkit.entity.Player;

import kern.listeners.PlayerListener;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class FakeLeaveCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission("yeuhlobby.fakeleave")) return true;
        if (!(sender instanceof Player)) return false;

        PlayerListener.addToSendingToGame((Player) sender);
        sender.sendMessage("\u00a77You will now appear as if you are joining a game when you leave the server.");
        return true;
    }
}
