/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.envisaje.imageframe;


/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
//import org.netbeans.paint.api.components.FileChooserUtils;
//import org.netbeans.paint.api.util.GraphicsUtils;
//import org.netbeans.paintui.PaintTopComponent;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import imagej.Log;
import imagej.MetaData;
import imagej.dataset.Dataset;
import imagej.imglib.dataset.ImgLibDataset;
import imagej.imglib.dataset.LegacyImgLibDataset;
import java.awt.BorderLayout;

import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JScrollPane;

import loci.formats.ChannelMerger;
import loci.formats.FormatException;
import loci.formats.gui.GUITools;
import mpicbg.imglib.image.Image;
import mpicbg.imglib.io.ImageOpener;
import mpicbg.imglib.type.numeric.RealType;
import org.openide.util.AsyncGUIJob;
import org.openide.util.Exceptions;

// Were this a plugin...
// @Plugin(
////	menuPath="File>Import>Bio-Formats...",
//	menu={
//    @Menu(label="File", weight=0, mnemonic='f'),
//    @Menu(label="Import", weight=0, mnemonic='i'),
//    @Menu(label="Bio-Formats...", weight=0, mnemonic='b')
//  },
//  accelerator="^O"
//)
public class OpenAction<T extends RealType<T>> extends AbstractAction {

    private static final String ICON_BASE = "imagej/envisaje/imageframe/resources/openFile24.png"; //NOI18N
    private Dataset dataset;
    private TopComponent tc = new TopComponent();
    NavigableImageFrame iPanel = null;
    private JScrollPane srcScrollPaneImage = null;

    public OpenAction() {
        putValue(Action.NAME, NbBundle.getMessage(OpenAction.class, "ACT_Open")); //NOI18N
        Icon ic = new ImageIcon(Utilities.loadImage(ICON_BASE));
        putValue(Action.SMALL_ICON, ic);
    }

    public void actionPerformed(ActionEvent e) {
        // prompt for input file
        final JFileChooser fileChooser = GUITools.buildFileChooser(new ChannelMerger());
        final int rval = fileChooser.showOpenDialog(null);
        if (rval != JFileChooser.APPROVE_OPTION) {
            return; // canceled
        }
        final String id = fileChooser.getSelectedFile().getAbsolutePath();

        Utilities.attachInitJob(tc, new AsyncGUIJob() {

            public void construct() {
                // open image
                final ImageOpener imageOpener = new ImageOpener();
                try {
                    final Image<T> img = imageOpener.openImage(id);
                    dataset = new LegacyImgLibDataset(img);
                    // TEMP - populate required axis label metadata
                    final MetaData metadata = ImgLibDataset.createMetaData(img.getName());
                    dataset.setMetaData(metadata);
                } catch (FormatException ex) {
                    Log.printStackTrace(ex);
                } catch (IOException ex) {
                    Log.printStackTrace(ex);
                }
                try {
                    iPanel = new NavigableImageFrame();
                    iPanel.setDataset(dataset);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            public void finished() {
                tc.removeAll();
                tc.setDisplayName(dataset.getMetaData().getLabel());
                tc.setLayout(new BorderLayout());
                tc.add(iPanel, BorderLayout.CENTER);
                //tc.repaint();
                //tc.validate();
                tc.open();
                tc.requestActive();
            }
        });
    }

    /*
    PaintTopComponent tc = new PaintTopComponent(pnl.getDimension(), pnl.isTransparent());
    Mode m = WindowManager.getDefault().findMode("editor");
    if (m != null) {
    m.dockInto(tc);
    }
    tc.open();
    tc.requestActive();
    
     */
//        Frame parent = WindowManager.getDefault().getMainWindow();
//	    JFileChooser jfc = new JFileChooser();
// //       JFileChooser jfc = FileChooserUtils.getFileChooser("image");
//        jfc.setFileFilter(new FF());
//        jfc.setMultiSelectionEnabled(true);
//        jfc.setDialogTitle(NbBundle.getMessage(OpenAction.class,
//                "TTL_OpenDlg")); //NOI18N
//        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
//        jfc.setFileHidingEnabled(false);
//        if (jfc.showOpenDialog(parent) == jfc.APPROVE_OPTION) {
//            File[] f = jfc.getSelectedFiles();
//            TopComponent last = null;
//            for (int i = 0; i < f.length; i++) {
//                try {
//                    BufferedImage bi = ImageIO.read(f[i]);
//
//                    // Loaded PNGs are slow to render;  unpack the data.
////                    if (bi.getType() != GraphicsUtils.DEFAULT_BUFFERED_IMAGE_TYPE) {
////                        BufferedImage nue = new BufferedImage(bi.getWidth(),
////                                bi.getHeight(),
////                                GraphicsUtils.DEFAULT_BUFFERED_IMAGE_TYPE);
////                        Graphics2D g2d = (Graphics2D) nue.createGraphics();
////                        g2d.drawRenderedImage(bi, AffineTransform.getTranslateInstance(0, 0));
////                        g2d.dispose();
////                        bi = nue;
////                    }
////                    last = new PaintTopComponent(bi, f[i]);
//                    last.setDisplayName(f[i].getName());
//                    last.open();
//                } catch (IOException ioe) {
//                    ErrorManager.getDefault().notify(ErrorManager.USER, ioe);
//                }
//            }
//            if (last != null) {
//                last.requestActive();
//            }
//        }
//private static class FF extends FileFilter {
//
//    final Set<String> fmts;
//
//    FF() {
//        //HashSet will be slightly faster, and this will be called
//        //repeatedly in the paint loop
//        fmts = new HashSet<String>(Arrays.asList(ImageIO.getReaderFormatNames()));
//    }
//
//    public boolean accept(File f) {
//        int ix = f.getName().lastIndexOf('.'); //NOI18N
//        if (ix != -1 && ix != f.getName().length() - 1) {
//            String s = f.getName().substring(ix + 1);
//            return (fmts.contains(s.toLowerCase())
//                    || fmts.contains(s.toUpperCase())) && f.isFile();
//        }
//        return f.isDirectory();
//    }
//
//    public String getDescription() {
//        return NbBundle.getMessage(FF.class, "LBL_ImageFileFormats"); //NOI18N
//    }
//}
}
