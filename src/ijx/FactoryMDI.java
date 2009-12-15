/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ijx;

import ij.*;
import ijx.gui.IjxDialog;
import ijx.gui.IjxGenericDialog;
import ijx.gui.IjxImageCanvas;
import ijx.gui.IjxImageWindow;
import ijx.gui.IjxStackWindow;
import ijx.gui.IjxWindow;
import ijx.gui.ImageCanvasSwing;
import ijx.gui.ImageWindowMDI;
import ijx.gui.ImageWindowSwing;
import ijx.gui.WindowSwing;
import ijx.plugin.frame.IjxPluginFrame;
import ij.plugin.frame.PlugInFrame;
import ij.process.ImageProcessor;
import java.awt.Image;
import java.awt.image.ColorModel;

/**
 *
 * @author GBH
 */
public class FactoryMDI implements IjxFactory {

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
        return new ImagePlus(title, (ImageStack) stack);
    }

    public IjxImagePlus[] newImagePlusArray(int n) {
        ImagePlus[] ipa = new ImagePlus[n];
        return ipa;
    }

    public IjxImageCanvas newImageCanvas(IjxImagePlus imp) {
        return new ImageCanvasSwing(imp);
    }

    public IjxImageStack newImageStack() {
        return new ImageStack();
    }

    public IjxImageStack newImageStack(int width, int height) {
        return new ImageStack(width, height);
    }

    public IjxImageStack newImageStack(int width, int height, int size) {
        return  new ImageStack(width, height, size);
    }

    public IjxImageStack newImageStack(int width, int height, ColorModel cm) {
        return new ImageStack(width, height, cm);
    }

    public IjxImageStack[] newImageStackArray(int n) {
        return new ImageStack[n];
    }

    public IjxImageWindow newImageWindow(String title) {
        return new ImageWindowSwing(title);
    }

    public IjxImageWindow newImageWindow(IjxImagePlus imp) {
        return new ImageWindowMDI(imp);
    }

    public IjxImageWindow newImageWindow(IjxImagePlus imp, IjxImageCanvas ic) {
        return new ImageWindowMDI(imp, ic);
    }

  @Override
  public IjxImageWindow newStackWindow(IjxImagePlus imp) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public IjxImageWindow newStackWindow(IjxImagePlus imp, IjxImageCanvas ic) {
    throw new UnsupportedOperationException("Not supported yet.");
  }

    public IjxWindow newWindow() {
        return new WindowSwing();
    }

    public IjxDialog newDialog() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public IjxGenericDialog newGenericDialog() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public IjxPluginFrame newPluginFrame(String title) {
        return new PlugInFrame(title);
    }




}
