package implementation.mdi;

import javax.swing.*;
import java.awt.BorderLayout;
//import psj.*;

public class InternalPaletteFrame extends JInternalFrame {
  public InternalPaletteFrame(String title, JComponent contents) {
    super(title);
    Integer PALETTE_LAYER = new Integer(3);
    putClientProperty("JInternalFrame.isPalette", Boolean.TRUE);
    getContentPane().setLayout(new BorderLayout());
    setBounds(0, 0, 800, 50);
    setResizable(true);
    setIconifiable(true);
   // psj.PSj.deskTopFrame.desktop.add(this, PALETTE_LAYER);
    getContentPane().add(contents, BorderLayout.NORTH);
    setVisible(true);
    return ;
  }
}
