/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imagejdev.imagine.tools.fills;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.TexturePaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;
import org.imagejdev.imagine.api.util.GraphicsUtils;
import org.imagejdev.imagine.api.util.ImageEditorProvider;
import org.imagejdev.imagine.tools.spi.Fill;
import org.imagejdev.misccomponents.explorer.Customizable;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Tim Boudreau
 */
public class PatternFill implements Fill, Customizable, Serializable {
    private final String path;
    private transient Reference <Paint> ref;
    private final String name;
    public PatternFill(FileObject fob) {
        path = fob.getPath();
        name = fob.getName();
        System.err.println("Created a pattern fill from " + path);
    }
    
    public static PatternFill create(FileObject fileObject) {
        return new PatternFill (fileObject);
    }
    
    private static FileObject getBaseFolder() throws IOException {
        FileObject result = Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject ("fills");
        if (result == null) {
            result = Repository.getDefault().getDefaultFileSystem().getRoot().createFolder("fills");
        }
        return result;
    }
    
    private static URL createThumbnail (String name, BufferedImage img) throws IOException {
        FileObject fld = Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject("FillThumbnails");
        if (fld == null) {
            fld = Repository.getDefault().getDefaultFileSystem().getRoot().createFolder("FillThumbnails");
        }
        FileObject tnail = fld.getFileObject (name, "png");
        if (tnail == null) {
            tnail = fld.createData(name, "png");
        }
        FileLock lock = tnail.lock();
        OutputStream out = tnail.getOutputStream(lock);
        try {
            BufferedImage small = new BufferedImage (16, 16, 
                    GraphicsUtils.DEFAULT_BUFFERED_IMAGE_TYPE);
            Graphics2D g = small.createGraphics();
            try {
                float lgW = img.getWidth();
                float lgH = img.getHeight();
                float smW = 16;
                float smH = 16;
                AffineTransform xform = AffineTransform.getScaleInstance(smW / lgW, smH / lgH);
                g.drawRenderedImage(img, xform);
            } finally {
                g.dispose();
            }
            ImageIO.write(small, "png", out);
        } finally {
            out.close();
            lock.releaseLock();
        }
        return tnail.getURL();
    }
    
    public static PatternFill add (final BufferedImage img, final String name) throws IOException {
        final FileObject root = getBaseFolder();
        final FileObject[] res = new FileObject[1];
        root.getFileSystem().runAtomicAction(new AtomicAction() {
            public void run() throws IOException {
                FileObject fob = root.getFileObject(name + ".instance");
                if (fob != null) {
                    DialogDescriptor dd = new DialogDescriptor(NbBundle.getMessage(PatternFill.class, "MSG_PATTERN_EXISTS"), NbBundle.getMessage(PatternFill.class, "TTL_PATTERN_EXISTS")); //NOI18N
                    if (!DialogDisplayer.getDefault().notify(dd).equals(DialogDescriptor.OK_OPTION)) {
                        return;
                    }
                } else {
                    fob = root.createData(name, "instance"); //NOI18N
                }
                URL url;
                if (img.getWidth() <= 16 && img.getHeight() <= 16) {
                    url = new URL("nbfs:/SystemFileSystem/" + fob.getPath()); //NOI18N
                } else {
                    url = createThumbnail (fob.getName(), img);
                }
                fob.setAttribute( "SystemFileSystem.icon", url); //NOI18N
                //NOI18N
                fob.setAttribute("instanceClass", PatternFill.class.getName()); //NOI18N
                FileLock lock = fob.lock();
                OutputStream out = fob.getOutputStream(lock);
                try {
                    ImageIO.write(img, "png", out);
                } finally {
                    out.close();
                    lock.releaseLock();
                }
                //XXX should not need to use serialization here!
                fob.setAttribute("instanceCreate", new PatternFill(fob)); //NOI18N
                res[0] = fob;
            }
        });
        return res[0] == null ? null : new PatternFill (res[0]);
    }
    
    public static Collection<? extends PatternFill> all() {
        return Lookups.forPath ("fills").lookupAll(PatternFill.class);
    }
    
    public static PatternFill add (FileObject src, String name) throws IOException {
        File file = FileUtil.toFile(src);
        if (file == null) {
            return null;
        }
        BufferedImage img = ImageIO.read(file);
        return add(img, name);
    }

    public Paint getPaint() {
        Paint result = ref == null ? null : ref.get();
        if (result == null) {
            result = loadPaint();
            ref = new SoftReference<Paint>(result);
        }
        return result;
    }

    public JComponent getCustomizer() {
        final JPanel result = new JPanel(new FlowLayout(FlowLayout.LEADING));
        final ImageEditorProvider prov = Lookup.getDefault().lookup(ImageEditorProvider.class);
        if (prov != null) {
            JButton jb = new JButton (NbBundle.getMessage(PatternFill.class, "LBL_EDIT"));
            jb.addActionListener (new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    FileObject fob = Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject(path);
                    if (fob != null) {
                        try {
                            prov.editImage(fob.getURL());
                        } catch (FileStateInvalidException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
                
            });
            result.add (jb);
        }
        return result;
    }
    
    private FileObject getFileObject() {
        return Repository.getDefault().getDefaultFileSystem().getRoot().getFileObject(path);
    }

    private Paint loadPaint() {
        FileObject fob = getFileObject();
        if (fob == null) {
            System.err.println("File object not found");
            return new Color (0,0,0,0);
        }
        BufferedImage img = null;
        for (Iterator<ImageReader> it =ImageIO.getImageReadersByFormatName("png"); it.hasNext();) {
            ImageReader r = it.next();
            try {
                InputStream in = fob.getInputStream();
                MemoryCacheImageInputStream imageIn = new MemoryCacheImageInputStream(in);
                r.setInput(imageIn);
                try {
                    img = r.read(0);
                } finally {
                    imageIn.close();
                    in.close();
                }
            } catch (IOException ioe) {
                Logger.getLogger(PatternFill.class.getName()).log (Level.FINE, 
                        "Exception reading image " + fob.getPath(), ioe);
                ioe.printStackTrace();
            }
            if (img != null) {
                break;
            }
        }
        if (img == null) {
            System.err.println("Image not loaded");
            return new Color (0,0,0,0);
        }
        Rectangle2D.Double r = new Rectangle2D.Double (0, 0, img.getWidth(), img.getHeight());
        return new TexturePaint(img, r);
    }
    
    @Override
    public String toString() {
        return name;
    }

    public void addChangeListener(ChangeListener l) {
        
    }

    public void removeChangeListener(ChangeListener l) {
        
    }
}
