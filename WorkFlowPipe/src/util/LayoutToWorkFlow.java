package util;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

import loci.workflow.IModule;
import loci.workflow.IModuleInfo;
import loci.workflow.Workflow;
import loci.workflow.WorkflowManager;
import loci.workflow.plugin.ItemWrapper;

import pipesapi.Module;
import pipesentity.Wire;


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
            Map<String, IModule> moduleMap = new HashMap<String, IModule>();

            // create a LOCI workflow
            Workflow workflow = new Workflow();
            workflow.setName("My Workflow");

            // add modules
            for (Module module : moduleList) {
                String name = module.getName().getValue();
                if (!"Pipe Output".equals(name)) { //TODO "Pipe Output" is an internal Pipes plugin
                  //  System.out.println("id is " + module.getID().getValue()); // i.e. "sw-9" //TODO might be necessary to distinguish instances
                    IModule lociModule = getLociModuleByName(name);
                    moduleMap.put(name, lociModule);
                    workflow.add(lociModule);
                }
            }

            // add wires
            if (null != wireList) { //TODO not getting any wires yet
                for (Wire wire : wireList) {
                    String srcModuleId = wire.getSrc().getModuleid();
                    String srcId = wire.getSrc().getId();
                    String dstModuleId = wire.getTgt().getModuleid();
                    String dstId = wire.getTgt().getId();

                    System.out.println("Wiring srcMod " + srcModuleId + " src " + srcId + " dstMod " + dstModuleId + " dst " + dstId);

                    workflow.wire(
                            moduleMap.get(srcModuleId),
                            srcId,
                            moduleMap.get(dstModuleId),
                            dstId);
                }

            }
            // finish up
	    workflow.finalize();

            //TODO to run this workflow with a single, default-named input do:
            //  workflow.input(new ItemWrapper("HELLO"));
            // of course we need a "Pipe Output" plugin to display results

            return workflow;
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

        private static IModule getLociModuleByName(String name) {
            IModule module = null;
            IModuleInfo moduleInfos[] = WorkflowManager.getInstance().getModuleInfos();
            for (IModuleInfo moduleInfo : moduleInfos) {
                if (moduleInfo.getName().equals(name)) {
                    module = WorkflowManager.getInstance().createInstance(moduleInfo);
                    //break;
                }
            }
            return module;
        }
}
