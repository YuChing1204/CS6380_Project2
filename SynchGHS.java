package node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiPredicate;

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
    private int numOfReceivedTest, numOfReceivedComplete, numOfReceivedUpdateFinish, numOfReceivedTestAccept, numOfReceivedTestReject, numOfReceivedLevelAck, numOfReceivedUpdateLevelAck;
    private List<Integer> test_edge;
    private List<Integer> test_weight;
    public HashMap<List<Integer>, Integer> test_edges = new HashMap<>();
    public List<Integer> mwoe_edge = new ArrayList<>();
    public List<List<Integer>> mwoe_edge_list = new ArrayList<>();
    private String edge;
    private int weight;
    public int mergeNode;
    public boolean mergeNodeUpdate = false;
    public boolean roundDone = false;
    public boolean check_bool = false;
    public boolean update_ack_bool = false;
    private HashSet<Integer> componentSet = new HashSet<Integer>();

    public int getLeader(){
        return leader;
    }

    public int getParent(){
        return parent;
    }

    public List<Integer> getChildren(){
        return children;
    }

    public List<Integer> getMwoeEdge(){
        return mwoe_edge;
    }

    public List<List<Integer>> getMwoeEdgeList(){
        return mwoe_edge_list;
    }

    public HashSet<Integer> getComponentSet(){
        return componentSet;
    }

    public int getLevel(){
        return level;
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
        this.componentSet = new HashSet<Integer>();
        this.componentSet.add(node.nodeUID);
    }

    public void check(Message message) {
        if (message.getType() == Message.MessageType.CHECK_LEVEL) {
            // System.out.println("Message.MessageType.CHECK_LEVEL, send from: " + message.getSender());
            if (message.getLevel() == level) {
                node.sendDirectMessage(message.getSender(), Message.MessageType.CHECK_LEVEL_ACK);
            } else {
                node.sendDirectMessage(message.getSender(), Message.MessageType.CHECK_LEVEL_NO_ACK);
            }
        }

        if (message.getType() == Message.MessageType.CHECK_LEVEL_ACK) {
            // System.out.println("Message.MessageType.CHECK_LEVEL_ACK, send from: " + message.getSender());
            numOfReceivedLevelAck += 1;
            if (numOfReceivedLevelAck == node.neighbors.size()) {
                check_bool = true;
                numOfReceivedLevelAck = 0;
            }
        }

        if (message.getType() == Message.MessageType.CHECK_LEVEL_NO_ACK) {
            // System.out.println("Message.MessageType.CHECK_LEVEL_NO_ACK, send from: " + message.getSender());
            node.sendDirectMessage(message.getSender(), Message.MessageType.CHECK_LEVEL);
        }
    }

    public void runAlgo(Message message){
        if (message.getType() == Message.MessageType.MWOE_SEARCH){
            node.broadcastChildren(Message.MessageType.MWOE_SEARCH);
            node.broadcast(Message.MessageType.MWOE_TEST);
        }

        if (message.getType() == Message.MessageType.MWOE_TEST) {
            // System.out.println("message.getType() == Message.MessageType.MWOE_TEST, send from: " + message.getSender());
            if (message.getLeader() == leader) {
                node.sendDirectMessage(message.getSender(), Message.MessageType.MWOE_TEST_REJECT);
            } else {
                node.sendDirectMessage(message.getSender(), Message.MessageType.MWOE_TEST_ACCPET);
            }
        }

        if (message.getType() == Message.MessageType.MWOE_TEST_ACCPET || message.getType() == Message.MessageType.MWOE_TEST_REJECT){
            numOfReceivedTest += 1;
            // System.out.println("message.getType() == Message.MessageType.MWOE_TEST_ACCPET || message.getType() == Message.MessageType.MWOE_TEST_REJECT");
            if (message.getType() == Message.MessageType.MWOE_TEST_ACCPET){
                numOfReceivedTestAccept += 1;
                // System.out.println("message.getType() == Message.MessageType.MWOE_TEST_ACCPET, sender: " + message.getSender());
                if (message.getSender() > node.nodeUID) {
                    test_edge = new ArrayList<>();
                    edge = "("+ String.valueOf(node.nodeUID) +","+ String.valueOf(message.getSender()) +")";
                    weight = Integer.parseInt(node.edgesMap.get(edge).get(0));
                    test_edge.add(node.nodeUID);
                    test_edge.add(message.getSender());
                    test_edges.put(test_edge, weight);
                } else {
                    test_edge = new ArrayList<>();
                    edge = "("+ String.valueOf(message.getSender()) +","+ String.valueOf(node.nodeUID) +")";
                    weight = Integer.parseInt(node.edgesMap.get(edge).get(0));
                    test_edge.add(message.getSender());
                    test_edge.add(node.nodeUID);
                    test_edges.put(test_edge, weight);
                }
            } else {
                numOfReceivedTestReject += 1;
                // System.out.println("message.getType() == Message.MessageType.MWOE_TEST_REJECT, sender: " + message.getSender());
            }

            if (numOfReceivedTest == node.neighbors.size()){
                int mwoeWeight = Integer.MAX_VALUE;
               
                for (List<Integer> key: test_edges.keySet()) {
                    if (test_edges.get(key) < mwoeWeight) {
                        mwoeWeight = test_edges.get(key);
                        mwoe_edge = new ArrayList<>(key);
                    }
                }

                mwoe_edge.add(mwoeWeight);
                numOfReceivedTest = 0;

                // System.out.println("mwoeEdge: " + mwoe_edge);
                if (mwoe_edge.size() >= 3) {
                    mwoe_edge_list.add(mwoe_edge);
                
                    if (mwoe_edge.get(0) == node.nodeUID) {
                        mergeNode = mwoe_edge.get(1);
                    } else {
                        mergeNode = mwoe_edge.get(0);
                    }
                }

                if (numOfReceivedTestAccept > 0) {
                    if (children.size() != 0) {
                        numOfReceivedComplete += 1;
                    }
                }

                if (node.nodeUID == leader && children.size() == 0){
                    mergeNodeUpdate = true;
                    node.sendDirectMessage(mergeNode, Message.MessageType.GHS_MERGE_REQUEST);
                } 
                if (children.size() == 0 && node.nodeUID != leader) {
                    node.sendDirectMessage(parent, Message.MessageType.MWOE_COMPLETE);
                }

                if (numOfReceivedComplete == (children.size() + 1) && numOfReceivedTestAccept > 0) {
                    for (List<Integer> edge: mwoe_edge_list) {
                        int weight = edge.get(2);
                        if (weight < mwoeWeight){
                            mwoeWeight = weight;
                            mwoe_edge = new ArrayList<>(edge);
                        }
                    }

                    if ((node.neighbors.contains(String.valueOf(mwoe_edge.get(0))) || node.neighbors.contains(String.valueOf(mwoe_edge.get(1)))) && children.contains(mwoe_edge.get(0)) == false && children.contains(mwoe_edge.get(1)) == false) {
                        if (mwoe_edge.get(0) == node.nodeUID){
                            mergeNode = mwoe_edge.get(1);
                        } else {
                            mergeNode = mwoe_edge.get(0);
                        }
                        node.sendDirectMessage(mergeNode, Message.MessageType.GHS_MERGE_REQUEST);
                    } else {
                        node.broadcastChildren(Message.MessageType.GHS_MERGE);
                    }

                    if (componentSet.contains(mwoe_edge.get(0))){
                        mergeNode = mwoe_edge.get(1);
                    } else {
                        mergeNode = mwoe_edge.get(0);
                    }

                    mergeNodeUpdate = true;
                    node.broadcastChildren(Message.MessageType.GHS_MERGENODE_UPDATE);
                    numOfReceivedComplete = 0;
                    numOfReceivedTestAccept = 0;
                }

            }
        }

        if (message.getType() == Message.MessageType.MWOE_COMPLETE) {
            // System.out.println("message.getType() == Message.MessageType.MWOE_COMPLETE, sender: " + message.getSender());
            int mwoeWeight = Integer.MAX_VALUE;
            numOfReceivedComplete += 1;
            mwoe_edge_list = message.getMwoeEdgeList();
            if (mwoe_edge.size() >= 3) {
                mwoe_edge_list.add(mwoe_edge);
            }
            
            if (parent != -1){
                node.sendDirectMessage(parent, Message.MessageType.MWOE_COMPLETE);
            } else if (mwoe_edge_list.size() > 0){
                if (numOfReceivedComplete == (children.size() + 1)) {
                    for (List<Integer> edge: mwoe_edge_list) {
                        int weight = edge.get(2);
                        if (weight < mwoeWeight){
                            mwoeWeight = weight;
                            mwoe_edge = new ArrayList<>(edge);
                        }
                    }

                    if ((node.neighbors.contains(String.valueOf(mwoe_edge.get(0))) || node.neighbors.contains(String.valueOf(mwoe_edge.get(1)))) && children.contains(mwoe_edge.get(0)) == false && children.contains(mwoe_edge.get(1)) == false) {
                        if (mwoe_edge.get(0) == node.nodeUID){
                            mergeNode = mwoe_edge.get(1);
                        } else {
                            mergeNode = mwoe_edge.get(0);
                        }
                        node.sendDirectMessage(mergeNode, Message.MessageType.GHS_MERGE_REQUEST);
                    } else {
                        node.broadcastChildren(Message.MessageType.GHS_MERGE);
                    }

                    if (componentSet.contains(mwoe_edge.get(0))){
                        mergeNode = mwoe_edge.get(1);
                    } else {
                        mergeNode = mwoe_edge.get(0);
                    }
                    mergeNodeUpdate = true;
                    node.broadcastChildren(Message.MessageType.GHS_MERGENODE_UPDATE);
                    numOfReceivedComplete = 0;
                }
            }
        }

        if (message.getType() == Message.MessageType.GHS_MERGENODE_UPDATE) {
            mergeNode = message.getMergeNode();
            mwoe_edge = message.getMwoeEdge();
            mergeNodeUpdate = true;
            node.broadcastChildren(Message.MessageType.GHS_MERGENODE_UPDATE);
        }

        if (message.getType() == Message.MessageType.GHS_MERGE) {
            // System.out.println("message.getType() == Message.MessageType.GHS_MERGE, sender: " + message.getSender());
            if (message.getMwoeEdge().size() > 0) {
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
        }

        if (message.getType() == Message.MessageType.GHS_MERGE_REQUEST) {
            // System.out.println("message.getType() == Message.MessageType.GHS_MERGE_REQUEST: " + message.getSender());
            if (message.getLeader() != leader){
                if (message.getSender() == mergeNode) {
                    node.sendDirectMessage(message.getSender(), Message.MessageType.GHS_MERGE_ACCPET);
                } else {
                    node.sendDirectMessage(message.getSender(), Message.MessageType.GHS_MERGE_REJECT);
                }
            } else {
                node.sendDirectMessage(message.getSender(), Message.MessageType.GHS_MERGE_REJECT);
            }
        }
        
        if (message.getType() == Message.MessageType.GHS_MERGE_ACCPET || message.getType() == Message.MessageType.GHS_MERGE_REJECT) {
            if (message.getType() == Message.MessageType.GHS_MERGE_ACCPET) {
                // System.out.println("message.getType() == Message.MessageType.GHS_MERGE_ACCPET, sender: " + message.getSender());
                if (message.getSender() == mergeNode) {
                    if (leader > message.getLeader()) {
                        node.sendDirectMessage(message.getSender(), Message.MessageType.GHS_UPDATE_LEADER_REVERSE);
                        children.add(mergeNode);
                        componentSet.add(mergeNode);
                    }
                }
            } else {
                if (parent != -1) {
                    node.sendDirectMessage(parent, Message.MessageType.GHS_ROUND_FINISH);
                } else {
                    if (children.size() == 0) {
                        roundDone = true;
                        update_ack_bool = true;
                    }
                }
            }
        }

        if (message.getType() == Message.MessageType.GHS_UPDATE_LEADER_REVERSE) {
            // System.out.println("message.getType() == Message.MessageType.GHS_UPDATE_LEADER_REVERSE, sender: " + message.getSender());
            mergeNode = node.nodeUID;
            if (parent != -1) {
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
                leader = message.getLeader();
                parent = message.getSender();

                if (children.contains(message.getSender())) {
                    children.remove(message.getSender());
                }
                if (children.size() != 0) {
                    node.broadcastChildren(Message.MessageType.GHS_UPDATE_LEADER);
                } else {
                    if (mergeNode == node.nodeUID) {
                        node.sendDirectMessage(parent, Message.MessageType.GHS_ROUND_FINISH);
                    }
                }
            }
        }

        if (message.getType() == Message.MessageType.GHS_UPDATE_LEADER) {
            // System.out.println("message.getType() == Message.MessageType.GHS_UPDATE_LEADER, sender: " + message.getSender());
            leader = message.getLeader();
            if (children.size() != 0) {
                node.broadcastChildren(Message.MessageType.GHS_UPDATE_LEADER);
            } else {
                node.sendDirectMessage(parent, Message.MessageType.GHS_UPDATE_FINISH);
            }
        }

        if (message.getType() == Message.MessageType.GHS_UPDATE_FINISH) {
            // System.out.println("message.getType() == Message.MessageType.GHS_UPDATE_FINISH, sender: " + message.getSender());
            numOfReceivedUpdateFinish += 1;
            if (numOfReceivedUpdateFinish == children.size()){
                if (mergeNode != node.nodeUID) {
                    node.sendDirectMessage(parent, Message.MessageType.GHS_UPDATE_FINISH);
                } else {
                    if (parent != -1) {
                        node.sendDirectMessage(parent, Message.MessageType.GHS_ROUND_FINISH);
                    } else {
                        roundDone = true;
                        update_ack_bool = true;
                    }
                }
                numOfReceivedUpdateFinish = 0;
            }
            
        }

        if (message.getType() == Message.MessageType.GHS_ROUND_FINISH) {
            // System.out.println("message.getType() == Message.MessageType.GHS_ROUND_FINISH, sender: " + message.getSender());
            if (parent == -1){
                node.broadcastChildren(Message.MessageType.GHS_UPDATE_LEVEL);
                roundDone = true;
            } else {
                node.sendDirectMessage(parent, Message.MessageType.GHS_ROUND_FINISH);
            }
        }

        if (message.getType() == Message.MessageType.GHS_UPDATE_LEVEL) {
            // System.out.println("message.getType() == Message.MessageType.GHS_UPDATE_LEVEL, sender: " + message.getSender());
            componentSet.addAll(message.getComponentSet());
            node.broadcastChildren(Message.MessageType.GHS_UPDATE_LEVEL);
            
            if (children.size() == 0) {
                roundDone = true;
                update_ack_bool = true;
                componentSet.addAll(message.getComponentSet());
                node.sendDirectMessage(parent, Message.MessageType.GHS_UPDATE_LEVEL_ACK);
            }
            roundDone = true;
        }

        if (message.getType() == Message.MessageType.GHS_UPDATE_LEVEL_ACK) {
            // System.out.println("message.getType() == Message.MessageType.GHS_UPDATE_LEVEL_ACK, sender: " + message.getSender());
            componentSet.addAll(message.getComponentSet());
            numOfReceivedUpdateLevelAck += 1;
            if (numOfReceivedUpdateLevelAck == children.size()) {
                update_ack_bool = true;
                numOfReceivedUpdateLevelAck = 0;
                if (parent != -1) {
                    node.sendDirectMessage(parent, Message.MessageType.GHS_UPDATE_LEVEL_ACK);
                }
            }
        }
        
    }
    
}
