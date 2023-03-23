import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {
    private Node serverNode;
    private ServerSocket serverSocket;

    public TCPServer(Node serverNode) {
        this.serverNode = serverNode;
    }

    public Node getServerNode() {
        return this.serverNode;
    }

    public void startListening() {
        try {
            this.serverSocket = new ServerSocket(this.serverNode.getPort());

            System.out.println("Server online with UID: " + this.serverNode.getUID());

            while (true) {
                Socket connectionSocket = serverSocket.accept();

                Runnable runnable = new Runnable() {
                    public void run() {
                        try {
                            ObjectOutputStream outToClient = new ObjectOutputStream(connectionSocket.getOutputStream());
                            outToClient.flush();
                            ObjectInputStream inFromClient = new ObjectInputStream(connectionSocket.getInputStream());

                            while (true) {
                                Message message = (Message) inFromClient.readObject();

                                System.out.println("Received " + message.getType() + " message from UID: "
                                        + message.getSenderUID());

                                if (message.getType() != Message.MessageType.HANDSHAKE) {
                                    serverNode.addReceivedMessage(message);
                                }
                            }
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                };

                Thread t = new Thread(runnable);
                t.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
