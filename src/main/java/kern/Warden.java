package kern;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import org.bukkit.Bukkit;

public class Warden {

    private Scanner s;
    private File f;

    Set<String> playersThatHaveSigned;

    public Warden() {
        playersThatHaveSigned = new HashSet<>();
        s = null;
        resetScanner();
    }

    private void resetScanner() {
        try {
            YEUHLobby.getPlugin().getDataFolder().mkdir();
            f = new File(YEUHLobby.getPlugin().getDataFolder(), "usersSigned.txt");
            f.createNewFile();
            s = new Scanner(f);
        } catch (IOException e) {
            Bukkit.getLogger().warning("[YEUHLobby] Something went wrong when opening userScores.txt!");
        }
    }

    public boolean hasPlayerSigned(String playerName) {
        if (playersThatHaveSigned.contains(playerName)) return true;

        if (s == null) return false;

        while (s.hasNextLine()) {
            String player = s.nextLine();

            if (!playersThatHaveSigned.contains(player))
                // if it finds other players while looking for one in specifics, it adds them to
                // the cache anyways
                playersThatHaveSigned.add(player);

            if (player.equals(playerName)) { return true; }
        }

        return false;
    }

    public void sign(String playerName) {
        playersThatHaveSigned.add(playerName);
        Bukkit.dispatchCommand(Bukkit.getPlayerExact(playerName), "opt in");
    }

    public void save() {
        Bukkit.getLogger().info("[UhcCore] Storing User Rule Signed data.");
        // finish the scanner
        while (s.hasNextLine()) {
            String player = s.nextLine();

            if (!playersThatHaveSigned.contains(player)) playersThatHaveSigned.add(player);
        }
        s.close();

        // write everything in the cache
        try (FileWriter myWriter = new FileWriter(f, false)) {
            for (String p : playersThatHaveSigned) { myWriter.write(p + "\n"); }
            myWriter.close();
        } catch (IOException e) {
            Bukkit.getLogger().warning("[UhcCore] Something went wrong when writing to usersSigned.txt!");
        }

        resetScanner();
        playersThatHaveSigned.clear();
    }

}
