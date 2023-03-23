import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class TCPClient {
    private Node clientNode;
    private ObjectOutputStream outToServer;
    private Node serverNode;

    public TCPClient() {
    }

    public TCPClient(Node clientNode, Node serverNode) {
        this.clientNode = clientNode;
        this.serverNode = serverNode;
    }

    public void connect() {
        try {
            Socket clientSocket = new Socket(this.serverNode.getHostName(), this.serverNode.getPort());

            System.out.println("Client connected to UID: " + this.serverNode.getUID());

            this.outToServer = new ObjectOutputStream(clientSocket.getOutputStream());
            this.outToServer.flush();

            try {
                Thread.sleep(2000);

                String text = "Hello from " + this.clientNode.getUID();
                new Message(this.clientNode.getUID(), text).send(this.serverNode, this.outToServer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ObjectOutputStream getOutputStream() {
        return this.outToServer;
    }

    public Node getServerNode() {
        return this.serverNode;
    }
}
