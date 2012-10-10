package imagej.core.commands.debug;

import imagej.command.Command;
import imagej.data.measure.MeasurementService;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

@Plugin(menuPath = "Plugins>Sandbox>Measure Tester")
public class MeasureTestPlugin implements Command {

	@Parameter
	private MeasurementService mSrv;
	
	@Override
	public void run() {
		mSrv.testMe();
	}
}
