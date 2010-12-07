package workspace;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Queue;

/**
 * This class encapsulates the connections to another host. All events received
 * are passed on to a queue, which another thread is expected to process. 
 */
public class NetworkConnection extends Thread
{
    // Our communication channels.
    private final Socket mySocket;
    private final ObjectInput myReader;
    private final ObjectOutput myWriter;
    private final String myName;
    private final Long myReceivingId;
    private final NetworkManager myNetMgr;
    
    /** We add received events to this queue. */
    private final Queue<NetworkEvent> myQueue;
    
    public NetworkConnection(NetworkManager manager, 
                             Long id, Socket s, Queue<NetworkEvent> q) 
        throws IOException
    {
        super("Reader-" + s.getInetAddress().getHostAddress() + ":" 
                        + s.getPort());
        myReceivingId = id;
        myName = s.getInetAddress().getHostAddress() + ":" + s.getPort();
        mySocket = s;
        myWriter = new ObjectOutputStream(s.getOutputStream());
        myReader = new ObjectInputStream(s.getInputStream());
        myQueue = q;
        myNetMgr = manager;
    }
    
    /** Continually read from the input stream. */
    public void run() {
        System.out.println("Connection to " + myName + " opened");
        while (true) {
            try {
                NetworkEvent obj = (NetworkEvent) myReader.readObject();
                obj.setSrcHostId(myReceivingId);
                myQueue.add(obj);
            }
            catch(IOException e) {
                // This happens when the other user dies...
                System.out.println("Connection to " + myName + " closed");
                break;
            }
            catch (ClassNotFoundException cnfe) {
                System.out.println("Unrecognized network data format");
                break;
            }
        }
        close();
    }
    
    /** Write to the output stream. */
    public void write(NetworkEvent event) throws IOException {
        myWriter.writeObject(event);
        myWriter.flush();
    }
    
    /** Close the streams and the socket. */
    public void close() {
        try {
            myReader.close();
        } catch (IOException ioe) {}    // Do nothing on error
        try {
            myWriter.close();
        } catch (IOException ioe) {}    // Do nothing on error
        try {
            mySocket.close();
        } catch (IOException ioe) {}    // Do nothing on error
        myNetMgr.disconnected(myReceivingId);
    }
    
    public String getHostAddress() { 
        return mySocket.getInetAddress().getHostAddress();
    }
    
    public int getPort() {
        return mySocket.getPort();
    }
    
    public String toString() {
        return myName;
    }
}
