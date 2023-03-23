package node;

import java.io.Serializable;

public class Message implements Serializable {

    private int senderUID;
	private int receiverUID;
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

    public Message() {
    }

    public Message(int senderUID, int receiverUID, MessageType type) {
        this.senderUID = senderUID;
        this.receiverUID = receiverUID;
        this.type = type;
    }
    
}
