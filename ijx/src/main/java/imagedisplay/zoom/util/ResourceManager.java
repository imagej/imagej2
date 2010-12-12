package imagedisplay.zoom.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import javax.swing.*;
import javax.imageio.ImageIO;


/**
 * Manage needed resources
 *
 * If you need to modify this file or use this library in your own project, please
 * let me know. Thanks!
 *
 * @author Qiang Yu (qiangyu@gmail.com)
 */
public final class ResourceManager {
    String baseURL;
    public ResourceManager(String url){
        baseURL = url+"/";
    }

    public Image getImage(String imageName) {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream ins;
        BufferedImage bi;

        ins =cl.getResourceAsStream(baseURL+imageName);
        try{
            bi = ImageIO.read(ins);
        }catch(Exception e){
            bi = null;
        }
        return bi;
    }

    public ImageIcon getImageIcon(String imageName) {
        Image i = getImage(imageName);
        if(i==null)
            return null;
        else
            return new ImageIcon(getImage(imageName));
    }
}
