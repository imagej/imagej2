package org.imagejdev.imagine.tools;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.imagejdev.imagine.spi.tools.Customizer;
import org.imagejdev.imagine.spi.tools.CustomizerProvider;
import org.imagejdev.imagine.tools.fills.AddFillPanel;
import org.imagejdev.imagine.tools.spi.Brush;
import org.imagejdev.imagine.tools.spi.MouseDrivenTool;
import org.imagejdev.misccomponents.explorer.SelectAndCustomizePanel;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=org.imagejdev.imagine.spi.tools.Tool.class)

public final class BrushTool extends MouseDrivenTool implements CustomizerProvider, Customizer {

    public BrushTool() {
        super("org/imagejdev/imagine/tools/resources/brush.png", //NOI18N
              org.openide.util.NbBundle.getMessage(BrushTool.class, 
              "NAME_BrushTool")); //NOI18N
    }

    @Override
    public JComponent createCustomizer() {
        return getComponent();
    }
    
    private Brush getBrush() {
        return (Brush) sel.getSelection();
    }

    protected void dragged(java.awt.Point p, int modifiers) {
        if (!isActive()) {
            return;
        }
        Brush brush = getBrush();

        if (brush != null) {
            brush.paint(getLayer().getSurface().getGraphics(), p);
        }
    }

    @Override
    public Customizer getCustomizer() {
        return this;
    }
    
    SelectAndCustomizePanel sel = new SelectAndCustomizePanel ("brushes", true); //NOI18N
    public JComponent getComponent() {
        JPanel pnl = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.EAST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 1;
        gbc.weightx = 1;
        gbc.gridx = 0;
        gbc.gridy = 0;
        pnl.add (sel, gbc);
        gbc.gridy ++;
        pnl.add (new AddFillPanel(), gbc);
        return pnl;
    }
}
