package kern.threads;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;

import kern.Game;
import kern.YEUHLobby;

public class ServerSocketThread implements Runnable {

	public ServerSocket listener;

	@Override
	public void run() {
		try {
			listener = new ServerSocket(58901);
			ExecutorService pool = Executors.newFixedThreadPool(YEUHLobby.MAX_GAMES + 1);
			while (!listener.isClosed() && YEUHLobby.getPlugin().isEnabled()) {
				Socket incomingGame = listener.accept();
				Game g = new Game(incomingGame);
				addGame(g);
				pool.execute(g);
			}
		} catch (IOException e) {
			// Bukkit.getLogger().warning(e.getStackTrace()[0].toString());
		}
	}

	private synchronized void addGame(Game g) { YEUHLobby.getPlugin().getGames().add(g); }
}