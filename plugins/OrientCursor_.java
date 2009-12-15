import edu.mbl.jif.ijplugins.PSViewer.PanelStackSelection;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;;
/*
 * APlugIn.java
 */


/**
 * @author GBH
 */
public class OrientCursor_ implements PlugIn {
    
    int x;
    int y;
    int z;
    int flags;
    int x2=x;
    int y2=y;
    int z2=z;
    int w;
    int h;
    ImagePlus[] stackImps;
    ImagePlus magStack;
    ImagePlus orientStack;
    byte[] orientPixels = null;
    double length = 100.0;
    
    public void run(String arg) {
        IJ.log("OrientationCursor started");
        
        if(!selectStacks()) return;
//        if (stackImps[0] == null || stackImps[1] == null) {
//            IJ.showMessage("Stack Selection", "Need to select two stacks.");
//            return;
//        }
        magStack = stackImps[0];
        IJ.log("magStack= " + stackImps[0].getWindow().getName());
        orientStack = stackImps[1];
        IJ.log("orientStack= " + stackImps[1].getWindow().getName());     
        w = magStack.getWidth();
        h = magStack.getHeight();
        if( orientStack.getWidth()!=w || orientStack.getHeight() != h) {
            IJ.error("Stack Selection", "Stacks are not the same width or height.");
            return;
        }
        if(magStack.getImageStackSize() != orientStack.getImageStackSize()) {
            IJ.error("Stack Selection", "Stacks do not have same number of slices.");
            return;
        }
        
//        getCursorLoc();
//        while (true) { //flags&16!=0) {
//            
//            getCursorLoc();
//            if (x!=x2 || y!=y2) {
//                int orient = getOrientation(x, y, z);
//                System.out.println(" = " + orient);
//                if(orient!=-1) {
//                    double dx = Math.cos(orient/2.0/Math.PI);
//                    double dy = Math.sin(orient/2.0/Math.PI);                    
//                    Roi roi = new Line(x, y, x+(int)(dx*length), y+(int)(dy*length));
//                    IJ.getImage().setRoi(roi);
//                    IJ.showStatus(x+","+y);
//                }
//            }
//            x2=x; y2=y;
//            IJ.wait(10);
//        }
    }
        public boolean selectStacks() {
        String title = "Select Stacks";
        String prompt = "Select Stacks";
        JComponent component = null;
        JComponent parent =  null;
        JOptionPane optionPane = new JOptionPane();
        Object msg[] = {title, component};
        optionPane.setMessage(msg);
        optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
        optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = optionPane.createDialog(parent, prompt);
        PanelStackSelection stackSelPanel = new PanelStackSelection();

        optionPane.add(stackSelPanel);
        dialog.setSize(400, 300);
        dialog.setVisible(true);
        Object value = optionPane.getValue();
        stackImps = stackSelPanel.getSelectedStacks();
        if (value == null || !(value instanceof Integer)) {
            System.out.println("Closed");
            return false;
        } else {
            int i = ((Integer) value).intValue();
            if (i == JOptionPane.CLOSED_OPTION) {
                System.out.println("Closed");
                return false;
            } else if (i == JOptionPane.OK_OPTION) {
                System.out.println("OKAY - value is: " + optionPane.getInputValue());
                return true;
            } else if (i == JOptionPane.CANCEL_OPTION) {
                System.out.println("Cancelled");
                return false;
            } else
                return false;
        }
    }
    
    
    public void  getCursorLoc() {
        ImagePlus imagePlus =  IJ.getImage();
        if (imagePlus.getCanvas()==null) return;
        flags = imagePlus.getCanvas().getModifiers();
        java.awt.Point loc = imagePlus.getCanvas().getCursorLoc();
        x = loc.x;
        y = loc.y;
        z = (imagePlus.getCurrentSlice()-1);
    }
    
    
    private int getOrientation(int x, int y, int z) {
        
        int offset = y * w + x;
        System.out.print("getOrientation: " + x + ", "+ y + ", "+ z + " - " + offset);
        if(z!=z2) {
            if(orientStack.getStack().getPixels(z) instanceof byte[])
                orientPixels = (byte[]) orientStack.getStack().getPixels(z);
        }
        if(orientPixels != null) {
            return (int) (orientPixels[offset] & 0xFF);
        }
        return -1;
    }
    

    
}
