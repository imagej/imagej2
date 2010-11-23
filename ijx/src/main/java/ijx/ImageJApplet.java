package ijx;


import ijx.gui.ImageCanvas;
import ijx.gui.MenuCanvas;
import ijx.gui.ScrollbarWithLabel;

import ijx.io.PluginClassLoader;
import ijx.IjxImagePlus;
import ijx.IjxTopComponent;
import ijx.ImageJX;
import ijx.app.IjxApplication;
import ijx.gui.IjxStackWindow;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import java.util.Vector;

/**
Runs ImageJ as an applet and optionally opens up to 
nine images using URLs passed as a parameters.
<p>
Here is an example applet tag that launches ImageJ as an applet
and passes it the URLs of two images:
<pre>
&lt;applet archive="../ij.jar" code="ijx.ImageJApplet.class" width=0 height=0&gt;
&lt;param name=url1 value="http://rsb.info.nih.gov/ij/images/FluorescentCells.jpg"&gt;
&lt;param name=url2 value="http://rsb.info.nih.gov/ij/images/blobs.gif"&gt;
&lt;/applet&gt;
</pre>
To use plugins, add them to ij.jar and add entries to IJ_Props.txt file (in ij.jar) that will  
create commands for them in the Plugins menu, or a submenu. There are examples 
of such entries in IJ.Props.txt, in the "Plugins installed in the Plugins menu" section.
<p>
Macros contained in a file named "StartupMacros.txt", in the same directory as the HTML file
containing the applet tag, will be installed on startup.
 */
public class ImageJApplet extends Applet {
    ScrollPane imagePane;
    ScrollbarWithLabel scrollC, scrollZ, scrollT;
    int heightWithoutImage;
    Panel north, south;
    public MenuCanvas menu;
    IjxImagePlus image;
    Vector pluginJarURLs;

    public ImageJApplet() {
        // Try to reset the security manager for signed applets
        try {
            System.setSecurityManager(null);
        } catch (SecurityException e) { /* ignore silently */ }
        setLayout(new BorderLayout());

        menu = new MenuCanvas();
        north = new Panel();
        north.setLayout(new BorderLayout());
        north.add(menu, BorderLayout.NORTH);
        add(north, BorderLayout.NORTH);

        imagePane = new ScrollPane();
        add(imagePane, BorderLayout.CENTER);

        south = new Panel();
        south.setLayout(new GridBagLayout());

        add(south, BorderLayout.SOUTH);
    }

    public Component add(Component c) {
        if (north.getComponentCount() < 2) {
            north.add(c, BorderLayout.SOUTH);
        } else if (getComponentCount() < 3) {
            add(c, BorderLayout.CENTER);
        } else if (getComponentCount() < 4) {
            GridBagConstraints b = new GridBagConstraints();
            b.fill = GridBagConstraints.HORIZONTAL;
            b.weightx = 1;
            b.gridx = 0;
            b.gridy = 0;
            south.add(c, b);
        } else {
            IJ.error("Too many components!");
        }
        return null;
    }

    public void pack() {
        north.doLayout();
        imagePane.doLayout();
        south.doLayout();
        doLayout();
    }

    public void setImageCanvas(ImageCanvas c) {
        if (c != null) {
            imagePane.removeAll();
            imagePane.add(c);
            c.requestFocus();

            if (scrollC != null) {
                south.remove(scrollC);
            }

            if (scrollZ != null) {
                south.remove(scrollZ);
            }

            if (scrollT != null) {
                south.remove(scrollT);
            }

            if (c instanceof ImageCanvas) {
                image = ((ImageCanvas) c).getImage();

                if (image.getWindow() instanceof IjxStackWindow) {
                    IjxStackWindow IjxStackWindow = (IjxStackWindow) image.getWindow();
                    GridBagConstraints b = new GridBagConstraints();

                    scrollC = IjxStackWindow.getCSelector();
                    scrollZ = IjxStackWindow.getZSelector();
                    scrollT = IjxStackWindow.getTSelector();

                    if (scrollC != null) {
                        b.fill = GridBagConstraints.HORIZONTAL;
                        b.weightx = 1;
                        b.gridx = 0;
                        b.gridy = 1;
                        south.add(scrollC, b);
                    }

                    if (scrollZ != null) {
                        b.fill = GridBagConstraints.HORIZONTAL;
                        b.weightx = 1;
                        b.gridx = 0;
                        b.gridy = 2;
                        south.add(scrollZ, b);
                    }

                    if (scrollT != null) {
                        b.fill = GridBagConstraints.HORIZONTAL;
                        b.weightx = 1;
                        b.gridx = 0;
                        b.gridy = 3;
                        south.add(scrollT, b);
                    }
                }
            }
            pack();
        }
    }

    public String getURLParameter(String key) {
        String url = getParameter(key);
        if (url == null) {
            return null;
        }
        if (url.indexOf(":/") < 0) {
            url = getCodeBase().toString() + url;
        }
        if (url.indexOf("://") < 0) {
            int index = url.indexOf(":/");
            if (index > 0) {
                url = url.substring(0, index) + ":///"
                        + url.substring(index + 2);
            }
        }
        return url;
    }

    public Vector getPluginJarURLs() {
        if (pluginJarURLs == null) {
            pluginJarURLs = new Vector();
            for (int i = 1; i < 999; i++) {
                String url = getURLParameter("plugin" + i);
                if (url == null) {
                    break;
                }
                pluginJarURLs.addElement(url);
            }
        }
        return pluginJarURLs;
    }

    /** Starts ImageJ if it's not already running. */
    public void init() {
        IjxTopComponent ij = IJ.getTopComponent();
        if (ij == null || (ij != null && !ij.isVisible())) {
            new ImageJX(this);
        }
        for (int i = 1; i <= 9; i++) {
            String url = getURLParameter("url" + i);
            if (url == null) {
                break;
            }
            IjxImagePlus imp = IJ.getFactory().newImagePlus(url);
            if (imp != null) {
                imp.show();
            }
        }
        /** Also look for up to 9 macros to run. */
        for (int i = 1; i <= 9; i++) {
            String url = getURLParameter("macro" + i);
            if (url == null) {
                break;
            }
            try {
                InputStream in = new URL(url).openStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                StringBuffer sb = new StringBuffer();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                in.close();
                IJ.runMacro(sb.toString());
            } catch (Exception e) {
                IJ.write("warning: " + e);
            }
        }
        /** Also look for up to 9 expressions to evaluate. */
        for (int i = 1; i <= 9; i++) {
            String macroExpression = getParameter("eval" + i);
            if (macroExpression == null) {
                break;
            }
            IJ.runMacro(macroExpression);
        }
        IJ.setClassLoader(new PluginClassLoader(getPluginJarURLs()));
    }

    public void destroy() {
        IjxApplication ij = IJ.getInstance();
        if (ij != null) {
            ij.quit();
        }
    }

    public void stop() {
        IjxTopComponent ij = IJ.getTopComponent();
        ij.dispose();
    }

    public void open(String url) {
        IJ.open(url);
    }

    public void eval(String expression) {
        IJ.runMacro(expression);
    }
}
