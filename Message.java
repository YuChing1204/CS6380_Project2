package node;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

public class Message implements Serializable {

    private int senderUID;
	private int receiverUID;
    private int leader;
	private MessageType type;
    private List<Integer> mwoe_edge;

    public enum MessageType {
        LOGIN,
        MWOE_SEARCH,
        MWOE_TEST,
        MWOE_TEST_ACCPET,
        MWOE_TEST_REJECT,
        MWOE_COMPLETE,
        GHS_MERGE,
        GHS_MERGE_REQUEST,
        GHS_MERGE_ACCPET,
        GHS_MERGE_REJECT,
        GHS_UPDATE_LEADER_REVERSE,
        GHS_UPDATE_LEADER,
        GHS_UPDATE_FINISH_1,
        GHS_UPDATE_FINISH_2,
        GHS_ROUND_FINISH,
        GHS_ROUND_FINISH_ACK
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

    public List<Integer> getMwoeEdge() {
        return mwoe_edge;
    }

    public Message() {
    }

    public Message(int senderUID, int receiverUID, MessageType type, int leader, List<Integer> mwoe_edge) {
        this.senderUID = senderUID;
        this.receiverUID = receiverUID;
        this.type = type;
        this.leader = leader;
        this.mwoe_edge = mwoe_edge;
    }
    
}
