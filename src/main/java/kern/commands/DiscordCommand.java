package kern.commands;

import kern.YEUHLobby;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class DiscordCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        sender.sendMessage(YEUHLobby.PREFIX
                + "Click to join our discord community! \u00a75\u00a7l>\u00a7d\u00a7l>\u00a75\u00a7l> \u00a77\u00a7nhttps://discord.gg/8ZDYBj2");
        return true;
    }
}
