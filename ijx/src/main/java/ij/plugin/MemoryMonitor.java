package ij.plugin;
import ij.*;
import ij.gui.*;
import ij.process.*;
import ij.macro.Interpreter;
import ijx.IjxImagePlus;
import ijx.gui.IjxImageCanvas;
import ijx.gui.IjxImageWindow;
import java.awt.*;

/**	This plugin continuously plots ImageJ's memory
	utilization. It could also be used as a starting
	point for a video acquisition plugin. Hold down the
	alt/option key when selecting the <code>Monitor Memory</code>
	command and the plugin will use a 640x480 window
	and display the frame rate. Click on the status bar in the
	ImageJ window to force the JVM to do garbage collection.
*/
public class MemoryMonitor implements PlugIn {
	int width = 200;
	int height = 75;
	long fps, startTime, elapsedTime;
	ImageProcessor ip;
	int frames;
	IjxImageCanvas ic;
	double[] mem;
	int index;
	long value;
	double max = 12*1204*1024; // 12MB
	long maxMemory = IJ.maxMemory();

	public void run(String arg) {
		if (Interpreter.isBatchMode()) return;
		if (IJ.altKeyDown()) {
			// simulate frame grabber
			width = 640;
			height = 480;
		}
		ip = new ByteProcessor(width, height, new byte[width*height], null);
	 	ip.setColor(Color.white);
		ip.fill();
	 	ip.setColor(Color.black);
		ip.setFont(new Font("SansSerif",Font.PLAIN,12));
		ip.setAntialiasedText(true);
		ip.snapshot();
		IjxImagePlus imp = IJ.getFactory().newImagePlus("Memory", ip);
		WindowManager.setCenterNextImage(true);
		imp.show();
		imp.lock();
		IjxImageWindow win = imp.getWindow();
		ic = win.getCanvas();
		mem = new double[width+1];
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		startTime = System.currentTimeMillis();
		win.setRunning(true);
       	while (win.isRunning()) {
			updatePixels();
         	showValue();
			imp.updateAndDraw();
         	if (width==640)
        		Thread.yield();
        	else
        		IJ.wait(100);
       		frames++;
		}
		imp.unlock();
	}
	
    void showValue() {
    	double value2 = (double)value/1048576L;
    	String s = IJ.d2s(value2,value2>50?0:2)+"MB";
    	if (maxMemory>0L) {
			double percent = value*100/maxMemory;
			s += " ("+(percent<1.0?"<1":IJ.d2s(percent,0)) + "%)";
		}
    	if (width==640) {
			elapsedTime = System.currentTimeMillis()-startTime;
			if (elapsedTime>0) {
				double scale = ic.getMagnification();
				fps = (frames*10000)/elapsedTime;
				s += ", " + fps/10 + "." + fps%10 + " fps";
			}
    	}
    	ip.moveTo(2, 15);
		ip.drawString(s);
	}

	void updatePixels() {
		double used = IJ.currentMemory();
		if (frames%10==0) value = (long)used;
		if (used>0.9*max) max *= 2.0;
		mem[index++] = used;
		if (index==mem.length) index = 0;
		ip.setLineWidth(1);
		ip.reset();
		int index2 = index+1;
		if (index2==mem.length) index2 = 0;
		double scale = height/max;
		ip.moveTo(0, height-(int)(mem[index2]*scale));
		for (int x=1; x<width; x++) {
			index2++;
			if (index2==mem.length) index2 = 0;
			ip.lineTo(x, height-(int)(mem[index2]*scale));
		}
	}

}
