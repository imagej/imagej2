package implementation.mdi;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
//import psj.PSj;


public class InternalFrameModal
      extends JInternalFrame
{
   // create opaque glass pane
   JPanel glass = new JPanel();
   static Object caller = null;

   public InternalFrameModal (String _title, JPanel panel, Object _caller) {
      caller = _caller;
      new InternalFrameModal(_title, panel, 0, 0);
   }


   public InternalFrameModal (String _title, JPanel panel) {
      new InternalFrameModal(_title, panel, 0, 0);
   }


   public InternalFrameModal (String _title, JPanel panel, int x, int y,
         Object _caller) {
      caller = _caller;
      new InternalFrameModal(_title, panel, x, y);
   }


   public InternalFrameModal (String _title, JPanel panel, int x, int y) {
      super(_title, false, true, false, false);
      //super(_title, true, true, true, true);
      setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      getContentPane().add(panel, BorderLayout.CENTER);
      glass.setOpaque(false);
      // Attach modal behavior to frame
      addInternalFrameListener(new ModalAdapter(glass));
      // Add modal internal frame to glass pane
      this.setPreferredSize(new Dimension(
            (int) panel.getPreferredSize().getWidth(),
            (int) panel.getPreferredSize().getHeight()));
      setLocation(x, y);
      //psj.PSj.deskTopFrame.desktop.add(this, null);
      //
      glass.setLayout(null);
      glass.add(this, null);
      // Change glass pane to our panel
     //psj.PSj.deskTopFrame.setGlassPane(glass);
      //glass.setVisible(true);
      //setVisible(true);
   }


   public void setModal () {
      // Show glass pane, and contained panel
      glass.setVisible(true);
      setVisible(true);
   }


   public void endModal () {
      glass.setVisible(false);
   }


   static class ModalAdapter
         extends InternalFrameAdapter
   {
      Component glass;

      public ModalAdapter (Component glass) {
         this.glass = glass;
         // Associate dummy mouse listeners, Otherwise mouse events pass through
         MouseInputAdapter adapter = new MouseInputAdapter()
         {};
         glass.addMouseListener(adapter);
         glass.addMouseMotionListener(adapter);
      }


      public void internalFrameClosed (InternalFrameEvent e) {
         glass.setVisible(false);
         onClose();
      }
   }



   public static void onClose () {

   }


/////////////////////////////////////////////////////////////////////////
// FOR TEST
   public static void main (String args[]) {
      final JFrame frame = new JFrame("Modal Internal Frame");
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      final JDesktopPane desktop = new JDesktopPane();

      ActionListener showModal =
            new ActionListener()
      {
         public void actionPerformed (ActionEvent e) {
            // Manually construct a message frame popup
//        JOptionPane optionPane = new JOptionPane();
//        optionPane.setMessage("Hello, World");
//        optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
//        JInternalFrame modal = optionPane.createInternalFrame(desktop, "Modal");
            JInternalFrame jIframe = new JInternalFrame("Another",
                  false, true, false, false);
            jIframe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            // create opaque glass pane
            JPanel glass = new JPanel();
            glass.setOpaque(false);
            // Attach modal behavior to frame
            jIframe.addInternalFrameListener(new ModalAdapter(glass));
            // Add modal internal frame to glass pane
            desktop.add(jIframe);
            glass.add(jIframe);
            // Change glass pane to our panel
            frame.setGlassPane(glass);
            // Show glass pane, then modal dialog
            glass.setVisible(true);
            jIframe.setPreferredSize(new Dimension(250, 100));
            jIframe.setLocation(10, 10);
            jIframe.setVisible(true);
         }
      };
      JInternalFrame internal = new JInternalFrame("Opener");
      desktop.add(internal);
      JButton button = new JButton("Open");
      button.addActionListener(showModal);
      Container iContent = internal.getContentPane();
      iContent.add(button, BorderLayout.CENTER);
      internal.setBounds(25, 25, 200, 100);
      internal.setVisible(true);

      Container content = frame.getContentPane();
      content.add(desktop, BorderLayout.CENTER);
      frame.setSize(500, 300);
      frame.setVisible(true);
   }
}
