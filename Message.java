package node;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Message implements Serializable {

    private int senderUID;
	private int receiverUID;
    private int leader;
	private MessageType type;
    private List<List<Integer>> mwoe_edge_list;
    private List<Integer> mwoe_edge;
    private int level;
    private int mergeNode;
    private HashSet<Integer> componentSet;

    public enum MessageType {
        LOGIN,
        MWOE_SEARCH,
        MWOE_TEST,
        MWOE_TEST_ACCPET,
        MWOE_TEST_REJECT,
        MWOE_COMPLETE,
        GHS_MERGENODE_UPDATE,
        GHS_MERGE,
        GHS_MERGE_REQUEST,
        GHS_MERGE_ACCPET,
        GHS_MERGE_REJECT,
        GHS_UPDATE_LEADER_REVERSE,
        GHS_UPDATE_LEADER,
        GHS_UPDATE_FINISH,
        GHS_ROUND_FINISH,
        GHS_UPDATE_LEVEL,
        GHS_UPDATE_LEVEL_ACK,
        CHECK_LEVEL,
        CHECK_LEVEL_ACK,
        CHECK_LEVEL_NO_ACK,
    }

    public MessageType getType() {
        return type;
    } 

    public int getSender() {
        return senderUID;
    }

    public int getReceiver() {
        return receiverUID;
    }

    public int getLeader() {
        return leader;
    }

    public int getLevel() {
        return level;
    }

    public int getMergeNode() {
        return mergeNode;
    }

    public List<Integer> getMwoeEdge(){
        return mwoe_edge;
    }

    public List<List<Integer>> getMwoeEdgeList() {
        return mwoe_edge_list;
    }

    public HashSet<Integer> getComponentSet(){
        return componentSet;
    }

    public Message() {
    }

    public Message(int senderUID, int receiverUID, MessageType type, int leader, List<Integer> mwoe_edge, List<List<Integer>> mwoe_edge_list, int level, int mergeNode, HashSet<Integer> componentSet) {
        this.senderUID = senderUID;
        this.receiverUID = receiverUID;
        this.type = type;
        this.leader = leader;
        this.mwoe_edge_list = mwoe_edge_list;
        this.level = level;
        this.mwoe_edge = mwoe_edge;
        this.mergeNode = mergeNode;
        this.componentSet = componentSet;
    }
    
}
