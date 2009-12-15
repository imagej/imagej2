package ijx;

import ijx.gui.IjxStackWindow;
import ijx.gui.IjxGenericDialog;
import ijx.gui.IjxImageWindow;
import ijx.gui.IjxWindow;
import ijx.gui.IjxDialog;
import ijx.gui.IjxImageCanvas;
import ijx.plugin.frame.IjxPluginFrame;
import ij.process.ImageProcessor;
import java.awt.Image;
import java.awt.image.ColorModel;

/**
 *
 * @author GBH
 */
public interface IjxFactory {

    IjxImagePlus newImagePlus();

    IjxImagePlus newImagePlus(String title, Image img);

    IjxImagePlus newImagePlus(String title, ImageProcessor ip);

    IjxImagePlus newImagePlus(String pathOrURL);

    IjxImagePlus newImagePlus(String title, IjxImageStack stack);

    IjxImagePlus[] newImagePlusArray(int n);

    IjxImageCanvas newImageCanvas(IjxImagePlus imp);

    IjxImageStack newImageStack();

    /** Creates a new, empty image stack. */
    IjxImageStack newImageStack(int width, int height);

    /** Creates a new image stack with a capacity of 'size'. */
    IjxImageStack newImageStack(int width, int height, int size);

    /** Creates a new, empty image stack. */
    IjxImageStack newImageStack(int width, int height, ColorModel cm);

    IjxImageStack[] newImageStackArray(int n);

    IjxImageWindow newImageWindow(String title);

    IjxImageWindow newImageWindow(IjxImagePlus imp);

    IjxImageWindow newImageWindow(IjxImagePlus imp, IjxImageCanvas ic);

    IjxImageWindow newStackWindow(IjxImagePlus imp);

    IjxImageWindow newStackWindow(IjxImagePlus imp, IjxImageCanvas ic);


    IjxWindow newWindow();

    IjxDialog newDialog();

    IjxGenericDialog newGenericDialog();
    
    IjxPluginFrame newPluginFrame(String title);

}