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
    private int numOfReceivedTest, numOfReceivedComplete, numOfReceivedFinish;
    private List<Integer> test_edge;
    private List<Integer> test_weight;
    private HashMap<List<Integer>, Integer> test_edges = new HashMap<>();
    public List<Integer> mwoe_edge = new ArrayList<>();
    public List<List<Integer>> mwoe_edge_list = new ArrayList<>();
    private String edge;
    private int weight;
    private int mergeNode;

    public int getLeader(){
        return leader;
    }

    public List<Integer> getChildren(){
        return children;
    }

    public List<Integer> getMwoeEdge(){
        return mwoe_edge;
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

    public void startSearch() {
        if (children.size() != 0){
            node.broadcastChildren(Message.MessageType.MWOE_SEARCH);
        }

        List<Integer> outgoingNeighbours = new ArrayList<>();
        for (String neighbour: this.node.neighbors) {
            int neighborInt = Integer.parseInt(neighbour);
            if (!children.contains(neighborInt) && neighborInt != parent) {
                outgoingNeighbours.add(neighbour);
            }
        }

        node.broadcast(Message.MessageType.MWOE_TEST, outgoingNeighbours);
    }

    public void runAlgo(Message message){
        if (message.getType() == Message.MessageType.MWOE_SEARCH){
            System.out.println("message.getType() == Message.MessageType.MWOE_SEARCH");
            this.startSearch();
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
            test_edge = new ArrayList<>();
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
            // System.out.println("test_edges: " + test_edges);

            if (numOfReceivedTest == node.neighbors.size()){
                int mwoeWeight = Integer.MAX_VALUE;
                mwoe_edge = new ArrayList<>();

                for (List<Integer> key: test_edges.keySet()) {
                    if (test_edges.get(key) < mwoeWeight) {
                        mwoeWeight = test_edges.get(key);
                        mwoe_edge = key;
                    }
                }

                mwoe_edge.add(mwoeWeight);
                mwoe_edge_list.add(mwoe_edge);
                numOfReceivedTest = 0;

                System.out.println("mwoeEdge: " + mwoe_edge);

                if (node.nodeUID == leader){
                    if (mwoe_edge.get(0) == node.nodeUID) {
                        mergeNode = mwoe_edge.get(1);
                    } else {
                        mergeNode = mwoe_edge.get(0);
                    }
                    if (children.size() == 0) {
                        node.sendDirectMessage(mergeNode, Message.MessageType.GHS_MERGE_REQUEST);
                    }
                } else {
                    node.sendDirectMessage(parent, Message.MessageType.MWOE_COMPLETE);
                }

            }
        }

        if (message.getType() == Message.MessageType.MWOE_COMPLETE) {
            System.out.println("message.getType() == Message.MessageType.MWOE_COMPLETE");
            int mwoeWeight = Integer.MAX_VALUE;
            numOfReceivedComplete += 1;
            if (message.getMwoeEdge().size() != 0) {
                mwoe_edge_list.add(message.getMwoeEdge());
            }

            if (numOfReceivedComplete == children.size()) {
                for (List<Integer> edge: mwoe_edge_list) {
                    int weight = edge.get(2);
                    if (weight < mwoeWeight){
                        mwoeWeight = weight;
                        mwoe_edge = edge;
                    }
                }

                if (leader != node.nodeUID) {
                    node.sendDirectMessage(parent, Message.MessageType.MWOE_COMPLETE);
                } else {
                    node.broadcastChildren(Message.MessageType.GHS_MERGE);
                }
                
                numOfReceivedComplete = 0;
            }
        }

        if (message.getType() == Message.MessageType.GHS_MERGE) {
            System.out.println("message.getType() == Message.MessageType.GHS_MERGE");
            mergeNode = -1;
            if (node.neighbors.contains(String.valueOf(message.getMwoeEdge().get(0))) || node.neighbors.contains(String.valueOf(message.getMwoeEdge().get(1)))) {
                if (message.getMwoeEdge().get(0) == node.nodeUID){
                    mergeNode = message.getMwoeEdge().get(1);
                } else {
                    mergeNode = message.getMwoeEdge().get(0);
                }
                node.sendDirectMessage(mergeNode, Message.MessageType.GHS_MERGE_REQUEST);
            } else {
                node.broadcastChildren(Message.MessageType.GHS_MERGE);
            }
        }

        if (message.getType() == Message.MessageType.GHS_MERGE_REQUEST) {
            System.out.println("message.getType() == Message.MessageType.GHS_MERGE_REQUEST");
            mergeNode = -1;
            if (mwoe_edge.get(0) == node.nodeUID){
                mergeNode = mwoe_edge.get(1);
            } else {
                mergeNode = mwoe_edge.get(0);
            }
            if (message.getLeader() != leader && message.getSender() == mergeNode){
                node.sendDirectMessage(message.getSender(), Message.MessageType.GHS_MERGE_ACCPET);
            } else {
                node.sendDirectMessage(message.getSender(), Message.MessageType.GHS_MERGE_REJECT);
            }
        }
        
        if (message.getType() == Message.MessageType.GHS_MERGE_ACCPET || message.getType() == Message.MessageType.GHS_MERGE_REJECT) {
            if (message.getType() == Message.MessageType.GHS_MERGE_ACCPET) {
                System.out.println("message.getType() == Message.MessageType.GHS_MERGE_ACCPET");
                System.out.println("leader, message.getLeader()" + leader + ", " + message.getLeader());
                System.out.println("message.getSender(), mergeNode " + message.getSender() + " , " + mergeNode);
                if (message.getSender() == mergeNode) {
                    if (leader > message.getLeader()) {
                        System.out.println("leader > message.getLeader()");
                        // node.sendDirectMessage(message.getSender(), Message.MessageType.GHS_UPDATE_LEADER_REVERSE);
                        children.add(mergeNode);
                    } else {
                        parent = mergeNode;
                    }
                }
            }
            System.out.println("leader, parent, children" + leader + ", " + parent + ", " + children);
        }

        if (message.getType() == Message.MessageType.GHS_UPDATE_LEADER_REVERSE) {
            System.out.println("message.getType() == Message.MessageType.GHS_UPDATE_LEADER_REVERSE");
            mergeNode = node.nodeUID;
            if (leader != node.nodeUID) {
                System.out.println("leader != node.nodeUID");
                leader = message.getLeader();
                if (children.contains(message.getSender())) {
                    children.remove(message.getSender());
                }
                node.sendDirectMessage(parent, Message.MessageType.GHS_UPDATE_LEADER_REVERSE);
                if (children.size() != 0) {
                    node.broadcastChildren(Message.MessageType.GHS_UPDATE_LEADER);
                }
                children.add(parent);
                parent = message.getSender();
            } else {
                System.out.println("leader = node.nodeUID");
                System.out.println("children: " + children);
                leader = message.getLeader();
                parent = message.getSender();

                if (children.contains(message.getSender())) {
                    children.remove(message.getSender());
                }
                if (children.size() != 0) {
                    System.out.println("children.size() != 0");
                    node.broadcastChildren(Message.MessageType.GHS_UPDATE_LEADER);
                } else {
                    System.out.println("children.size() = 0");
                    System.out.println("mergeNode, node.nodeUID " + mergeNode + ", " + node.nodeUID);
                    System.out.println("parent: " + parent);
                    if (mergeNode == node.nodeUID) {
                        System.out.println("mergeNode == node.nodeUID");
                        node.sendDirectMessage(parent, Message.MessageType.GHS_ROUND_FINISH);
                    }
                }
            }
        }

        if (message.getType() == Message.MessageType.GHS_UPDATE_LEADER) {
            System.out.println("message.getType() == Message.MessageType.GHS_UPDATE_LEADER");
            leader = message.getLeader();
            if (children.size() != 0) {
                node.broadcastChildren(Message.MessageType.GHS_UPDATE_LEADER);
            } else {
                node.sendDirectMessage(parent, Message.MessageType.GHS_UPDATE_FINISH);
            }
        }

        if (message.getType() == Message.MessageType.GHS_UPDATE_FINISH) {
            System.out.println("message.getType() == Message.MessageType.GHS_UPDATE_FINISH");
            numOfReceivedFinish += 1;
            if (numOfReceivedFinish == children.size() - 1){
                if (mergeNode != node.nodeUID) {
                    node.sendDirectMessage(parent, Message.MessageType.GHS_UPDATE_FINISH);
                } else {
                    node.sendDirectMessage(parent, Message.MessageType.GHS_ROUND_FINISH);
                }
            }
        }

        if (message.getType() == Message.MessageType.GHS_ROUND_FINISH) {
            System.out.println("round finish");
            System.out.println("children" + children);
        }
        
    }
    
}
