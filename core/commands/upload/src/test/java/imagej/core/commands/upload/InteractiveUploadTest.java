package imagej.core.commands.upload;

import java.io.File;

import imagej.ImageJ;
import imagej.command.Command;
import imagej.command.CommandInfo;
import imagej.command.CommandService;
import imagej.event.EventHandler;
import imagej.event.EventService;
import imagej.event.StatusEvent;
import imagej.event.StatusService;

public class InteractiveUploadTest {

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		Thread.currentThread().setContextClassLoader(SampleImageUploader.class.getClassLoader());
		try {
			final ImageJ context = ImageJ.createContext(CommandService.class, StatusService.class, EventService.class);
			context.getService(EventService.class).subscribe(new Object() {
				@SuppressWarnings("unused")
				@EventHandler
				protected void onEvent(final StatusEvent e) {
					final int value = e.getProgressValue();
					final int maximum = e.getProgressMaximum();
					final String message = e.getStatusMessage();
					if (maximum > 0) System.err.print("(" + value + "/" + maximum + ")");
					if (message != null) System.err.print(" " + message);
					System.err.println();
				}
			});
			final CommandInfo info = new CommandInfo(SampleImageUploader.class.getName()) {
				@Override
				public Class<Command> loadClass() {
					return (Class<Command>)(Class<?>)SampleImageUploader.class;
				}
			};
			context.getService(CommandService.class).run(info, "sampleImage", new File("/tmp/test.tif"));
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

}
