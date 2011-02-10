/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflow;

import imagej.workflow.util.xmllight.XMLException;

/**
 *
 * @author Aivar Grislis
 */
public interface IModuleFactory {

    /**
     * Creates a module from XML.
     *
     * @param xml string with XML description
     * @return module instance
     * @throws XMLException
     */
    public IModule create(String xml) throws XMLException;

    /**
     * Creates a module from XML.
     *
     * @param xml string with XML description
     * @param null or unique instance identifier
     * @return module instance
     * @throws XMLException
     */
    public IModule create(String xml, String instanceId) throws XMLException;
}
