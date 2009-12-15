package ij.plugin;
import ijx.IjxImagePlus;
import ij.*;
//import ij.plugin.*;
import ij.plugin.frame.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Vector;
import ij.process.*;
import ij.gui.*;

/** Implements the Image/Color/Color Picker command. */
public class ColorPicker extends ImagePlus implements PlugIn {
    static int id;

    public void run(String arg) {
        IjxImagePlus imp = WindowManager.getImage(id);
        if (imp!=null && imp.getWindow()!=null) {
            imp.getWindow().toFront();
            return;
        }
        int colorWidth = 22;
        int colorHeight = 16;
        int columns = 5;
        int rows = 20;
        int width = columns*colorWidth;
        int height = rows*colorHeight;
        ColorGenerator cg = new ColorGenerator(width, height, new int[width*height], this);
        cg.drawColors(colorWidth, colorHeight, columns, rows);
        setProcessor("CP", cg);
        id = getID();
        show();
        IJ.register(ColorPicker.class);
    }

    /** Overrides ImagePlus.show(). **/
    public void show() {
        if (img==null && ip!=null)
            img = ip.createImage();
        IJ.setCenterOnScreen(true);
        win = new ImageWindow(this, new ColorCanvas(this));
        draw();
        IJ.showStatus("");
    }
    
}

class ColorGenerator extends ColorProcessor {
    int w, h;
    IjxImagePlus imp;
    int[] colors = {0xff0000, 0x00ff00, 0x0000ff, 0xffffff, 0x00ffff, 0xff00ff, 0xffff00, 0x000000};

    public ColorGenerator(int width, int height, int[] pixels, IjxImagePlus imp) {
        super(width, height, pixels);
        this.imp = imp;
    }
        void drawColors(int colorWidth, int colorHeight, int columns, int rows) {
        w = colorWidth;
        h = colorHeight;
        setColor(0xffffff);
        setRoi(0, 0, 110, 320);
        fill();
        drawRamp();
        resetBW();
        flipper();
        drawLine(0, 256, 110, 256);
        
        int x = 1;
        int y = 0;
        refreshBackground();
        refreshForeground();

        Color c;
        float hue, saturation=1f, brightness=1f;
        double w=colorWidth, h=colorHeight;
        for ( x=2; x<10; x++) {
            for ( y=0; y<32; y++) {
                hue = (float)(y/(2*h)-.15);
                if (x<6) { 
                    saturation = 1f;
                    brightness = (float)(x*4/w);
                } else {
                    saturation = 1f - ((float)((5-x)*-4/w));
                    brightness = 1f;
                }
                c = Color.getHSBColor(hue, saturation, brightness);
                setRoi(x*(int)(w/2), y*(int)(h/2), (int)w/2, (int)h/2);
                setColor(c);
                fill();
            }
        }
        drawSpectrum(h);        
        resetRoi();
    }
       
    void drawColor(int x, int y, Color c) 
    {
        setRoi(x*w, y*h, w, h);
        setColor(c);
        fill();
    }

    public void refreshBackground() {
        //Boundary for Background Selection
        setColor(0x444444);
        drawRect((w*2)-12, 276, (w*2)+4, (h*2)+4);
        setColor(0x999999);
        drawRect((w*2)-11, 277, (w*2)+2, (h*2)+2);
        setRoi((w*2)-10, 278, w*2, h*2);//Paints the Background Color
        setColor(Toolbar.getBackgroundColor());
        fill();
        imp.updateAndDraw();
    }

    public void refreshForeground() {
        //Boundary for Foreground Selection
        setColor(0x444444);
        drawRect(8, 266, (w*2)+4, (h*2)+4);
        setColor(0x999999);
        drawRect(9, 267, (w*2)+2, (h*2)+2);
        setRoi(10, 268, w*2, h*2); //Paints the Foreground Color
        setColor(Toolbar.getForegroundColor());
        fill();
        imp.updateAndDraw();
    }

    void drawSpectrum(double h) {
        Color c;
        for ( int x=5; x<7; x++) {
            for ( int y=0; y<32; y++) {
                float hue = (float)(y/(2*h)-.15);        
                c = Color.getHSBColor(hue, 1f, 1f);
                setRoi(x*(int)(w/2), y*(int)(h/2), (int)w/2, (int)h/2);
                setColor(c);
                fill();
            }
        }
    setRoi(55, 32, 22, 16); //Solid red
    setColor(0xff0000);
    fill();
    setRoi(55, 120, 22, 16); //Solid green
    setColor(0x00ff00);
    fill();
    setRoi(55, 208, 22, 16); //Solid blue
    setColor(0x0000ff);
    fill();
    setRoi(55, 80, 22, 8); //Solid yellow
    setColor(0xffff00);
    fill();
    setRoi(55, 168, 22, 8); //Solid cyan
    setColor(0x00ffff);
    fill();
    setRoi(55, 248, 22, 8); //Solid magenta
    setColor(0xff00ff);
    fill();
    }

    void drawRamp() {
        int r,g,b;
        for (int x=0; x<w; x++) {
             for (double y=0; y<(h*16); y++) {
                r = g = b = (byte)y;
                pixels[(int)y*width+x] = 0xff000000 | ((r<<16)&0xff0000) | ((g<<8)&0xff00) | (b&0xff);
            }
        }
    }

    void resetBW() {   //Paints the Color Reset Button
        setColor(0x000000);
        drawRect(92, 300, 9, 7);
        setColor(0x000000);
        setRoi(88, 297, 9, 7);
        fill();
    }

    void flipper() {   //Paints the Flipper Button
        int xa = 90; 
        int ya = 272; 
        setColor(0x000000);
        drawLine(xa, ya, xa+9, ya+9);//Main Body
        drawLine(xa+1, ya, xa+9, ya+8);
        drawLine(xa, ya+1, xa+8, ya+9);
        drawLine(xa, ya, xa, ya+5);//Upper Arrow
        drawLine(xa+1, ya+1, xa+1, ya+6);
        drawLine(xa, ya, xa+5, ya);
        drawLine(xa+1, ya+1, xa+6, ya+1);
        drawLine(xa+9, ya+9, xa+9, ya+4);//Lower Arrow
        drawLine(xa+8, ya+8, xa+8, ya+3);
        drawLine(xa+9, ya+9, xa+4, ya+9);
        drawLine(xa+8, ya+8, xa+3, ya+8);
    }
}

class ColorCanvas extends ImageCanvas {

    Vector colors;
    boolean background = false;
    long mouseDownTime;

    public ColorCanvas(IjxImagePlus imp) {
        super(imp);
    }

    public void mousePressed(MouseEvent e) {
        //super.mousePressed(e);
        ImageProcessor ip = imp.getProcessor();
        ip.setLineWidth(1);
        if (Toolbar.getToolId()==Toolbar.DROPPER)
			IJ.setTool(Toolbar.RECTANGLE );
        
        Rectangle flipperRect = new Rectangle(86, 268, 18, 18);
        Rectangle resetRect = new Rectangle(86, 294, 18, 18);
        Rectangle foreground1Rect = new Rectangle(9, 266, 45, 10);
        Rectangle foreground2Rect = new Rectangle(9, 276, 23, 25);
        Rectangle background1Rect = new Rectangle(33, 302, 45, 10);
        Rectangle background2Rect = new Rectangle(56, 277, 23, 25);
        int x = offScreenX(e.getX());
        int y = offScreenY(e.getY());
        long difference = System.currentTimeMillis()-mouseDownTime;
        boolean doubleClick = (difference<=250);
        mouseDownTime = System.currentTimeMillis();
        if (flipperRect.contains(x, y)) {
            Color c = Toolbar.getBackgroundColor();
            Toolbar.setBackgroundColor(Toolbar.getForegroundColor());
            Toolbar.setForegroundColor(c);
        } else if(resetRect.contains(x,y)) {
            Toolbar.setForegroundColor(new Color(0x000000));
            Toolbar.setBackgroundColor(new Color(0xffffff)); 
        } else if ((background1Rect.contains(x,y)) || (background2Rect.contains(x,y))) {
            background = true;
            if (doubleClick) editColor();
             ((ColorGenerator)ip).refreshForeground();
            ((ColorGenerator)ip).refreshBackground();
        } else if ((foreground1Rect.contains(x,y)) || (foreground2Rect.contains(x,y))) {
            background = false;
            if (doubleClick) editColor();
            ((ColorGenerator)ip).refreshBackground();
            ((ColorGenerator)ip).refreshForeground();
        } else {
            //IJ.log(" " + difference + " " + doubleClick);
            if (doubleClick)
                editColor();
            else {
               setDrawingColor(offScreenX(e.getX()), offScreenY(e.getY()), background); 
            }
        }
        if (ip instanceof ColorGenerator) {
            if (background){
                ((ColorGenerator)ip).refreshForeground();
                ((ColorGenerator)ip).refreshBackground();
            } else {
                ((ColorGenerator)ip).refreshBackground();
                ((ColorGenerator)ip).refreshForeground();
            }
        } 
    }

    void editColor() {
        Color c  = background?Toolbar.getBackgroundColor():Toolbar.getForegroundColor();
        ColorChooser cc = new ColorChooser((background?"Background":"Foreground")+" Color", c, false);
        c = cc.getColor();
        if (background)
            Toolbar.setBackgroundColor(c);
        else
            Toolbar.setForegroundColor(c);
     }

    public void refreshColors() {
        ImageProcessor ip = imp.getProcessor();
        ((ColorGenerator)ip).refreshBackground();
        ((ColorGenerator)ip).refreshForeground();
    }
}

