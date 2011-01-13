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
package org.imagejdev.paintui.actions;

import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.imagejdev.imagine.api.util.GraphicsUtils;
import org.imagejdev.paintui.FileChooserUtils;
import org.imagejdev.paintui.PaintTopComponent;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Timothy Boudreau
 */
public class OpenFileAction extends AbstractAction {

    private static final String ICON_BASE = "org/imagejdev/paintui/resources/openFile24.png"; //NOI18N

    public OpenFileAction() {
        putValue(Action.NAME, NbBundle.getMessage(OpenFileAction.class, "ACT_Open")); //NOI18N
        Icon ic = new ImageIcon(Utilities.loadImage(ICON_BASE));
        putValue(Action.SMALL_ICON, ic);
    }

    public void actionPerformed(ActionEvent e) {
        Frame parent = WindowManager.getDefault().getMainWindow();
//	JFileChooser jfc = new JFileChooser();
        JFileChooser jfc = FileChooserUtils.getFileChooser("image");
        jfc.setFileFilter(new FF());
        jfc.setMultiSelectionEnabled(true);
        jfc.setDialogTitle(NbBundle.getMessage(OpenFileAction.class, "TTL_OpenDlg")); //NOI18N
        jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        jfc.setFileHidingEnabled(false);
        if (jfc.showOpenDialog(parent) == jfc.APPROVE_OPTION) {
            File[] f = jfc.getSelectedFiles();
            TopComponent last = null;
            for (int i = 0; i < f.length; i++) {
                try {
                    BufferedImage bi = ImageIO.read(f[i]);

                    // Loaded PNGs are slow to render;  unpack the data.
//                    if (bi.getType() != GraphicsUtils.DEFAULT_BUFFERED_IMAGE_TYPE) {
//                        BufferedImage nue = new BufferedImage(bi.getWidth(),
//                                bi.getHeight(),
//                                GraphicsUtils.DEFAULT_BUFFERED_IMAGE_TYPE);
//                        Graphics2D g2d = (Graphics2D) nue.createGraphics();
//                        g2d.drawRenderedImage(bi, AffineTransform.getTranslateInstance(0, 0));
//                        g2d.dispose();
//                        bi = nue;
//                    }
                    if (bi.getType() != GraphicsUtils.DEFAULT_BUFFERED_IMAGE_TYPE) {
                        BufferedImage nue = toCompatibleImage(bi);
                        bi = nue;
                    }
                    last = new PaintTopComponent(bi, f[i]);
                    last.setDisplayName(f[i].getName());
                    last.open();
                } catch (IOException ioe) {
                    ErrorManager.getDefault().notify(ErrorManager.USER, ioe);
                }
            }
            if (last != null) {
                last.requestActive();
            }
        }
    }

    public static BufferedImage toCompatibleImage(BufferedImage image) {
        if (image.getColorModel().equals(CONFIGURATION.getColorModel())) {
            return image;
        }
        BufferedImage compatibleImage = CONFIGURATION.createCompatibleImage(
                image.getWidth(), image.getHeight(), image.getTransparency());
        Graphics g = compatibleImage.getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        return compatibleImage;
    }
    private static final GraphicsConfiguration CONFIGURATION =
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();

    private static class FF extends FileFilter {

        final Set<String> fmts;

        FF() {
            //HashSet will be slightly faster, and this will be called
            //repeatedly in the paint loop
            fmts = new HashSet<String>(Arrays.asList(ImageIO.getReaderFormatNames()));
        }

        public boolean accept(File f) {
            int ix = f.getName().lastIndexOf('.'); //NOI18N
            if (ix != -1 && ix != f.getName().length() - 1) {
                String s = f.getName().substring(ix + 1);
                return (fmts.contains(s.toLowerCase())
                        || fmts.contains(s.toUpperCase())) && f.isFile();
            }
            return f.isDirectory();
        }

        public String getDescription() {
            return NbBundle.getMessage(FF.class, "LBL_ImageFileFormats"); //NOI18N
        }
    }
}
