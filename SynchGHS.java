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
    private int numOfReceivedTest, numOfReceivedComplete;
    private List<Integer> test_edge;
    private List<Integer> test_weight;
    private HashMap<List<Integer>, Integer> test_edges = new HashMap<>();
    public HashMap<List<Integer>, Integer> mwoe_edges = new HashMap<>();
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
        this.numOfReceivedTest = 0;
        this.numOfReceivedComplete = 0;
        this.test_edge = new ArrayList<>();
        this.test_weight = new ArrayList<>();
        this.parent = -1;
    }

    public void runAlgo(Message message){
        if (message.getType() == Message.MessageType.MWOE_SEARCH){
            System.out.println("message.getType() == Message.MessageType.MWOE_SEARCH");
            if (children.size() != 0){
                node.broadcastChildren(Message.MessageType.MWOE_SEARCH);
            } else {
                node.broadcast(Message.MessageType.MWOE_TEST);
            }
        }

        if (message.getType() == Message.MessageType.MWOE_TEST) {
            System.out.println("message.getType() == Message.MessageType.MWOE_TEST");
            if (message.getLeader() == leader) {
                node.sendDirectMessage(message.getSender(), Message.MessageType.MWOE_TEST_REJECT);
            } else {
                node.sendDirectMessage(message.getSender(), Message.MessageType.MWOE_TEST_ACCPET);
            }
        }

        if (message.getType() == Message.MessageType.MWOE_TEST_ACCPET || message.getType() == Message.MessageType.MWOE_TEST_REJECT){
            numOfReceivedTest += 1;
            System.out.println("message.getType() == Message.MessageType.MWOE_TEST_ACCPET || message.getType() == Message.MessageType.MWOE_TEST_REJECT");
            if (message.getType() == Message.MessageType.MWOE_TEST_ACCPET){
                System.out.println("message.getType() == Message.MessageType.MWOE_TEST_ACCPET");
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

            if (numOfReceivedTest == node.neighbors.size()){
                int mwoeWeight = Integer.MAX_VALUE;
                List<Integer> mwoeEdge = new ArrayList<>();

                test_edges.keySet().forEach((key) -> {
                    if (test_edges.get(key) < mwoeWeight) {
                        int mwoeWeight1 = test_edges.get(key);
                        List<Integer> mwoeEdge1 = key;
                    }
                });
                mwoe_edges.put(mwoeEdge, mwoeWeight);
                numOfReceivedTest = 0;
            }

            if (node.nodeUID == leader){
                node.broadcastChildren(Message.MessageType.GHS_MERGE);
            } else {
                node.sendDirectMessage(parent, Message.MessageType.MWOE_COMPLETE);
            }
        }

        if (message.getType() == Message.MessageType.MWOE_COMPLETE){
            numOfReceivedComplete += 1;
            System.out.println("message.getType() == Message.MessageType.MWOE_COMPLETE");
            if (leader != node.nodeUID){
                node.sendDirectMessage(parent, Message.MessageType.MWOE_COMPLETE);
            } else {
                if (numOfReceivedComplete == node.neighbors.size()) {
                    node.broadcastChildren(Message.MessageType.GHS_MERGE);
                    numOfReceivedComplete = 0;
                }
            }
        }
        
    }
    
}
