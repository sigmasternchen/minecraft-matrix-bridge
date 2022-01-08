package at.overflow.bukkit.matrixbridge;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collection;

public class MatrixPlugin extends JavaPlugin implements Listener, Endpoint {

    final private static int MAX_LENGTH = 100;

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
    public void onDisable() {}

    @EventHandler
    public void login(PlayerJoinEvent event) {
        this.receiver.send(this.properties.getMinecraftServerName(), "Player " + event.getPlayer().getDisplayName() + " joined the game.");
    }

    @EventHandler
    public void logout(PlayerQuitEvent event) {
        this.receiver.send(this.properties.getMinecraftServerName(), "Player " + event.getPlayer().getDisplayName() + " quit the game.");
    }

    @EventHandler
    public void chat(AsyncPlayerChatEvent e){
        this.receiver.send(e.getPlayer().getName(), e.getMessage());
    }

    @EventHandler
    public void death(PlayerDeathEvent e){
      this.receiver.send(e.getPlayer().getName(), e.getDeathMessage());
    }

    @EventHandler
    public void sleep(TimeSkipEvent e){
      if(e.getSkipReason() == TimeSkipEvent.SkipReason.NIGHT_SKIP){
	this.receiver.send(this.properties.getMinecraftServerName(), "Everyone went to bed");
      }
    }

    @EventHandler
    public void server(ServerCommandEvent e) {
        if (!e.getCommand().substring(0, 4).toLowerCase().equals("say "))
            return;

        this.receiver.send(this.properties.getMinecraftServerName(), e.getCommand().substring(4));
    }

    @Override
    public void send(String from, String message) {
        if (message.length() > MAX_LENGTH) {
            message = message.substring(0, MAX_LENGTH) + " [...]";
        }

        getServer().broadcastMessage(ChatColor.GREEN + "<" + from + ">" + ChatColor.WHITE + " " + message);
    }

    @Override
    public Collection<? extends Player> getAllOnlinePlayers() {
        return Bukkit.getOnlinePlayers();
    }
}
