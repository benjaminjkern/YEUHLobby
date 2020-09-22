package kern;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class YEUHLobby extends JavaPlugin implements PluginMessageListener {

	private static YEUHLobby plugin;

	private List<Game> games;
	private Queue<Player> playerQueue;

	@Override
	public void onEnable() {
		plugin = this;
		gamesReady = new HashSet<>();
		games = new List<>();

		for (int i = 0; i < 1;i++) {
			games.add(new Game("solo"+i, 3, 20));
		}

		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
		this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);
		
		Bukkit.getScheduler().runTaskAsynchronously(new CheckServersThread());
		Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
	}
  
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
	  if (!channel.equals("BungeeCord")) return;

	  ByteArrayDataInput in = ByteStreams.newDataInput(message);
	  String subchannel = in.readUTF();
	  if (subchannel.equals("SomeSubChannel")) {
		// Use the code sample in the 'Response' sections below to read
		// the data.
	  }
	}

	public Queue<Player> getPlayerQueue() { return playerQueue; }

	public static YEUHLobby getPlugin() { return plugin; }

}