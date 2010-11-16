package ijx.exec;

//import edu.mbl.jif.utils.prefs.Prefs.FileChooser;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JProgressBar;
import javax.swing.ListModel;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;

public class OpenUsingSwingWorker extends SwingWorker<Object[], Object> {

  enum FILETYPE {

    LST, MSG;
  }
  private FILETYPE _fileType;
  private File[] _files = null;
  private DefaultListModel _listModel = null;
  private List<Object> _objects = null;
  private int _progress = 0;

  public OpenUsingSwingWorker(FILETYPE fileType, File[] files, ListModel listModel) {
    _fileType = fileType;
    _files = files;
    _listModel = (DefaultListModel) listModel;
    _objects = new ArrayList<Object>();
  }

  @Override
  protected Object[] doInBackground() {
    int progress = 0;

    if (_fileType == FILETYPE.LST) {
      BufferedReader bufferedReader = null;
      String line = null;

      for (File file : _files) {
        if (isCancelled()) {
          break;
        }

        try {
          bufferedReader = new BufferedReader(new FileReader(file));

          while ((line = bufferedReader.readLine()) != null) {
            InetAddress inetAddress = InetAddress.getByName(line);

            _objects.add(inetAddress);
            publish(inetAddress);
          }
        } catch (FileNotFoundException ex) {
          Logger.getLogger(OpenUsingSwingWorker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
          Logger.getLogger(OpenUsingSwingWorker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
          Logger.getLogger(OpenUsingSwingWorker.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
          try {
            bufferedReader.close();
          } catch (IOException ex) {
            Logger.getLogger(OpenUsingSwingWorker.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
// ? add sleep
        setProgress(++_progress * 100 / _files.length);
      }
    } else if (_fileType == FILETYPE.MSG) {
      MsgParser msgParser = new MsgParser();
      for (File file : _files) {
        if (isCancelled()) {
          break;
        }
        try {
          MessageRecord messageRecord = new MessageRecord(msgParser.parseMsg(file));
          _objects.add(messageRecord);
          publish(messageRecord);
//        } catch (IOException ex) {
//          Logger.getLogger(OpenSwingWorker.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedOperationException ex) {
          Logger.getLogger(OpenUsingSwingWorker.class.getName()).log(Level.SEVERE, null, ex);
        }
// ? add sleep
        setProgress(++progress * 100 / _files.length);
      }
    }
    return _objects.toArray();
  }

  @Override
  protected void process(List<Object> chunks) {
    for (Object object : chunks) {
      _listModel.addElement(object);
    }
  }
}

class SwingWorkerPropertyChangeListener implements PropertyChangeListener {

  private ProgressJDialog _progressJDialog = null;
  private SwingWorker _swingWorker = null;

  public SwingWorkerPropertyChangeListener(ProgressJDialog progressJDialog, SwingWorker swingWorker) {
    _progressJDialog = progressJDialog;
    _swingWorker = swingWorker;
  }

  public void propertyChange(PropertyChangeEvent evt) {
    String propertyName = evt.getPropertyName();

    if (propertyName.equals("state")) {
      // StateValue changed
      SwingWorker.StateValue stateValue = (StateValue) evt.getNewValue();
      if (stateValue.equals(SwingWorker.StateValue.STARTED)) {
        _progressJDialog.setVisible(true);
      } else if (stateValue.equals(SwingWorker.StateValue.DONE)) {
        _progressJDialog.dispose();
      }
    } else if (propertyName.equals("progress")) {
      // Progress change
      if (_progressJDialog.isCancelled()) {
        _swingWorker.cancel(true);
      } else {
        _progressJDialog.setProgress((Integer) evt.getNewValue());
      }
    }
  }
}

class ProgressJDialog extends JDialog {

  private boolean _cancelled = false;
  private JButton _jButton = null;
  private JProgressBar _jProgressBar = null;

  public ProgressJDialog(boolean indeterminate, String taskName) {
    super((JDialog) null, "Performing Task...", true);

    _jProgressBar = new JProgressBar();

    _jProgressBar.setBorder(BorderFactory.createTitledBorder(taskName));
    _jProgressBar.setIndeterminate(indeterminate);
    _jProgressBar.setStringPainted(true);
    initComponents();
  }

  private void createJButton() {
    _jButton = new JButton("Cancel");

    _jButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    });
    _jButton.setMnemonic(KeyEvent.VK_C);
  }

  private void initComponents() {
    createJButton();
    add(_jProgressBar, BorderLayout.CENTER);
    add(_jButton, BorderLayout.LINE_END);
    addWindowListener(new WindowAdapter() {

      @Override
      public void windowClosed(WindowEvent e) {
        _cancelled = true;
      }
    });
    pack();
    setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
  }

  public boolean isCancelled() {
    return _cancelled;
  }

  public void setProgress(int progress) {
    _jProgressBar.setValue(progress);
  }
}

class MessageRecord {

  public MessageRecord(String msg) {
  }
}

class MsgParser {

  public MsgParser() {
  }

  public String parseMsg(File file) {
    return "";
  }

  public static void main(String[] args) {
    /* EXAMPLE USAGE BELOW */
    JList jList1 = new JList();
    JFileChooser jFileChooser1 = new JFileChooser();
    if (jFileChooser1.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
   //   SwingWorker openSwingWorker = new OpenUsingSwingWorker(FILETYPE.valueOf(jFileChooser1.getFileFilter().getDescription()),
   //           jFileChooser1.getSelectedFiles(), jList1.getModel());

  //    openSwingWorker.addPropertyChangeListener(new SwingWorkerPropertyChangeListener(
  //            new ProgressJDialog(false, "Opening files..."), openSwingWorker));
  //    openSwingWorker.execute();
    }
  }
}

