package kern;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import kern.commands.*;
import kern.threads.*;
import kern.listeners.*;
import kern.nations.NationListener;
import kern.nations.NationManager;

public class YEUHLobby extends JavaPlugin implements PluginMessageListener {

	private static YEUHLobby plugin;
	private static ScoreKeeper sk;

	public static final String PREFIX = "\u00a75(\u00a7d\u00a7lYEUH\u00a75) \u00a7f";

	private List<Game> games;

	public static int gamesPlayedToday = 0;
	public static int gamesPlayedTotal = 0;

	public static final int MAX_GAMES = 2;

	private ServerSocketThread socketThread;

	@Override
	public void onEnable() {
		plugin = this;
		sk = new ScoreKeeper();
		games = new ArrayList<>();

		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

		socketThread = new ServerSocketThread();
		Bukkit.getScheduler().runTaskAsynchronously(plugin, socketThread);
		Bukkit.getScheduler().runTask(plugin, new DeleteEntitiesThread());
		Bukkit.getScheduler().runTask(plugin, new AnnouncementThread());
		Bukkit.getScheduler().runTask(plugin, new KillServerThread());
		Date d = new Date();
		Bukkit.getScheduler().runTaskLater(plugin, new LowerRatingsThread(),
				(long) (20 * ((24 - d.getHours()) * 3600 + (60 - d.getMinutes()) * 60 + (60 - d.getSeconds()))));
		Bukkit.getScheduler().runTaskLater(plugin, () -> { sk.updateBoards(); }, 20 * 60);

		Bukkit.getServer().getPluginManager().registerEvents(new PlayerListener(), plugin);
		Bukkit.getServer().getPluginManager().registerEvents(new GUIInventoryListener(), plugin);
		Bukkit.getServer().getPluginManager().registerEvents(new CommandItemListener(), plugin);
		Bukkit.getServer().getPluginManager().registerEvents(new CreativeListener(), plugin);
		Bukkit.getServer().getPluginManager().registerEvents(new NationListener(), plugin);

		getCommand("games").setExecutor(new GamesCommand());
		getCommand("spectate").setExecutor(new SpectateCommand());
		getCommand("rating").setExecutor(new RatingCommand());
		getCommand("stats").setExecutor(new StatsCommand(sk));
		getCommand("plugin").setExecutor(new PluginCommand());
		getCommand("help").setExecutor(new HelpCommand());
		getCommand("rules").setExecutor(new RulesCommand());
		getCommand("discord").setExecutor(new DiscordCommand());
		getCommand("updatescenarioboard").setExecutor(new UpdateScenarioBoardCommand());
		getCommand("updatetopboards").setExecutor(new UpdateTopBoardsCommand());
		getCommand("list").setExecutor(new ListCommand());
		getCommand("join").setExecutor(new JoinCommand());
		getCommand("fakeleave").setExecutor(new FakeLeaveCommand());
		getCommand("patreon").setExecutor(new PatreonCommand());
		getCommand("member").setExecutor(new MemberCommand());
		getCommand("removeallentities").setExecutor(new RemoveAllCommand());
		getCommand("nation").setExecutor(new NationCommand());
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		// i dont care
	}

	public static void setGamesPlayedToday(int gamesPlayedToday) { YEUHLobby.gamesPlayedToday = gamesPlayedToday; }

	public static void setGamesPlayedTotal(int gamesPlayedTotal) { YEUHLobby.gamesPlayedTotal = gamesPlayedTotal; }

	@Override
	public void onDisable() {
		sk.storeData();
		try {
			socketThread.listener.close();
		} catch (IOException e) {}
		try {
			for (Game g : games) {
				g.disable(false);
				games.remove(g);
			}
		} catch (ConcurrentModificationException e) {}
	}

	public List<Game> getOpenGames() {
		List<Game> openGames = games.stream().filter(g -> g != null && g.acceptingNew).collect(Collectors.toList());
		openGames.sort((a, b) -> b.currentSize - a.currentSize);
		return openGames;
	}

	public List<Game> getPlayingGames() {
		return games.stream()
				.filter(g -> g != null && g.gameState.equals("PLAYING") || g.gameState.equals("DEATHMATCH"))
				.collect(Collectors.toList());
	}

	public List<Game> getStartingGames() {
		return games.stream().filter(g -> g != null && g.gameState.equals("WAITING") && g.currentSize > 0)
				.collect(Collectors.toList());
	}

	public List<Game> getGames() { return games; }

	public static ScoreKeeper getScoreKeeper() { return sk; }

	public static YEUHLobby getPlugin() { return plugin; }

	public static void broadcastInfoMessage(String s, String perm, boolean includeConsole) {
		if (includeConsole) Bukkit.getLogger().info(YEUHLobby.PREFIX + s);
		Bukkit.getOnlinePlayers()
				.forEach(player -> {
					if (perm.isEmpty() || player.hasPermission(perm)) player.sendMessage(YEUHLobby.PREFIX + s);
				});
	}

	public static void broadcastInfoMessage(String s) { broadcastInfoMessage(s, "", false); }

	public static void broadcastRawMessage(String s, String perm, boolean includeConsole) {
		if (includeConsole) Bukkit.getLogger().info(s);
		Bukkit.getOnlinePlayers()
				.forEach(player -> { if (perm.isEmpty() || player.hasPermission(perm)) player.sendMessage(s); });
	}

	public static void broadcastRawMessage(String s) { broadcastRawMessage(s, "", true); }

}