
package imagej.ui.swt;

import imagej.ImageJ;
import imagej.ext.InstantiableException;
import imagej.ext.plugin.PluginInfo;
import imagej.ext.tool.Tool;
import imagej.ext.tool.ToolService;
import imagej.ui.ToolBar;
import imagej.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import net.miginfocom.swt.MigLayout;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

/**
 * SWT implementation of {@link ToolBar}.
 * 
 * @author Curtis Rueden
 */
public class SWTToolBar extends Composite implements ToolBar {

	private final Display display;
	private final ToolService toolService;
	private final Map<String, Button> toolButtons;

	public SWTToolBar(final Display display, final Composite parent) {
		super(parent, 0);
		this.display = display;
		toolService = ImageJ.get(ToolService.class);
		toolButtons = new HashMap<String, Button>();
		setLayout(new MigLayout());
		populateToolBar();
	}

	// -- ToolBar methods --

	@Override
	public ToolService getToolService() {
		return toolService;
	}

	// -- Helper methods --

	private void populateToolBar() {
		for (final Tool tool : toolService.getTools()) {
			final PluginInfo<Tool> info = tool.getInfo();
			try {
				final Button button = createButton(tool);
				toolButtons.put(info.getName(), button);
			}
			catch (final InstantiableException e) {
				Log.warn("Invalid tool: " + info, e);
			}
		}
	}

	private Button createButton(final Tool tool)
		throws InstantiableException
	{
		final PluginInfo<Tool> info = tool.getInfo();
		final String name = info.getName();
		final URL iconURL = info.getIconURL();

		final Image iconImage = loadImage(iconURL);
		final Button button = new Button(this, SWT.TOGGLE);
		if (iconImage != null) button.setImage(iconImage);
		if (iconURL == null) {
			button.setText(name);
			Log.warn("Invalid icon for tool: " + tool);
		}

		// TODO
//		button.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				getToolService().setActiveTool(tool);
//			}
//		});

		return button;
	}

	private Image loadImage(final URL iconURL) {
		try {
			final InputStream iconStream = iconURL.openStream();
			final Image image = new Image(display, iconStream);
			iconStream.close();
			return image;
		}
		catch (final IOException e) {
			return null;
		}
	}

}
