/*
 * $Id: OpenBrowserAction.java 3418 2009-07-27 15:49:55Z kschaefe $
 *
 * Copyright 2006 Sun Microsystems, Inc., 4150 Network Circle,
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

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;

/**
 * An action for opening a {@link URI} in a browser. The URI may be {@code null} and if so this
 * action does nothing.
 * 
 * @author Karl Schaefer
 * @author joshy (original version)
 */
public class OpenBrowserAction extends AbstractAction {
    private static Logger log = Logger.getLogger(OpenBrowserAction.class.getName());

    private URI uri;
    
    /** Creates a new instance of OpenBrowserAction */
    public OpenBrowserAction() {
        this((URI) null);
    }

    /**
     * Creates a new action for the specified URI.
     * 
     * @param uri
     *            the URI
     * @throws NullPointerException
     *             if {@code uri} is {@code null}
     * @throws IllegalArgumentException
     *             if the given string violates RFC&nbsp;2396
     */
    public OpenBrowserAction(String uri) {
        this(URI.create(uri));
    }
    
    /**
     * Creates a new action for the specified URL.
     * 
     * @param url
     *            the URL
     * @throws URISyntaxException
     *             if the URL cannot be converted to a valid URI
     */
    public OpenBrowserAction(URL url) throws URISyntaxException {
        this(url.toURI());
    }
    
    /**
     * Creates a new action for the specified URI.
     * 
     * @param uri
     *            the URI
     */
    public OpenBrowserAction(URI uri) {
        setURI(uri);
    }
    
    /**
     * Gets the current URI.
     * 
     * @return the URI
     */
    public URI getURI() {
        return uri;
    }

    /**
     * Sets the current URI.
     * 
     * @param uri
     *            the new URI
     */
    public void setURI(URI uri) {
        this.uri = uri;
    }
    
    /**
     * {@inheritDoc}
     */
    public void actionPerformed(ActionEvent e) {
        if (uri == null || !Desktop.isDesktopSupported()) {
            return;
        }
        
        if (Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(uri);
            } catch (IOException ioe) {
                log.log(Level.WARNING, "unable to browse: " + uri, ioe);
            }
        }
    }
}
