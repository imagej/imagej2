package fiji.scripting;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.IconGroup;
import org.fife.ui.rtextarea.GutterIconInfo;
import java.util.List;
import java.util.ArrayList;
import javax.swing.Icon;
import javax.swing.text.BadLocationException;

public class BreakpointManager {

	private Gutter gutter;
	private ArrayList lineNumberList;
	private RSyntaxTextArea textArea;
	private IconGroup iconGroup;

	public BreakpointManager(Gutter gut, RSyntaxTextArea area, IconGroup group) {
		gutter = gut;
		textArea = area;
		iconGroup = group;
		lineNumberList = new ArrayList();
	}

	public List findBreakpointsLineNumber() {
		lineNumberList.clear();
		GutterIconInfo[] icons = gutter.getBookmarks();
		for (int i = 0; i < icons.length; i++) {
			Icon icon = icons[i].getIcon();
			int offset = icons[i].getMarkedOffset();
			try {
				lineNumberList.add(textArea.getLineOfOffset(offset));
			} catch (BadLocationException ble) { 							//It never happens
				System.out.println("bad location exception");
			}
		}
		return lineNumberList;
	}
}
