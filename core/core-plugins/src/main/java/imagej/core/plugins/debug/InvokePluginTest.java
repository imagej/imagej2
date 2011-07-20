/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.core.plugins.debug;

import imagej.ImageJ;
import imagej.data.Dataset;
import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.ext.module.ModuleException;
import imagej.ext.module.ModuleInfo;
import imagej.ext.module.ModuleItem;
import imagej.ext.plugin.ImageJPlugin;
import imagej.ext.plugin.Plugin;
import imagej.ext.plugin.PluginModule;
import imagej.ext.plugin.PluginService;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author GBH
 */
@Plugin(menuPath = "Plugins>Debug>InvokePluginTest")
public class InvokePluginTest implements ImageJPlugin {

	final PluginService pluginService = ImageJ.get(PluginService.class);
	private Object[] passedParameters;
	private Dataset dataset;

	@Override
	public void run() {
		testRun();
//		try {
//			final ModuleInfo info = findModuleInfoFor("imagej.io.plugins.NewImage");
//			// add parameter inputFile
//			info.inputs();
//			PluginModule pModule =  (PluginModule) info.createModule();
//			pModule.getInputs();
//			setInputsFromParameters(info.createModule(), passedParameters);
//			pluginService.run(info, true);
//		} catch (ModuleException ex) {
//			Logger.getLogger(InvokePluginTest.class.getName()).log(Level.SEVERE, null, ex);
//		}
	}

	void testRun() {
		String name = "Untitled";
		String bitDepth = "8-bit";
		boolean signed = false;
		boolean floating = false;
		String fillType = "Ramp";
		int width = 512;
		int height = 512;
		Dataset dataset;
		invoke("imagej.io.plugins.NewImage",
				name, bitDepth, signed, floating, fillType, width, height);
	}

	void testRun(Dataset dataset) {
		String name = "Untitled";
		String bitDepth = "8-bit";
		boolean signed = false;
		boolean floating = false;
		String fillType = "Ramp";
		int width = 512;
		int height = 512;
		invoke("imagej.io.plugins.NewImage", dataset,
				name, bitDepth, signed, floating, fillType, width, height);
	}

	//TODO...
	// private void invoke(String plugin, Object[] parameters) {}
	// private void invoke(String plugin, Map<String, Object> parameters) {}
	
	private void invoke(String plugin, Dataset operand, Object... parameters) {
		this.dataset = operand;
	}

	private void invoke(String plugin, Object... parameters) {
		try {
			final ModuleInfo info = findModuleInfoFor(plugin);
			// add parameter inputFile
			//info.inputs();
			PluginModule pModule = (PluginModule) info.createModule();
			if(this.dataset == null) {
				Util.setInputFromActive(pModule);
			}
			if (setInputsFromParameters(pModule, parameters)) {
				pluginService.run(pModule, true);
			} else {
				pluginService.run(info, true);
			}
			Map<String, Object> outMap = pModule.getOutputs();
			for (Map.Entry<String, Object> entry : outMap.entrySet()) {
				System.out.println("Output: " + entry.getKey() + "/" + entry.getValue());
			}
		} catch (ModuleException ex) {
			Logger.getLogger(InvokePluginTest.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public ModuleInfo findModuleInfoFor(String pluginName) {
		final List<ModuleInfo> modules = pluginService.getModules();
		for (final ModuleInfo info : modules) {
			System.out.println("     " + info.getName() + " : "
					+ info.getDelegateClassName());
			if (pluginName.equals(info.getDelegateClassName())) {
				// add parameter inputFile
				return info;
			}
		}
		return null;
	}

	public boolean setInputsFromParameters(final PluginModule module, final Object... params)
			throws ModuleException {
		Map<String, Object> inputMap = module.getInputs();
		if (inputMap.size() != params.length) {
			System.err.println("inputMap.size() != parameters.length");
			return false;
		}
		final Iterable<ModuleItem<?>> inputs = module.getInfo().inputs();
		int n = 0;
		for (final ModuleItem<?> item : inputs) {
			final String name = item.getName();
			System.out.print("input  " + name);
			Object pValue = params[n];
			System.out.println(", param[" + n + "] = " + pValue);
			//final Object value = module.getInput(name);
			module.setInput(name, pValue);
			module.setResolved(name, true);
			n++;
		}
		return true;
	}

}
