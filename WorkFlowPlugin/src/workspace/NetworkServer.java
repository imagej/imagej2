package workspace;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class NetworkServer extends Thread {
    
    private final NetworkManager netManager;
    private final ServerSocket server;
    
    public NetworkServer(NetworkManager n, ServerSocket s) throws IOException {
        super("Server-" + s.getLocalPort());
        netManager = n;
        server = s;
    }
    
    public int getPort() { return server.getLocalPort(); }

    public void run() {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Listening for Network Connection on " 
                             + host + ":" + server.getLocalPort());
            while (true) {
                Socket s = server.accept();
                netManager.connect(s);
            }
        }
        catch (SocketException se) {
            // Okay - happens when we close this socket.
        }
        catch (IOException e) {
            // Probably not okay.
            close();
        }
    }
    
    /** This stops the thread. */
    public void close() {
        System.out.println("Closing network connection");
        try {
            server.close();
        }
        catch(IOException ioe) {
        }
    }
}
