package imagej.ui.dnd;

import imagej.display.Display;
import imagej.display.DisplayService;
import imagej.display.TextDisplay;

import org.scijava.plugin.Plugin;

@Plugin(type = DragAndDropHandler.class)
public class TextDragAndDropHandler extends AbstractDragAndDropHandler {

	public static final String MIME_TYPE =
		"text/plain; class=java.util.String; charset=Unicode";

	@Override
	public boolean isCompatible(Display<?> display, DragAndDropData data) {
		if ((display != null) && !(display instanceof TextDisplay)) return false;
		for (final String mimeType : data.getMimeTypes()) {
			if (MIME_TYPE.equals(mimeType)) return true;
		}
		return false;
	}

	@Override
	public boolean drop(Display<?> display, DragAndDropData data) {
		String str = (String) data.getData(MIME_TYPE);
		if (display == null) {
			System.out.println("Creating display");
			DisplayService dispSrv = getContext().getService(DisplayService.class);
			dispSrv.createDisplay(str);
			return true;
		}
		if (!(display instanceof TextDisplay)) return false;
		TextDisplay txtDisp = (TextDisplay) display;
		txtDisp.append(str); // TODO - do a paste rather than an append
		return true;
	}

}
