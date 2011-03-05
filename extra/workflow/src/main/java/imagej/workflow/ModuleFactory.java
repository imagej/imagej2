//
// ModuleFactory.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.workflow;

import java.util.HashMap;
import java.util.Map;

import imagej.workflow.util.xmllight.XMLParser;
import imagej.workflow.util.xmllight.XMLException;
import imagej.workflow.util.xmllight.XMLTag;

/**
 *
 * @author Aivar Grislis
 */
public class ModuleFactory implements IModuleFactory {
    private static ModuleFactory s_instance;
    private Map<String, IModuleFactory> m_factories = new HashMap<String, IModuleFactory>();
    
    private ModuleFactory() {
        register(Workflow.WORKFLOW, WorkflowFactory.getInstance());
        //register(Component.COMPONENT, ComponentFactory.getInstance());
        register(PluginModule.PLUGIN, PluginModuleFactory.getInstance());
    }

    /**
     * Gets singleton instance.
     *
     * @return instance
     */
    public static synchronized ModuleFactory getInstance() {
        if (null == s_instance) {
            s_instance = new ModuleFactory();
        }
        return s_instance;
    }

    public void register(String tagName, IModuleFactory factory) {
        m_factories.put(tagName, factory);
    }
    
    /**
     * Creates a component from XML.
     * 
     * @param xml
     * @return
     * @throws XMLException
     */
    public IModule create(String xml) throws XMLException {
        return create(xml, null);
    }

    /**
     * Creates a component from XML, given the unique instance identifier.
     *
     * @param xml
     * @param instanceId
     * @return
     * @throws XMLException
     */
    public IModule create(String xml, String instanceId) throws XMLException {
        IModule module = null;
        XMLParser xmlHelper = new XMLParser();
        XMLTag tag = xmlHelper.getNextTag(xml);
        IModuleFactory factory = m_factories.get(tag.getName());
        if (null != factory) {
            module = factory.create(xml, instanceId);
        }
        else {
            throw new XMLException("Invalid tag " + tag.getName());
        }
        return module;
    }
}
