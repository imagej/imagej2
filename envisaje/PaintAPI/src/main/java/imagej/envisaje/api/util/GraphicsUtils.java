/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.envisaje.api.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import org.openide.util.Utilities;

/**
 *
 * @author tim
 */
public final class GraphicsUtils {
    private GraphicsUtils(){}
    
    public static int DEFAULT_BUFFERED_IMAGE_TYPE = 
            Utilities.isMac() ? BufferedImage.TYPE_INT_ARGB_PRE :
                BufferedImage.TYPE_INT_ARGB;
    
    public static void setHighQualityRenderingHints (Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_RENDERING, 
                RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, 
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
                RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, 
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        
    }
    
}
