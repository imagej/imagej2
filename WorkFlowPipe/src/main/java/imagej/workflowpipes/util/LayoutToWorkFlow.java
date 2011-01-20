package imagej.workflowpipes.util;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Random;

import imagej.workflow.IModule;
import imagej.workflow.IModuleInfo;
import imagej.workflow.Workflow;
import imagej.workflow.WorkflowManager;
import imagej.workflow.plugin.ItemWrapper;

import imagej.workflowpipes.pipesapi.Module;
import imagej.workflowpipes.pipesentity.Wire;


/**
 * Static methods to allow transforms from Layouts to WorkFlows
 * @author rick
 *
 */
public class LayoutToWorkFlow {
    private static Random s_random = new Random();

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
                String id = module.getID().getValue();

                System.out.println("Module Name " + module.getName().getValue() + " Id " + module.getID().getValue());
                if (!"Pipe Output".equals(name)) { //TODO "Pipe Output" is an internal Pipes plugin
                  //  System.out.println("id is " + module.getID().getValue()); // i.e. "sw-9" //TODO might be necessary to distinguish instances
                    IModule lociModule = createLociModuleInstanceForName(name);
                    moduleMap.put(id, lociModule);
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
                    IModule srcModule = moduleMap.get(srcModuleId);
                    if (null == srcModule) {
                        System.out.println("!Missing src loci module for id " + srcModuleId);
                        hasWiredModules = false;
                    }
                    IModule dstModule = moduleMap.get(dstModuleId);
                    if (null == dstModule) {
                        System.out.println("!Missing dst loci module for id " + dstModuleId);
                        hasWiredModules = false;
                    }
                    if (hasWiredModules)
                        workflow.wire(
                            moduleMap.get(srcModuleId),
                            srcId,
                            moduleMap.get(dstModuleId),
                            dstId);
                }

            }
            // finish up
	    workflow.finalize();

            // run this workflow with random inputs
            String inputs[] = workflow.getInputNames();
            for (String input : inputs) {
                String randomName = randomName();
                System.out.println("Workflow input " + input + " is " + randomName);
                workflow.input(new ItemWrapper(randomName), input);
            }
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

        private static IModule createLociModuleInstanceForName(String name) {
            IModule module = null;
            IModuleInfo moduleInfos[] = WorkflowManager.getInstance().getModuleInfos();
            for (IModuleInfo moduleInfo : moduleInfos) {
                if (moduleInfo.getName().equals(name)) {
                    module = WorkflowManager.getInstance().createInstance(moduleInfo);
                    break;
                }
            }
            return module;
        }

        private static String randomName() {
            String names[] = { "Abel", "Alan", "Arthur", "Bertrand", "Betty", "Bill", "Catherine",
                "Donna", "Edward", "Fred", "George", "Hugh", "Ingrid", "Jack", "Kathy", "Louis",
                "Martha", "Nora", "Orlando", "Penelope", "Roger", "Simon", "Thomas", "Veronica",
                "Willy", "Xavier", "Yolanda" };

            return names[s_random.nextInt(names.length)];
        }
}
