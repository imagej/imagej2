package org.imagejdev.tests;

import java.io.File;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.xml.sax.SAXException;

/**
 * (based on http://wiki.netbeans.org/DevFaqDynamicSystemFilesystem)
 * @author GBH <imagejdev.org>
 */
@ServiceProvider(service = FileSystem.class)
public class DynamicLayerFromXML extends MultiFileSystem {

    private static DynamicLayerFromXML INSTANCE;
    private static File file;

    public DynamicLayerFromXML() {
        // will be created on startup, exactly once
        INSTANCE = this;
        file = new File("dynamic.xml");
        GeneratorOfXML.generate(file);
        setPropagateMasks(true); // permit *_hidden masks to be used
        this.enable();
    }
    static FileSystem f = FileUtil.createMemoryFileSystem();

    static boolean hasContent() {
        return INSTANCE.getDelegates().length > 0;
    }

    static void enable() {
        if (!hasContent()) {
            try {
                XMLFileSystem xmlFile = new XMLFileSystem(file.toURI().toString());
                INSTANCE.setDelegates(xmlFile);
                //FileUtil.createMemoryFileSystem());
                //new XMLFileSystem(DynamicLayerFromXML.class.getResource("dynamicContent.xml")));
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    static void disable() {
        INSTANCE.setDelegates();
    }
}
