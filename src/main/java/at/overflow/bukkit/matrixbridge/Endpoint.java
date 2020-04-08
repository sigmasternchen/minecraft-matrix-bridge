package at.overflow.bukkit.matrixbridge;

import org.bukkit.entity.Player;

import java.util.Collection;

public interface Endpoint {
    void send(String from, String message);
    Collection<? extends Player> getAllOnlinePlayers();
}
