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

public class ValueNoLabel
      extends JPanel
{
   GridLayout gridLayout1 = new GridLayout();
   JLabel theValue = new JLabel();
   Border border1;


   public ValueNoLabel () {
      try {
         jbInit();
      }
      catch (Exception ex) {
         ex.printStackTrace();
      }
   }


   Color light = new Color(164, 169, 191);
   void jbInit () throws Exception {
      border1 = BorderFactory.createLineBorder(Color.gray, 1);
      theValue.setBackground(light);
      theValue.setOpaque(true);
      theValue.setToolTipText("");
      theValue.setVerifyInputWhenFocusTarget(true);
      theValue.setHorizontalAlignment(SwingConstants.CENTER);
//      theValue.putClientProperty(
//                           com.sun.java.swing.SwingUtilities2.AA_TEXT_PROPERTY_KEY,
//                           Boolean.TRUE );
      theValue.setFont(new java.awt.Font("Lucinda Console", 0, 10));
      theValue.setText("");
      gridLayout1.setColumns(1);
      gridLayout1.setRows(1);
      this.setLayout(gridLayout1);
      this.setBorder(border1);
      this.add(theValue, null);
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
