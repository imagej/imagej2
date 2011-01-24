/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflowpipes.modules;

import java.awt.image.BufferedImage;
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
 * Plugin reads in an image, scales in half, outputs.
 *
 * @author Aivar Grislis
 */
@Input({
    @Item(name=Input.DEFAULT, type=Item.Type.IMAGE)
})
@Output({
    @Item(name=Output.DEFAULT, type=Item.Type.IMAGE)
})
public class ScaleInHalfPlugin extends AbstractPlugin implements IPlugin {

    public void process() {
        System.out.println("In ScaleInHalfPlugin");
        RenderedImage image = (RenderedImage) get().getItem();

        // scale in half
        BufferedImage img = (BufferedImage) image;
        int width = image.getWidth() / 2;
        int height = image.getHeight() / 2;
        BufferedImage quarterImage = new BufferedImage(
                width, height, BufferedImage.TYPE_INT_RGB);
        quarterImage.createGraphics().drawImage(
                 img.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH), 0, 0, null);

        ItemWrapper item = new ItemWrapper(quarterImage);
        put(item);
    }
}