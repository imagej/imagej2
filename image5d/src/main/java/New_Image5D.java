import i5d.Image5D;
import ij.*;
import ij.gui.*;
import ij.plugin.*;

/** Creates and displays a new Image5D. As always, code extensively copied from ImageJ code.
 * This time: ij.gui.NewImage.
 * 
 * @author Joachim Walter
 */
public class New_Image5D implements PlugIn {
    
    private static final int OLD_FILL_WHITE=0;
    
    static final String NAME = "new.name";
    static final String TYPE = "new.type";
    static final String FILL = "new.fill";
    static final String WIDTH = "new.width";
    static final String HEIGHT = "new.height";
    static final String CHANNELS = "new.channels";
    static final String SLICES = "new.slices";
    static final String FRAMES = "new.frames";

    private static String name = Prefs.getString(NAME, "Untitled");
    private static int width = Prefs.getInt(WIDTH, 400);
    private static int height = Prefs.getInt(HEIGHT, 400);
    private static int channels = Prefs.getInt(CHANNELS, 1);
    private static int slices = Prefs.getInt(SLICES, 1);
    private static int frames = Prefs.getInt(FRAMES, 1);
    private static int type = Prefs.getInt(TYPE, NewImage.GRAY8);
    private static int fillWith = Prefs.getInt(FILL, OLD_FILL_WHITE);
    private static String[] types = {"8-bit", "16-bit", "32-bit"};
//    private static String[] fill = {"White", "Black", "Ramp", "Clipboard"};
    private static String[] fill = {"White", "Black", "Ramp"};
    
    
    public void run(String arg) {
        //TODO: GUI
//        new Image5DWindow(createImage5D("", 200, 200, 2, 3, 2, NewImage.GRAY8, NewImage.FILL_BLACK, true));
        openImage();
    }
    
    public static Image5D createImage5D(String title, int width, int height, int nChannels, int nSlices, int nFrames, int bitDepth, int options, boolean fill) {
        int imageType = ImagePlus.GRAY8;
        switch (bitDepth) {
            case 8:
                imageType = ImagePlus.GRAY8;
                break;
            case 16:
                imageType = ImagePlus.GRAY16;
                break;
            case 32:
                imageType = ImagePlus.GRAY32;
                break;
            default:
                return null;
        }

//        int options = 0;
//        if (type.indexOf("white")!=-1)
//            options = NewImage.FILL_WHITE;
//        else if (type.indexOf("black")!=-1)
//            options = NewImage.FILL_BLACK;
//        else if (type.indexOf("ramp")!=-1)
//            options = NewImage.FILL_RAMP;
        options |= NewImage.CHECK_AVAILABLE_MEMORY;

        // Create Image5D
        Image5D i5d = new Image5D(title, imageType, width, height, nChannels, nSlices, nFrames, fill);
        
        if (fill) {
            for (int c=1; c<=nChannels; c++) {
                for (int s=1; s<=nSlices; s++) {
                    for (int f=1; f<=nFrames; f++) {
                        ImagePlus imp = NewImage.createImage(title, width, height, 1, bitDepth, options);
                        i5d.setPixels(imp.getProcessor().getPixels(), c, s, f);
                    }
                }
            }          
        } else {
            ImagePlus imp = NewImage.createImage(title, width, height, 1, bitDepth, options);
            for (int c=1; c<=nChannels; c++) {
                for (int s=1; s<=nSlices; s++) {
                    for (int f=1; f<=nFrames; f++) {
                        i5d.setPixels(imp.getProcessor().getPixels(), c, s, f);
                    }
                }
            }  
        }
        
        i5d.updateImageAndDraw();
        return i5d;        
    }

    
    boolean showDialog() {
        if (type<NewImage.GRAY8|| type>NewImage.GRAY32)
            type = NewImage.GRAY8;
        if (fillWith<OLD_FILL_WHITE||fillWith>NewImage.FILL_RAMP)
            fillWith = OLD_FILL_WHITE;
        GenericDialog gd = new GenericDialog("New...", IJ.getInstance());
        gd.addStringField("Name:", name, 12);
        gd.addChoice("Type:", types, types[type]);
        gd.addChoice("Fill With:", fill, fill[fillWith]);
        gd.addNumericField("Width:", width, 0, 5, "pixels");
        gd.addNumericField("Height:", height, 0, 5, "pixels");
        gd.addNumericField("Channels:", channels, 0, 5, "");
        gd.addNumericField("Slices:", slices, 0, 5, "");
        gd.addNumericField("Frames:", frames, 0, 5, "");
        gd.showDialog();
        if (gd.wasCanceled())
            return false;
        name = gd.getNextString();
        String s = gd.getNextChoice();
        if (s.startsWith("8"))
            type = NewImage.GRAY8;
        else if (s.startsWith("16"))
            type = NewImage.GRAY16;
        else
            type = NewImage.GRAY32;
        
        fillWith = gd.getNextChoiceIndex();
        width = (int)gd.getNextNumber();
        height = (int)gd.getNextNumber();
        channels = (int)gd.getNextNumber();
        slices = (int)gd.getNextNumber();
        frames = (int)gd.getNextNumber();
        return true;
    }
    
    public static void openImage5D(String title, int width, int height, int nChannels, int nSlices, int nFrames, int type, int options, boolean fill ) {
        int bitDepth = 8;
        if (type==NewImage.GRAY16) bitDepth = 16;
        else if (type==NewImage.GRAY32) bitDepth = 32;
        long startTime = System.currentTimeMillis();
        Image5D i5d = createImage5D(title, width, height, nChannels, nSlices, nFrames, bitDepth, options, true);

        i5d.setDefaultColors();
        i5d.setDefaultChannelNames();
        if (i5d!=null) {
            i5d.show(IJ.d2s(((System.currentTimeMillis()-startTime)/1000.0),2)+" seconds");
        }
    }
    
    void openImage() {
        if (!showDialog())
            return;
//        if (fillWith>FILL_RAMP)
//            {showClipboard(); return;}
        try {
            openImage5D(name, width, height, channels, slices, frames, type, fillWith, true);
        }
        catch(OutOfMemoryError e) {IJ.outOfMemory("New_Image5D");}
    }
    
}
