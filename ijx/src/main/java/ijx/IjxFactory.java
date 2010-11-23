package ijx;

import ijx.gui.IjxGenericDialog;
import ijx.gui.IjxImageWindow;
import ijx.gui.IjxWindow;
import ijx.gui.IjxDialog;
import ijx.gui.IjxImageCanvas;
import ijx.plugin.frame.IjxPluginFrame;
import ijx.process.ImageProcessor;
import ijx.app.IjxApplication;
import ijx.gui.IjxProgressBar;
import ijx.gui.IjxToolbar;
import ijx.gui.MenuBuilder;
import ijx.sezpoz.ActionIjx;
import java.awt.Image;
import java.awt.image.ColorModel;
import java.util.Map;
import javax.swing.Action;
import net.java.sezpoz.IndexItem;

/**
 *
 * @author GBH
 */
public interface IjxFactory {
    public IjxTopComponent newTopComponent(IjxApplication app, String title);

    IjxProgressBar newProgressBar(int canvasWidth, int canvasHeight);

    IjxToolbar newToolBar();

    IjxMenus newMenus(IjxTopComponent topComponent, ImageJX ijx, ImageJApplet applet);

    public MenuBuilder newMenuBuilder(
            Map<String, Action> commands,
            Map<String, String> menuCommands,
            Map<String, String> toolbarCommands,
            Map<String, IndexItem<ActionIjx, ?>> items);

    IjxImagePlus newImagePlus();

    IjxImagePlus newImagePlus(String title, Image img);

    IjxImagePlus newImagePlus(String title, ImageProcessor ip);

    IjxImagePlus newImagePlus(String pathOrURL);

    IjxImagePlus newImagePlus(String title, IjxImageStack stack);

    IjxImagePlus CompositeImage(IjxImagePlus imp);

    IjxImagePlus CompositeImage(IjxImagePlus imp, int mode);

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
