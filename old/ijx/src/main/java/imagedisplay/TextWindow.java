package imagedisplay;

import java.io.*;

import java.awt.*;
import javax.swing.*;

public class TextWindow extends JFrame {
  private JScrollPane jScrollPane1 = new JScrollPane();
  private JEditorPane textArea = new JEditorPane();

  public TextWindow(String title) {
    super(title);
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
    textArea.setText(text);
    if (this.isVisible()) {
      textArea.repaint();
    }
  }

  public static void listFileWindow(String filename, String title) {
  char[] content = openTextFile(filename);
  if (content != null) {
    TextWindow tf = new TextWindow(title);
    tf.set(new String(content));

    tf.setBounds(200, 300, 500, 600);
    tf.setVisible(true);
  }
}

public static char[] openTextFile(String fileName) {
  char[] data = null;
  int chars_read = 0;
  FileReader in = null;
  try {
    File file = new File(fileName);
    int size = (int) file.length();
    data = new char[size];
    in = new FileReader(file);
    while (in.ready()) {
      chars_read += in.read(data, chars_read, size - chars_read);
    }
    in.close();
  } catch (IOException e) {
    System.err.println("Cannot open file: " + fileName);
  }
  return data;
}

  // TextWindow
}
