package at.overflow.bukkit.matrixbridge;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class MatrixPlugin extends JavaPlugin implements Listener, Endpoint {

    private Endpoint receiver;

    private BridgePropertyReader properties;

    public MatrixPlugin() {
        properties = new BridgePropertyReader();

        this.receiver = new BridgeService(this, properties);
    }

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void login(PlayerJoinEvent event) {
        this.receiver.send("Server", "Player " + event.getPlayer().getDisplayName() + " joined the game.");
    }

    @EventHandler
    public void logout(PlayerQuitEvent event) {
        this.receiver.send("Server", "Player " + event.getPlayer().getDisplayName() + " quit the game.");
    }

    @EventHandler
    public void chat(AsyncPlayerChatEvent e){
        this.receiver.send(e.getPlayer().getDisplayName(), e.getMessage());
    }

    @EventHandler
    public void broadcast(BroadcastMessageEvent e){
        // TODO
    }

    @Override
    public void send(String from, String message) {
        getServer().broadcastMessage(ChatColor.GREEN + "[" + from + "]" + ChatColor.WHITE + " " + message);
    }
}
