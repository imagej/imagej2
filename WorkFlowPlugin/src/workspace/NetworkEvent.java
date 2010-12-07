package workspace;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Base event class for network communication. 
 */
public class NetworkEvent implements Externalizable
{
    /** Serializable ID. */
    private static final long serialVersionUID = 3487595894809311L;
    
    /** An empty array for events without data. */
    private static final Object[] EMPTY_DATA = new Object[0];
    
    /** The message counter. */
    private static final AtomicInteger ourCounter = new AtomicInteger();
    
    // Data members
    private byte myType;
    private final MsgId myMsgId = new MsgId();
	private Object[] myData;
	private Long mySrcHostId = null;     // not serialized!
	
	public NetworkEvent(byte type, Object... data) {
	    myType = type;
		myData = data == null ? EMPTY_DATA : data;
	}
	
	/** The default constructor, necessary for deserializing. */
	public NetworkEvent() {}
	
	public String toString() {
	    return "{" + myMsgId + " (" + myType + ") " 
	         + Arrays.toString(myData) + "}";
	}
	
	// Gettors/Settors ==========================
	public byte getEventType() { return myType; }
	public Object getData(int i) { return myData[i]; }
	void setSrcHostId(Long id) { mySrcHostId = id; }
	public Long getSrcHostId() { return mySrcHostId; }
	
	// MsgIds ================
	void setHostId(long host) { myMsgId.host = host; }
	MsgId getMsgId() { return myMsgId; }
	
	/** A MsgId encapsulates a message and an id. */
	static class MsgId {
	    private int id;        // Class members set by NetworkManager
	    private long host = 0; // (indirectly)
	    
	    /** When constructed, we take an initial id from our counter. */
	    private MsgId() { id = ourCounter.incrementAndGet(); }
	    
	    public boolean equals(Object obj) {
	        return obj instanceof MsgId &&
	               ((MsgId) obj).host == host &&
	               ((MsgId) obj).id == id;
	    }
	    
	    public int hashCode() {
	        return (int)(host % 31295) + id;
	    }
	    
	    public String toString() {
	        return "[" + host + "," + id + "]";
	    }
	}
	
	// Externalizable ==========================
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
	    myMsgId.host = in.readLong();
	    myMsgId.id = in.readInt();
	    myType = in.readByte();
	    int size = in.readInt();
	    myData = new Object[size];
	    for (int i = 0; i < size; i++)
	        myData[i] = in.readObject();
	}
	
	public void writeExternal(ObjectOutput out) throws IOException {
	    out.writeLong(myMsgId.host);
	    out.writeInt(myMsgId.id);
	    out.writeByte(myType);
	    out.writeInt(myData.length);
	    for (Object o : myData)
	        out.writeObject(o);
	}
}
