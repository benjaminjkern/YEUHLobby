package kern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class Game implements Runnable {
    public String server;
    public int minSize;
    public int maxSize;
    public int currentSize;
    public boolean acceptingNew;
    public String gameState;

    private PrintWriter outPrint;

    private Socket socket;

    public Game(Socket socket) {
        this.socket = socket;
        try {
            outPrint = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void sendPlayerToGame(Player player) {
        outPrint.println("RATING:" + player.getName() + ":" + YEUHLobby.getPlugin().getScoreKeeper().getScore(player));

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(YEUHLobby.getPlugin(), "BungeeCord", out.toByteArray());
    }

    public synchronized void disable(boolean removeFromList) {
        acceptingNew = false;
        try {
            socket.close();
        } catch (IOException e) {}
        if (removeFromList) YEUHLobby.getPlugin().getGames().remove(this);
    }

    public void run() {
        try (Scanner in = new Scanner(socket.getInputStream())) {

            // get servername
            String input = in.nextLine();
            if (!input.startsWith("SERVERNAME:")) throw new Exception(
                    "First message must be server name! Otherwise I don't know what server to connect to!");
            String serverName = input.substring("SERVERNAME:".length());
            for (Game g : YEUHLobby.getPlugin().getGames())
                if (serverName.equals(g.server)) throw new Exception("That server is already registered!");
            server = serverName;
            Bukkit.getLogger().info("Registering new server: " + server);

            while (in.hasNextLine()) {
                input = in.nextLine();
                // Bukkit.getLogger().info("[" + server + "] " + input);

                if (input.startsWith("GAMESTATE:")) {
                    gameState = input.substring("GAMESTATE:".length());
                    switch (gameState) {
                        case "WAITING":
                        case "STARTING":
                            acceptingNew = true;
                            break;
                        default:
                            acceptingNew = false;
                    }
                    continue;
                }
                if (input.startsWith("CURRENTSIZE:")) {
                    currentSize = Integer.parseInt(input.substring("CURRENTSIZE:".length()));
                    continue;
                }
                if (input.startsWith("MINSIZE:")) {
                    minSize = Integer.parseInt(input.substring("MINSIZE:".length()));
                    continue;
                }
                if (input.startsWith("MAXSIZE:")) {
                    maxSize = Integer.parseInt(input.substring("MAXSIZE:".length()));
                    continue;
                }
                if (input.startsWith("RATING:")) {
                    // update player rating
                    String[] inputBits = input.split(":");
                    if (inputBits.length == 3) {
                        YEUHLobby.getPlugin().getScoreKeeper().setScore(inputBits[1], Double.parseDouble(inputBits[2]));
                        continue;
                    }
                }
                if (input.startsWith("RATINGREQUEST:")) {
                    // update player rating
                    String[] inputBits = input.split(":");
                    if (inputBits.length == 2) {
                        try {
                            double value = YEUHLobby.getPlugin().getScoreKeeper().getScore(inputBits[1], true);
                            outPrint.println("RATING:" + inputBits[1] + ":" + value);
                        } catch (PlayerDoesntExistException e) {
                            outPrint.println("RATING:" + inputBits[1] + ":" + null);
                        }
                        continue;
                    }
                }
                if (input.equals("SHUTTINGDOWN")) return;

                Bukkit.getLogger().info(input + " was not recognized!");
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning(e.getStackTrace()[0].toString());
        } finally {
            disable(true);
        }
    }

    public String toString() {
        return "  " + server + ": " + gameState + "\n    Players: " + currentSize + " / " + maxSize + " (" + minSize
                + " needed to start game)\n    Lobby Open: " + acceptingNew;
    }

    public String toString(int i) {
        return "GAME " + i + " (" + server + "): " + gameState + "\n    Players:" + currentSize + "/" + maxSize + "("
                + minSize + " needed to start game)\n    Lobby Open: " + acceptingNew;
    }
}