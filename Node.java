package node;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Node {
	public int nodeUID;
	public String myHostName;
	public String myPort;
	public List<String> neighbors;
	private NodeLookup nodeLookup;
	public HashMap<String, List<String>> addressMap;
	public HashMap<String, List<String>> edgesMap;
	public Node node;
	private int numOfNeighbors;
	private Set<String> loginMessages;
	private Boolean loginComplete;
	public int numOfNode;

	class NodeLookup {
		HashMap<String, List<String>> addressMap;
		HashMap<String, List<String>> neighborsMap;
		HashMap<String, List<String>> edgesMap;

		public NodeLookup(HashMap<String, List<String>> addressMap, HashMap<String, List<String>> neighborsMap, HashMap<String, List<String>> edgesMap) {
			this.addressMap = addressMap;
			this.neighborsMap = neighborsMap;
			this.edgesMap = edgesMap;
		}
		
		public HashMap<String, List<String>> getAddressMap() {
			return addressMap;
		}

		public String getHostName(String id) {
			return addressMap.get(id).get(0);
		}

		public String getPort(String id) {
			return addressMap.get(id).get(1);
		}

		public List<String> getNeighbors(String id) {
			return neighborsMap.get(id);
		}

	}

	public Node(String[] args) {
		List<HashMap<String, List<String>>> infoMapList = ReadFile.readConfig();
		NodeLookup nodeLookup = new NodeLookup(infoMapList.get(0), infoMapList.get(1), infoMapList.get(2));
		nodeUID = Integer.parseInt(args[0]);
		myHostName = nodeLookup.getHostName(String.valueOf(nodeUID));
		myPort = nodeLookup.getPort(String.valueOf(nodeUID));
		neighbors = nodeLookup.getNeighbors(String.valueOf(nodeUID));
		addressMap = nodeLookup.addressMap;
		numOfNeighbors = neighbors.size();
		loginMessages = new HashSet<String> ();
		loginComplete = false;
		numOfNode = nodeLookup.addressMap.size();
		edgesMap = nodeLookup.edgesMap;
	}

	public synchronized void broadcast(String type) {
		for (int i=0; i < numOfNeighbors; i++){
            String neighbor = neighbors.get(i);
            String hostName = addressMap.get(neighbor).get(0);
            String port = addressMap.get(neighbor).get(1);
			Message message = new Message(nodeUID, Integer.parseInt(neighbor), Message.MessageType.LOGIN);

			try (Socket s = new Socket(hostName, Integer.parseInt(port))) {
				ObjectOutputStream object = new ObjectOutputStream(s.getOutputStream());
				loginMessages.add(neighbor);
				object.writeObject(message);
				object.close();
                s.close();

            } catch(IOException e){
                System.out.println("client " + e);
            }
        }
	}

	private void init() {
		//login
		while (loginMessages.size() < neighbors.size()) {
			broadcast("login");
			try {
				Thread.sleep(5000);
			} catch(InterruptedException err){
				System.out.println(err);
				// this part is executed when an exception (in this example InterruptedException) occurs
			}
		}

		loginComplete = true;
		System.out.println("********* Login complete! ************");
		System.out.println("loginMessages" + loginMessages);
		System.out.println("neighbors" + neighbors);
		System.out.println("**************************************");
	}
	

    public static void main(String args[]) throws Exception
    {
		Node node = new Node(args);
        Server server = new Server(node);
        server.start();

		node.init();
    }
    
}
