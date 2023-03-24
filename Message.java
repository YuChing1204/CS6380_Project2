package node;

import java.io.Serializable;

public class Message implements Serializable {

    private int senderUID;
	private int receiverUID;
    private int leader;
	private MessageType type;

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
        GHS_UPDATE_LEADER
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

    public Message() {
    }

    public Message(int senderUID, int receiverUID, MessageType type, int leader) {
        this.senderUID = senderUID;
        this.receiverUID = receiverUID;
        this.type = type;
        this.leader = leader;
    }
    
}
