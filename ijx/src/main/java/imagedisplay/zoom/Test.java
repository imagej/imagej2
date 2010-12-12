package imagedisplay.zoom;


import imagedisplay.zoom.core.ZoomScrollPane;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;



/**
 * Created by IntelliJ IDEA.
 * User: qiang
 * Date: Feb 14, 2005
 * Time: 6:23:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class Test
      extends JFrame
{
   ZoomScrollPane zsp = null;
   public Test () {
      this.setDefaultCloseOperation(EXIT_ON_CLOSE);
      this.getContentPane().setLayout(new BorderLayout());
      zsp = new ZoomScrollPane(new MyJPanel(),
                               JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                               JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      this.getContentPane().add(zsp, BorderLayout.CENTER);
//zsp.getZoomJPanel().setZoomParameters(200,200, 1,1);
      JPanel btnpanel = new JPanel();
      JButton btn1 = new JButton("zoom in");
      JButton btn2 = new JButton("zoom out");
      JButton btn3 = new JButton("restore");
      JButton btn4 = new JButton("fit");

      btnpanel.add(btn1);
      btnpanel.add(btn2);
      btnpanel.add(btn3);
      btnpanel.add(btn4);

      btn1.addActionListener(new ActionListener()
      {
         public void actionPerformed (ActionEvent ae) {
            zsp.zoom(0, 0, zsp.getZoomFactorX() + 1, zsp.getZoomFactorY() + 1);
         }
      });

      btn2.addActionListener(new ActionListener()
      {
         public void actionPerformed (ActionEvent ae) {
            zsp.zoom(0, 0, zsp.getZoomFactorX() - 1, zsp.getZoomFactorY() - 1);
         }
      });

      btn3.addActionListener(new ActionListener()
      {
         public void actionPerformed (ActionEvent ae) {
            zsp.restore();
         }
      });

      btn4.addActionListener(new ActionListener()
      {
         public void actionPerformed (ActionEvent ae) {
            zsp.fitToScreen();
         }
      });

      this.getContentPane().add(btnpanel, BorderLayout.SOUTH);

      this.setSize(400, 400);
      this.setVisible(true);
   }


   public static void main (String[] argv) {
      new Test();
   }
}
