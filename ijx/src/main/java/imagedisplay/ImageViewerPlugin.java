/*
ImageViewerPlugin
 */

package imagedisplay;

import java.util.List;
import javax.swing.AbstractButton;

/**
 *
 * @author GBH
 */
public interface ImageViewerPlugin {
    
    List<AbstractButton> getButtons();
    
    GraphicOverlay getOverlay();

}
