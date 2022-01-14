package at.overflow.bukkit.matrixbridge;

import io.kamax.matrix.client.MatrixPasswordCredentials;
import io.kamax.matrix.client._MatrixClient;
import io.kamax.matrix.client._SyncData;
import io.kamax.matrix.client.regular.MatrixHttpClient;
import io.kamax.matrix.client.regular.SyncOptions;
import io.kamax.matrix.event._MatrixEvent;
import io.kamax.matrix.hs._MatrixRoom;
import io.kamax.matrix.json.event.MatrixJsonRoomMessageEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandException;

import java.util.Collection;
import java.util.Iterator;

public class BridgeService extends Thread implements Endpoint {
    MatrixPlugin receiver;
    _MatrixClient client;

    private BridgePropertyReader properties;

    private void parseCommand(String body) {
        if(body.startsWith("!players")) {
            sendAllPlayers();
	}
    }

    private void sendAllPlayers() {
        StringBuilder fallback = new StringBuilder();
        StringBuilder playersFormatted = new StringBuilder();
        Collection<? extends Player> online = this.getAllOnlinePlayers();

        if(online.size() == 0) {
          this.send(this.properties.getMinecraftServerName(), "No player online");
          return;
        }

        playersFormatted.append("<ul>");
        fallback.append("Online:\n\n");

        for (Player p : online) {
          playersFormatted.append("<li>").append(p.getName()).append("</li>");
          fallback.append(" - ").append(p.getName()).append("\n");
	}

	this.send("Online",
		  playersFormatted.toString(),
		  fallback.toString());
    }

    public BridgeService(MatrixPlugin receiver, BridgePropertyReader properties) {

        this.properties = properties;

        this.receiver = receiver;

        try {
            client = new MatrixHttpClient(properties.getDomain());
            client.discoverSettings();

            client.login(new MatrixPasswordCredentials(properties.getUsername(), properties.getPassword()));

            this.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        String syncToken = null;

        while (true) {

            try {

                _SyncData data = client.sync(SyncOptions.build().setSince(syncToken).get());

                for (_SyncData.JoinedRoom joinedRoom : data.getRooms().getJoined()) {
                    _MatrixRoom room = client.getRoom(joinedRoom.getId());

		    if((properties.getRoom() != null)
		       && !room.getAddress().contentEquals(properties.getRoom())) {
		      continue;
		    }

                    for (_MatrixEvent rawEv : joinedRoom.getTimeline().getEvents()) {
                        if ("m.room.message".contentEquals(rawEv.getType())) {
                            MatrixJsonRoomMessageEvent msg = new MatrixJsonRoomMessageEvent(rawEv.getJson());

			    // Ignore own messages
                            if (properties.getDomain().equals(msg.getSender().getDomain()) && properties.getUsername().equals(msg.getSender().getLocalPart()))
                                continue;

			    // Process messages with body
                            if (msg.getBody() != null) {
                                if (syncToken != null && msg.getBody().startsWith("!")) {
                                    this.parseCommand(msg.getBody());
				} else if (syncToken != null
					   && msg.getBody().startsWith("$")
					   && msg.getSender().getId().equals(properties.getAdmin())) {
				  try {
				    Bukkit.getScheduler().runTask(this.receiver,
								  new Runnable() {
								    @Override
								    public void run() {
								      MatrixCommandSender s = new MatrixCommandSender(room, msg.getId(), Bukkit.getServer());
								      try {
									if(!Bukkit.getServer().dispatchCommand(s,
													      msg.getBody().substring(1))) {
									  s.sendMessage("No target found");
									}
								      } catch (CommandException e) {
									s.sendMessage(e.getMessage());
								      } finally {
									s.sendBuffer();
								      }
								    }
								  }
				      );
				  } catch (Exception e) {
				    e.printStackTrace();
				  }
                                } else {
                                    this.receiver.send(msg.getSender().getId(), msg.getBody());
                                }
                            }
			    room.sendReadReceipt(msg.getId());
                        }
                    }
                }

                for (_SyncData.InvitedRoom invitedRoom : data.getRooms().getInvited()) {
                    client.getRoom(invitedRoom.getId()).join();
                }

                syncToken = data.nextBatchToken();

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("waiting before retrying");

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void send(String from, String message) {
        message = message.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
        for (_MatrixRoom room : client.getJoinedRooms()) {
	  if((properties.getRoom() != null)
	     && !room.getAddress().contentEquals(properties.getRoom())) {
	    continue;
	  }
	  room.sendFormattedText("<font color='green'>&lt;" + from + "&gt;</font> " + message, message);
        }
    }
  
  public void send(String from, String messageFormatted, String fallback) {
        for (_MatrixRoom room : client.getJoinedRooms()) {
	  if((properties.getRoom() != null)
	     && !room.getAddress().contentEquals(properties.getRoom())) {
	    continue;
	  }
	  room.sendFormattedText("<font color='green'>&lt;" + from + "&gt;</font> " + messageFormatted, fallback);
        }
    }

    @Override
    public Collection<? extends Player> getAllOnlinePlayers() {
      return Bukkit.getOnlinePlayers();
    }
}
