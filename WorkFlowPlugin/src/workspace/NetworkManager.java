package workspace;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JOptionPane;

import workspace.NetworkEvent.MsgId;

/**
 * A NetworkManager maintains connections and host information. The network
 * is peer-to-peer over TCP. 
 */
public class NetworkManager
{    
    /** The size of our most-recently-received message list. */
    private static final int RECEIVED_MSG_BUFFER_SIZE = 10000;
    
    /** Messages that we've heard already. Includes messages we send. */
    private final Set<MsgId> myReceivedMsgs = 
        new HashSet<MsgId>(RECEIVED_MSG_BUFFER_SIZE * 2);
    
    /** The other hosts we communicate directly with. Keyed on identifier. */
    private final Map<Long, NetworkConnection> myConnections = 
        new HashMap<Long, NetworkConnection>();
    
    /** The network server we use to listen for connections. */
    private NetworkServer myServer = null;
    
    /** 
     * The network listener thread we use to process events. Although it can
     * be stopped, for now, we never stop listening.  
     */
    private final NetworkListener myListener = new NetworkListener("Network Listener");
    
    /** 
     * The hash we use to "sign" our messages. Hashes are unique because they
     * are just abcdp, where the host is listening at a.b.c.d:p.
     */
    private long myServerHash = 0L;
    
    public NetworkManager() {
        // Just start the listener thread.
        myListener.start();
    }
    
    public long getServerHash() { return myServerHash; }
    public int getServerPort() { return myServer.getPort(); }
    
    /**
     * Be careful when calling this method. If hash is 0, will reset any
     * existing connections. 
     */
    protected void setServerHash(long hash) {
        if (myServerHash != 0 && hash == 0) {
            // Drop all connections and reopen a server connection. 
            stop();
            openServerConnection();
        }
        else 
            myServerHash = hash;
        
        System.out.println("Reset server hash to " + myServerHash);
    }
    
    public String getServerAddress() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        }
        catch (UnknownHostException uhe) {
            return "not connected";
        }
    }
    
    /** Allow subclasses to access the host information. */
    protected NetworkConnection getHost(Long host) {
        return myConnections.get(host);
    }
    
    /** Broadcast the event to all connected parties. */
    public void sendEvent(NetworkEvent event) {
        if (!myReceivedMsgs.contains(event.getMsgId())) {
            // Only set the host id if we are initiating the event
            event.setHostId(myServerHash);
            myReceivedMsgs.add(event.getMsgId());
        }
        for (NetworkConnection conn : myConnections.values()) {
            try {
                System.out.println("Sending to " + conn + ": " + event);
                conn.write(event);
            }
            catch (IOException ioe) {
                System.out.println("Could not send event to " + conn);
            }
        }
    }
    
    /**
     * Send the event to the host. If we don't know how to reach the host,
     * returns false.
     */
    public boolean sendEvent(Long host, NetworkEvent event) {
        NetworkConnection conn = myConnections.get(host);
        try {
            if (conn != null) {
                if (!myReceivedMsgs.contains(event.getMsgId())) {
                    // Only set the host id if we are initiating the event
                    event.setHostId(myServerHash);
                    myReceivedMsgs.add(event.getMsgId());
                }
                System.out.println("Sending to " + conn + ": " + event);
                conn.write(event);
                return true;
            }
            else {
                System.out.println("Unrecognized host: " + host);
            }
        }
        catch(IOException e) {
            System.out.println("Could not send event to " + conn);
        }
        return false;
    }
    
    /** Open a client connection. */
    protected boolean establishConnection(String host, int port) {
        // Ensure that we have a server hash before allowing any type of
        // connection!
        if (myServerHash == 0) {
            if (!openServerConnection()) {
                return false;
            }
        }
        
        try {
            connect(new Socket(host, port));
            return true;
        }
        catch(IOException e) {
            error("Could not connect on " + host + ":" + port + ".");
            return false;
        }
    }
    
    /** Open a server connection. */
    public boolean openServerConnection() { 
        if (myServer != null) {
            myServer.close();
            myServer = null;
        }
        
        try {
            // Opens the connection on any open port.
            ServerSocket s = new ServerSocket(0); 
            myServer = new NetworkServer(this, s);
            
            // We don't want to reset our identifier after we've already
            // opened a connection, since someone else might identify us 
            // through it.
            if (myServerHash == 0) {
                setServerHash(getHostIdentifier(InetAddress.getLocalHost(), 
                                                myServer.getPort()));
            }
        }
        catch (IOException ioe) {
            // This means the socket could not be opened.
            myServer = null;
            error("Could not open server connection: " + ioe.getMessage());
            return false;
        }
        
        myServer.start();
        return true;
    }
    
    /** Disconnect from the given host, if connected. */
    protected void disconnect(Long id) {
        NetworkConnection conn = myConnections.remove(id);
        if (conn == null)
            error("Not connected to host " + id);
        else
            conn.close();
    }

    /** Close all connections and reset the server. */
    public void stop() {
        if (myServer != null) {
            myServer.close();
            myServer = null;
        }

        // Closes the underlying sockets, including outputs.
        for (NetworkConnection conn : myConnections.values()) {
            conn.close();
        }
        myConnections.clear();
    }

    void connect(Socket s) throws IOException {
        Long host = getHostIdentifier(s.getInetAddress(), s.getPort());
        if (myConnections.containsKey(host)) {
            throw new IOException("Already connected to host: " + host);
        }
        
        NetworkConnection conn = 
            new NetworkConnection(this, host, s, myListener.queue);
        myConnections.put(host, conn);
        conn.start();
    }
    
    /**
     * Process objects as they are read. Subclasses should override this method,
     * as the default implementation simply prints out the event. 
     */
    protected boolean eventReceived(NetworkEvent obj) {
        System.out.println("Received event: " + obj);
        return false;
    }
    
    /**
     * Receive notification that we've disconnected from a host. The
     * NetworkConnection calls this method once close() executes. Subclasses 
     * should override this method to do any cleanups. Make sure to call the
     * super-implementation.
     */
    protected void disconnected(Long id) {
        // Remove this connection from our active connections.
        myConnections.remove(id);
    }
    
    /** Displays an error message to the user. */
    public static void error(String msg) {
        JOptionPane.showMessageDialog(
            null, msg, "Network", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Returns the long (8-byte) identifier for the given host. Assumes IPv4.
     * Future versions that use IPv6 should return 10 bytes (or use a 
     * different algorithm).
     */
    public static long getHostIdentifier(InetAddress addr, int port) {
        byte[] ip = addr.getAddress();
        if (ip.length != 4) {
            throw new RuntimeException("IPv6 addresses not supported");
        }
        
        long id = (ip[0] << 24) + (ip[1] << 16) + (ip[2] << 8) + ip[3];
        return (id << 32) + port;
    }
    
    /** Our network listener. */
    private class NetworkListener extends Thread {
        public final BlockingQueue<NetworkEvent> queue = 
            new LinkedBlockingQueue<NetworkEvent>();
        
        /** Set this to false to stop the thread. */
        public boolean running = true;
        
        protected NetworkListener(String name) {
            super(name);
        }
        
        public void run() {
            while (running) {
                NetworkEvent e = null;
                try {
                    e = queue.take();
                }
                catch (InterruptedException ie) {
                }
                
                if (e != null && !myReceivedMsgs.contains(e.getMsgId())) {
                    if (myReceivedMsgs.size() >= RECEIVED_MSG_BUFFER_SIZE)
                        myReceivedMsgs.clear();
                    myReceivedMsgs.add(e.getMsgId());
                    eventReceived(e);
                }
            }
            queue.clear();
            myReceivedMsgs.clear();
        }
    };
}
