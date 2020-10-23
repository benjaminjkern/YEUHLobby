package kern.commands;

import kern.YEUHLobby;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class PatreonCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        sender.sendMessage(YEUHLobby.PREFIX
                + "Like the server? Click to go to our Patreon Page! \u00a75\u00a7l>\u00a7d\u00a7l>\u00a75\u00a7l> \u00a77\u00a7nhttps://patreon.com/yeuh/");
        return true;
    }
}
