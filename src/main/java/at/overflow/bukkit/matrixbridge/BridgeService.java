package at.overflow.bukkit.matrixbridge;

import io.kamax.matrix.client.MatrixPasswordCredentials;
import io.kamax.matrix.client._MatrixClient;
import io.kamax.matrix.client._SyncData;
import io.kamax.matrix.client.regular.MatrixHttpClient;
import io.kamax.matrix.client.regular.SyncOptions;
import io.kamax.matrix.event._MatrixEvent;
import io.kamax.matrix.hs._MatrixRoom;
import io.kamax.matrix.json.event.MatrixJsonRoomMessageEvent;

public class BridgeService extends Thread implements Endpoint {
    Endpoint receiver;

    _MatrixClient client;

    private BridgePropertyReader properties;

    public BridgeService(Endpoint receiver, BridgePropertyReader properties) {

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

                    for (_MatrixEvent rawEv : joinedRoom.getTimeline().getEvents()) {
                        if ("m.room.message".contentEquals(rawEv.getType())) {
                            MatrixJsonRoomMessageEvent msg = new MatrixJsonRoomMessageEvent(rawEv.getJson());

                            if (properties.getDomain().equals(msg.getSender().getDomain()) && properties.getUsername().equals(msg.getSender().getLocalPart()))
                                continue;

                            if (msg.getBody() != null) {
                                this.receiver.send(msg.getSender().getId(), msg.getBody());
                            }
                        }
                    }
                }

                for (_SyncData.InvitedRoom invitedRoom : data.getRooms().getInvited()) {
                    client.getRoom(invitedRoom.getId()).join();
                }

                syncToken = data.nextBatchToken();

            } catch(Exception e) {
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
            room.sendFormattedText("<font color='green'>&lt;" + from + "&gt;</font> " + message, message);
        }
    }
}
