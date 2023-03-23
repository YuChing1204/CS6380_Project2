import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Node {
    private ArrayList<Node> allNodes;
    private String hostName;
    private ArrayList<Integer> neighbours;
    private List<TCPClient> neighbourClients = Collections.synchronizedList(new ArrayList<TCPClient>());
    private int port;
    private int UID;
    private List<Message> receivedMessages = Collections.synchronizedList(new ArrayList<Message>());

    private int leaderUID;
    private List<Integer> childNodes = new ArrayList<Integer>();
    private int parentUID;

    public Node() {
    }

    public Node(int UID, String hostName, int port) {
        this.hostName = hostName;
        this.port = port;
        this.UID = UID;

        this.neighbours = new ArrayList<Integer>();
    }

    public void addReceivedMessage(Message msg) {
        synchronized (this.receivedMessages) {
            this.receivedMessages.add(msg);
        }
    }

    public String getHostName() {
        return this.hostName;
    }

    public int getPort() {
        return this.port;
    }

    public int getUID() {
        return this.UID;
    }
}
