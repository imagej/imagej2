/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.workflow;

import loci.util.xmllight.XMLException;

/**
 *
 * @author Aivar Grislis
 */
public interface IModuleFactory {

    /**
     * Creates a module from XML.
     *
     * @param xml
     * @return
     */
    public IModule create(String xml) throws XMLException;
}
