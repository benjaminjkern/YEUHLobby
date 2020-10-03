package kern.threads;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.bukkit.Bukkit;

import kern.Game;
import kern.YEUHLobby;

public class ServerSocketThread implements Runnable {

	@Override
	public void run() {
		try (ServerSocket listener = new ServerSocket(58901)) {
			ExecutorService pool = Executors.newFixedThreadPool(YEUHLobby.MAX_GAMES + 1);
			while (!listener.isClosed()) {
				Socket incomingGame = listener.accept();
				Game g = new Game(incomingGame);
				addGame(g);
				pool.execute(g);
			}
		} catch (Exception e) {
			Bukkit.getLogger().warning(e.getStackTrace()[0].toString());
		}
	}

	private synchronized void addGame(Game g) { YEUHLobby.getPlugin().getGames().add(g); }
}