package org.imagejdev.imagefiletypes;

import java.awt.Image;
import java.beans.*;

/**
 * <p>
 * This class describes the properties of the ImageFile class, which follows JavaBean
 * convention.  This class can be used by builder tools such as the BeanBox, but
 * we'll use it with the JGoodies databinding framework to keep models and views
 * in synch with one another.
 * </p>
 *
 * @author Tom Wheeler
 */
public class ImageFileBeanInfo extends SimpleBeanInfo {

    /* (non-Javadoc)
     * @see java.beans.BeanInfo#getPropertyDescriptors()
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            // associates information with specific properties.  Provided we coded
            // the ImageFile bean in a consistent way (which we did), then this is pretty
            // much boilerplate code
            PropertyDescriptor name = new PropertyDescriptor(ImageFile.NAME_PROP, ImageFile.class);
            name.setBound(true);
            name.setDisplayName("ImageFile's Name");

            PropertyDescriptor age = new PropertyDescriptor(ImageFile.TYPE_PROP, ImageFile.class);
            age.setBound(true);
            age.setDisplayName("Type of ImageFile");

            PropertyDescriptor playsFetch = new PropertyDescriptor(ImageFile.PLAYS_FETCH_PROP, ImageFile.class);
            playsFetch.setBound(true);
            playsFetch.setDisplayName("Plays Fetch");

            PropertyDescriptor sex = new PropertyDescriptor(ImageFile.SEX_PROP, ImageFile.class);
            sex.setBound(true);
            sex.setDisplayName("Sex");

            PropertyDescriptor breed = new PropertyDescriptor(ImageFile.BREED_PROP, ImageFile.class);
            breed.setBound(true);
            breed.setDisplayName("Breed");

            return new PropertyDescriptor[]{ name, age, playsFetch, sex, breed };
        } catch (IntrospectionException e) {
            throw new Error(e.toString());
        }
    }

    /* (non-Javadoc)
     * @see java.beans.BeanInfo#getIcon(int)
     */
    public Image getIcon(int type) {
        // This should return the icon for this bean, though it is not used
        // by NetBeans unless the node for the object extends the BeanNode class
        // (this example extends DataNode because it deals with files much better).

        if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
            return loadImage("/org/imagejdev/imagefiletypes/resources/ImageFileicon.gif");
        }

        return null;
    }

}
