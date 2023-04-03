package node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import node.Message.MessageType;

public class SynchGHS {
    private Node node;
    private List<Message> bufferedMessages;
	private List<Message> messageList;
    private Boolean done;
    private int leader;
    private List<Integer> children;
    private int parent;
    public int level;
    private int numOfReceovedTest;
    private List<Integer> test_edge;
    private List<Integer> test_weight;
    private HashMap<List<Integer>, Integer> test_edges = new HashMap<>();
    private String edge;
    private int weight;

    public int getLeader(){
        return leader;
    }

    public List<Integer> getChildren(){
        return children;
    }

    public SynchGHS(Node node){
        this.bufferedMessages = new ArrayList<>();
        this.messageList = new ArrayList<>();
        this.node = node;
        this.children = new ArrayList<>();
        this.level = 0;
        this.leader = node.nodeUID;
        this.numOfReceovedTest = 0;
        this.test_edge = new ArrayList<>();
        this.test_weight = new ArrayList<>();
    }

    public void runAlgo(Message message){
        if (message.getType() == Message.MessageType.MWOE_SEARCH){
            if (children.size() != 0){
                node.broadcastChildren(Message.MessageType.MWOE_SEARCH);
            } else {
                node.broadcast(Message.MessageType.MWOE_TEST);
            }
        }

        if (message.getType() == Message.MessageType.MWOE_TEST) {
            if (message.getLeader() == leader) {
                node.sendDirectMessage(message.getSender(), Message.MessageType.MWOE_TEST_REJECT);
            } else {
                node.sendDirectMessage(message.getSender(), Message.MessageType.MWOE_TEST_ACCPET);
            }
        }

        if (message.getType() == Message.MessageType.MWOE_TEST_ACCPET || message.getType() == Message.MessageType.MWOE_TEST_REJECT){
            numOfReceovedTest += 1;

            if (message.getType() == Message.MessageType.MWOE_TEST_ACCPET){
                if (message.getSender() > node.nodeUID) {
                    edge = "("+ String.valueOf(node.nodeUID) +","+ String.valueOf(message.getSender()) +")";
                    weight = Integer.parseInt(node.edgesMap.get(edge).get(0));
                    test_edge.add(node.nodeUID);
                    test_edge.add(message.getSender());
                    test_edges.put(test_edge, weight);
                } else {
                    edge = "("+ String.valueOf(message.getSender()) +","+ String.valueOf(node.nodeUID) +")";
                    weight = Integer.parseInt(node.edgesMap.get(edge).get(0));
                    test_edge.add(message.getSender());
                    test_edge.add(node.nodeUID);
                    test_edges.put(test_edge, weight);
                }
            }

            if (numOfReceovedTest == node.neighbors.size()){
                int mwoeWeight = Integer.MAX_VALUE;
                List<Integer> mwoeEdge;

                test_edges.keySet().forEach((key) -> {
                    if (test_edges.get(key) < mwoeWeight) {
                        mwoeWeight = test_edges.get(key);
                        mwoeEdge = key;
                    }
              });
              numOfReceovedTest = 0;
            }

            node.sendDirectMessage(parent, Message.MessageType.MWOE_COMPLETE);
        }

        if (message.getType() == Message.MessageType.MWOE_COMPLETE){
            if (message.getLeader() != node.nodeUID){
                node.sendDirectMessage(parent, Message.MessageType.MWOE_COMPLETE);
            } else {
                node.broadcastChildren(Message.MessageType.GHS_MERGE);
            }
        }
	// ADDED
	if (message.getType() == Message.MessageType.GHS_MERGE) {
		/* FIXME
		if (mergeNode not in neighbor) {
			node.broadcastChildren(Message.MessageType.GHS_MERGE);
		}
		else if (mergenode in neighbor) {
			node.sendDirectMessage(mergeNode, Message.MessageType.GHS_MERGE_REQUEST)
		}
		*/
	}
	if (message.getType() == Message.MessageType.GHS_MERGE_REQUEST) {
		if (message.getLeader() != leader) {
			node.sendDirectMessage(message.getSender(), Message.MessageType.GHS_MERGE_ACCEPT)
		} else if (message.getLeader() == leader) {
			node.sendDirectMessage(message.getSender(), Message.MessageType.GHS_MERGE_REJECT)
		}
	}
	if (message.getType() == Message.MessageType.GHS_MERGE_ACCEPT || message.getType() == Message.MessageType.GHS_MERGE_REJECT) {
		if (message.getType() == Message.MessageType.GHS_MERGE_ACCEPT) {
			// print info
			if (message.getSender() == mergeNode) {
				if (leader > message.getLeader()) {
					// send code on changing leader
					children.add(mergeNode);
				}
			}
		}
	}

    }
    
}