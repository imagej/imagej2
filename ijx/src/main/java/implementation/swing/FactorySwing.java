package implementation.swing;

import ijx.CompositeImage;
import ijx.ImageJApplet;
import ijx.ImagePlus;
import ijx.ImageStack;
import ijx.gui.IjxToolbar;
import ijx.process.ImageProcessor;
import ijx.IjxFactory;
import ijx.IjxImagePlus;
import ijx.IjxImageStack;
import ijx.IjxMenus;
import ijx.IjxTopComponent;
import ijx.ImageJX;
import ijx.app.IjxApplication;
import ijx.gui.AbstractImageCanvas;
import ijx.gui.AbstractImageWindow;
import ijx.gui.AbstractStackWindow;
import ijx.gui.IjxDialog;
import ijx.gui.IjxGenericDialog;
import ijx.gui.IjxImageCanvas;
import ijx.gui.IjxImageWindow;
import ijx.gui.IjxProgressBar;
import ijx.gui.IjxWindow;
import ijx.gui.MenuBuilder;
import ijx.plugin.frame.IjxPluginFrame;
import ijx.sezpoz.ActionIjx;
import java.awt.Container;
import java.awt.Image;
import java.awt.image.ColorModel;
import java.util.Map;
import javax.swing.Action;
import javax.swing.JFrame;
import net.java.sezpoz.IndexItem;

/**
 *
 * @author GBH
 */
public class FactorySwing implements IjxFactory {
    static {
        System.out.println("using FactorySwing");
    }

    public IjxTopComponent newTopComponent(IjxApplication app, String title) {
        return new TopComponentSwing(app, title);
    }

    public IjxProgressBar newProgressBar(int canvasWidth, int canvasHeight) {
        return new ProgressBarSwing(canvasWidth, canvasHeight);
    }

    public IjxToolbar newToolBar() {
        return (IjxToolbar) new ToolbarSwing();
    }

    public MenuBuilder newMenuBuilder(
            Map<String, Action> commands,
            Map<String, String> menuCommands,
            Map<String, String> toolbarCommands,
            Map<String, IndexItem<ActionIjx, ?>> items) {

        return new MenuBuilderSwing(commands, menuCommands, toolbarCommands, items);
    }

    public IjxMenus newMenus(IjxTopComponent topComponent, ImageJX ijx, ImageJApplet applet) {
        return new MenusSwing(topComponent, ijx, applet);
    }

    public IjxImagePlus newImagePlus() {
        return new ImagePlus();
    }

    public IjxImagePlus newImagePlus(String title, Image img) {
        return new ImagePlus(title, img);
    }

    public IjxImagePlus newImagePlus(String title, ImageProcessor ip) {
        return new ImagePlus(title, ip);
    }

    public IjxImagePlus newImagePlus(String pathOrURL) {
        return new ImagePlus(pathOrURL);
    }

    public IjxImagePlus newImagePlus(String title, IjxImageStack stack) {
        return new ImagePlus(title, (IjxImageStack) stack);
    }

    public IjxImagePlus CompositeImage(IjxImagePlus imp) {
        return new CompositeImage(imp);
    }

    public IjxImagePlus CompositeImage(IjxImagePlus imp, int mode) {
        return new CompositeImage(imp, mode);
    }

    public IjxImagePlus[] newImagePlusArray(int n) {
        ImagePlus[] ipa = new ImagePlus[n];
        return ipa;
    }

    public IjxImageCanvas newImageCanvas(IjxImagePlus imp) {
        return new AbstractImageCanvas(imp, new ImagePanelSwing(imp));
    }

    public IjxImageStack newImageStack() {
        return new ImageStack();
    }

    public IjxImageStack newImageStack(int width, int height) {
        return new ImageStack(width, height);
    }

    public IjxImageStack newImageStack(int width, int height, int size) {
        return new ImageStack(width, height, size);
    }

    public IjxImageStack newImageStack(int width, int height, ColorModel cm) {
        return new ImageStack(width, height, cm);
    }

    public IjxImageStack[] newImageStackArray(int n) {
        return new ImageStack[n];
    }

    public IjxImageWindow newImageWindow(String title) {
        return new AbstractImageWindow(title, (Container) new ImageWindowSwing());
        //return new ImageWindow(title);
    }

    public IjxImageWindow newImageWindow(IjxImagePlus imp) {
        return new AbstractImageWindow(imp, (Container) new ImageWindowSwing());
    }

    public IjxImageWindow newImageWindow(IjxImagePlus imp, IjxImageCanvas ic) {
        return new AbstractImageWindow(imp, ic, (Container) new ImageWindowSwing());
    }

    @Override
    public IjxImageWindow newStackWindow(IjxImagePlus imp) {
        return new AbstractStackWindow(imp, (Container) new ImageWindowSwing());
    }

    @Override
    public IjxImageWindow newStackWindow(IjxImagePlus imp, IjxImageCanvas ic) {
        return new AbstractStackWindow(imp, ic, (Container) new ImageWindowSwing());
    }

    public IjxWindow newWindow() {
        return new WindowSwing();
    }

    public IjxGenericDialog newGenericDialog() {
        throw new UnsupportedOperationException("Not supported yet."); // @todo
    }

    public IjxPluginFrame newPluginFrame(String title) {
        return (IjxPluginFrame) new PlugInFrameSwing(title);
    }

    public IjxDialog newDialog() {
        return null;
    }
}
