package ijx.implementation.mdi;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TextWindowInternal extends JInternalFrame {
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JTextArea textArea = new JTextArea();

  public TextWindowInternal(String title) {
    super(title, true, true, true, true);
    try {
      jbInit(title);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit(String title) throws Exception {
    this.getContentPane().add(textArea, BorderLayout.CENTER);
    this.setTitle(title);
    this.setBounds(100, 100, 300, 500);
    //this.setDefaultCloseOperation(HIDE_ON_CLOSE);
    textArea.setEditable(false);
    textArea.setText("Text...");
    textArea.setFont(new Font("Courier", Font.PLAIN, 12));
    textArea.setLineWrap(true);
    jScrollPane1.setHorizontalScrollBarPolicy(
      JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    this.getContentPane().add(jScrollPane1, BorderLayout.CENTER);
    jScrollPane1.getViewport().add(textArea, null);
  }

  public void set(String text) {
    textArea.setText(text);
    textArea.repaint();
  }

  public void append(String text) {
    textArea.append(text);
    if (this.isVisible()) {
      textArea.repaint();
    }
  }
  // TextWindowInternal
}
