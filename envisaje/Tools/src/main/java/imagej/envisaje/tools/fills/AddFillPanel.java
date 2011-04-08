/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.tools.fills;

import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import imagej.envisaje.api.selection.Selection;
import imagej.envisaje.api.image.Layer;
import imagej.envisaje.api.image.Surface;
import imagej.envisaje.misccomponents.FileChooserUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author tim
 */
public final class AddFillPanel extends JPanel implements ActionListener, LookupListener, ChangeListener {
    private final JButton selButton;
    private Lookup.Result <Selection> lkp;
    public AddFillPanel() {
        JButton addButton = new JButton (NbBundle.getMessage(AddFillPanel.class, 
                "LBL_ADD_FILL"));
        addButton.addActionListener(this);
        selButton = new JButton (NbBundle.getMessage(AddFillPanel.class,
                "LBL_FILL_FROM_SELECTION"));
        selButton.addActionListener(this);
        selButton.setName ("sel");
        setLayout (new FlowLayout(FlowLayout.LEADING));
        add (addButton);
        add (selButton);
    }
    
    @Override
    public void addNotify() {
        super.addNotify();
        lkp = Utilities.actionsGlobalContext().lookupResult (Selection.class);
        lkp.addLookupListener(this);
        lkp.allInstances();
        resultChanged(null);
    }
    
    @Override
    public void removeNotify() {
        lkp.removeLookupListener(this);
        lkp = null;
        selButton.setEnabled(false);
        if (last != null) {
            last.removeChangeListener(this);
            last = null;
        }
        super.removeNotify();
    }

    public void actionPerformed(ActionEvent e) {
        JButton jb = (JButton) e.getSource();
        if ("sel".equals(jb.getName())) {
            Collection <? extends Selection> coll = lkp.allInstances();
            Selection sel = coll.iterator().next();
            Layer layer = Utilities.actionsGlobalContext().lookup(Layer.class);
            Surface surface = layer.getSurface();
            BufferedImage img = surface.getImage();
            Shape shape = sel.asShape();
            Rectangle bds = shape == null ? layer.getBounds() : shape.getBounds();
            System.err.println("Shape is " + shape + " bounds " + bds);
            BufferedImage sub = img.getSubimage(bds.x, bds.y, bds.width, bds.height);
            DialogDescriptor.InputLine input = new DialogDescriptor.InputLine(NbBundle.getMessage(
                    AddFillPanel.class, "MSG_ASK_FILL_NAME"), NbBundle.getMessage(AddFillPanel.class,
                    "TTL_ASK_FILL_NAME"));
            if (DialogDisplayer.getDefault().notify(input).equals(DialogDescriptor.OK_OPTION)) {
                try {
                    String in = input.getInputText();
                    if (in == null || in.trim().length() == 0) {
                        Toolkit.getDefaultToolkit().beep();
                        return;
                    }
                    String name = in.trim();
                    PatternFill.add(sub, name);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } else {
            JFileChooser jfc = FileChooserUtils.getFileChooser("image");
            jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
            jfc.setFileFilter(new FileFilter() {
                Set<String> names = new HashSet<String>(Arrays.asList(ImageIO.getReaderFormatNames()));

                @Override
                public boolean accept(File f) {
                    int ix = f.getName().lastIndexOf('.') + 1;
                    String ext = "";
                    if (ix != -1 && ix < f.getName().length() - 1) {
                        ext = f.getName().substring (ix);
                    }
                    return f.isDirectory() || names.contains (ext);
                }

                @Override
                public String getDescription() {
                    return NbBundle.getMessage (AddFillPanel.class, 
                            "DESC_IMAGE_FILES");
                }
            });
            if (jfc.showOpenDialog(Frame.getFrames()[0]) == JFileChooser.APPROVE_OPTION) {
                try {
                    File f = jfc.getSelectedFile();
                    FileObject ob = FileUtil.toFileObject(FileUtil.normalizeFile(f));
                    PatternFill.add(ob, ob.getName());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    Selection last;
    public void resultChanged(LookupEvent arg0) {
        if (lkp == null) {
            return;
        }
        Collection <? extends Selection> coll = lkp.allInstances();
        boolean enable = (!coll.isEmpty());
        if (enable) {
            Selection s = coll.iterator().next();
            if (last != s) {
                if (last != null) {
                    last.removeChangeListener(this);
                }
                s.addChangeListener(this);
            }
            enable = s.asShape() != null;
            if (enable) {
                Layer layer = Utilities.actionsGlobalContext().lookup(Layer.class);
                enable = layer != null && layer.getSurface() != null &&
                        layer.getSurface().getImage() != null;
            }
        }
        selButton.setEnabled(enable);
    }

    public void stateChanged(ChangeEvent e) {
        resultChanged(null);
    }
}
