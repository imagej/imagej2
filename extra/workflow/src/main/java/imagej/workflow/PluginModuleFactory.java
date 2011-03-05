//
// PluginModuleFactory.java
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

/**
 *
 * @author aivar
 */
public class PluginModuleFactory implements IModuleFactory {
    private static PluginModuleFactory s_instance = null;

    private PluginModuleFactory() {
    }

    public static synchronized PluginModuleFactory getInstance() {
        if (null == s_instance) {
            s_instance = new PluginModuleFactory();
        }
        return s_instance;
    }

    /**
     * Creates a plugin module from XML.
     *
     * @param xml string with XML representation
     * @return the module
     */
    public IModule create(String xml) {
        return create(xml, null);
    }

    /**
     * Creates a plugin module from XML, given a unique instance identifier.
     *
     * @param xml sring with XML representation
     * @param instanceId null or unique instance identifler
     * @return the module
     */
    public IModule create(String xml, String instanceId) {
        PluginModule module = new PluginModule();
        if (null != instanceId) {
            module.setInstanceId(instanceId);
        }
        module.fromXML(xml);
        return module;
    }
}
