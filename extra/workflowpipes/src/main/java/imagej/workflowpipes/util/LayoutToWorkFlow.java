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
