/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflowpipes.modules;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

import imagej.workflow.plugin.AbstractPlugin;
import imagej.workflow.plugin.IPlugin;
import imagej.workflow.plugin.ItemWrapper;
import imagej.workflow.plugin.annotations.Item;
import imagej.workflow.plugin.annotations.Input;
import imagej.workflow.plugin.annotations.Output;

/**
 * Plugin reads in an image.
 *
 * @author Aivar Grislis
 */
@Input({
    @Item(name=ImageInputPlugin.IMAGE_NAME, type=Item.Type.STRING)
})
@Output({
    @Item(name=Output.DEFAULT, type=Item.Type.IMAGE)
})
public class ImageInputPlugin extends AbstractPlugin implements IPlugin {
    public static final String IMAGE_NAME = "Image name";

    public void process() {
        System.out.println("In ImageInputPlugin");
        String name = (String) get(IMAGE_NAME).getItem();
        RenderedImage image = null;
        try {
            // Read from a file
            File file = new File(name);
            image = ImageIO.read(file);
        } catch (IOException e) {
            System.out.println("error reading file '" + name + "'");
        }
        ItemWrapper item = new ItemWrapper(image);
        put(item);
    }
}

