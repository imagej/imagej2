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

public class ValueLabel16
      extends JPanel
{
   GridLayout gridLayout1 = new GridLayout();
   JLabel theValue = new JLabel();
   JLabel theLabel = new JLabel();
   Border border1;
   String label;

   public ValueLabel16 () {
      this("");
   }


   public ValueLabel16 (String _label) {
      label = _label;
      try {
         jbInit();
      }
      catch (Exception ex) {
         ex.printStackTrace();
      }
   }


   Color light = new Color(164, 169, 191);
   Color dark = new Color(134, 141, 171);
   void jbInit () throws Exception {
      border1 = BorderFactory.createLineBorder(Color.gray, 1);
      theValue.setBackground(light);
      theValue.setOpaque(true);
      theValue.setToolTipText("");
      theValue.setVerifyInputWhenFocusTarget(true);
      theValue.setHorizontalAlignment(SwingConstants.CENTER);
      theValue.setFont(new java.awt.Font("Dialog", 0, 9));
      theValue.setText("");
      gridLayout1.setColumns(1);
      gridLayout1.setRows(2);
      this.setLayout(gridLayout1);
      theLabel.setBackground(dark);
      theLabel.setFont(new java.awt.Font("Dialog", 0, 8));
      theLabel.setOpaque(true);
      theLabel.setRequestFocusEnabled(false);
      theLabel.setHorizontalAlignment(SwingConstants.CENTER);
      theLabel.setText(label);
      this.setBorder(border1);
      this.add(theValue, null);
      this.add(theLabel, null);
   }


   public void set (final String s) {
      SwingUtilities.invokeLater(new Runnable()
      {
         public void run () {
            theValue.setText(s);
         }
      });
   }
   public void set (final String s, final Color color) {
   SwingUtilities.invokeLater(new Runnable()
   {
      public void run () {
         theValue.setText(s);
         theValue.setForeground(color);
      }
   });
}

}
