package kern.commands;

import kern.YEUHLobby;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MemberCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        sender.sendMessage(YEUHLobby.PREFIX
                + "Wanna become a member of \u00a7d\u00a7lYEUH\u00a7f? It's easy!\n  Just click on this link and leave a comment about us! \u00a77(Make sure to leave your Minecraft Username)\n  \u00a75\u00a7l>\u00a7d\u00a7l>\u00a75\u00a7l> \u00a77\u00a7nhttps://www.minecraftforum.net/forums/servers-java-edition/pc-servers/3035898-yeuh-battle-royale-uhc");
        return true;
    }
}
