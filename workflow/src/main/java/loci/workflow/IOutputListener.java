/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

import loci.workflow.plugin.ItemWrapper;

/**
 *
 * @author Aivar Grislis
 */
public interface IOutputListener {

    /**
     * Tells listener that an output image is ready.
     *
     * @param name used for sharing listeners
     * @param image
     */
    public void outputImage(String name, ItemWrapper image);
}
