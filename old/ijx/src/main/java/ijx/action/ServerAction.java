/*
 * $Id: ServerAction.java 3197 2009-01-21 17:54:30Z kschaefe $
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package ijx.action;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.AccessControlException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

//import org.jdesktop.swing.Application;


/**
 * An action which will invoke an http POST operation.
 *
 * @author Mark Davidson
 */
public class ServerAction extends AbstractAction {
    // Server action support
    private static final Logger LOG = Logger.getLogger(ServerAction.class
            .getName());
    private static final String PARAMS = "action-params";
    private static final String HEADERS = "action-headers";
    private static final String URL = "action-url";

    private static final String URL_CACHE = "_URL-CACHE__";

    public ServerAction() {
        this("action");
    }

    public ServerAction(String name) {
        super(name);
    }

    /**
     * @param name display name of the action
     * @param command the value of the action command key
     */
    public ServerAction(String name, String command) {
        this(name, command, null);
    }

    public ServerAction(String name, Icon icon) {
        super(name, icon);
    }

    /**
     * @param name display name of the action
     * @param command the value of the action command key
     * @param icon icon to display
     */
    public ServerAction(String name, String command, Icon icon) {
        super(name, icon);
        putValue(Action.ACTION_COMMAND_KEY, command);
    }

    /**
     * Set the url for the action.
     * <p>
     * @param url a string representation of the url
     */
    public void setURL(String url) {
        putValue(URL, url);
        putValue(URL_CACHE, null);
    }

    public String getURL() {
        return (String)getValue(URL);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getParams() {
        return (Map)getValue(PARAMS);
    }

    private void setParams(Map<String, String> params) {
        putValue(PARAMS, params);
    }

    /**
     * Adds a name value pair which represents a url parameter in an http
     * POST request.
     */
    public void addParam(String name, String value) {
        Map<String, String> params = getParams();
        if (params == null) {
            params = new HashMap<String, String>();
            setParams(params);
        }
        params.put(name, value);
    }

    /**
     * Return a parameter value corresponding to name or null if it doesn't exist.
     */
    public String getParamValue(String name) {
        Map<String, String> params = getParams();
        return params == null ? null : params.get(name);
    }

    /**
     * Return a set of parameter names or null if there are no params
     */
    public Set<String> getParamNames() {
        Map<String, String> params = getParams();
        return params == null ? null : params.keySet();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getHeaders() {
        return (Map)getValue(HEADERS);
    }

    private void setHeaders(Map<String, String> headers) {
        putValue(HEADERS, headers);
    }

    /**
     * Adds a name value pair which represents a url connection request property.
     * For example, name could be "Content-Type" and the value could be
     * "application/x-www-form-urlencoded"
     */
    public void addHeader(String name, String value) {
        Map<String, String> map = getHeaders();
        if (map == null) {
            map = new HashMap<String, String>();
            setHeaders(map);
        }
        map.put(name, value);
    }

    /**
     * Return a header value corresponding to name or null if it doesn't exist.
     */
    public String getHeaderValue(String name) {
        Map<String, String> headers = getHeaders();
        return headers == null ? null : headers.get(name);
    }

    /**
     * Return a set of parameter names or null if there are no params
     */
    public Set<String> getHeaderNames() {
        Map<String, String> headers = getHeaders();
        return headers == null ? null : headers.keySet();
    }

    /**
     * Invokes the server operation when the action has been invoked.
     */
    public void actionPerformed(ActionEvent evt) {
        URL execURL = (URL)getValue(URL_CACHE);
        if (execURL == null && !"".equals(getURL())) {
            try {
                String url = getURL();
                if (url.startsWith("http")) {
                    execURL = new URL(url);
                } else {
                }
                if (execURL == null) {
                    // XXX TODO: send a message
                    return;
                } else {
                    // Cache this value.
                    putValue(URL_CACHE, execURL);
                }

            } catch (MalformedURLException ex) {
                LOG.log(Level.WARNING, "something went wrong...", ex);
            }
        }

        try {
            URLConnection uc = execURL.openConnection();

            // Get all the header name/value pairs ans set the request headers
            Set<String> headerNames = getHeaderNames();
            if (headerNames != null && !headerNames.isEmpty()) {
                Iterator<String> iter = headerNames.iterator();
                while (iter.hasNext()) {
                    String name = (String)iter.next();
                    uc.setRequestProperty(name, getHeaderValue(name));
                }
            }
            uc.setUseCaches(false);
            uc.setDoOutput(true);

            ByteArrayOutputStream byteStream = new ByteArrayOutputStream(512);
            PrintWriter out = new PrintWriter(byteStream, true);
            out.print(getPostData());
            out.flush();

            // POST requests must have a content-length.
            String length = String.valueOf(byteStream.size());
            uc.setRequestProperty("Content-length", length);

            // Write POST data to real output stream.
            byteStream.writeTo(uc.getOutputStream());

            BufferedReader buf = null;
            if (uc instanceof HttpURLConnection) {
                HttpURLConnection huc = (HttpURLConnection)uc;
                int code = huc.getResponseCode();
                String message = huc.getResponseMessage();

                // Handle the result.
                if (code < 400) {
                    // action succeeded send to status bar
                    // XXX TODO: setStatusMessage(createMessage(code, message));
                    // Format the response
                    // TODO: This should load asychnonously
                    buf = new BufferedReader(new InputStreamReader(uc.getInputStream()));

                } else {
                    // action has failed show dialog
                    // XXX TODO: setStatusMessage(createMessage(code, message));
                    buf = new BufferedReader(new InputStreamReader(huc.getErrorStream()));
                }
                String line;

                StringBuffer buffer = new StringBuffer();
                while ((line = buf.readLine()) != null) {
            // RG: Fix for J2SE 5.0; Can't cascade append() calls because
            // return type in StringBuffer and AbstractStringBuilder are different
                    buffer.append(line);
                    buffer.append('\n');
                }
            // JW: this used the Debug - maybe use finest level?    
            LOG.finer("returned from connection\n" + buffer.toString());    
            }
        } catch (UnknownHostException ex) {
            LOG.log(Level.WARNING, "UnknownHostException detected. Could it be a proxy issue?", ex);
            
        } catch (AccessControlException ex) {
            LOG.log(Level.WARNING, "AccessControlException detected", ex);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "IOException detected", ex);
        }
    }

    /**
     * Retrieves a string which represents the parameter data for a server action.
     * @return a string of name value pairs prefixed by a '?' and delimited by an '&'
     */
    private String getPostData() {
        // Write the data into local buffer
        StringBuffer postData = new StringBuffer();

        // TODO: the action should be configured to retrieve the data.

        // Get all the param name/value pairs and build the data string
        Set<String> paramNames = getParamNames();
        if (paramNames != null && !paramNames.isEmpty()) {
            Iterator<String> iter = paramNames.iterator();
        try {
            while (iter.hasNext()) {
                String name = iter.next();
                postData.append('&').append(name).append('=');
                postData.append(getParamValue(name));
            }
        }
        catch (Exception ex) {  // RG: append(char) throws IOException in J2SE 5.0
            /** @todo Log it */
        }
            // Replace the first & with a ?
            postData.setCharAt(0, '?');
        }
        
        LOG.finer("ServerAction: POST data: " + postData.toString());
        return postData.toString();
    }


    /**
     * Creates a human readable message from the server code and message result.
     * @param code an http error code.
     * @param msg server message
     */
    private String createMessage(int code, String msg) {
        StringBuffer buffer = new StringBuffer("The action \"");
        buffer.append(getValue(NAME));

        if (code < 400) {
            buffer.append("\" has succeeded ");
        } else {
            buffer.append("\" has failed\nPlease check the Java console for more details.\n");
        }
        // RG: Fix for J2SE 5.0; Can't cascade append() calls because
        // return type in StringBuffer and AbstractStringBuilder are different
        buffer.append("\nServer response:\nCode: ");
        buffer.append(code);
        buffer.append(" Message: ");
        buffer.append(msg);

        return buffer.toString();
    }
}
