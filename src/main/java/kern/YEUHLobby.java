package kern;

import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import kern.commands.*;
import kern.threads.*;
import kern.listeners.*;

public class YEUHLobby extends JavaPlugin implements PluginMessageListener {

	private static YEUHLobby plugin;
	private static Warden warden;
	private static ScoreKeeper sk;

	public static final String PREFIX = "\u00a75(\u00a7d\u00a7lYEUH\u00a75) \u00a7f";

	private List<Game> games;
	private Queue<Player> playerQueue;

	public static final int MAX_GAMES = 2;

	@Override
	public void onEnable() {
		plugin = this;
		sk = new ScoreKeeper();
		warden = new Warden();
		games = new ArrayList<>();
		playerQueue = new LinkedList<>();

		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

		Bukkit.getScheduler().runTaskAsynchronously(plugin, new ServerSocketThread());
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new CheckServersThread());
		Bukkit.getScheduler().runTask(plugin, new DeleteEntitiesThread());

		Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(), plugin);
		Bukkit.getServer().getPluginManager().registerEvents(new GUIInventoryListener(), plugin);

		getCommand("games").setExecutor(new GamesCommand());
		getCommand("spectate").setExecutor(new SpectateCommand());
		getCommand("rating").setExecutor(new RatingCommand());
		getCommand("opt").setExecutor(new OptCommand());
		getCommand("plugin").setExecutor(new PluginCommand());
		getCommand("help").setExecutor(new HelpCommand());
		getCommand("rules").setExecutor(new RulesCommand());
		getCommand("discord").setExecutor(new DiscordCommand());
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		// i dont care
	}

	@Override
	public void onDisable() {
		sk.storeData();
		warden.save();
		for (Game g : games) {
			g.disable(false);
			games.remove(g);
		}
	}

	public List<Game> getOpenGames() { return games.stream().filter(g -> g.acceptingNew).collect(Collectors.toList()); }

	public List<Game> getPlayingGames() {
		return games.stream().filter(g -> g.gameState.equals("PLAYING") || g.gameState.equals("DEATHMATCH"))
				.collect(Collectors.toList());
	}

	public List<Game> getGames() { return games; }

	public Queue<Player> getPlayerQueue() { return playerQueue; }

	public static Warden getWarden() { return warden; }

	public static ScoreKeeper getScoreKeeper() { return sk; }

	public static YEUHLobby getPlugin() { return plugin; }

}