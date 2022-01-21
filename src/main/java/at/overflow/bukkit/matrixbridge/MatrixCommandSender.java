package at.overflow.bukkit.matrixbridge;

import java.util.UUID;
import java.util.Set;
import java.util.ArrayList;
import com.google.gson.JsonObject;
import io.kamax.matrix.hs._MatrixRoom;
import net.kyori.adventure.text.Component;
import org.bukkit.Server;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;
import java.lang.System;

public class MatrixCommandSender implements RemoteConsoleCommandSender {
  private _MatrixRoom room;
  private String eventId;
  private Server s;
  private ArrayList<String> buffer;
  private long lastSend;
  
  public MatrixCommandSender(_MatrixRoom r, String event, Server s) {
    this.room = r;
    this.eventId = event;
    this.s = s;

    this.buffer = new ArrayList();
    this.lastSend = System.currentTimeMillis();
  }

  public void sendBuffer() {
    JsonObject reply = new JsonObject();
    JsonObject relates = new JsonObject();
    JsonObject inReply = new JsonObject();

    String message = String.join("\n", this.buffer);

    inReply.addProperty("event_id", this.eventId);
    relates.add("m.in_reply_to", inReply);
    
    reply.addProperty("msgtype", "m.text");
    reply.addProperty("body", stripMessage(message));

    reply.add("m.relates_to", relates);
    
    this.room.sendEvent("m.room.message", reply);

    this.buffer = new ArrayList();
    this.lastSend = System.currentTimeMillis();
  }

  private String stripMessage(String m) {
    final String[] codes = {
      "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a",
      "b", "c", "d", "e", "f", "k", "l", "m", "n", "o", "r"};
    for(String c : codes) {
      m = m.replaceAll("§" + c, "");
    }
    return m;
  }

  @Override
  public void sendMessage(String message) {
    this.buffer.add(message);
    if(System.currentTimeMillis() - this.lastSend > 1000) {
      this.sendBuffer();
    }
  }
  
  @Override
  public void sendMessage(String messages[]) {
    for(String m : messages) {
      this.sendMessage(m);
    }
  }

  @Override
  public void sendMessage(UUID sender, String message) {
    this.sendMessage(message);
  }
  
  @Override
  public void sendMessage(UUID sender, String messages[]) {
    this.sendMessage(messages);
  }
  
  @Override
  public Server getServer() {
    return this.s;
  }

  @Override
  public String getName() {
    return this.eventId;
  }

  @Override
  public Component name() {
    return null;
  }
  
  @Override
  public boolean isPermissionSet(String s) {
    return true;
  }

  @Override
  public boolean isPermissionSet(Permission permission) {
    return true;
  }

  @Override
  public boolean hasPermission(String s) {
    return true;
  }

  @Override
  public boolean hasPermission(Permission permission) {
    return true;
  }

  @Override
  public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
    return null;
  }

  @Override
  public PermissionAttachment addAttachment(Plugin plugin) {
    return null;
  }

  @Override
  public PermissionAttachment addAttachment(Plugin plugin, int i) {
    return null;
  }

  @Override
  public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
    return null;
  }

  @Override
  public void removeAttachment(PermissionAttachment permissionAttachment) {

  }

  @Override
  public void recalculatePermissions() {

  }

  @Override
  public Set<PermissionAttachmentInfo> getEffectivePermissions() {
    return null;
  }

  @Override
  public boolean isOp() {
    return true;
  }

  @Override
  public void setOp(boolean b) {

  }

  @Override
  public Spigot spigot() {
    return null;
  }
  
}
