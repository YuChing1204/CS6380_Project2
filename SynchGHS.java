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
    private int numOfReceivedTest, numOfReceivedComplete, numOfReceivedFinish, numOfReceivedRoundAck;
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
        this.parent = -1;
    }

    public void startSearch() {
        this.numOfReceivedTest = 0;
        this.numOfReceivedComplete = 0;
        this.numOfReceivedFinish = 0;
        this.numOfReceivedRoundAck = 0;

        this.test_edge = new ArrayList<>();
        this.test_weight = new ArrayList<>();
        this.test_edges = new HashMap<>();
        this.mwoe_edge = new ArrayList<>();
        this.mwoe_edge_list = new ArrayList<>();

        if (children.size() != 0){
            node.broadcastChildren(Message.MessageType.MWOE_SEARCH);
        }

        node.broadcast(Message.MessageType.MWOE_TEST, this.node.neighbors);
    }

    public void sendMergeRequest() {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                node.sendDirectMessage(mergeNode, Message.MessageType.GHS_MERGE_REQUEST);
            }
        };
        
        Thread t = new Thread(runnable);
        t.start();
    }

    public void sendLeaderReverse(int recepientId) {
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                node.sendDirectMessage(recepientId, Message.MessageType.GHS_UPDATE_LEADER_REVERSE);
            }
        };
        
        Thread t = new Thread(runnable);
        t.start();
    }

    public void runAlgo(Message message){
        System.out.println(message.getType() + "from " + message.getSender());

        if (message.getType() == Message.MessageType.MWOE_SEARCH){
            this.startSearch();
        }

        if (message.getType() == Message.MessageType.MWOE_TEST) {
            if (message.getLeader() == leader) {
                node.sendDirectMessage(message.getSender(), Message.MessageType.MWOE_TEST_REJECT);
            } else {
                node.sendDirectMessage(message.getSender(), Message.MessageType.MWOE_TEST_ACCPET);
            }
        }

        if (message.getType() == Message.MessageType.MWOE_TEST_ACCPET || message.getType() == Message.MessageType.MWOE_TEST_REJECT){
            numOfReceivedTest += 1;
            test_edge = new ArrayList<>();
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

            if (numOfReceivedTest == node.neighbors.size()){
                int mwoeWeight = Integer.MAX_VALUE;
                mwoe_edge = new ArrayList<>();

                for (List<Integer> key: test_edges.keySet()) {
                    if (test_edges.get(key) < mwoeWeight) {
                        mwoeWeight = test_edges.get(key);
                        mwoe_edge.addAll(key);
                    }
                }

                mwoe_edge.add(mwoeWeight);
                mwoe_edge_list.add(mwoe_edge);
                numOfReceivedTest = 0;

                if (node.nodeUID == leader){
                    if (mwoe_edge.get(0) == node.nodeUID) {
                        mergeNode = mwoe_edge.get(1);
                    } else {
                        mergeNode = mwoe_edge.get(0);
                    }
                    if (children.size() == 0) {
                        this.sendMergeRequest();
                    }
                } else {
                    node.sendDirectMessage(parent, Message.MessageType.MWOE_COMPLETE);
                }

            }
        }

        if (message.getType() == Message.MessageType.MWOE_COMPLETE) {
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
            mergeNode = -1;
            if (node.neighbors.contains(String.valueOf(mwoe_edge.get(0))) || node.neighbors.contains(String.valueOf(mwoe_edge.get(1)))) {
                if (mwoe_edge.get(0) == node.nodeUID){
                    mergeNode = mwoe_edge.get(1);
                } else {
                    mergeNode = mwoe_edge.get(0);
                }
                this.sendMergeRequest();
            } else {
                node.broadcastChildren(Message.MessageType.GHS_MERGE);
            }
        }

        if (message.getType() == Message.MessageType.GHS_MERGE_REQUEST) {
            mergeNode = -1;
            
            if (mwoe_edge.size() != 0) {
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
        }
        
        if (message.getType() == Message.MessageType.GHS_MERGE_ACCPET || message.getType() == Message.MessageType.GHS_MERGE_REJECT) {
            if (message.getType() == Message.MessageType.GHS_MERGE_ACCPET) {
                if (message.getSender() == mergeNode) {
                    if (leader > message.getLeader()) {
                        children.add(mergeNode);
                        this.sendLeaderReverse(message.getSender());
                    }

                    this.mergeNode = this.node.nodeUID;
                }
            } else {
                if (this.parent != -1) {
                    node.sendDirectMessage(parent, Message.MessageType.GHS_UPDATE_FINISH_2);
                } else {
                    this.level += 1;

                    System.out.println("children " + this.children);
                    System.out.println("parent " + this.parent);
                    System.out.println("leader " + this.leader);
                    System.out.println("new level " + this.level);

                    this.startSearch();
                }
            }
        }

        if (message.getType() == Message.MessageType.GHS_UPDATE_LEADER_REVERSE) {
            int oldParent = this.parent;

            this.parent = message.getSender();
            this.leader = message.getLeader();

            if (children.contains(message.getSender())) {
                children.remove(Integer.valueOf(message.getSender()));
            }
            
            if (oldParent != -1) {
                children.add(oldParent);
                node.sendDirectMessage(oldParent, Message.MessageType.GHS_UPDATE_LEADER_REVERSE);
            }

            if (children.size() != 0) {
                node.broadcastChildren(Message.MessageType.GHS_UPDATE_LEADER);
            }

            if (oldParent == -1 && children.size() == 0) {
                node.sendDirectMessage(this.parent, Message.MessageType.GHS_UPDATE_FINISH_2);
            }
        }

        if (message.getType() == Message.MessageType.GHS_UPDATE_LEADER) {
            this.leader = message.getLeader();

            if (children.size() != 0) {
                node.broadcastChildren(Message.MessageType.GHS_UPDATE_LEADER);
            } else {
                node.sendDirectMessage(parent, Message.MessageType.GHS_UPDATE_FINISH_1);
            }
        }

        if (message.getType() == Message.MessageType.GHS_UPDATE_FINISH_1) {
            numOfReceivedFinish += 1;
            if (numOfReceivedFinish == children.size()) {
                if (mergeNode != node.nodeUID) {
                    node.sendDirectMessage(parent, Message.MessageType.GHS_UPDATE_FINISH_1);
                } else {
                    node.sendDirectMessage(parent, Message.MessageType.GHS_UPDATE_FINISH_2);
                }
            }
        }

        if (message.getType() == Message.MessageType.GHS_UPDATE_FINISH_2) {
            if (this.parent != -1) {
                node.sendDirectMessage(this.parent, Message.MessageType.GHS_UPDATE_FINISH_2);
            } else {
                this.level += 1;
                node.broadcastChildren(Message.MessageType.GHS_ROUND_FINISH);
            }
        }

        if (message.getType() == Message.MessageType.GHS_ROUND_FINISH) {
            this.level += 1;

            if (children.size() != 0) {
                node.broadcastChildren(Message.MessageType.GHS_ROUND_FINISH);
            } else {
                System.out.println("children " + this.children);
                System.out.println("parent " + this.parent);
                System.out.println("leader " + this.leader);
                System.out.println("new level " + this.level);

                node.sendDirectMessage(this.parent, Message.MessageType.GHS_ROUND_FINISH_ACK);
            }
        }

        if (message.getType() == Message.MessageType.GHS_ROUND_FINISH_ACK) {
            this.numOfReceivedRoundAck += 1;

            System.out.println("children " + this.children);
            System.out.println("parent " + this.parent);
            System.out.println("leader " + this.leader);
            System.out.println("new level " + this.level);

            if (numOfReceivedRoundAck == children.size()) {
                if (this.parent != -1) {
                    node.sendDirectMessage(this.parent, Message.MessageType.GHS_ROUND_FINISH_ACK);
                } else {
                    this.startSearch();
                }
            }
        }
    }
}
