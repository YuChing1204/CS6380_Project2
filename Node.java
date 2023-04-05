package node;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import node.Message;
import node.Message.MessageType;

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
    private SynchGHS mstTree;
    private List<Integer> children;
    private Integer numOfChildren;
	public Queue<Message> messageList = new LinkedList<>();
	public Queue<Message> checkMessageList = new LinkedList<>();
	public Queue<Message> bufferMessageList = new LinkedList<>();
	private Queue<Message> removedMessage = new LinkedList<>();
	private Message curMessage, checkMessage;

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

	public synchronized void broadcast(Message.MessageType type) {
		for (int i=0; i < numOfNeighbors; i++){
            String neighbor = neighbors.get(i);
            String hostName = addressMap.get(neighbor).get(0);
            String port = addressMap.get(neighbor).get(1);
			Message message = new Message(nodeUID, Integer.parseInt(neighbor), type, mstTree.getLeader(), mstTree.getMwoeEdge(), mstTree.getMwoeEdgeList(), mstTree.getLevel(), mstTree.mergeNode, mstTree.getComponentSet());

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

    public synchronized void broadcastChildren(Message.MessageType type) {
        numOfChildren = mstTree.getChildren().size();
        children = mstTree.getChildren();
		for (int i=0; i < numOfChildren; i++){
            String child = String.valueOf(children.get(i));
            String hostName = addressMap.get(child).get(0);
            String port = addressMap.get(child).get(1);
			Message message = new Message(nodeUID, Integer.parseInt(child), type, mstTree.getLeader(), mstTree.getMwoeEdge(), mstTree.getMwoeEdgeList(), mstTree.getLevel(), mstTree.mergeNode, mstTree.getComponentSet());

			try (Socket s = new Socket(hostName, Integer.parseInt(port))) {
				ObjectOutputStream object = new ObjectOutputStream(s.getOutputStream());
				object.writeObject(message);
				object.close();
                s.close();

            } catch(IOException e){
                System.out.println("client " + e);
            }
        }
	}

    public synchronized void sendDirectMessage(int receiver, Message.MessageType type){
        String receiverNode = String.valueOf(receiver);
        String hostName = addressMap.get(receiverNode).get(0);
        String port = addressMap.get(receiverNode).get(1);
		Message message = new Message(nodeUID, receiver, type, mstTree.getLeader(), mstTree.getMwoeEdge(),mstTree.getMwoeEdgeList(), mstTree.getLevel(), mstTree.mergeNode, mstTree.getComponentSet());

        try (Socket s = new Socket(hostName, Integer.parseInt(port))) {
            ObjectOutputStream object = new ObjectOutputStream(s.getOutputStream());
            object.writeObject(message);
            object.close();
            s.close();

        } catch(IOException e){
            System.out.println("client " + e);
        }
    }

    public synchronized void startSynchGHS(){
        System.out.println("********** startSynchGHS ***************");
        if (mstTree.getLeader() == nodeUID & mstTree.level == 0) {
            broadcast(Message.MessageType.MWOE_TEST);
        } else if (mstTree.getLeader() == nodeUID){
			broadcast(Message.MessageType.MWOE_TEST);
            broadcastChildren(Message.MessageType.MWOE_SEARCH);
        }        
    }

    public synchronized void processMessage(Message message){
		// System.out.println(message.getType());

		if (message.getType() == Message.MessageType.CHECK_LEVEL || message.getType() == Message.MessageType.CHECK_LEVEL_ACK || message.getType() == Message.MessageType.CHECK_LEVEL_NO_ACK) {
			checkMessageList.add(message);
		} else {
			messageList.add(message);
		}
		while (removedMessage.size() != 0){
			Message add = removedMessage.remove();
			messageList.add(add);
		}

		if (loginComplete) {
			while (messageList.size() != 0 || checkMessageList.size() != 0)  {
				if (messageList.size() != 0) {
					curMessage = messageList.remove();
					// System.out.println(messageList.size());
					// System.out.println(curMessage.getType());
					// System.out.println("curMessage.getLevel(): " + curMessage.getLevel());
					// System.out.println("mstTree.level: " + mstTree.level);

					// mstTree.runAlgo(curMessage);

					if (curMessage.getType() == Message.MessageType.GHS_MERGE_REQUEST){
							// System.out.println("mergeNodeUpdate: " + mstTree.mergeNodeUpdate);
							// System.out.println("mergeNode: " + mstTree.mergeNode);
						if (mstTree.mergeNodeUpdate == true){
							mstTree.runAlgo(curMessage);
						} else {
							Message remove = curMessage;
							removedMessage.add(remove);
						}
					} else {
						mstTree.runAlgo(curMessage);
					}
				}

				if (mstTree.roundDone && messageList.size() == 0 && (mstTree.level == 0 || mstTree.level == 1 || mstTree.level == 2 || mstTree.level == 3) && mstTree.update_ack_bool) {
					System.out.println("*****************************");
					System.out.println("round finish! level: " + mstTree.level);
					System.out.println("node uid: " + nodeUID);
					System.out.println("parent: " + mstTree.getParent());
					System.out.println("children: " + mstTree.getChildren());
					System.out.println("leader: " + mstTree.getLeader());
					// System.out.println("component: " + mstTree.getComponentSet());
					System.out.println("*****************************");

					mstTree.level += 1;
					mstTree.roundDone = false;
					// System.out.println("level: " + mstTree.getLevel());
					broadcast(Message.MessageType.CHECK_LEVEL);
				}

				if (checkMessageList.size() != 0) {
					checkMessage = checkMessageList.remove();
					mstTree.check(checkMessage);
				}

				if (mstTree.check_bool) {
					mstTree.test_edges = new HashMap<>();
					mstTree.mergeNode = 0;
					mstTree.mergeNodeUpdate = false;
					mstTree.mwoe_edge_list = new ArrayList<>();
					mstTree.mwoe_edge = new ArrayList<>();
					mstTree.check_bool = false;
					mstTree.update_ack_bool = false;
					if (mstTree.getComponentSet().size() == numOfNode){
						System.out.println("******** mstTree finally complete! ********");
						break;
					}
					System.out.println("******** next round ********");
					try {
						Thread.sleep(5000);
					} catch(InterruptedException err){
						System.out.println(err);
					}

					if (mstTree.level <= 3) {
						startSynchGHS();
					}
				}
			}
		} 
    }

	private void init() {
        mstTree = new SynchGHS(this);

		//login
		while (loginMessages.size() < neighbors.size()) {
			broadcast(Message.MessageType.LOGIN);
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
        node.startSynchGHS();
    }
    
}
