//
// IPlugin.java
//

/*
Multiple instance chainable plugin framework.

Copyright (c) 2010, UW-Madison LOCI
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
  * Redistributions of source code must retain the above copyright
    notice, this list of conditions and the following disclaimer.
  * Redistributions in binary form must reproduce the above copyright
    notice, this list of conditions and the following disclaimer in the
    documentation and/or other materials provided with the distribution.
  * Neither the name of the UW-Madison LOCI nor the
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

package imagej.workflow.plugin;

/**
 * Interface for a Plugin.  Defines the main processing method that a plugin
 * implements, as well as methods that are available to the plugin internally
 * to get and put named objects.
 *
 * @author Aivar Grislis
 */
public interface IPlugin {

    /**
     * Gets the default input object from previous in chain.  Called from within
     * implementation.
     *
     * @return object
     */
    Object get();

    /**
     * Gets a named input object from previous in chain.  Called from within
     * implemenation.
     *
     * @param inName
     * @return object
     */
    Object get(String inName);

    /**
     * This is the body of the plugin, defined in implemenation.
     */
    void process();

    /**
     * Puts the default output object to next in chain (if any).  Called from
     * within implementation.
     *
     * @param object
     */
    void put(Object object);

    /**
     * Puts named output object to next in chain (if any).  Called from within
     * implementation.
     *
     * @param outName
     * @param object
     */
    void put(String outName, Object object);
}
