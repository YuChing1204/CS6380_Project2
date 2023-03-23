import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Message implements Serializable {
    public enum MessageType {
        HANDSHAKE
    }

    private int senderUID = -1;
    private MessageType type;

    // variables for handshake message
    private String text;

    public Message() {
    }

    public Message(int senderUID, String text) {
        this.senderUID = senderUID;
        this.text = text;
        this.type = MessageType.HANDSHAKE;
    }

    public Message(int senderUID, MessageType type) {
        this.senderUID = senderUID;
        this.type = type;
    }

    public int getSenderUID() {
        return this.senderUID;
    }

    public MessageType getType() {
        return this.type;
    }

    public void send(Node serverNode, ObjectOutputStream outToServer) {
        try {
            outToServer.writeObject(this);
            outToServer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
