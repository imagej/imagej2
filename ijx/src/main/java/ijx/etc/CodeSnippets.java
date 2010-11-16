
package ijx.etc;

/**
 *
 * @author GBH <imagejdev.org>
 */
public class CodeSnippets {
    /*

        if (Frame.class.isAssignableFrom(w.getClass())) {
            ((Frame) w).;
        }
        if (w instanceof JInternalFrame) {
            ((JInternalFrame) w).;
        }

     *
     *
     * If called with various args:
    @MenuItem(label = "Macro...",
              menu = "Image>Math",
              commandKey="math.macro")
    public static final ActionListener MAC = callWithArg("math.macro", "macro");

    private static ActionListener callWithArg(final String commandKey, final String arg) {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                IJ.runUserPlugIn(commandKey, "ij.plugin.filter.ImageMath", arg, false);
            }
        };
    }
     * If not called with no args:
    @MenuItem(label = "Macro...",
              menu = "Image>Math",
              commandKey="math.macro")

    private static ActionListener callWithArg(final String commandKey, final String arg) {
        return new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                IJ.runUserPlugIn(commandKey, "ij.plugin.filter.ImageMath", null, false);
            }
        };
    }

     */
}
