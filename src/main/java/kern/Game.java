package kern;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import kern.listeners.PlayerListener;
import net.milkbowl.vault.permission.Permission;

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
    public long endTime;
    public long startTime;

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
        endTime = Long.MAX_VALUE;
        startTime = Long.MAX_VALUE;
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
        if (!players.contains(player.getUniqueId())) {
            players.add(player.getUniqueId());

            Bukkit.getLogger().info(
                    "\u00a70(\u00a7d\u00a7l>\u00a70) \u00a77" + player.getDisplayName() + " \u00a75(" + server + ")");
        }

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(YEUHLobby.getPlugin(), "BungeeCord", out.toByteArray());
        PlayerListener.addToSendingToGame(player);

    }

    public synchronized void disable(boolean removeFromList) {
        acceptingNew = false;

        if (gameState != null && gameState.equals("ENDED")) {
            YEUHLobby.setGamesPlayedToday(YEUHLobby.gamesPlayedToday + 1);
            YEUHLobby.setGamesPlayedTotal(YEUHLobby.gamesPlayedTotal + 1);
            if (gameWinners.isEmpty()) Bukkit.getLogger().info("Game ended without a winner being sent!");
            else {
                Set<String> seen = new HashSet<>();
                playing.removeAll(gameWinners);
                for (String winner : gameWinners) {
                    if (seen.contains(winner)) continue;
                    seen.add(winner);
                    if (winner.equalsIgnoreCase("YEUH-BOT")) continue;
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
                if (seen.contains("YEUH-BOT")) {
                    YEUHLobby.broadcastInfoMessage("\u00a7d\u00a7lYEUH-BOT \u00a7fhas won the game!");
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

    public void broadcastMessage(String msg) {
        try {
            boolean first = true;
            for (String s : msg.split("\n")) {
                outPrint.println((first ? "" : "RAW") + "MSG:" + s);
                first = false;
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to Broadcast: " + e.toString());
        }
    }

    public void broadcastRawMessage(String msg) {
        try {
            for (String s : msg.split("\n")) outPrint.println("RAWMSG:" + s);
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to Broadcast: " + e.toString());
        }
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
            YEUHLobby.broadcastRawMessage("\u00a75[Admin] \u00a7fRegistering New Server: \u00a7d" + server,
                    "yeuhlobby.admin", true);

            while (in.hasNextLine()) {
                String nextLine = in.nextLine();
                input = nextLine.split(":");

                lastPing = new Date().getTime();

                switch (input[0]) {
                    case "GAMESTATE":
                        if (input.length != 2) break;
                        gameState = input[1];
                        switch (input[1]) {
                            case "WAITING":
                                acceptingNew = true;
                                YEUHLobby.broadcastInfoMessage(
                                        "Game \u00a7d" + server + " \u00a7fis ready! Use \u00a7d/join \u00a7fto join!");
                                break;
                            case "STARTING":
                                outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats("YEUH-BOT", true));
                                outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats("YEUH-ANIMAL", true));
                                outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats("YEUH-MONSTER", true));

                                for (UUID uuid : players) alive.add(Bukkit.getOfflinePlayer(uuid).getName());
                                for (String p : alive) { YEUHLobby.getScoreKeeper().getStats(p).games++; }
                                for (int i = alive.size(); i < maxSize; i++) alive.add("YEUH-BOT");
                                for (String p : alive) { playing.add(p); }

                                YEUHLobby.broadcastRawMessage(
                                        "\u00a75[Admin] \u00a7d" + server + " \u00a7fis starting!", "yeuhlobby.admin",
                                        true);

                                if (endTime == Long.MAX_VALUE) endTime = new Date().getTime();

                                acceptingNew = false;
                                break;
                            case "PLAYING":
                                endTime = Long.MAX_VALUE;
                                if (startTime == Long.MAX_VALUE) startTime = new Date().getTime();
                                acceptingNew = false;
                                break;
                            case "ENDED":
                                YEUHLobby.broadcastRawMessage("\u00a75[Admin] \u00a7d" + server + " \u00a7fhas ended!",
                                        "yeuhlobby.admin", true);
                                if (endTime == Long.MAX_VALUE) endTime = new Date().getTime();
                                break;
                            default:
                                acceptingNew = false;
                        }
                        continue;
                    case "ACTIVESCENARIOS":
                        if (input.length != 2) break;
                        activeScenarios = input[1];
                        continue;
                    case "CURRENTSIZE":
                        if (input.length != 2) break;
                        currentSize = Integer.parseInt(input[1]);
                        continue;
                    case "MINSIZE":
                        if (input.length != 2) break;
                        minSize = Integer.parseInt(input[1]);
                        continue;
                    case "MAXSIZE":
                        if (input.length != 2) break;
                        maxSize = Integer.parseInt(input[1]);
                        continue;
                    case "DISCONNECTED":
                        if (input.length != 2) break;
                        players.remove(Bukkit.getOfflinePlayer(input[1]).getUniqueId());
                        YEUHLobby.getScoreKeeper().getStats(input[1]).seen();

                        Bukkit.getLogger().info("\u00a70(\u00a7d\u00a7l<\u00a7c\u00a7l-\u00a70) \u00a77" + input[1]
                                + " \u00a75(" + server + ")");

                        continue;
                    case "KILL":
                        if (input.length != 3 && input.length != 4) break;
                        YEUHLobby.getScoreKeeper().kill(input[1], input[2]);
                        outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats(input[1], true));
                        outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats(input[2], true));
                        if (input.length != 4) alive.remove(input[2]);
                        continue;
                    case "DEATH":
                        if (input.length != 2 && input.length != 3) break;
                        if (input.length != 3) alive.remove(input[1]);
                        YEUHLobby.getScoreKeeper().loseGame(maxSize, input[1], alive.toArray(new String[0]));

                        outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats(input[1], true));
                        for (String p : alive)
                            outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats(p, true));
                        continue;
                    case "WIN":
                        if (input.length != 2) break;
                        gameWinners.add(input[1]);

                        continue;
                    case "STATSREQUEST":
                        if (input.length != 2) break;

                        outPrint.println("STATS:" + YEUHLobby.getScoreKeeper().getStats(input[1], true));
                        Permission perms = YEUHLobby.getPlugin().getServer().getServicesManager()
                                .getRegistration(Permission.class).getProvider();
                        String group = perms.getPrimaryGroup(Bukkit.getWorld("creative"), input[1]);
                        if (!group.equalsIgnoreCase("default")) outPrint.println("GROUP:" + input[1] + ":" + group);
                        continue;
                    case "SHUTTINGDOWN":
                        if (input.length != 1) break;
                        YEUHLobby.broadcastRawMessage("\u00a75[Admin] \u00a7fShutting Down Server: \u00a7d" + server,
                                "yeuhlobby.admin", true);
                        continue;
                    default:
                        // nothing
                }

                Bukkit.getLogger().info("[" + server + "] " + String.join(":", input) + " was not recognized!");
            }
        } catch (IOException e) {
            Bukkit.getLogger().warning(e.toString());
        } finally {
            disable(true);
        }
    }

    public String toString() {
        if (gameState.equals("PLAYING")) {
            long now = new Date().getTime();
            long seconds = (now - startTime) / 1000;
            long minutes = seconds / 60;
            seconds = seconds % 60;

            String gameTime = (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
            return "  \u00a75\u00a7l" + server + "\u00a7f: \u00a7d" + gameState + "\n    \u00a7fPlayers: \u00a7d"
                    + currentSize + " \u00a75/ \u00a7d" + maxSize + "\n    \u00a7fGame Time: \u00a7d" + gameTime;
        }
        return "  \u00a75\u00a7l" + server + "\u00a7f: \u00a7d" + gameState + "\n    \u00a7fPlayers: \u00a7d"
                + currentSize + " \u00a75/ \u00a7d" + maxSize + "\n    \u00a7fLobby Open: \u00a7d" + acceptingNew;
    }

    public String toString(int i) {
        return "GAME " + i + " (" + server + "): " + gameState + "\n    Players:" + currentSize + "/" + maxSize + "("
                + minSize + " needed to start game)\n    Lobby Open: " + acceptingNew;
    }

    public String getLastPing(long time) {
        long diff = new Date().getTime() - time;
        if (diff < 1000) return diff + " milliseconds ago";
        if (diff < 60 * 1000) return diff / 1000 + " seconds ago";
        if (diff < 60 * 60 * 1000) return diff / (60 * 1000) + " minutes ago";
        if (diff < 24 * 60 * 60 * 1000) return diff / (60 * 60 * 1000) + " hours ago";
        return diff / (24 * 60 * 60 * 1000) + " days ago";
    }

    public String getLastPing() { return getLastPing(lastPing); }

    public boolean forceKill() {
        disable(true);
        return forceKill(server);
    }

    public static boolean forceKill(String name) {
        File tempScript;
        try {
            tempScript = createTempScript(name);
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

    private static File createTempScript(String name) throws IOException {
        File tempScript = File.createTempFile("script", null);

        Writer streamWriter = new OutputStreamWriter(new FileOutputStream(tempScript));
        PrintWriter printWriter = new PrintWriter(streamWriter);

        printWriter.println("#!/bin/bash");
        printWriter.println("ps a | grep \"java[^.]*" + name + "\" | awk '{print $1}' | xargs kill -9");

        printWriter.close();

        return tempScript;
    }
}