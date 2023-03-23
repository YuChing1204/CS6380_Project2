package node;

import java.util.ArrayList;
import java.util.List;

public class SynchGHS {
    private Node node;
    private List<Message> bufferedMessages;
	private List<Message> messageList;
    private Boolean done;
    private int leader;

    public SynchGHS(Node node){
        this.bufferedMessages = new ArrayList<>();
        this.messageList = new ArrayList<>();
        this.node = node;
    }
    
}
