/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.paintui;

import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import imagej.envisaje.api.util.ImageEditorProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author tim
 */
@ServiceProvider(service=ImageEditorProvider.class)

public class ImageEditorSupport implements ImageEditorProvider {

    public void editImage(URL url) {
        FileObject ob = URLMapper.findFileObject(url);
        if (ob != null) {
            File f = FileUtil.toFile(ob);
            if (f != null) {
                try {
                    BufferedImage img = ImageIO.read(f);
                    PaintTopComponent tc = new PaintTopComponent(img, f);
                    tc.open();
                    tc.requestActive();
                    return;
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        Toolkit.getDefaultToolkit().beep();
    }

}
