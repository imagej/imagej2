package imagej.legacy.plugin;

import ij.ImagePlus;
import ij.process.ByteProcessor;
import imagej.command.Command;
import imagej.legacy.LegacyOutputTracker;
import imagej.plugin.Plugin;


// Test to see this can work
// a command that mixes IJ1 and IJ2 code without dealing with the legacy layer

@Plugin(menuPath = "Plugins>Sandbox>Mixed World Command")
public class TestMixedModeCommand implements Command {

	@Override
	public void run() {
		final String title = "junkolaTheFourth";
		final int size = 100;
		ByteProcessor proc = new ByteProcessor(size, size);
		ImagePlus imp = new ImagePlus(title, proc);
		if (imp.getWidth() != size) System.out.println("Error: size wrong");
		if (imp.getHeight() != size) System.out.println("Error: size wrong");
		if (imp.getBitDepth() != 8) System.out.println("Error: bit depth wrong");
		if (!imp.getTitle().equals(title)) System.out.println("Error: title wrong");
		imp.show();
		if (LegacyOutputTracker.getOutputImps().contains(imp)) System.out
			.println("Error: output imp is in output list");
		if (LegacyOutputTracker.getClosedImps().contains(imp)) System.out
			.println("Error: output imp is in closed list");
		imp.repaintWindow();
		if (LegacyOutputTracker.getOutputImps().contains(imp)) System.out
			.println("Error: output imp is in output list");
		if (LegacyOutputTracker.getClosedImps().contains(imp)) System.out
			.println("Error: output imp is in closed list");
		imp.hide();
		if (LegacyOutputTracker.getOutputImps().contains(imp)) System.out
			.println("Error: output imp is in output list");
		if (LegacyOutputTracker.getClosedImps().contains(imp)) System.out
			.println("Error: output imp is in closed list");
		imp.close();
		if (LegacyOutputTracker.getOutputImps().contains(imp)) System.out
			.println("Error: output imp is in output list");
		if (LegacyOutputTracker.getClosedImps().contains(imp)) System.out
			.println("Error: output imp is in closed list");
		System.out
			.println("If no messages before this then terminated successfully");
	}

}
