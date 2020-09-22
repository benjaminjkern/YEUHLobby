public class Game {
    private String server;
    public int minSize;
    public int maxSize;
    public int currentSize;
    public boolean ready;

    public Game(String server, int minSize, int maxSize) {
        this.server = server;
        this.minSize = minSize;
        this.maxSize = maxSize;
        currentSize = 0;
        ready = false;
    }

	public void sendPlayerToGame(Player player) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);
        player.sendPluginMessage(YEUHLobby.getPlugin(), "BungeeCord", out.toByteArray());
    }
    
    public boolean readyForPlayers(Player player) {

    }
}