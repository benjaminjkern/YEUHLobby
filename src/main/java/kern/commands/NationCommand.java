package kern.commands;

import kern.nations.NationManager;
import kern.nations.NationPlayer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NationCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (args.length == 0) return false;
        switch (args[0].toLowerCase()) {
            case "info":
                NationManager.sendNationInfo(sender);
                return true;
            case "help":
                sender.sendMessage("\u00a7d\u00a7l\u00a7oYEUH \u00a7fNation Help:");
                sender.sendMessage("  - \u00a7d/nation help");
                sender.sendMessage("    Displays this help menu.");
                sender.sendMessage("  - \u00a7d/nation info");
                sender.sendMessage("    Displays info about the nations you are a part of.");
                sender.sendMessage("  - \u00a7d/nation money");
                sender.sendMessage("    Displays your money.");
                sender.sendMessage("  - \u00a7d/nation map");
                sender.sendMessage("    Displays the nation map.");
                sender.sendMessage(
                        "  - \u00a7d/nation create \u00a7f(Aliases: \u00a7d/nation new\u00a7f, \u00a7d/nation found\u00a7f)");
                sender.sendMessage("    Create a new nation. You cannot be the leader of multiple nations");
                sender.sendMessage("  - \u00a7d/nation createcity");
                sender.sendMessage("    Create a new city.");
                sender.sendMessage("\u00a7dNEED TO FINISH THIS");
                return true;
        }
        if (sender instanceof Player && ((Player) sender).getWorld().getName().equals(NationManager.NATION_WORLD)) {
            NationPlayer nationPlayer = NationManager.getPlayer((Player) sender);
            switch (args[0].toLowerCase()) {
                case "money":
                    if (args.length > 1) { nationPlayer.money = Double.parseDouble(args[1]); }

                    sender.sendMessage("Your money: " + NationManager.CURRENCY + nationPlayer.money);
                    return true;
                case "map":
                    nationPlayer.sendNationMap();
                    return true;
                case "create":
                case "new":
                case "found":
                    if (args.length == 1) nationPlayer.createNation();
                    else if (args.length == 2) {
                        nationPlayer.createNation(args[1]);
                    } else {
                        nationPlayer.createNation(args[1], args[2]);
                    }
                    return true;
                case "createcity":
                case "newcity":
                case "foundcity":
                    if (args.length == 1) nationPlayer.createCity();
                    else if (args.length == 2) {
                        nationPlayer.createCity(args[1]);
                    } else {
                        nationPlayer.createCity(args[1], args[2]);
                    }
                    return true;
                case "claim":
                    if (args.length == 1) nationPlayer.claimChunks();
                    else {
                        nationPlayer.claimChunks(args[1]);
                    }
                    return true;
                case "setspawn":
                    nationPlayer.setSpawn();
                    return true;
                case "tp":
                case "teleport":
                    if (args.length == 1) nationPlayer.teleport();
                    else if (args.length == 2) nationPlayer.teleport(args[1]);
                    else nationPlayer.teleport(args[1], args[2]);
                    return true;
                case "join":
                    if (args.length == 1) nationPlayer.joinNation();
                    else nationPlayer.joinNation(args[1]);
                    return true;
                case "leave":
                    if (args.length == 1) nationPlayer.leaveNation();
                    else nationPlayer.leaveNation(args[1]);
                    return true;
                case "invite":
                    if (args.length == 1) sender
                            .sendMessage("\u00a7cPlease specify a player with \u00a7f/nation invite [Player]\u00a7c!");
                    else if (args.length == 2) nationPlayer.invitePlayer(args[1]);
                    else nationPlayer.invitePlayer(args[1], args[2]);
                    return true;
                case "kick":
                    if (args.length == 1)
                        sender.sendMessage("\u00a7cPlease specify a player with \u00a7f/nation kick [Player]\u00a7c!");
                    else if (args.length == 2) nationPlayer.kickPlayer(args[1]);
                    else nationPlayer.kickPlayer(args[1], args[2]);
                    return true;
                case "leaveconfirm":
                    if (args.length == 1) nationPlayer.leaveNationConfirm();
                    else nationPlayer.leaveNationConfirm(args[1]);
                    return true;
                case "joincity":
                    if (args.length == 1) nationPlayer.joinCity();
                    else nationPlayer.joinCity(args[1]);
                    return true;
                case "leavecity":
                    // if (args.length == 1) nationPlayer.leaveCity();
                    // else nationPlayer.leaveCity(args[1]);
                    // return true;
                case "Unimplemented":
                    sender.sendMessage("Unimplemented!");
                    return true;
            }
        }

        sender.sendMessage("Command not found. Use \u00a7d/nation help \u00a7ffor help.");
        return true;
    }
}
