import ij.*;
import ij.gui.ColorChooser;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.*;
import ij.process.BinaryProcessor;
import ij.process.ByteProcessor;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import javax.swing.JOptionPane;
/**
 *Draws simple quadric and Koch fractal contours and a 
 * Sierpinski gasket type fractal in ImageJ.
 * The theoretical Df for the Koch Line=(ln4/ln3)=1.26;
 * for the Quadric Flake = (ln3/ln(5^(0.5))=1.37,                        
 * Quadric Cross (ln3.33/ln(5^(0.5))=1.49,
 * Koch Flake (ln4/ln3)=1.26, 
 * 8-Segment Quadric Fractal (ln8/ln4)=1.5,
 * 18-Segment Quadric Fractal (ln18/ln6)=1.61",
 * 32-Segment Quadric Fractal (ln32/ln8)=1.67",
 * 50-Segment Quadric Fractal (ln50/ln10)=1.70};
 * @author Audrey Karperien, Charles Sturt University
 */
public class Fractal_Generator implements PlugIn {
float D=500;
int Iterations=3;
int type=3;
public float [][] FatFractalOrigins=
{{0, 100, 200, 0, 200}, {0, 100, 0, 200, 200}};
public static float [][] DefaultFatFractalRules=
{{0f, 1f/3f, 2f/3f, 2f/3f, 0f },
{0f, 1f/3f, 2f/3f, 0f, 2f/3f}
};
public float [][] FatFractalRules=
{{0f, 1f/3f, 2f/3f, 2f/3f, 0f },
{0f, 1f/3f, 2f/3f, 0f, 2f/3f}
};
static  String s = "s", r="r", l="l";
double mag;
public float bigx,  bigy, smallx, smally;
public int i =0;
//Specifies number of breaks in the generator segment
//(3 breaks for 4 pieces).
int cutsperpiece = 3;    
public String [] outlines = new String[]{
"Plain Outline?", "Funky Outline", "No Outline"};
public String [] PlainOrFunky = new String[]{
"Plain Outline?", "Funky Outline"};
public int imageSize=600;

public boolean fill=false, funky=false,grad=false, outline=false;
String title="Title Not Set";
int border=100;
public float Stroke=1;
//Default of 30; offsets position nearest left side.FIXME legacy
float length = 1;        
public int FracSize=0;

public String [] fills = new String[]{
"Solid", "Gradient", "No fill"};
public static int 
NO_FILL=2, 
GRADIENT=1, 
SOLID_FILL=0, 
PLAIN_OUTLINE=0,
FUNKY_OUTLINE=1,
NO_OUTLINE=2,
KOCH_LINE=0,
FAT_FRACTAL=1,
QUADRIC_FLAKE=2,
QUADRIC_CROSS=3,
KOCH_FLAKE=4,     
QUADRIC_8=5,
QUADRIC_18=6,
QUADRIC_32=7,
QUADRIC_50=8;
;
public Color gradcolour=Color.MAGENTA;
public String [] choices = new String[]{
"Koch Line (ln4/ln3)=1.26", 
"Fat Fractal",
"Quadric Flake (ln3/ln(5^(0.5))=1.37",                        
"Quadric Cross (ln3.33/ln(5^(0.5))=1.49",
"Koch Flake (ln4/ln3)=1.26", 
"8-Segment Quadric Fractal (ln8/ln4)=1.50",
"18-Segment Quadric Fractal (ln18/ln6)=1.61",
"32-Segment Quadric Fractal (ln32/ln8)=1.67",
"50-Segment Quadric Fractal (ln50/ln10)=1.70"};
    /** Creates a new instance of NewClass */
    public Fractal_Generator() 
    {        
        if(!GetOptions())return; 
    }   
    
    public void runKoch(String s)
    {            
            BufferedImage B =
                    new BufferedImage(imageSize, imageSize,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = (Graphics2D)B.getGraphics();
            ImagePlus img = null;
            DoKoch(g2);
            new ImagePlus(title, B).show("done");
            
    }
    void setTitle()
    {
        String s = "Not Set";
        switch(type)
        {
            case(0):{s="Koch Line";break;}
            case(1):{s="Fat Fractal";}
            case(2):{s="Quadric Flake"; break;}
            case(3):{s="Quadric Cross"; break;}
            case(4):{s="Koch Snowflake"; break;}
            case(5):{s="8 Segment Quadric"; break;}
            case(6):{s="18 Segment Quadric"; break;}
            case(7):{s="32 Segment Quadric"; break;}
            case(8):{s="50 Segment Quadric"; break;}
        }
        title=s+": "+(int)Iterations+"X; "+D+" pix";
                        
    }
    public boolean GetOptions()
    {                   
        GenericDialog fd = new GenericDialog("Type of Fractal");            
        fd.addChoice("Type of Fractal", choices, choices[QUADRIC_CROSS]);
        fd.showDialog();
        if(fd.wasCanceled())return false;
        type = (int)fd.getNextChoiceIndex();            
        if(type==IJ.CANCELED)return false;
        else
        {//if not cancelled  
                if (type==FAT_FRACTAL) 
                {
                    DrawIt();
                    return true;
                }                               
                Iterations = (int)IJ.getNumber("Iterations", Iterations);
                if(Iterations==IJ.CANCELED)return false;
                else//if not cancelled
                {
                    setDistance();
                    D =  (float)IJ.getNumber("Distance Between Points", D);
                    if (D==IJ.CANCELED)return false; 
                    if(type==QUADRIC_FLAKE||
                            type==QUADRIC_CROSS||
                            type==QUADRIC_8||
                            type==QUADRIC_18||
                            type==QUADRIC_32||
                            type==QUADRIC_50)
                    {
                        mag=type==QUADRIC_CROSS?3:
                            type==QUADRIC_8?4:
                                type==QUADRIC_18?6:
                                    type==QUADRIC_50?10:
                                        8;
                        if(type==QUADRIC_FLAKE)mag=Math.sqrt(5.0f);
                        setTitle();
                        Iterate();
                    }//if it is not quadric, go on
                    else
                    {                                              
                        if(type==KOCH_LINE||type==KOCH_FLAKE)
                        {
                            GenericDialog gd = new GenericDialog("Get Options");
                            if(type==KOCH_FLAKE)
                            {
                                gd.addChoice("Fill", fills, fills[NO_FILL]);
                                gd.addChoice("Outline", outlines, 
                                        outlines[PLAIN_OUTLINE]);
                            } 
                            else gd.addChoice("Outline", PlainOrFunky, 
                                    PlainOrFunky[PLAIN_OUTLINE]);                                            
                            gd.addNumericField("Stroke width?", Stroke, 2);
                            
                            gd.showDialog();
                            if(gd.wasCanceled())return false;
                            
                            outline=false; 
                            funky=false;
                            fill=false; 
                            grad=false;
                            if(type==KOCH_FLAKE)
                            {//set up for a flake
                                int f = gd.getNextChoiceIndex();
                                if(f==SOLID_FILL)fill=true;
                                else if(f==GRADIENT)grad=true;
                                int out = gd.getNextChoiceIndex();
                                if(out==PLAIN_OUTLINE)outline=true;
                                else if(out==FUNKY_OUTLINE)funky=true;
                                if(!fill&&!grad&&!outline&&!funky)
                                {
                                    IJ.showMessage("No drawing selected"); 
                                    return false;
                                }
                             }               
                            else
                            {//or set up for a koch line
                                int out=gd.getNextChoiceIndex();
                                if (out==0)outline=true;
                                else funky=true;
                            }
                            
                            Stroke=(float)gd.getNextNumber();
                            title="Koch-"+Iterations+"X: "+D+"-pix";
                            imageSize=(int)(D+(border*2)+(D/10));
                            
                            if(grad) 
                            {
                                ColorChooser cc=
                                        new ij.gui.ColorChooser("Select the " +
                                        "end colour for this gradient",
                                     ij.gui.Toolbar.getForegroundColor(), true);
                                gradcolour=cc.getColor();

                            }//once the user options 
                            //are set, do a koch line or flake
                      setTitle();
                      runKoch("go");
                      }
//end if type is 0 or 4 koch           
                    }   
        }//endif not cancelled
        }//end if cancelled
    return true;               
    }
    
    public static float scaleForCross = (float)Math.sqrt(5.0f);                 
    public static float scaleFor2Seg=(float)Math.sqrt(5.0f);
    public static float scaleForKoch=3f;    
    public static float scaleFor8Seg=4f; 
    public static float scaleFor18Seg=6f;
    public static float scaleFor32Seg=8f;
    public static float scaleFor50Seg=10f; 
    public static float scaleforFAT=1f/3f;
    
    public int distance=100;
    void setDistance()  
    {
        float scale=scaleForKoch;       
        if(type==QUADRIC_FLAKE)scale =scaleFor2Seg;
        if(type==QUADRIC_CROSS)scale=scaleForCross;
        if(type==QUADRIC_8)scale=scaleFor8Seg;
        if (type==QUADRIC_18)scale = scaleFor18Seg;
        if(type==QUADRIC_32)scale=scaleFor32Seg;
        if(type==QUADRIC_50)scale=scaleFor50Seg;
        D=(float)Math.pow(scale, Iterations);
        if(type==QUADRIC_CROSS) D=(float)Math.pow(scale, Iterations+1);
    }
    void DrawIt()
    {
        double x=20;
        float [][]XYSize=GetNewOriginsAndRulesForFatFractal();
        int w=(int)(D+x+x);
        BufferedImage B
                = new BufferedImage(w, w, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2 = (Graphics2D)B.getGraphics();
        DrawFat(g2, x, x, XYSize);
        
        new ImagePlus
                (title, new BinaryProcessor(new ByteProcessor(B))).show("done");
        
        
    }
    
    
    
    /**
     * Main method in case user wants to double click. Starts ImageJ.
     * @param args String array
     */
    public static void main(String args[]) {
        String  s = "txt";
        new ImageJ();
        new Fractal_Generator();
        
    }
       
    /**
     * Draws a set of x,y points for making a quadric fractal.
     * 
     * 
     * @param Iterations int for the number of Iterations to use
     * @param DistanceBetweenPoints float for the distance
     * between points in the starting figure
     * @param type specifies the type of fractal to draw
     */
    public   void Iterate() 
    {
        if(type==QUADRIC_18)Iterate18by4();
        if(type==QUADRIC_FLAKE)Iterate2by4();
        if(type==QUADRIC_CROSS)Iterate3by4();
        if(type==QUADRIC_8)Iterate8by4();
        if(type==QUADRIC_32)Iterate32by4();
        if(type==QUADRIC_50)Iterate50by4();
        
    }
    
    
    public  void Iterate3by4() 
    {
      
        double q=D, off=D;
        int b = 0;
        for (b = 0; b < Iterations; b++) 
        {
            q=q+(D+((D/Math.pow(scaleForCross, b))));
            off=off+(b+1)*D/Math.pow(scaleForCross, b);
            if (b>3)off=off+3*(b+1)*D/Math.pow(scaleForCross, b);
            if  (b>5)off=off+5*(b+1)*D/Math.pow(scaleForCross, b);
        }
        int w=(int)(q+scaleForCross*D);
        int h=w;
        BufferedImage B
                = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2 = (Graphics2D)B.getGraphics();   
        
        if (ij.gui.Toolbar.getForegroundColor()==Color.black)
        {
            g2.setColor(Color.black);
            g2.setBackground(Color.white);
        }
        else 
        {
            g2.setColor(Color.white);
            g2.setBackground(Color.black);
        }
        g2.clearRect(0,0, w, h);
        float As[]={-90, 0, 90, 180};
        float Xs[]={0, D, D, 0 };
        float Ys[]={0, 0, D, D };
        float x=0, y=0;
        int firstx=0, firsty=0;
        for (int A=0; A<4; A++) 
        {//once for each side of the figure
            x=Xs[A];//get the starting x
            y=Ys[A];//get the starting y
            float [][] f = Iterate3(x,y,As[A]);//make the side
            int [][]F= new int[2][ f[0].length];//add an offset
            for (int i = 0; i < f[0].length; i++) 
            {
                F[0][i]=(int)f[0][i]+(int)(off);
                F[1][i]=(int)f[1][i]+(int)((off));
            }
            //draw this side of the figure
            g2.drawPolyline(F[0], F[1], F[0].length);
        }
        new ImagePlus
                (title, new BinaryProcessor(new ByteProcessor(B))).show("done");
        
    }
    
    
    public  void Iterate50by4() 
    {
         
        int w=(int)(D*mag);
        w=w+2*(int)D;
        int h = (int)(D*mag);
//        if(type==QUADRIC_50) 
//        {
//            w=w+2*(int)D;
//            h=w;
//        }
        w=w*2;
        h=h*2;
        BufferedImage B
                = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2 = (Graphics2D)B.getGraphics();
         if (ij.gui.Toolbar.getForegroundColor()==Color.black)
        {
            g2.setColor(Color.black);
            g2.setBackground(Color.white);
        }
        else 
        {
            g2.setColor(Color.white);
            g2.setBackground(Color.black);
        }
        g2.clearRect(0,0, w, h);
        float a1=0, a2=90, a3=180, a4=-90;
        float As[]={0, 90, 180, -90};
        float x=0, y=0;
        for (int A=0; A<4; A++) 
        {
            float [][] f = Iterate50(x,y,As[A]);
            int [][]F= new int[2][ f[0].length];
            for (int i = 0; i < f[0].length; i++) 
            {
                F[0][i]=(int)f[0][i]+(int)(7*D);
                F[1][i]=(int)f[1][i]+(int)((5*D));
            }
            g2.drawPolyline(F[0], F[1], F[0].length);
            int end = f[0].length-1;
            x=f[0][end];
            y=f[1][end];
        }
      new ImagePlus
                (title, new BinaryProcessor(new ByteProcessor(B))).show("done");
        
        
    }
    
    public  void Iterate2by4() 
    {
        
        int w=(int)(D*mag);
        w=w+2*(int)D;
        int h = (int)(D*mag);
        w=w*2;
        h=w;
        BufferedImage B
                = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2 = (Graphics2D)B.getGraphics();
         if (ij.gui.Toolbar.getForegroundColor()==Color.black)
        {
            g2.setColor(Color.black);
            g2.setBackground(Color.white);
        }
        else 
        {
            g2.setColor(Color.white);
            g2.setBackground(Color.black);
        }
        g2.clearRect(0,0, w, h);
        float As[]={0, -90, 180, 90};
        
        float x=0;//
        float y=0;
        int xoff=(int)(4*D);
        int yoff=(int)(4*D);
        
        for (int A=0; A<4; A++) 
        {
            float [][] f = Iterate2(x,y,As[A]);
            int [][]F= new int[2][ f[0].length];
            for (int i = 0; i < f[0].length; i++) 
            {
                F[0][i]=(int)f[0][i]+xoff;//(int)(4*D);
                F[1][i]=(int)f[1][i]+yoff;//(int)((4*D));
            }
            
            g2.drawPolyline(F[0], F[1], F[0].length);
            int end = f[0].length-1;
            x=f[0][end];
            y=f[1][end];
        }
       new ImagePlus
                (title, new BinaryProcessor(new ByteProcessor(B))).show("done");
        
           
        
    }
     
      

    public  void Iterate32by4() 
    {
        
        int w=(int)(D*mag);
        w=w+2*(int)D;
        int h = (int)(D*mag);
        if(type==QUADRIC_32)
        {
            w=w+2*(int)D;h=w;
        }
        w=w*2;
        h=h*2;
        BufferedImage B
                = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2 = (Graphics2D)B.getGraphics();
         if (ij.gui.Toolbar.getForegroundColor()==Color.black)
        {
            g2.setColor(Color.black);
            g2.setBackground(Color.white);
        }
        else 
        {
            g2.setColor(Color.white);
            g2.setBackground(Color.black);
        }
        g2.clearRect(0,0, w, h);
        float a1=0, a2=90, a3=180, a4=-90;
        float As[]={0, 90, 180, -90};
        float x=0, y=0;
        for (int A=0; A<4; A++) 
        {
            float [][] f = oldIterate32(x, y, As[A]);
            int [][]F= new int[2][ f[0].length];
            for (int i = 0; i < f[0].length; i++) 
            {
                F[0][i]=(int)f[0][i]+(int)((mag*2)*D);
                F[1][i]=(int)f[1][i]+(int)((mag*D));
            }
            g2.drawPolyline(F[0], F[1], F[0].length);
            int end = f[0].length-1;
            x=f[0][end];
            y=f[1][end];
        }
        
        new ImagePlus
                (title, new BinaryProcessor(new ByteProcessor(B))).show("done");
        
        
    }
    
    
    public  void Iterate18by4()
    {
        
        int w=(int)(D*mag);
        w=w+2*(int)D;
        int h = (int)(D*mag);
        if(type==QUADRIC_32) 
        {
            w=w+2*(int)D;h=w;
        }
        w=w*3;
        h=h*3;
        BufferedImage B
                = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2 = (Graphics2D)B.getGraphics();
         if (ij.gui.Toolbar.getForegroundColor()==Color.black)
        {
            g2.setColor(Color.black);
            g2.setBackground(Color.white);
        }
        else 
        {
            g2.setColor(Color.white);
            g2.setBackground(Color.black);
        }
        g2.clearRect(0,0, w, h);
        float a1=0, a2=90, a3=180, a4=-90;
        float As[]={0, 90, 180, -90};
        float x=0, y=0;
        for (int A=0; A<4; A++) 
        {
            float [][] f = Iterate18(x,y,As[A]);
            int [][]F= new int[2][ f[0].length];
            for (int i = 0; i < f[0].length; i++) 
            {
                F[0][i]=(int)f[0][i]+(int)(mag*D);
                F[1][i]=(int)f[1][i]+(int)((mag*D)/2);
            }
            g2.drawPolyline(F[0], F[1], F[0].length);
            int end = f[0].length-1;
            x=f[0][end];
            y=f[1][end];
        }       
     new ImagePlus
                (title, new BinaryProcessor(new ByteProcessor(B))).show("done");
            
    }
    
    
    public  void Iterate8by4()
    {
        int w=(int)(D*mag);
        w=w+2*(int)D;
        int h = (int)(D*mag);
        if(type==QUADRIC_32) 
        {
            w=w+2*(int)D;h=w;
        }
        w=w*2;
        h=h*2;
        BufferedImage B
                = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2 = (Graphics2D)B.getGraphics();
         if (ij.gui.Toolbar.getForegroundColor()==Color.black)
        {
            g2.setColor(Color.black);
            g2.setBackground(Color.white);
        }
        else 
        {
            g2.setColor(Color.white);
            g2.setBackground(Color.black);
        }
        g2.clearRect(0,0, w, h);
        float a1=0, a2=90, a3=180, a4=-90;
        float As[]={0, 90, 180, -90};
        float x=0, y=0;
        for (int A=0; A<4; A++) 
        {
            float [][] f = Iterate8(x,y,As[A]);
            int [][]F= new int[2][ f[0].length];
            for (int i = 0; i < f[0].length; i++) 
            {
                F[0][i]=(int)f[0][i]+(int)(mag*D);
                F[1][i]=(int)f[1][i]+(int)((mag*D)/2);
            }
            g2.drawPolyline(F[0], F[1], F[0].length);
            int end = f[0].length-1;
            x=f[0][end];
            y=f[1][end];
        }        
        
        new ImagePlus
                (title, new BinaryProcessor(new ByteProcessor(B))).show("done");
        
    }
    
    
    public float [][] Iterate18() {
        return Iterate18(0,0,0);
    }
    
    
    public float[][] Iterate18(float seedx, 
            float seedy, float seedangle) 
    {
        
        float[][]  f;
        int count=0;
        
        int n =1+ (int)Math.pow(18, Iterations+1);
        
        float [][] GrowingArray = new float [3][n];
        
        f = MakeStartingArray18(D,seedx, seedy, seedangle);
        if(Iterations<=1)return f;
        
        for (int i = 0;  i < f[0].length; i++) 
        {
            GrowingArray[0][i]=f[0][i];
            GrowingArray[1][i]=f[1][i];
            GrowingArray[2][i]=f[2][i];
        }
        
        float Length=D;
        count=0;
        
        for (int Q=0; Q<Iterations-1; Q++) 
        {
            count=0; Length=Length/scaleFor18Seg;
            
            for (int i = 0; i<f[0].length-1;i++) 
            {
                float [][] T=MakeStartingArray18( Length,
                        f[0][i], f[1][i], f[2][i]);
                
                for(int j =0; j < T[0].length-1; j++) 
                {
                    GrowingArray[0][count]=  T[0][j];
                    GrowingArray[1][count]=T[1][j];
                    GrowingArray[2][count]=T[2][j];
                    count++;
                }
                //for the last one, record the alst point
                if(i==f[0].length-2) 
                {
                    GrowingArray[0][count]=T[0][T[0].length-1];
                    GrowingArray[1][count]=T[1][T[1].length-1];
                    GrowingArray[2][count]=T[2][T[2].length-1];
                    count++;
                };
            }
            f=new float[3][count];
            for (int z=0; z < count; z++) 
            {
                f[0][z]=GrowingArray[0][z];
                f[1][z]=GrowingArray[1][z];
                f[2][z]=GrowingArray[2][z];
            }
        }        
        System.gc();
        return f;       
    }
    
    /**
     *
     * @param ITERS
     * @param DistanceBetweenPoints
     * @param seedx
     * @param seedy
     * @param seedangle
     * @return
     */
    public float[][] Iterate2(float seedx,
            float seedy, float seedangle) 
    {
        
        float[][]  f;
        int count=0;
        
        int n =1+ (int)Math.pow(3, Iterations+1);
        
        float [][] GrowingArray = new float [3][n];
        
        f = MakeStartingArray2(D, seedx, seedy, seedangle);
        if(Iterations<=1)return f;
        //make the starting array
        //this stores each point and the orientation from segment to segment
        //the orientation stored along with an x,y pair is
        for (int i = 0;  i < f[0].length; i++) {
            GrowingArray[0][i]=f[0][i];
            GrowingArray[1][i]=f[1][i];
            GrowingArray[2][i]=f[2][i];}
        
        float Length=D;
        count=1;
        
        float TheLastXDrawn=f[0][0];
        float TheLastYDrawn=f[1][0];
        float FirstX=f[0][0];
        float FirstY=f[1][0];
        for (int Q=0; Q<Iterations-1; Q++) 
        {//go through once for each iteration
            count=0; 
            Length=Length/scaleFor2Seg;
            TheLastXDrawn=FirstX;
            TheLastYDrawn=FirstY;
            //scaleFor2Seg the length at each iteratino
            for (int i = 0; i<f[0].length-1;i++) 
            {//for each segment in the previous array
                //make a new 3-part segment
                //the direction depends on the 
                //orientation of the previous segment
                float angleOfThisSegment=f[2][i];
                float x = TheLastXDrawn;
                float y = TheLastYDrawn;
                float [][] T=MakeStartingArray2( Length,
                        x, y, angleOfThisSegment);
                
                for(int j =0; j < T[0].length-1; j++) 
                {
                    GrowingArray[0][count]=  T[0][j];
                    GrowingArray[1][count]=T[1][j];
                    GrowingArray[2][count]=T[2][j];
                    count++;
                }
                TheLastXDrawn=T[0][T[0].length-1];
                TheLastYDrawn=T[1][T[0].length-1];
                //for the last one, record the alst point
                if(i==f[0].length-2) 
                {
                    GrowingArray[0][count]=T[0][T[0].length-1];
                    GrowingArray[1][count]=T[1][T[1].length-1];
                    GrowingArray[2][count]=T[2][T[2].length-1];
                    count++;
                };
            }
            f=new float[3][count];
            for (int z=0; z < count; z++)
            {
                f[0][z]=GrowingArray[0][z];
                f[1][z]=GrowingArray[1][z];
                f[2][z]=GrowingArray[2][z];
            }           
        }        
        System.gc();
        return f;       
    }
    
    //public float [][]times4(){}
    public float [][] Iterate50()
    {
        return Iterate50(0,0,0);
    }
    public float[][] Iterate50(float seedx,
            float seedy, float seedangle) 
    {
        
        float[][]  f;
        int count=0;
        
        int n =1+ (int)Math.pow(50, Iterations+1);
        
        float [][] GrowingArray = new float [3][n];
        
        f = MakeStartingArray50(D, seedx, seedy,  seedangle);
        if(Iterations<=1)return f;
        
        for (int i = 0;  i < f[0].length; i++)
        {
            GrowingArray[0][i]=f[0][i];
            GrowingArray[1][i]=f[1][i];
            GrowingArray[2][i]=f[2][i];}
        
        float Length=D;
        count=0;
        
        for (int Q=0; Q<Iterations-1; Q++) 
        {
            count=0; Length=Length/scaleFor50Seg;
            
            for (int i = 0; i<f[0].length-1;i++)
            {
                float [][] T=MakeStartingArray50( Length,
                        f[0][i], f[1][i], f[2][i]);
                
                for(int j =0; j < T[0].length-1; j++)
                {
                    GrowingArray[0][count]=  T[0][j];
                    GrowingArray[1][count]=T[1][j];
                    GrowingArray[2][count]=T[2][j];
                    count++;
                }
                //for the last one, record the alst point
                if(i==f[0].length-2) 
                {
                    GrowingArray[0][count]=T[0][T[0].length-1];
                    GrowingArray[1][count]=T[1][T[1].length-1];
                    GrowingArray[2][count]=T[2][T[2].length-1];
                    count++;
                };
            }
            f=new float[3][count];
            for (int z=0; z < count; z++) 
            {
                f[0][z]=GrowingArray[0][z];
                f[1][z]=GrowingArray[1][z];
                f[2][z]=GrowingArray[2][z];
            }            
            
        }
        
        System.gc();
        return f;
        
        
    }
    
    
    public float [][] Iterate8() {
        return Iterate8(0,0,0);
    }
    
    
    public float[][] Iterate8(float seedx, float seedy, 
            float seedangle) 
    {
        
        float[][]  f;
        int count=0;
        
        int n =1+ (int)Math.pow(8, Iterations+1);
        
        float [][] GrowingArray = new float [3][n];
        
        f = MakeStartingArray8(D, seedx, seedy, seedangle);
        if(Iterations<=1)return f;
        
        for (int i = 0;  i < f[0].length; i++) 
        {
            GrowingArray[0][i]=f[0][i];
            GrowingArray[1][i]=f[1][i];
            GrowingArray[2][i]=f[2][i];
        }
        
        float Length=D;
        count=0;
        
        for (int Q=0; Q<Iterations-1; Q++)
        {
            count=0; 
            Length=Length/scaleFor8Seg;
            
            for (int i = 0; i<f[0].length-1;i++)
            {
                float [][] T=MakeStartingArray8( Length,
                        f[0][i], f[1][i], f[2][i]);
                
                for(int j =0; j < T[0].length-1; j++)
                {
                    GrowingArray[0][count]=  T[0][j];
                    GrowingArray[1][count]=T[1][j];
                    GrowingArray[2][count]=T[2][j];
                    count++;
                }
                //for the last one, record the alst point
                if(i==f[0].length-2) 
                {
                    GrowingArray[0][count]=T[0][T[0].length-1];
                    GrowingArray[1][count]=T[1][T[1].length-1];
                    GrowingArray[2][count]=T[2][T[2].length-1];
                    count++;
                };
            }
            f=new float[3][count];
            for (int z=0; z < count; z++) 
            {
                f[0][z]=GrowingArray[0][z];
                f[1][z]=GrowingArray[1][z];
                f[2][z]=GrowingArray[2][z];
            }
            
            
        }
        
        System.gc();
        return f;
        
        
    }
    
    
    
    
    
    
float [] LengthenArray(float old [], int addedLength)
{
    //copy the old array's values into a longer array
    float [] NewArray= new float[old.length+addedLength];
    int i =0;
    for (i = 0; i <old.length ; i++)
    {
        NewArray[i]=old[i];
    }
    return NewArray;
    
}


public void reeIterate32() 
    {        
        
    int w=(int)(D*mag);
        w=w+2*(int)D;
        int h = (int)(D*mag);
        if(type==QUADRIC_32)
        {
            w=w+2*(int)D;h=w;
        }
        w=w*2;
        h=h*2;
        BufferedImage B
                = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2 = (Graphics2D)B.getGraphics();
        //float a1=0, a2=90, a3=180, a4=-90;
        float As[]={0, 90, 180, -90};
        float x=0, y=0;
        for (int A=0; A<4; A++) 
        {

   ////////////////////////////////////// 
    float[][]  FF, f;
        int count=0;      
        float [][] GrowingArray = new float [3][]; 
        f=new float[3][];
        FF = MakeStartingArray32(D, x, y, As[A]);
        f[0]=new float[FF[0].length];
         f[1]=new float[FF[1].length];
          f[2]=new float[FF[2].length];
        for (int py=0; py < FF[0].length; py++)
        {
            f[0][py]=FF[0][py];
            f[1][py]=FF[1][py];
            f[2][py]=FF[2][py];
        }
        //if(Iterations<=1) return f;
        //Do 1 segment at a time
        GrowingArray[0]=new float[f[0].length];        
        GrowingArray[1]=new float[f[1].length];        
        GrowingArray[2]=new float[f[2].length];
        
        for (int i = 0;  i < f[0].length; i++)
        {//store points from the iterator
            GrowingArray[0][i]=f[0][i];
            GrowingArray[1][i]=f[1][i];
            GrowingArray[2][i]=f[2][i];
        }
        
        float Length=D;
        count=0;
        for (int i=0; i < FF[0].length; i++)
        {
            Length=D/scaleFor32Seg;
            float [][] T=MakeStartingArray32( Length,
                        FF[0][i], FF[1][i], FF[2][i]== -90?180:FF[2][i]-90);
        for (int Q=0; Q<Iterations-1; Q++)
        {
            count=0; 
            Length=Length/scaleFor32Seg;
            
            for (int ik = 0; ik<f[0].length-1;ik++)
            {//make an iterator at each pt in f, the basic iterator
                
                float [][] TT=MakeStartingArray32( Length,
                        f[0][ik], f[1][ik], f[2][ik]== -90?180:f[2][ik]-90);
                //store the new segment 
                GrowingArray[0]=LengthenArray(GrowingArray[0], TT[0].length);
                GrowingArray[1]=LengthenArray(GrowingArray[1], TT[1].length);
                GrowingArray[2]=LengthenArray(GrowingArray[2], TT[2].length);
                for(int j =0; j < TT[0].length-1; j++)
                {
                    GrowingArray[0][count]=TT[0][j];
                    GrowingArray[1][count]=TT[1][j];
                    GrowingArray[2][count]=TT[2][j];
                    count++;
                }
                //for the last one, record the alst point
                if(ik==f[0].length-2) {
                    GrowingArray[0][count]=TT[0][TT[0].length-1];
                    GrowingArray[1][count]=TT[1][TT[1].length-1];
                    GrowingArray[2][count]=TT[2][TT[2].length-1];
                    count++;
                };
            }//before starting the next iteration, store the points
            f=new float[3][count];
            for (int z=0; z < count; z++)
            {
                f[0][z]=GrowingArray[0][z];
                f[1][z]=GrowingArray[1][z];
                f[2][z]=GrowingArray[2][z];
            } 
        }
        
            int [][]F= new int[2][ f[0].length];
            for (int j = 0; j < f[0].length; j++) 
            {
                F[0][j]=(int)f[0][j]+(int)((mag*2)*D);
                F[1][j]=(int)f[1][j]+(int)((mag*D));
            }
            g2.drawPolyline(F[0], F[1], F[0].length);
            int end = f[0].length-1;
            x=f[0][end];
            y=f[1][end];
        }
        
        
        
        
        }
        
        //ImagePlus T =
                new ImagePlus("new"+title, B).show("done");
        
        //ImageWindow iww = new ImageWindow(new ImagePlus(title, B));
        //iww.setVisible(true);
        //change this to draw f
    }


    public float[][] oldIterate32( 
            float seedx, float seedy, float seedangle) 
    {        
        float[][]  f;
        int count=0;      
        float [][] GrowingArray = new float [3][];        
        f = MakeStartingArray32(D, seedx, seedy, seedangle);
        if(Iterations<=1)return f;        
        GrowingArray[0]=new float[f[0].length];        
        GrowingArray[1]=new float[f[1].length];        
        GrowingArray[2]=new float[f[2].length];
        
        for (int i = 0;  i < f[0].length; i++)
        {//store points from the iterator
            GrowingArray[0][i]=f[0][i];
            GrowingArray[1][i]=f[1][i];
            GrowingArray[2][i]=f[2][i];
        }
        
        float Length=D;
        count=0;
        
        for (int Q=0; Q<Iterations-1; Q++)
        {
            count=0; 
            Length=Length/scaleFor32Seg;
            
            for (int i = 0; i<f[0].length-1;i++)
            {//make an iterator at each pt in f, the basic iterator
                
                float [][] T=MakeStartingArray32( Length,
                        f[0][i], f[1][i], f[2][i]== -90?180:f[2][i]-90);
                //store the new segment 
                GrowingArray[0]=LengthenArray(GrowingArray[0], T[0].length);
                GrowingArray[1]=LengthenArray(GrowingArray[1], T[1].length);
                GrowingArray[2]=LengthenArray(GrowingArray[2], T[2].length);
                for(int j =0; j < T[0].length-1; j++)
                {
                    GrowingArray[0][count]=T[0][j];
                    GrowingArray[1][count]=T[1][j];
                    GrowingArray[2][count]=T[2][j];
                    count++;
                }
                //for the last one, record the alst point
                if(i==f[0].length-2) {
                    GrowingArray[0][count]=T[0][T[0].length-1];
                    GrowingArray[1][count]=T[1][T[1].length-1];
                    GrowingArray[2][count]=T[2][T[2].length-1];
                    count++;
                };
            }//before starting the next iteration, store the points
            f=new float[3][count];
            for (int z=0; z < count; z++)
            {
                f[0][z]=GrowingArray[0][z];
                f[1][z]=GrowingArray[1][z];
                f[2][z]=GrowingArray[2][z];
            } 
        }
        System.gc();
        return f;
    } 
    
    public  float [][] Iterate3(int ITERS, float DistanceBetweenPoints) {
        return Iterate3(0,0,-90);
    }
    
    /**Makes a Quadric Cross, for which the 
     *number of new part is 3.33 and the scale is sqrt5,
     *so the df=log 3.33/log sqrt 5=  1.48*/
    
    public  float[][] Iterate3(
            float seedx, float seedy, float seedangle)
    {   
        int count=0;
        int n =1+ (int)Math.pow(6, Iterations+1);
        float [][] GrowingArray = new float [3][n];
        float[][]  f = MakeStartingArray3(D,
                seedangle, seedx, seedy);
        if(Iterations<2)return f;
        else {
            float DistanceBetweenPoints=D;
            float DistanceToFirstPoint=D;
            float LastLength=DistanceBetweenPoints;
            DistanceToFirstPoint = (float)
            (DistanceBetweenPoints-(DistanceBetweenPoints/scaleForCross))/2.0f;
            DistanceBetweenPoints = DistanceBetweenPoints/scaleForCross;
            float d =DistanceToFirstPoint ;
            float l = DistanceBetweenPoints ;
            f=MakeNewArray3(d, l, f[0][0], f[1][0], f[2][0]);
            if(Iterations<3)return f;
            
            for (int Q=2; Q<Iterations; Q++) 
            {
                //start with the starting array
                //start at a count of 0
                count=0;
                LastLength=DistanceBetweenPoints;
                DistanceToFirstPoint = (float)
                (DistanceBetweenPoints-(DistanceBetweenPoints/scaleForCross))/2.0f;
                DistanceBetweenPoints = DistanceBetweenPoints/scaleForCross;
                
                for (int p=0; p < f[0].length; p++)
                {
                    
                    GrowingArray[0][count]=f[0][p];
                    GrowingArray[1][count]=f[1][p];
                    GrowingArray[2][count]=f[2][p];
                    count++;
                    
                    boolean Stop=true;
                    
                    if (p>0&&p<f[0].length-1)Stop=false;
                    
                    if(!Stop) 
                    {
                        Stop=true;
                        int Cw=1, Cn=1, Ct=1, Cr=1, Cq=1;
                        int test=0;
                        int start=0;
                        for (int j = 0; j<f[0].length; j++)
                        {
                            if(j==0) 
                            {
                                test = Q-1;
                                start=test;
                                if ((p)==test) 
                                {
                                    Stop=false;j=f[0].length;}
                                
                            } else 
                            {
                                if (Cw==3) 
                                {
                                    test=test+2;Cw=0;
                                } Cw++;
                                if (Cn==9) 
                                {
                                    if(f[0].length>9)test=test+2;Cn=0;
                                } Cn++;
                                if (Ct==27)
                                {
                                    test=test+2;Ct=0;
                                } Ct++;
                                if (Cr==81) 
                                {
                                    test=test+2;Cr=0;
                                } Cr++;
                                if (Cq==243)
                                {
                                    test=test+2;Cq=0;
                                } Cq++;
                                test=test+5;
                            }
                            
                            
                            if ((p)==test) 
                            {
                                Stop=false;j=f[0].length;
                            }
                        }
                        
                        
                    }
                    
                    if(!Stop) 
                    {
                        
                        //make a new structure to tack on in place of the current one
                        float [][] temp = MakeNewArray3(DistanceToFirstPoint,
                                DistanceBetweenPoints, f[0][p], f[1][p], f[2][p]);
                        //copy everything from the last array up to the point
                        //that started this new structure
                        //copy the new points at the old first point in a growing array
                        count--;
                        p++;
                        p++;
                        p++;
                        for (int j=0; j<temp[0].length; j++)
                        {
                            GrowingArray[0][count]=temp[0][j];
                            GrowingArray[1][count]=temp[1][j];
                            GrowingArray[2][count]=temp[2][j];
                            count++;
                            
                        }
                        
                        
                        
                    }//go to the next p value
                }//end p
                //Now put all the points so far into 
                //a resized array for the next iteration
                f=new float[3][count];
                for (int w=0; w<count; w++) 
                {
                    f[0][w]=GrowingArray[0][w];
                    f[1][w]=GrowingArray[1][w];
                    f[2][w]=GrowingArray[2][w];
                }
                
                
            }//end q Iterations
            
            System.gc();
            return f;}
        
        
    }
    
    
    
    float [][]MakeStartingArray18
            (float d, float seedx, float seedy,
            float seedangle
            ) {
        int NumberOfParts=18;
        int N=NumberOfParts+1;
        float [] NewX=new float[N];
        float [] NewY=new float[N];
        //make the rules for each point by specifying
        //the relative turn from each previous point
        NewX[0]=seedx;
        NewY[0]=seedy;
        
        String [] turns = {l, s, r, s, r, r, 
        l, l, s, r, r, l, l, s, l, s, r, s};
        float [] Newangle=ApplyGrammar(seedangle, turns);
        
        
        for (int i = 1; i < NumberOfParts+1; i++) {
            switch ((int)Newangle[i-1]) {
                case(0):
                {NewX[i]=NewX[i-1]+ d; 
                 NewY[i]=NewY[i-1];break;}
                case(90):
                {NewX[i]=NewX[i-1]; 
                 NewY[i]=NewY[i-1]+d;break;}
                case(-90):
                {NewX[i]=NewX[i-1]; 
                 NewY[i]=NewY[i-1]-d;break;}
                case(180):
                {NewX[i]=NewX[i-1]-d; 
                 NewY[i]=NewY[i-1];break;}
            }}
        
        return new float[][]{NewX, NewY, Newangle};
    }
    
    /**Returns a replacement segment for a passed point
     * and orientation, made of three new segments of the specified size.
     * Makes an array of points and orientations for each segment defined
     *by pts[0]topts[1].
     * @param seedx float for the starting point
     * @param seedy float for the starting point
     * @param float seedangle float that tells the orientation of this segment; if the
     * orientation is right (0) then the segment goes right, up,then right, which
     * is a left turn followed by a right turn
     * @param D float for the distance between points or the length of each segment
     * in the replacement*/
    float [][]MakeStartingArray2
            (float d, float seedx, float seedy,
            float seedangle
            ) {
        int NumberOfParts=3;
        int N=NumberOfParts+1;
        float [] NewX=new float[N];
        float [] NewY=new float[N];
        float[] Newangle=new float [N];
        //make the rules for each point by specifying
        //the relative turn from each previous point
        NewX[0]=seedx;
        NewY[0]=seedy;
        if (seedangle==0) 
        {
            NewX[1]=NewX[0]+d;
            NewY[1]=NewY[0];
            Newangle[0]=0;
            NewX[2]=NewX[1];
            NewY[2]=NewY[1]-d;
            Newangle[1]=-90;
            NewX[3]=NewX[2]+d;
            NewY[3]=NewY[2];
            Newangle[2]=0;
        }
        if (seedangle==90) 
        {
            NewX[1]=NewX[0];
            NewY[1]=NewY[0]+d;
            Newangle[0]=90;
            NewX[2]=NewX[1]+d;
            NewY[2]=NewY[1];
            Newangle[1]=0;
            NewX[3]=NewX[2];
            NewY[3]=NewY[2]+d;
            Newangle[2]=90;
        }
        if (seedangle==180) 
        {
            NewX[1]=NewX[0]-d;
            NewY[1]=NewY[0];
            Newangle[0]=180;
            NewX[2]=NewX[1];
            NewY[2]=NewY[1]+d;
            Newangle[1]=90;
            NewX[3]=NewX[2]-d;
            NewY[3]=NewY[2];
            Newangle[2]=180;
        }
        if (seedangle==-90) 
        {
            NewX[1]=NewX[0];
            NewY[1]=NewY[0]-d;
            Newangle[0]=-90;
            NewX[2]=NewX[1]-d;
            NewY[2]=NewY[1];
            Newangle[1]=180;
            NewX[3]=NewX[2];
            NewY[3]=NewY[2]-d;
            Newangle[2]=-90;
        }
        
        return new float[][]{NewX, NewY, Newangle};
    }
    
   float [][]MakeStartingArray32
            (float d, float seedx, float seedy, float seedangle
            ) 
    {
        int NumberOfParts=32;
        int N=NumberOfParts+1;
//        float [] Newangle=new float[N];
        float [] NewX=new float[N];
        float [] NewY=new float[N];
        //make the rules for each point 
        //by specifying the relative turn from each previous point
        NewX[0]=seedx;
        NewY[0]=seedy;
        String [] turns = {r, l, l, r, r, s,
        l, r, r, s, r, l,
        l, s, r, s, l, s,
        r,r, l, s, l,
        l,r,s, l,l,
        r,r,l,r};
        float [] Newangle=ApplyGrammar(seedangle, turns);
        
        
        for (int j = 1; j < NumberOfParts+1;j++)
        {
            switch ((int)Newangle[j-1]) 
            {
                case(0):
                {NewX[j]=NewX[j-1]+d; 
                 NewY[j]=NewY[j-1];break;}
                case(90):
                {NewX[j]=NewX[j-1]; 
                 NewY[j]=NewY[j-1]+d;break;}
                case(-90):
                {NewX[j]=NewX[j-1]; 
                 NewY[j]=NewY[j-1]-d;break;}
                case(180):
                {NewX[j]=NewX[j-1]-d; 
                 NewY[j]=NewY[j-1];break;}
            }
        }
        
        return new float[][]{NewX, NewY, Newangle};
    }
    
    
    
    float [][]MakeStartingArray8
            (float d, float seedx, float seedy, float seedangle
            ) 
    {
        int NumberOfParts=8;
        int N=NumberOfParts+1;
        float [] NewX=new float[N];
        float [] NewY=new float[N];
        //make the rules for each point by specifying the
        //relative turn from each previous point
        NewX[0]=seedx;
        NewY[0]=seedy;
        String [] turns = {l, r, r, s, l, l, r};
        float [] Newangle=ApplyGrammar(seedangle, turns);
        
        
        for (int i = 1; i < NumberOfParts+1; i++)
        {
            switch ((int)Newangle[i-1]) {
                case(0):
                {
                    NewX[i]=NewX[i-1]+d; 
                 
                    NewY[i]=NewY[i-1];break;
                }
                case(90):
                {
                    NewX[i]=NewX[i-1]; 
                    NewY[i]=NewY[i-1]+d;break;
                }
                case(-90):
                {
                    NewX[i]=NewX[i-1]; 
                    NewY[i]=NewY[i-1]-d;break;
                }
                case(180):
                {
                    NewX[i]=NewX[i-1]-d; 
                    NewY[i]=NewY[i-1];break;
                }
            }
        }        
        return new float[][]{NewX, NewY, Newangle};
    }
    
    float [][]MakeStartingArray50(float d, float seedx, float seedy,
            float seedangle
            ) 
    {
        int NumberOfParts=50;
        int N=NumberOfParts+1;
        float [] NewX=new float[N];
        float [] NewY=new float[N];
        float [] Newangle=new float[N];
        Newangle[0]=seedangle;
        //make the rules for each point by
        //specifying the relative turn from each previous point
        NewX[0]=seedx;
        NewY[0]=seedy;
        String [] Dstring={
            r,
            l,r,r,s, s,l,s,
            r,s,
            l,l,s,s,
            l
                    ,s,r,s
                    ,s,s,
            r,
            r,s,s,l,
            s,r,s,
            s,l,l,s,s,s,l,s,r,s,s,r,r,s,l,s,r,s,s,l,l,r
        };
        
        for (int i = 1; i < Dstring.length; i++) {
            Newangle[i]=
                    Dstring[i]==s ? Newangle[i-1]
                    : Dstring[i]==l ? Newangle[i-1]==-90?180:Newangle[i-1]-90
                    :  Dstring[i]==r ? Newangle[i-1]==180?-90:Newangle[i-1]+90:0;
            
        }
        //   float [] Newangle=ApplyGrammar(seedangle, Dstring);
        
        for (int i = 1; i < NumberOfParts+1; i++) 
        {
            switch ((int)Newangle[i-1]) 
            {
                case(0):
                {
                    NewX[i]=NewX[i-1]+d; 
                    NewY[i]=NewY[i-1];break;
                }
                case(90):
                {
                    NewX[i]=NewX[i-1]; 
                    NewY[i]=NewY[i-1]+d;break;
                }
                case(-90):
                {
                    NewX[i]=NewX[i-1]; 
                    NewY[i]=NewY[i-1]-d;break;
                }
                case(180):
                {
                    NewX[i]=NewX[i-1]-d; 
                    NewY[i]=NewY[i-1];break;
                }
            }
        }        
        return new float[][]{NewX, NewY, Newangle};
    }
    
    
    
    static float [] ApplyGrammar(float seedangle, String [] S) 
    {
        float [] Newangle=new float[S.length+2];
        Newangle[0]=seedangle;
        
        
        for (int i = 0; i < S.length; i++) 
        {
            
            Newangle[i+1]=
                    S[i]==s ? Newangle[i]
                    : S[i]==l ? Newangle[i]==-90?180:Newangle[i]-90
                    :  S[i]==r ? Newangle[i]==180?-90:Newangle[i]+90:0;
            
        }
        Newangle[Newangle.length-1]=Newangle[Newangle.length-2];
        return Newangle;
    }
    
    
    float [][]MakeStartingArray3
            (float d,
            float startingDirection, float seedx, float seedy           ) 
    {
        
        int N=4;
        float [] Newangle=new float[N];
        float [] NewX=new float[N];
        float [] NewY=new float[N];
        //make the rules for each point 
        //by specifying the relative turn from each previous point
        NewX[0]=seedx;
        NewY[0]=seedy;
        Newangle[0]=startingDirection;
        Newangle[1]=Newangle[0]==180?-90:Newangle[0]+90;
        Newangle[2]=Newangle[1]==180?-90:Newangle[1]+90;
        Newangle[3]=Newangle[2];
        
        // float DistanceBetweenPoints=DistanceForFirstPoint;
        for (int i = 1; i < N; i++) 
        {
            switch ((int)Newangle[i-1])
            {
                case(0):
                {
                    NewX[i]=NewX[i-1]+d; 
                    NewY[i]=NewY[i-1];break;
                }
                case(90):
                {
                    NewX[i]=NewX[i-1]; 
                    NewY[i]=NewY[i-1]+d;break;
                }
                case(-90):
                {
                    NewX[i]=NewX[i-1]; 
                    NewY[i]=NewY[i-1]-d;break;
                }
                case(180):
                {
                    NewX[i]=NewX[i-1]-d; 
                    NewY[i]=NewY[i-1];break;
                }
            }
        }
        
        return new float[][]{NewX, NewY, Newangle};
    }
    
    
    /**Returns an array of 16 points that is the old array with three similar structure around it.
     * @param an array of */
    float [][] MakeNewArray3(float d, float L, 
            float px, float py, float seedangle)
    {
        int N=16;
        float [] NewX=new float [N];
        float [] NewY=new float [N];
        
        NewX[0]=px;
        NewY[0]=py;
        
        String [] turns ={l, r, r, l, r, l, r, r, l, r, l , r, r, l, s};
        float [] Newangle=ApplyGrammar(seedangle, turns);
        
        float S=L;
        for (int i = 1; i < N; i++)
        {
            if (i==1||i==5||i==6||i==10||i==11||i==15)
            {
                if(i==1||i==15)S=(float)(Iterations+1)*d;
                else S=d;} else S=L;
            switch ((int)Newangle[i-1])
            {
                case(0):
                {
                    NewX[i]=NewX[i-1]+S; NewY[i]=NewY[i-1];break;
                }
                case(90):
                {
                    NewX[i]=NewX[i-1]; NewY[i]=NewY[i-1]+S;break;
                }
                case(-90):
                {
                    NewX[i]=NewX[i-1]; NewY[i]=NewY[i-1]-S;break;
                }
                case(180):
                {
                    NewX[i]=NewX[i-1]-S; NewY[i]=NewY[i-1];break;
                }
            }
        }
        return new float [][]{NewX, NewY, Newangle};
        
    }
    
    public void run(String str) {
    }
    
    
    public static float [][] MakeActualNewPositionsInFatFractal
            (
            float XYRules[][],
            float size,
            float SKale,
            double iterations
            ) 
    {
        int newparts=XYRules[0].length;
        int finallength=(int)Math.pow(newparts, iterations);
        float [][] newxysize=new float [3] [finallength];
        //System.out.println("There should be " + finallength);
        
        
        //start at 0,0
        //record a starting position for each new part at an iteration
        //record the size along with the x and y coordinates
        //do this for each iteration
        //n*(p^i)
        float [] Oldx=new float [1];
        float [] Oldy=new float [1];
        Oldx[0]=0f;
        Oldy[0]=0f;
        
        float lastsize=size, currentsize=size;;
        
        
        for (int i =0;i<(int)iterations; i++) 
        {
            lastsize=currentsize;
            currentsize=lastsize*SKale;//size/((float)Math.pow(scale, (float)i));
            int c=0;//start counting at the start of the array
            for (int b=0; b<Oldx.length; b++) 
            {
                for (int f = 0;f<newparts; f++)
                {
                    newxysize[0][c]=Oldx[b]+(lastsize*XYRules[0][f]);
                    newxysize[1][c]=Oldy[b]+(lastsize*XYRules[1] [f]);
                    newxysize[2][c]=currentsize;
                    //System.out.println(newxysize[2][c]);
                    c++;
                }
                
            }
            
            Oldx=new float [c];
            Oldy=new float [c];
            for (int m=0; m < c; m++) 
            {
                Oldx[m]=newxysize[0][m];
                Oldy[m]=newxysize[1][m];
            }
        }
        //for (int i = 0; i < newxysize[0].length; i++)
        //System.out.println(newxysize[0][i]+"\t"+newxysize[1][i]);
        return newxysize;
    }
    
    /**Returns a new set of origins and resets the values in
     *{@link MakeStructure#ShVars variables}
     *for the rules defining a custom fat fractal.*/
    float [][] GetNewOriginsAndRulesForFatFractal() 
    {
        
        
        boolean quit=false;
        int parts = DefaultFatFractalRules[0].length;
        Iterations=5;        
        
        int p=(int)IJ.getNumber("Number of new parts", parts);
        if (p==IJ.CANCELED) 
        {
            quit=true; 
        } 
        else
        {
            parts=p; 
        }
        if (!quit)
        {
            float S = 
                    getFraction("Scale for new parts (fraction)", 
                    (float)scaleforFAT);
            if (S<0) 
            {
                quit=true;
            }
            else 
            {
                scaleforFAT=S; 
            }
        }
        if (!quit) 
        {
            int T=(int)IJ.getNumber("Number of iterations", Iterations);
            if (T==IJ.CANCELED) 
            {
                quit=true;
            } 
            else 
            {
                Iterations=T;
            }
        }
        if(!quit)
        {
            float SS=(float)IJ.getNumber("Starting Size", (float)D);
            if (SS==IJ.CANCELED) 
            {
                quit=true;
            } 
            else                
            {
                D=SS; 
            }
        }
        
        //first get the array of possibilities
        
        
        if (!quit)
            
        { //then record the user's choices for positions
            //make a rules array
            float [][]Rules=new float[2][parts];
            for (int i  = 0; i < parts; i++) 
            {
                if(quit) i=parts;
                else
                {
                    float defx =  (i<DefaultFatFractalRules[0].length)?
                        DefaultFatFractalRules[0][i]: 0;
                    float F=getFraction("Fraction from X for position "+i, defx);
                    if (F<0)
                    {
                        quit=true; Rules[0][i]=defx;} else Rules[0][i]=F;
                    if(!quit) 
                    {
                        float defy =  (i<DefaultFatFractalRules[1].length)?
                            DefaultFatFractalRules[1][i]: 0;
                        F=getFraction("Fraction from Y for position "+i, defy);
                        if (F<0) 
                        {
                            quit=true;Rules[1][i]=defy;} else Rules[1][i]=F;
                    }
                }
            }
            FatFractalRules=new float[2][parts];
            for (int i = 0; i < parts; i++)
            {
                FatFractalRules[0][i]=Rules[0][i];
                FatFractalRules[1][i]=Rules[1][i];
            }
        }
        title="Fat Fractal-" + Iterations + "X; "+D+ "pix";
        //return this array of rules
        return (MakeActualNewPositionsInFatFractal(FatFractalRules,
                (float)D, (float)scaleforFAT, (double) Iterations));//return rules;
    }
    
    
    static float getFraction(String request, float defaultvalue)
    {
        int sineindex=-1,   cosindex=-1,    lastsine=-1,    lastcos=-1, 
                slash=-1, m=-1;
        float sine=0, cos=0;
        float num=1f;
        float den=1f;
        
        String  s= null;
        String g = null;
        float f = defaultvalue;
        Object[]D=new Object[2];
        D[0]=(Object)request;
        D[1]=Float.toString(defaultvalue);
        s=  IJ.getString(request, Float.toString(defaultvalue));
        //s=(String)JOptionPane.showInputDialog (D[0], D[1]);
        if (s==null)return -1 ;
        
        else 
        {//get substring information
            sineindex=s.toLowerCase().indexOf("sin");
            cosindex=s.toLowerCase().indexOf("cos");
            lastsine=s.toLowerCase().lastIndexOf("sin");
            lastcos=s.toLowerCase().lastIndexOf("cos");
            slash=s.indexOf("/");
            m=s.indexOf("*");
            
            //if there are no symbols, return the number
            if (sineindex<0&&cosindex<0&&m<0&slash<0)
                f=toFloat(s);
            
            if (sineindex<0&&cosindex<0&&m<0&slash>0)
            {//if there is only a slash, divide the two numbers
                float n=toFloat(s.substring(slash+1, s.length()));
                float k=toFloat(s.substring(0, slash));
                f=k/n;
            } //if there is only a slash, divide
            if (sineindex<0&&cosindex>=0&&m<0&slash<0)
            {//if there is a cos only, find it
                f=Cos(s.substring(cosindex+3, s.length()));
                
            }//if there is only a cos, find cos
            if (sineindex>=0&&cosindex<0&&m<0&slash<0)
            {//if there is a sin only, find it
                f=Sin(s.substring(sineindex+3, s.length()));
                
            } //if there is a *
            if(m>0) 
            {
                //if there is only a *, multiply
                if (sineindex<0&&cosindex<0&&slash<0)
                {//if there is only a multiply
                    float n=toFloat(s.substring(m+1, s.length()));
                    float h=toFloat(s.substring(0, m));
                    f=h*n;
                }
                //if there is one sin and a multiply
                if (sineindex>=0&&cosindex<0&&lastsine==sineindex&&slash<0&&m>0)
//if there is only one sine
                {//multiply the sin of this number by the other number
                    if (sineindex<m) 
                    {
                        f=Sin(s.substring(sineindex+3, m));
                        f=f*toFloat(s.substring(m+1, s.length()));
                    } else if (sineindex>m) 
                    {
                        f=Sin(s.substring(sineindex+3, s.length()));
                        f=f*toFloat(s.substring(0, m));
                    }
                }
                
                if (sineindex<0&&cosindex>=0&&lastcos==cosindex&&slash<0) //if there is only one cos
                {//multiply the sin of this number by the other number
                    if (cosindex<m) 
                    {
                        f=Cos(s.substring(cosindex+3, m));
                        f=f*toFloat(s.substring(m+1, s.length()));
                    } else 
                    {
                        f=Cos(s.substring(cosindex+3, s.length()));
                        f=f*toFloat(s.substring(0, m));
                    }
                }
                
            }//end mult
            if(slash>0) 
            {
                //if there is only a slash, divide
                if (sineindex<0&&cosindex<0&&m<0) 
                {
                    float n=toFloat(s.substring(slash+1, s.length()));
                    float h=toFloat(s.substring(0, slash));
                    f=h/n;
                }
                if (sineindex>=0&&cosindex<0&&lastsine==sineindex&&m<0) 
//if there is only one sine
                {//divide the sin of this number by the other number
                    if (sineindex<slash) 
                    {
                        float t=Sin(s.substring(sineindex+3, slash));
                        float y=toFloat(s.substring(slash+1, s.length()));
                        f=t/y;
                    } 
                    else 
                    {
                        float t=Sin(s.substring(sineindex+3, s.length()));
                        float y=toFloat(s.substring(0, slash));
                        f=y/t;
                    }
                }
                if (sineindex<0&&cosindex>=0&&lastcos==cosindex&&m<0) 
//if there is only one sine
                {//multiply the sin of this number by the other number
                    if (cosindex<slash) 
                    {
                        float t=Cos(s.substring(cosindex+3, slash));
                        float y=toFloat(s.substring(slash+1, s.length()));
                        f=t/y;
                    } 
                    else 
                    {
                        float t=Cos(s.substring(cosindex+3, s.length()));
                        float y=toFloat(s.substring(0, slash));
                        f=y/t;
                    }
                }
                
                
            }//end divide
            if (sineindex>=0&&cosindex<0&&lastsine>sineindex&&m<0&&slash>0) 
            {//divide the two sines
                f=Sin(s.substring(sineindex+3, slash));
                f=f/Sin(s.substring(lastsine+3, s.length()));
                
            }
            if (sineindex>=0&&cosindex<0&&lastsine>sineindex&&m>0&&slash<0)
            {//multiply the two sines
                f=Sin(s.substring(sineindex+3, m));
                f=f*Sin(s.substring(lastsine+3, s.length()));
                
            }
            if (sineindex<0&&cosindex>=0&&lastcos>cosindex&&m<0&&slash>0) 
            {//divide the two cosines
                f=Cos(s.substring(cosindex+3, slash));
                f=f/Cos(s.substring(lastcos+3, s.length()));
                
            }
            if (sineindex<0&&cosindex>=0&&lastcos>cosindex&&m>0&&slash<0) 
            {//multiply the two sines
                f=Cos(s.substring(cosindex+3, m));
                f=f*Cos(s.substring(lastcos+3, s.length()));
                
            }
            if (sineindex>=0&&cosindex>=0&&m<0&&slash>=0) 
            {//multiply the two sines
                if(sineindex<cosindex) 
                {
                    float r=Sin(s.substring(sineindex+3, slash));
                    float t=Cos(s.substring(cosindex+3, s.length()));
                    f=r/t;
                } 
                else 
                {
                    float r=Sin(s.substring(sineindex+3, s.length()));
                    float t=Cos(s.substring(cosindex+3, slash));
                    f=t/r;
                }
                
                
            }
            if (sineindex>=0&&cosindex>=0&&m>=0&&slash<0) 
            {//multiply the two sines
                if(sineindex<cosindex) 
                {
                    float r=Sin(s.substring(sineindex+3, slash));
                    float t=Cos(s.substring(cosindex+3, s.length()));
                    f=r*t;
                } 
                else 
                {
                    float r=Sin(s.substring(sineindex+3, s.length()));
                    float t=Cos(s.substring(cosindex+3, slash));
                    f=t*r;
                }
                
                
            }
            //if there are two sines, and a multiply
            //if there are two sines and a divide
            //if there are two cos and a multiply
            //if there are two cos and a divid
            
        }
        // JOptionPane.showMessageDialog(null, f+" cos at "+cos+"sine at "+sine);
        return f;
    }//end method

    
    static float Cos(String s) {
        float f=1f;
        try
        {
            f=(float)Math.cos(Math.toRadians(Float.parseFloat(s)));
        } 
        catch(NumberFormatException nf)
        {
                f=1f;
        }
        catch(NullPointerException np) 
                {
                    f=1;
                }
        return f;
    }
    
    static float Sin(String s) 
    {
        float f=1f;
        try {
            f=(float)Math.sin(Math.toRadians(Float.parseFloat(s)));
        } 
        catch(NumberFormatException nf) 
        {
                f=1f;
        } 
        catch(NullPointerException np) 
                {
                    f=1;
        }
        return f;
    }
    
    static float toFloat(String s)
    {
        float f=1f;
        try
        {
            f=Float.parseFloat(s);} catch(NumberFormatException nf) 
            {
                f=1f;} 
        catch(NullPointerException np)
                {
                    f=1;
                }
        return f;
    }
    
    
    public void DrawFat(
            Graphics2D G,
            double sx,
            double sy,            
            float [][] XYSize)
    {
        G.setColor(Color.white);        
        int [][]P=new int [3][XYSize[0].length];
        int xc=(int)sx, yc=(int)sy;
        
        for (int i = 0; i < XYSize[0].length; i++)
        {
            P[0][i]=(int)XYSize[0][i]+xc;
            P[1][i]=(int)XYSize[1][i]+yc;
            P[2][i]=(int)XYSize[2][i];
            if (P[2][i]<1)P[2][i]=1;
        }
        
        
        int totalpoints=P[0].length;
        for (int i = 0; i<totalpoints; i++)
        {
            G.fillRect(P[0][i], P[1][i],
                    P[2][i], P[2][i]);      
        }
        
        
    }
    
    
   
       
        
        
        
        
        void setBigSmall() {
            
            bigx = array[0][0];
            bigy = array[0][1];
            smallx = array[0][0];
            smally = array[0][1];
            for (int m = 0; m <array.length; m++) 
            {
                bigx=Math.max(array[m][0], bigx);
                bigy=Math.max(array[m][1], bigy);
                smallx=Math.min(array[m][0], smallx);
                smally=Math.min(array[m][1], smally);
            }
            FracSize = (int)Math.max((bigx-smallx), (bigy-smally));
        }
        
        
        /**********************************************************************/
        ////////////////////////////////////////////////////////////////////////
        /**
         * Scales, rotates, and appends a passed GeneralPath
         * repeatedly to restore the original
         * length but increase detail.
         * Makes a new side, having one more level of detail,
         * for a Koch figure.
         *
         * @param p GeneralPath of koch_ generator from
         * {@link #drawiterator}
         * @return GeneralPath has same length as original
         * but greater detail
         */
        public GeneralPath scaleit(GeneralPath p) 
        {
            //start method to scale iterator
            //declare variables for making new path
            
            GeneralPath p1 = new GeneralPath();
            //make a path for linking iterators
            GeneralPath ps= new GeneralPath();
            if (true) 
            {//declare transforms and scale initial segment iterator
                AffineTransform scalep =
                        AffineTransform.getScaleInstance(1f/scaleForKoch, 
                        1f/scaleForKoch);
                ps.append(scalep.createTransformedShape(p), false);                
                //scale the original path given
                //declare affine transforms to use in main structure using                
                AffineTransform at =
                        AffineTransform.getRotateInstance(
                        Math.PI/3f,
                        ps.getCurrentPoint().getX(),
                        ps.getCurrentPoint().getY());
                AffineTransform tr =
                        AffineTransform.getTranslateInstance
                        (length/scaleForKoch, 0);
                at.concatenate(tr);
                //concatenate transforms for first segment
                AffineTransform atro=
                        AffineTransform.getRotateInstance
                        (
                        5f*Math.PI/3f,
                        ps.getCurrentPoint().getX(),
                        ps.getCurrentPoint().getY()
                        );
                AffineTransform tr1 =
                        AffineTransform.getTranslateInstance
                        (
                        length/scaleForKoch, 0
                        );
                tr1.concatenate(atro);
                
                //concatenate transforms for second segment
                AffineTransform attr =
                        AffineTransform.getTranslateInstance
                        (2*length/scaleForKoch, 0);
                
                //transform for last segment
                //append entire shape using scaled path and transforms
                p1.append(ps, true);//add to p1 the first segment of
                
                p1.append(at.createTransformedShape(ps),false);
                
                //appends 2nd segment works
                p1.append(tr1.createTransformedShape(ps), false);
                
                ///*appends 3rd segment
                p1.append(attr.createTransformedShape(ps),false);
                
                //appends 4th segment works
                ///*return the new path
            }
            ps.reset();
            p.reset();
            return(p1);
        }/*end scaleit method*///////END/////////
        
        /**********************************************************************/
        ////////////////////////////////////////////////////////////////////////
        /**
     * Returns Koch figure as a GeneralPath, specified by
     * {@link #makeAflake},
     * {@link #pixelwidth}, and {@link #Iterations}.
     * 
     * Calls {@link #drawiterator drawiterator} to make first side, calls
     * {@link #scaleit scaleit}
     * passing the side recursively for the number of
     * Iterations requested,
     * passes final segment to
     * {@link #makefinal makefinal}
     * to be appended to itself
     * as a final side. Then either appends the side three times as
     * a flake or returns one side.
     * For figures at 1 iteration level, scaling is done by
     * {@link java.awt.geom.AffineTransform}
     * within this method rather than by calling scaleit() as for higher
     * level figures.
     * 
     * 
     * @return kochpath {@link java.awt.geom.GeneralPath
     * GeneralPath} defining specified Koch flake or line
     */
        public GeneralPath getfinalKoch()//puts sides together
        {
            
            GeneralPath kochpath = new GeneralPath();
            int i=0;//declare counter
            GeneralPath[] path = new GeneralPath[1+(int)(Iterations-2)];
            if(Iterations==1)path = new GeneralPath[1];
            //make an array of paths to store the new paths
            //1.generate the iterator and scale it as per Iterations above
            if (true) {
                
                path[0] =drawiterator();//call internal method to make
                // the first iterator
                if (Iterations>1) {
                    
                    if (true) {
                        for (i=1; i<=(Iterations-2); i++) {
                            if (true)path[i] =
                                    scaleit(path[i-1]);//iterate the iterator
                        }
                    }//start a loop to iterate the iterator according
                    // to the variable "Iterations"
                }
                if (true) {
                    GeneralPath side = new GeneralPath();
                    GeneralPath tempside = new GeneralPath();
                    if (Iterations!=1)tempside = path[i-1];
                    if (Iterations==1) {
                        side.append(path[0].createTransformedShape(
                                AffineTransform.getScaleInstance(3, 3)), false);
                        
                        kochpath.append(path[0].createTransformedShape(
                                AffineTransform.getScaleInstance(3, 3)), false);
                        
                        tempside.append(path[0].createTransformedShape(
                                AffineTransform.getScaleInstance(3,3)), false);
                        
                    } else {
                        side = makefinal(path[i-1]);
                        //the finalized side to be constucted in triangle
                        //make and position sides
                        kochpath.append(side, false);
                        //invoke method to make first side
//                    //new trial code june2007
//                    {double theta=Math.PI/3f;
//                     double theta2=Math.PI*(2f/3f);
//                        AffineTransform at = AffineTransform.getRotateInstance(theta);
//                        AffineTransform at2 = AffineTransform.getRotateInstance(theta);
//                        if(n==1)return (kochpath);
//                        if(n==2)return (kochpath.transform(at));
//                        else return (kochpath.transform(at2));
//                    }//end newcodejune2007
                        if (type==KOCH_FLAKE) 
                        {
                            AffineTransform tr = 
                                    AffineTransform.getTranslateInstance
                                    (-1*D,0);//prepare position for next side
                            AffineTransform at  =
                                    AffineTransform.getRotateInstance(
                                    2f*Math.PI/3f,
                                    kochpath.getCurrentPoint().getX(),
                                    tempside.getCurrentPoint().getY());
                            tr.concatenate(at);
                            kochpath.append(
                                    tr.createTransformedShape(side),false);
                            
                            //appends 2nd segment
                            tr.setToTranslation(1*D,0);
                            //prepare position for next side
                            at.setToRotation(-4f*Math.PI/6f,
                                    kochpath.getCurrentPoint().getX(),
                                    kochpath.getCurrentPoint().getY());
                            tr.concatenate(at);
                            kochpath.append(
                                    tr.createTransformedShape(side), false);
                            
                            //appends 3rd segment
                        }}
                    if ((type==KOCH_FLAKE)&&(Iterations==1)) 
                    {
                        AffineTransform tr = AffineTransform.getTranslateInstance
                                (-1*D,0);
                        //prepare position for next side
                        AffineTransform at  =
                                AffineTransform.getRotateInstance(2f*Math.PI/3f,
                                kochpath.getCurrentPoint().getX(),
                                tempside.getCurrentPoint().getY());
                        tr.concatenate(at);
                        kochpath.append(tr.createTransformedShape(side),false);
                        
                        //appends 2nd segment works
                        tr.setToTranslation(1*D,0);
                        //prepare position for next side
                        at.setToRotation(-4*Math.PI/6,
                                kochpath.getCurrentPoint().getX(),
                                kochpath.getCurrentPoint().getY());
                        tr.concatenate(at);
                        kochpath.append(tr.createTransformedShape(side), false);
                        
                        //appends 3rd segment
                    }}
            } return (kochpath);
        }//end this funky method///////END/////////
        public GeneralPath getfinalKochtest()//puts sides together
        {
            
            GeneralPath kochpath = new GeneralPath();
            int i=0;//declare counter
            GeneralPath[] path = new GeneralPath[1+(int)(Iterations-2)];
            if(Iterations==1)path = new GeneralPath[1];
            //make an array of paths to store the new paths
            //1.generate the iterator and scale it as per Iterations above
            if (true) {
                
                path[0] =drawiterator();//call internal method to make
                // the first iterator
                if (Iterations>1) {
                    
                    if (true) {
                        for (i=1; i<=(Iterations-2); i++) {
                            if (true)path[i] =
                                    scaleit(path[i-1]);//iterate the iterator
                        }
                    }//start a loop to iterate the iterator according
                    // to the variable "Iterations"
                }
                if (true) {
                    GeneralPath side = new GeneralPath();
                    GeneralPath tempside = new GeneralPath();
                    if (Iterations!=1)tempside = path[i-1];
                    if (Iterations==1) {
                        side.append(path[0].createTransformedShape(
                                AffineTransform.getScaleInstance(3, 3)), false);
                        
                        kochpath.append(path[0].createTransformedShape(
                                AffineTransform.getScaleInstance(3, 3)), false);
                        
                        tempside.append(path[0].createTransformedShape(
                                AffineTransform.getScaleInstance(3,3)), false);
                        
                    } else {
                        side = makefinal(path[i-1]);
                        //the finalized side to be constucted in triangle
                        //make and position sides
                        kochpath.append(side, false);
                        
                        if (type==KOCH_FLAKE)
                        {
                            Point2D x = kochpath.getCurrentPoint();
                            AffineTransform at  =
                                    AffineTransform.getRotateInstance(
                                    2f*Math.PI/3f,
                                    x.getX(),
                                    x.getY());
                            side.transform(at);
                            kochpath.append(side,false);
                            side.transform(at);
                            
                            //appends 3rd segment
                        }}}
            }
            return (kochpath);
        }//end this funky method///////END/////////
        //get the path, draw it, then get the next path
        /**
         * Rotates and appends a passed GeneralPath four
         * times as for making a koch_ curve side out of 4 scaled koch_ curves.
         * Depends on width and iteration being the same as for initial
         * {@link java.awt.geom.GeneralPath}. Is called by {@link
         * #getfinalKoch getfinalKoch method}.
         * method.
         *
         *
         * @param ps {@link java.awt.geom.GeneralPath GeneralPath}
         * <I> usually</I> from
         * {@link #scaleit scaleit}
         * </I>
         * @return {@link java.awt.geom.GeneralPath GeneralPath}
         * depicting final side of Koch figure
         * @see #getfinalKoch
         */
        public GeneralPath makefinal(GeneralPath ps)
        {//start make final
            
            GeneralPath p1 = new GeneralPath();
            if(true)
            {//declare affine transforms to use in main structure
                //using ultimate length
                if (true)
                {
                    AffineTransform at  =
                            AffineTransform.getRotateInstance(
                            Math.PI/3f, ps.getCurrentPoint().getX(),
                            ps.getCurrentPoint().getY());
                    AffineTransform tr =
                            AffineTransform.getTranslateInstance
                            (D/scaleForKoch,0);
                    at.concatenate(tr);//concatenate transforms for first segment
                    AffineTransform atro =
                            AffineTransform.getRotateInstance(5f*Math.PI/3f,
                            ps.getCurrentPoint().getX(),
                            ps.getCurrentPoint().getY());
                    AffineTransform tr1 = AffineTransform.getTranslateInstance(
                            D/scaleForKoch, 0);
                    tr1.concatenate(atro);//concatenate transforms for second segment
                    AffineTransform attr =
                            AffineTransform.getTranslateInstance
                            (2f*D/scaleForKoch, 0);
                    //transform for last segment
                    AffineTransform at3 =
                            AffineTransform.getRotateInstance(
                            4f*Math.PI/3f,ps.getCurrentPoint().getX(),
                            ps.getCurrentPoint().getY());
                    at3.concatenate(tr);//concatenate for star
                    //make side shape using path p and transforms
                    p1.append(ps, true);
                    
                    //add to p1 the first segment of basic iterator, p
                    p1.append(at.createTransformedShape(ps),false);
                    
                    //appends 2nd segment
                    p1.append(tr1.createTransformedShape(ps), false);
                    
                    //appends 3rd segment
                    p1.append(attr.createTransformedShape(ps),false);
                    
                    //appends 4th segment works
                }
            }
            return(p1);//return path to be drawn in final shape
            
        }/*endmakefinal*///////END/////////
        /**
         * Returns a {@link java.awt.geom.GeneralPath} for the
         * basic iterator of both koch_ flakes and lines having f
         * our segments each 1/3 the length of the original line.
         *
         * The size of one piece for a Koch line is set from
         * newlength = oldlength/3.
         * The basic 4 segment pattern is: at first third along
         * horizontal length, raise to tiangle's full height
         * (Pythagorus, y =
         * (Math.sqrt (3)/2)*(length/3)), go back to horizontal,
         * go one horizontal. Was working better than rotate theta.
         * <p><img src=".././GUI/doc-files/kiterator.jpg"  width = "220"
         * align = "center"></p>
         *
         * @return GeneralPath koch_ curve 4-segment basic
         * iterator at full width
         * @see #getfinalKoch
         */
        public GeneralPath drawiterator() 
        {
            //Path for drawing iterator
            GeneralPath p = new GeneralPath();
            //draw the iterator
            if (true) 
            {
                p.moveTo(0, 0);///start the path
                
                //set the start and end points to the scaled values
                length = (float)D / (float)scaleForKoch;
                
                float spacer = (length/(float)scaleForKoch);
                for  (i=1; i<cutsperpiece; i++) {
                    //start for to draw iterator one cut at a time
                    if ((int)p.getCurrentPoint().getX()==(length/3F))
                    {
                        //at the first third along the horizontal length
                        p.lineTo((length/2F), 
                                ((((float)(Math.sqrt(3f)))/2f)*(length/3f)));
                        //make line to raised tiangle's full height
                        i = i + (2*cutsperpiece);//remove this later; legacy                        
                        //increment the counter back to the horizontal portion
                        p.lineTo((2F/3F)*length, 0);                        
                        //draw back up to horizontal at 2nd third
                    }///*end if to make the middle dip
                    p.lineTo(((float)p.getCurrentPoint().getX()+ spacer), 0);                    
                    ///draw one horizontal cut section
                }//end for to draw each cut
                //now you have the basic segment iterator,
                // p;this never changes so do not change its variables
            }
            return(p);//done making iterator
        }//end draw iterator method///////END/////////
        /**
         * Returns a float array of x and y coordinates defining a Koch figure.
         *
         * Takes no parameters but depends on iteration level, width, and type
         * set in constructor. Calls getfinalKoch() to make a GeneralPath,
         * then gets the PathIterator on it to make the array. Removes duplicated
         * points
         * and re-orders segments.
         *
         * @param br  {@link MakeStructure#Shell Shell}
         * @return array of <CODE>floats</CODkoch_s x and y coordinates defining
         * the final koch figure
         */        
        public float [][] getfinalArray() 
        {
            //return an array of coords for a Koch snowflake's three sides
            float [][]array = null;
            float[][]newarray=null;
            int I = 0;
            //end new code
            if(true)
                bigloop: 
                {
                    float [] coords = new float[6];
                    GeneralPath GP = getfinalKoch();
                    //invoke local functionto make 
                    //a general path of the Koch curve
                    java.awt.geom.PathIterator it = GP.getPathIterator(null);
                    int NUMPERSIDE = (int)(Math.pow(4f,(this.Iterations+1)));
                    int NUMPTS=(int)(3f*NUMPERSIDE);
                    if (type==KOCH_LINE)NUMPTS=NUMPERSIDE;                    
                    array = new float[NUMPTS][2];
                    newarray=new float[0][0];
                    while(!(it.isDone())) 
                    {//traverse the path and record its points
                        if (true) 
                        {
                            int seg = it.currentSegment(coords);
                            it.next();
                            if (seg!=it.SEG_CLOSE) {
                                array[I][0]=coords[0];
                                array[I][1]=coords[1];
                            }I++;                            
                        }                        
                    }
                    if (true)
                        lineloop: 
                        {
                            if (type==KOCH_LINE) 
                            {
                                int n = 1, m = 0;
                                float[][]E = new float [I][2];
                                newarray = new float[I][2]; 
                                {
                                    //make an array of adjusted points
                                    m=0;
                                    E[0][0]=array[0][0];
                                    E[0][1]=array[0][1];
                                    for (m=1;m<(I); m++)
                                    {
                                        if (true) 
                                        {
                                            if ((array[m][0]==E[n-1][0])
                                            &&
                                                    (array[m][1]==E[n-1][1]
                                                    )) 
                                            {}//do nothing
                                            else
                                            {
                                                E[n][0]=array[m][0];
                                                E[n][1]=array[m][1];
                                                n++;
                                            }
                                            
                                        }else break;
                                    }
                                } newarray = new float [n][2];
                                
                                for (int j = 0; j < newarray.length; j++)
                                {
                                    newarray[j][0] = E[j][0];
                                    newarray[j][1]=E[j][1];
                                }//end for j                                
                            }//end if makeaflake is false
                        }else break bigloop;//end if true line loop
                    if (true)
                        flakeloop:
                        {if (type==KOCH_FLAKE) 
                         {
                             newarray = new float[I][2];
                             if (true) 
                             {//make an array of adjusted points
                                 int n = 0;
                                 for (int m=0;m<(I-2); m++) 
                                 {
                                     if (((m+1)!=((I/3f)))
                                     &&((m+1)!=(2*(I/3f)))) 
                                     {
                                         newarray[n][0]=array[m][0];
                                         newarray[n][1]=array[m][1];
                                         n++;
                                     }
                                 }
                                 newarray[I-2][0]=array[(int)(I/3f)][0];
                                 newarray[I-2][1]=array[(int)(I/3f)][1];
                                 
                                 for (int j = 0; j < I-1; j++) 
                                 {
                                     if (true) 
                                     {
                                         newarray[j][0]=array[j][0];
                                         newarray[j][1]=array[j][1];
                                         
                                     }//end if true
                                 }//end for j
                             }
                         }//end if makeaflake
                        }else break bigloop;//end if true flake loop
                }//end if true bigloop              
                return newarray;
        }//////END/////////
        
        /**Invokes QFractalGenerator or KFractalGenerator class.
         *Returns an array of points from which to draw a fractal.
         *@param SIZE a float for the size of a side in the
         *fractal
         *@param qtype a float coding the type of fractal
         *@param farray a float array for the new points
         *@see MakeStructure.KFractalGenerator#getfinalArray
         *@see MakeStructure.QFractalGenerator#getArray
         *@return float array of x and y points for fractal
         */
        public float[][]  makeFractal(float[][]farray) 
        {/*start if the file to make is a KFractalGenerator*/
            try {/**get an array of points for drawing a koch flake*/
                if (true) 
                {   
                    farray = getfinalArray();
                }
                
                if (farray.length==0) {
                    
                    JOptionPane.showMessageDialog(null,
                            "Error. Try again using different "
                            +"size or less iterations.");
                    
                }
            } catch (java.lang.OutOfMemoryError m) {
                JOptionPane.showMessageDialog(null,
                        "Out of Memory. Try changing "
                        +"size or decreasing iterations.");
                
            } catch (java.lang.ArrayIndexOutOfBoundsException ob) {
                JOptionPane.showMessageDialog(null,
                        "Sorry. Array Exception. Can't create this image. " +
                        "Try changing size or iterations.");
                
            } finally {
            }
            return farray;
        }//////END/////////
        
        
        ///////////////////////////////////////////////////////////////////////
        /** Draws array of x and y coordinates from
         * calling function,
         * usually {@link #DoQuadricOrKoch}, which obtains array
         * from {@link MakeStructure.KFractalGenerator}
         * .
         *
         * Draws points onto screen, then further draws using parameters
         * assigned previously for
         * drawing (see below). Corrects problems with order of sides for
         * filling shapes. Stroke is
         * set previously in DoQuadricOrKoch method.
         *
         * @see MakeStructure.ShVars#makeaflake
         * @see MakeStructure.ShVars#grad
         * @see MakeStructure.ShVars#funky
         * @see MakeStructure.ShVars#outline
         * @see #DoQuadricOrKoch(Graphics2D g)
         * @param G Graphics2D on which to draw
         * @param HH Graphics2D for buffering
         * @param array float array of x and y coordinates for fractal
         */
        
        public void drawKFractal(Graphics2D G, float [][] array)
        {//if this is not a quadric (if it is a koch_)
            int [][] N = new int [0][0];
            int H=0;
            int I = array.length;//set the counter to the number of points
            //in the array of koch_ snowflake points
            //////////////////////////////////////////
            if (type==KOCH_LINE) 
            {                
                int m=0;
                G.setColor(ij.gui.Toolbar.getForegroundColor());
                for ( m=0; m<array.length-1; m++) {
                    if (funky) {//set line colours if selected
                      if (((m-2)%4)==0)
                      {
                       G.setColor(ij.gui.Toolbar.getForegroundColor().darker());                         
                      }
                        if ((m%4)==0)
                        {
                            G.setColor(Roi.getColor());                            
                        }
                    }
                    
                    G.drawLine((int)array[m][0], (int)array[m][1],
                            (int)array[m+1][0], (int)array[m+1][1]);}
                
            } else {
                ///////////////////////////////////////////////
                N = new int [2][array.length];H = 0;
                for (int m=0;m<(I-2); m++) 
                {//put the coords of the first side into an array because
                    //the array is not sequential
                    if ((m<(I/3f)))
                    {//the first side is the first third of the points
                        N[0][H] = (int)array[m][0];
                        N[1][H]= (int)array[m][1];
                        H++;
                    }
                }
                for (int m=((int)((I/3f)*2f));m<(I-1); m++) 
                {//the second side is the last third
                    N[0][H] = (int)array[m][0];
                    N[1][H]= (int)array[m][1];
                    H++;
                }
                for (int m=((int)(I/3f));m<((int)((I/3f)*2f))-1; m++) 
                {//the third side is the second third
                    N[0][H] = (int)array[m][0];
                    N[1][H]= (int)array[m][1];
                    H++;
                }
                if (outline||funky) {//draw the outline if selected
                    if(outline)G.setColor(ij.gui.Toolbar.getForegroundColor());
                    for (int m = 0; m < H-1; m++)
                    {//go through each point in the array
                        if (funky) {//set line colours if selected
                            if (((m-2)%4)==0) {
                                G.setColor(ij.gui.Toolbar.getForegroundColor().darker());
                                
                            }
                            if ((m%4)==0) {
                                G.setColor(Roi.getColor());
                                
                            }
                        }
                        
                        {
                            G.drawLine(N[0][m], N[1][m],N[0][m+1],
                                    N[1][m+1]);
                            
                        }
                        //draw each segment of the form
                    }//end draw koch_
                    {
                        G.drawLine(N[0][H-1], N[1][H-1], N[0][0],
                                N[1][0]);
                        
                    }//draw the last segment
                }//end draw the V.outline
                if (fill||grad) 
                {//fill it if selected
                    if (grad) 
                    {//set a colour gradient if selected
                        GradientPaint gr =
                                new GradientPaint(N[1][1],N[0][0],
                                ij.gui.Toolbar.getForegroundColor(),
                                N[1][H/2], N[0][H/2],
                                gradcolour, true);
                        G.setPaint(gr);
                        
                    } else G.setColor(Roi.getColor());
                    if(fill&&funky)
                        G.setColor(ij.gui.Toolbar.getBackgroundColor());
                    
                    G.fillPolygon(N[0], N[1], H);                        
                    //fill the shape
                }//end fill koch_
            }
        }//////END/////////
        
        float[][]array = new float[0][0];
        public void DoKoch(Graphics2D G) 
        {
            
            array = new float[0][0];
            float SIZE=(float)((int)(D*0.5f));
            
            array = makeFractal(array);
            if (array!=null) {
                setBigSmall();
            } else {
                JOptionPane.showMessageDialog(null,
                        "Sorry, there was an error. " +
                        "Please reduce the size of the " +
                        "side or the iterations.");
                return;
                
            }
            if (true) {
                G.setBackground(ij.gui.Toolbar.getBackgroundColor());
                G.clearRect(0,0, imageSize, imageSize);
                RenderingHints qualityHints = new  RenderingHints(
                        RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                qualityHints.put(RenderingHints.KEY_RENDERING,
                        RenderingHints.VALUE_RENDER_QUALITY);
                G.setRenderingHints(qualityHints);
                BasicStroke bstroke = new BasicStroke(Stroke,
                        BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER);
                G.setStroke(bstroke);//set quality of rendering
                float [] coords = new float[6];
                double CY =
                        smally + ((bigy-smally)/2f);
                //centre of shape
                double CX =
                        smallx + ((bigx-smallx)/2f);
                //centre of shape
                double ytrans= (FracSize/2f) -CY;
                //calculate for moving the shape to the centre
                double xtrans =(FracSize/2f) -CX;
                if(type==KOCH_LINE)xtrans=xtrans+border;
                for (int m=0;m<array.length; m++)
                {//move the shape's points to centre it
                    array[m][0]=array[m][0]+(float)xtrans;
                    array[m][1]=array[m][1]+(float)ytrans+25;
                }
                drawKFractal(G, array);
            }//end what to do if V.true
            
            
        }//end draw Koch or Quadric curves
    
    
    
}



