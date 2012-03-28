/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.workflowpipes.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import imagej.workflow.IModule;
import imagej.workflow.IModuleInfo;
import imagej.workflow.Workflow;
import imagej.workflow.WorkflowManager;
import imagej.workflow.plugin.ItemWrapper;

import imagej.workflowpipes.modules.ModuleBase;
import imagej.workflowpipes.pipesapi.Module;
import imagej.workflowpipes.pipesentity.Wire;


/**
 * Static methods to allow transforms from Layouts to WorkFlows
 * @author rick
 *
 */
public class LayoutToWorkFlow {

        /**
         * Gets a LOCI workflow based upon lists of Pipes wires and modules.
         * 
         * @param wireList
         * @param moduleList
         * @return
         */
	public static Workflow getWorkFlow( List<Wire> wireList, List<Module> moduleList )
	{
            // get loci module by instance id
            Map<String, IModule> lociModuleMap = new HashMap<String, IModule>();

            // create a LOCI workflow
            Workflow workflow = new Workflow();
            workflow.setName("Pipes Workflow");
            workflow.setDebug(true);

            // add modules
            for (Module module : moduleList) {
                String name = module.getName().getValue();
                String id = module.getID().getValue();

                System.out.println("Module Name " + module.getName().getValue() + " Id " + module.getID().getValue());
                if (!"Pipe Output".equals(name)) { //TODO "Pipe Output" is an internal Pipes plugin
                    IModule lociModule = createLociModuleInstanceForName(name, id);
                    Map<String, Object> inputs = getInputs(module, wireList);
                    lociModule.setInputs(inputs);
                    lociModuleMap.put(id, lociModule);
                    workflow.add(lociModule);
                }
            }

            // add wires
            if (null != wireList) {
                for (Wire wire : wireList) {
                    String srcModuleId = wire.getSrc().getModuleid();
                    String srcId = wire.getSrc().getId();
                    String dstModuleId = wire.getTgt().getModuleid();
                    String dstId = wire.getTgt().getId();

                    System.out.println("Wiring src module id " + srcModuleId + " src name " + srcId + " dst module id " + dstModuleId + " dst name " + dstId);

                    boolean hasWiredModules = true;
                    IModule srcModule = lociModuleMap.get(srcModuleId);
                    if (null == srcModule) {
                        System.out.println("!Missing src loci module for id " + srcModuleId);
                        hasWiredModules = false;
                    }
                    IModule dstModule = lociModuleMap.get(dstModuleId);
                    if (null == dstModule) {
                        System.out.println("!Missing dst loci module for id " + dstModuleId);
                        hasWiredModules = false;
                    }
                    if (hasWiredModules)
                        workflow.wire(
                            lociModuleMap.get(srcModuleId),
                            srcId,
                            lociModuleMap.get(dstModuleId),
                            dstId);
                }

            }
            // finish up
	    workflow.finalize();

           // of course we need a "Pipe Output" plugin to display results

            return workflow;
	}
        
        /**
         * Builds a map of name to value object user inputs.
         * 
         * @param lociModule
         * @param module
         * @param wireList
         * @return
         */
        private static Map<String, Object> getInputs(Module module, List<Wire> wireList)
        {
            List<String> wiredInputNames = new ArrayList<String>();
            String moduleId = module.getID().getValue();
            for (Wire wire : wireList) {
                if (moduleId.equals(wire.getTgt().getModuleid())) {
                    wiredInputNames.add(wire.getTgt().getId());
                }
            }
            return ((ModuleBase) module).getInputs(wiredInputNames);
        }

        /**
         * Saves an existing workflow.
         *
         * @param workflow
         */
        public static void saveWorkFlow( Workflow workflow ) {
            WorkflowManager.getInstance().addWorkflow( workflow );
            WorkflowManager.getInstance().saveAllWorkflows();
        }

        /**
         * Disposes of an existing workflow.
         *
         * @param name
         * @return
         */
        public static void disposeOfWorkFlow( Workflow workflow ) {
            workflow.quit();
            workflow.clear();
        }

        private static IModule createLociModuleInstanceForName(String name, String instanceId) {
            IModule module = null;
            IModuleInfo moduleInfos[] = WorkflowManager.getInstance().getModuleInfos();
            for (IModuleInfo moduleInfo : moduleInfos) {
                if (moduleInfo.getName().equals(name)) {
                    // create module and pass in the unique instance identifier
                    module = WorkflowManager.getInstance().createInstance(moduleInfo, instanceId);
                    break;
                }
            }
            return module;
        }
}
