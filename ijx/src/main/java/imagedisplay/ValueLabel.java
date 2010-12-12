package imagedisplay;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */

public class ValueLabel
    extends JPanel {
  GridLayout gridLayout1 = new GridLayout();
  JLabel theValue = new JLabel();
  JLabel theLabel = new JLabel();
  Border border1;
  String label;

  public ValueLabel() {
    this("");
  }

  public ValueLabel(String _label) {
    label = _label;
    try {
      jbInit();
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  void jbInit() throws Exception {
    border1 = BorderFactory.createLineBorder(Color.gray, 1);
    theValue.setBackground(Color.lightGray);
    theValue.setOpaque(true);
    theValue.setToolTipText("");
    theValue.setVerifyInputWhenFocusTarget(true);
    theValue.setHorizontalAlignment(SwingConstants.CENTER);
    theValue.setText("Value");
    gridLayout1.setColumns(1);
    gridLayout1.setRows(2);
    this.setLayout(gridLayout1);
    theLabel.setBackground(Color.gray);
    theLabel.setFont(new java.awt.Font("Dialog", 0, 9));
    theLabel.setOpaque(true);
    theLabel.setRequestFocusEnabled(false);
    theLabel.setHorizontalAlignment(SwingConstants.CENTER);
    theLabel.setText(label);
    this.setBorder(border1);
    this.add(theValue, null);
    this.add(theLabel, null);
  }

  public void set(final String s) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        theValue.setText(s);
      }
    });
  }
}
