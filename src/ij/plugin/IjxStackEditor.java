package ij.plugin;

import ijx.IjxImagePlus;

/**
 *
 * @author GBH
 */
public interface IjxStackEditor extends PlugIn {

    void convertImagesToStack();

    void convertStackToImages(IjxImagePlus imp);

    void run(String arg);

}
