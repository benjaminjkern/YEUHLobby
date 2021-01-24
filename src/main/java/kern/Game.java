package kern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import kern.listeners.PlayerListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
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
    public String activeScenarios;

    public long lastPing;

    public PrintWriter outPrint;

    private Set<UUID> players;
    private List<String> playing;
    private List<String> alive;
    private List<String> gameWinners;

    private Socket socket;

    public Game(Socket socket) {
        this.players = new HashSet<>();
        this.alive = new ArrayList<>();
        this.playing = new ArrayList<>();
        this.gameWinners = new ArrayList<>();

        this.socket = socket;

        lastPing = new Date().getTime();
        try {
            outPrint = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public Set<UUID> getPlayers() { return players; }

    public List<String> getAlive() { return alive; }

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

        if (gameState.equals("ENDED")) {
            Set<String> seen = new HashSet<>();
            if (gameWinners.isEmpty()) Bukkit.getLogger().info("Game ended without a winner being sent!");
            else {
                playing.removeAll(gameWinners);
                for (String winner : gameWinners) {
                    if (seen.contains(winner)) continue;
                    seen.add(winner);
                    YEUHLobby.getScoreKeeper().winGame(maxSize * (maxSize - playing.size()), winner,
                            playing.toArray(new String[0]));
                    Bukkit.getLogger().info(YEUHLobby.PREFIX + "\u00a7d\u00a7l" + winner + " \u00a7fhas won the game!");
                    if (gameWinners.size() == 1) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.sendMessage(YEUHLobby.PREFIX + "\u00a7d\u00a7l" + winner + " \u00a7fhas won the game!");
                        }
                    }

                    Player playerWinner = Bukkit.getPlayerExact(winner);
                    if (playerWinner != null) playerWinner
                            .sendMessage(YEUHLobby.PREFIX + "\u00a7dCongrats on your win! You now have \u00a7f\u00a7l"
                                    + YEUHLobby.getScoreKeeper().getStats(winner, true).wins + "\u00a7d wins!");
                }
            }
        }

        if (YEUHLobby.getPlugin().isEnabled()) {
            Bukkit.getScheduler().runTask(YEUHLobby.getPlugin(), () -> YEUHLobby.getScoreKeeper().updateBoards());
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
            YEUHLobby.broadcastInfoMessage("\u00a75[Admin] \u00a7fRegistering New Server: \u00a7d" + server,
                    "yeuhlobby.admin");

            boolean err;

            while (in.hasNextLine()) {
                String nextLine = in.nextLine();
                input = nextLine.split(":");

                lastPing = new Date().getTime();

                err = true;
                switch (input[0]) {
                    case "GAMESTATE":
                        if (input.length != 2) break;
                        gameState = input[1];
                        switch (input[1]) {
                            case "WAITING":
                                acceptingNew = true;
                                YEUHLobby.broadcastInfoMessage("Game \u00a7d" + server + " \u00a7fis ready!");
                                break;
                            case "STARTING":
                                outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats("YEUH-BOT", true));
                                outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats("YEUH-ANIMAL", true));
                                outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats("YEUH-MONSTER", true));

                                for (UUID uuid : players) alive.add(Bukkit.getOfflinePlayer(uuid).getName());
                                for (String p : alive) { YEUHLobby.getScoreKeeper().getStats(p).games++; }
                                for (int i = alive.size(); i < maxSize; i++) alive.add("YEUH-BOT");
                                for (String p : alive) { playing.add(p); }

                                YEUHLobby.broadcastInfoMessage(
                                        "\u00a75[Admin] \u00a7d" + server + " \u00a7fis starting!", "yeuhlobby.admin");

                                acceptingNew = false;
                                break;
                            case "ENDED":
                                YEUHLobby.broadcastInfoMessage("\u00a75[Admin] \u00a7d" + server + " \u00a7fhas ended!",
                                        "yeuhlobby.admin");
                            default:
                                acceptingNew = false;
                        }
                        err = false;
                        break;
                    case "ACTIVESCENARIOS":
                        if (input.length != 2) break;
                        activeScenarios = input[1];
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
                        players.remove(Bukkit.getOfflinePlayer(input[1]).getUniqueId());

                        if (alive.contains(input[1])) {
                            Player p = Bukkit.getPlayerExact(input[1]);
                            if (p != null) p.sendMessage(YEUHLobby.PREFIX
                                    + "You disconnected while a game was running! Use \u00a7d/join \u00a7fto go back to the game.");
                        }

                        err = false;
                        break;
                    case "KILL":
                        if (input.length != 3 && input.length != 4) break;
                        YEUHLobby.getScoreKeeper().kill(input[1], input[2]);
                        outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats(input[1], true));
                        outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats(input[2], true));
                        if (input.length != 4) alive.remove(input[2]);
                        err = false;
                        break;
                    case "DEATH":
                        if (input.length != 2 && input.length != 3) break;
                        if (input.length != 3) alive.remove(input[1]);
                        YEUHLobby.getScoreKeeper().loseGame(maxSize, input[1], alive.toArray(new String[0]));

                        outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats(input[1], true));
                        for (String p : alive)
                            outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats(p, true));
                        err = false;
                        break;
                    case "WIN":
                        if (input.length != 2) break;
                        gameWinners.add(input[1]);
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

    public String getLastPing() {
        long diff = new Date().getTime() - lastPing;
        if (diff < 1000) return diff + " milliseconds ago";
        if (diff < 60 * 1000) return diff / 1000 + " seconds ago";
        if (diff < 60 * 60 * 1000) return diff / (60 * 1000) + " minutes ago";
        if (diff < 24 * 60 * 60 * 1000) return diff / (60 * 60 * 1000) + " hours ago";
        return diff / (24 * 60 * 60 * 1000) + " days ago";
    }

    public boolean forceKill() {
        disable(true);

        File tempScript;
        try {
            tempScript = createTempScript();
        } catch (IOException e) {
            return false;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder("bash", tempScript.toString());
            pb.inheritIO();
            Process process = pb.start();
            return process.waitFor() == 0;
        } catch (Exception e) {}
        tempScript.delete();
        return false;
    }

    public File createTempScript() throws IOException {
        File tempScript = File.createTempFile("script", null);

        Writer streamWriter = new OutputStreamWriter(new FileOutputStream(tempScript));
        PrintWriter printWriter = new PrintWriter(streamWriter);

        printWriter.println("#!/bin/bash");
        printWriter.println("ps a | grep \"java[^.]*" + server + "\" | awk '{print $1}' | xargs kill -9");

        printWriter.close();

        return tempScript;
    }
}