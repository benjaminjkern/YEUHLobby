package kern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import kern.listeners.PlayerListener;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class Game implements Runnable {
    public String server;
    public int minSize;
    public int maxSize;
    public int currentSize;
    public boolean acceptingNew;
    public String gameState;

    public PrintWriter outPrint;

    private Set<UUID> players;
    private Set<String> disconnectedPlayers;
    private Set<String> winners;

    private Socket socket;

    public Game(Socket socket) {
        this.players = new HashSet<>();
        this.disconnectedPlayers = new HashSet<>();
        this.winners = new HashSet<>();

        this.socket = socket;
        try {
            outPrint = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Set<UUID> getPlayers() { return players; }

    public Set<String> getDisconnectedPlayers() { return disconnectedPlayers; }

    public Set<String> getWinners() { return winners; }

    public void sendPlayerToGame(Player player) {
        PlayerListener.addToSendingToGame(player);
        players.add(player.getUniqueId());

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(YEUHLobby.getPlugin(), "BungeeCord", out.toByteArray());
    }

    public synchronized void disable(boolean removeFromList) {
        acceptingNew = false;

        for (String winner : winners) {
            Bukkit.getLogger().info(YEUHLobby.PREFIX + "\u00a7d\u00a7l" + winner + " \u00a7fhas won the game!");
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(YEUHLobby.PREFIX + "\u00a7d\u00a7l" + winner + " \u00a7fhas won the game!");
            }
            winners.remove(winner);
        }

        try {
            socket.close();
        } catch (IOException e) {}

        if (removeFromList) YEUHLobby.getPlugin().getGames().remove(this);
    }

    public void run() {
        try (Scanner in = new Scanner(socket.getInputStream())) {

            // get servername before doing anything else
            String[] input = in.nextLine().split(":");
            if (!input[0].equals("SERVERNAME")) throw new IllegalArgumentException(
                    "First message must be server name! Otherwise I don't know what server to connect to!");
            for (Game g : YEUHLobby.getPlugin().getGames())
                if (input[1].equals(g.server)) throw new IllegalArgumentException("That server is already registered!");
            server = input[1];
            Bukkit.getLogger().info("Registering New Server: " + server);

            boolean err;
            Player player;

            while (in.hasNextLine()) {
                input = in.nextLine().split(":");

                err = true;
                switch (input[0]) {
                    case "GAMESTATE":
                        if (input.length != 2) break;
                        gameState = input[1];
                        switch (input[1]) {
                            case "WAITING":
                                acceptingNew = true;
                                break;
                            case "STARTING":
                                outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats("YEUH-BOT", true));
                                outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats("YEUH-ANIMAL", true));
                                outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats("YEUH-MONSTER", true));
                                acceptingNew = false;
                                break;
                            default:
                                acceptingNew = false;
                        }
                        err = false;
                        break;
                    case "CURRENTSIZE":
                        if (input.length != 2) break;
                        currentSize = Integer.parseInt(input[1]);
                        err = false;
                        break;
                    case "MINSIZE":
                        if (input.length != 2) break;
                        minSize = Integer.parseInt(input[1]);
                        err = false;
                        break;
                    case "MAXSIZE":
                        if (input.length != 2) break;
                        maxSize = Integer.parseInt(input[1]);
                        err = false;
                        break;
                    case "DISCONNECTED":
                        if (input.length != 2) break;
                        disconnectedPlayers.add(input[1]);
                        players.remove(input[1]);

                        Player p = Bukkit.getPlayerExact(input[1]);
                        if (p != null) p.sendMessage(YEUHLobby.PREFIX
                                + "You disconnected while a game was running! Use \u00a7d/join \u00a7fto go back to the game.");

                        err = false;
                        break;
                    case "TIMEOUT":
                        if (input.length != 2) break;
                        disconnectedPlayers.remove(input[1]);
                        players.remove(input[1]); // redundant
                        err = false;
                        break;
                    case "KILL":
                        if (input.length != 3) break;
                        YEUHLobby.getScoreKeeper().kill(input[1], input[2]);
                        outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats(input[1], true));
                        outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats(input[2], true));
                        err = false;
                        break;
                    case "DEATH":
                        if (input.length != 2) break;
                        YEUHLobby.getScoreKeeper().getStats(input[1]).environmentDeath();
                        outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats(input[1], true));
                        err = false;
                        break;
                    case "WIN":
                        if (input.length != 2) break;
                        YEUHLobby.getScoreKeeper().getStats(input[1]).winGame();
                        outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats(input[1], true));

                        player = Bukkit.getPlayerExact(input[1]);
                        if (player != null) {
                            player.sendMessage(YEUHLobby.PREFIX
                                    + "\u00a7dCongrats on your win! You now have \u00a7f\u00a7l"
                                    + YEUHLobby.getScoreKeeper().getStats(input[1], true).wins + "\u00a7d wins!");
                        } else {
                            winners.add(input[1]);
                        }
                        err = false;
                        break;
                    case "STATSREQUEST":
                        if (input.length != 2) break;

                        outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats(input[1], true));

                        err = false;
                        break;
                    case "SHUTTINGDOWN":
                        if (input.length != 1) break;
                        Bukkit.getLogger().info("Shutting Down Server: " + server);
                        err = false;
                        break;
                    default:
                        // nothing
                }

                if (err)
                    Bukkit.getLogger().info("[" + server + "] " + String.join(":", input) + " was not recognized!");
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning(e.getStackTrace()[0].toString());
        } finally {
            disable(true);
        }
    }

    public String toString() {
        return "  \u00a75\u00a7l" + server + "\u00a7f: \u00a7d" + gameState + "\n    \u00a7fPlayers: \u00a7d"
                + currentSize + " \u00a75/ \u00a7d" + maxSize + " \u00a77(" + minSize
                + " needed to start game)\n    \u00a7fLobby Open: \u00a7d" + acceptingNew;
    }

    public String toString(int i) {
        return "GAME " + i + " (" + server + "): " + gameState + "\n    Players:" + currentSize + "/" + maxSize + "("
                + minSize + " needed to start game)\n    Lobby Open: " + acceptingNew;
    }
}