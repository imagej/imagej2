package ij.macro;
import ij.*;
import ij.plugin.*;
import ij.plugin.frame.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Hashtable;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Set;

/** This class implements the text editor's Macros/Find Functions command.
It was written by jerome.mutterer at ibmp.fr, and is based on Mark Longair's CommandFinder plugin.
*/

public class FunctionFinder implements TextListener,  WindowListener, KeyListener, ItemListener {
    Dialog d;
    TextField prompt;
    List completions;
    String [] commands ;
    
    public FunctionFinder() {
        String exists = IJ.runMacro("return File.exists(getDirectory('macros')+'functions.html');");
        if (exists=="0")	{
            String installLocalMacroFunctionsFile = "functions = File.openUrlAsString('"+IJ.URL+"/developer/macro/functions.html');\n"+
                    "f = File.open(getDirectory('macros')+'functions.html');\n"+
                    "print (f, functions);\n"+
                    "File.close(f);";
            try { IJ.runMacro(installLocalMacroFunctionsFile);
            } catch (Throwable e) { IJ.error("Problem downloading functions.html"); return;}
        }
        String f = IJ.runMacro("return File.openAsString(getDirectory('macros')+'functions.html');");
        String [] l = f.split("\n");
        commands= new String [l.length];
        int c=0;
        for (int i=0; i<l.length; i++) {
            String line = l[i];
            if (line.startsWith("<b>")) {
                commands[c]=line.substring(line.indexOf("<b>")+3,line.indexOf("</b>"));
                c++;
            }
        }
        if (c==0) {
        	IJ.error("ImageJ/macros/functions.html is corrupted");
        	return;
        }
        
        ImageJ imageJ = IJ.getInstance();
        d = new Dialog(imageJ, "Built-in Functions");
        d.setLayout(new BorderLayout());
        d.addWindowListener(this);
        Panel northPanel = new Panel();
        prompt = new TextField("", 30);
        prompt.addTextListener(this);
        prompt.addKeyListener(this);
        northPanel.add(prompt);
        d.add(northPanel, BorderLayout.NORTH);
        completions = new List(12);
        completions.addKeyListener(this);
        populateList("");
        d.add(completions, BorderLayout.CENTER);
        d.pack();
        
        Frame frame = WindowManager.getFrontWindow();
        if (frame==null) return;
        java.awt.Point posi=frame.getLocationOnScreen();
        int initialX = (int)posi.getX() + 38;
        int initialY = (int)posi.getY() + 84;
        d.setLocation(initialX,initialY);
        d.setVisible(true);
        d.toFront();
    }

    public void populateList(String matchingSubstring) {
        String substring = matchingSubstring.toLowerCase();
        completions.removeAll();
        try {
            for(int i=0; i<commands.length; ++i) {
                String commandName = commands[i];
                if (commandName.length()==0)
                    continue;
                String lowerCommandName = commandName.toLowerCase();
                if( lowerCommandName.indexOf(substring) >= 0 ) {
                    completions.add(commands[i]);
                }
            }
        } catch (Exception e){}
    }
    
    public void edPaste(String arg) {
        Frame frame = WindowManager.getFrontWindow();
        try {
            TextArea ta = ((Editor)frame).getTextArea();
            int start = ta.getSelectionStart( );
            int end = ta.getSelectionEnd( );
            try {
                ta.replaceRange(arg.substring(0,arg.length()), start, end);
            } catch (Exception e) { }
            if (IJ.isMacOSX())
                ta.setCaretPosition(start+arg.length());
        } catch (Exception e) { }
        
    }
    public void itemStateChanged(ItemEvent ie) {
        populateList(prompt.getText());
    }
    
    protected void runFromLabel(String listLabel) {
        edPaste(listLabel);
        d.dispose();
    }
    
    public void keyPressed(KeyEvent ke) {
        int key = ke.getKeyCode();
        int items = completions.getItemCount();
        Object source = ke.getSource();
        if (source==prompt) {
            if (key==KeyEvent.VK_ENTER) {
                if (1==items) {
                    String selected = completions.getItem(0);
                    runFromLabel(selected);
                }
            } else if (key==KeyEvent.VK_UP) {
                completions.requestFocus();
                if(items>0)
                    completions.select(completions.getItemCount()-1);
            } else if (key==KeyEvent.VK_ESCAPE) {
                d.dispose();
            } else if (key==KeyEvent.VK_DOWN)  {
                completions.requestFocus();
                if (items>0)
                    completions.select(0);
            }
        } else if (source==completions) {
            if (key==KeyEvent.VK_ENTER) {
                String selected = completions.getSelectedItem();
                if (selected!=null)
                    runFromLabel(selected);
            }
        }
    }
    
    public void keyReleased(KeyEvent ke) { }
    
    public void keyTyped(KeyEvent ke) { }
    
    public void textValueChanged(TextEvent te) {
        populateList(prompt.getText());
    }
        
    public void windowClosing(WindowEvent e) {
        d.dispose();
    }
    
    public void windowActivated(WindowEvent e) { }
    public void windowDeactivated(WindowEvent e) { }
    public void windowClosed(WindowEvent e) { }
    public void windowOpened(WindowEvent e) { }
    public void windowIconified(WindowEvent e) { }
    public void windowDeiconified(WindowEvent e) { }
}

