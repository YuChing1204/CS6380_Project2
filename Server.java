// Server class that
// receives data and sends data
package node;

import java.io.*;
import java.net.*;

public class Server extends Thread {

    private Node node;

	public Server(Node node) {
		this.node = node;
	}

	public void run() {
		try (ServerSocket ss = new ServerSocket(Integer.parseInt(node.myPort))){
            while(true) {
            // connect it to client socket
            Socket s = ss.accept();
            // System.out.println("Connection established");

            ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            Object message = ois.readObject();

            Message received_message = (Message) message;

            if (received_message.getType() == Message.MessageType.LOGIN){
                // System.out.println("Send from " + received_message.getSender());
            } else {
                node.processMessage(received_message);
            }

            ois.close();
            s.close();
            }
		}catch(IOException | ClassNotFoundException e){
			System.out.println("server:" + e);
		}
	}
}
