package imagej.envisaje.imagefiletypes.file;

import java.awt.Image;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.SimpleBeanInfo;
import org.openide.loaders.UniFileLoader;
import org.openide.util.Utilities;

/**
 * <p>
 * This class describes information about the loader/file type.  I think
 * it is used to show the icon for the loader in the System Options 
 * dialog box, but I don't see that it is useful beyond that.  Aside
 * from the icon path, this is boilerplate code and there is little of interest
 * in here.
 * </p>
 *
 * @author Tom Wheeler
 */
public class ImageFileDataLoaderBeanInfo extends SimpleBeanInfo {
    
    /*
     * @see java.beans.BeanInfo#getAdditionalBeanInfo()
     */
    public BeanInfo[] getAdditionalBeanInfo() {
        try {
            return new BeanInfo[] {Introspector.getBeanInfo(UniFileLoader.class)};
        } catch (IntrospectionException e) {
            throw new AssertionError(e);
        }
    }
    
    /*
     * @see java.beans.BeanInfo#getIcon(int)
     */
    public Image getIcon(int type) {
        if (type == BeanInfo.ICON_COLOR_16x16 || type == BeanInfo.ICON_MONO_16x16) {
            return Utilities.loadImage("/imagej/envisaje/imagefiletypes/resources/ImageFileicon.gif");
        }
        
        return null;        
    }    
}
