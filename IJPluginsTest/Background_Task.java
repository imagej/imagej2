import ij.*;
import ij.plugin.PlugIn;
import ij.process.*;
import java.awt.*;

/**
This plugin is an example of how to set up a plugin to perform  long-running task.
*/
public class Background_Task implements PlugIn {
	int hours = 10;
	ImageProcessor ip;
	ImagePlus img;

	public void run(String arg) {
		if (IJ.versionLessThan("1.19h"))
			return;
		Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
		long runTime = hours*3600;
		ip = new ByteProcessor(60, hours*60+20);
		ip.setColor(Color.white);
		ip.fill();
		ip.setColor(Color.black);
		for (int y=21; y<ip.getHeight(); y++)
			if ((y-20)%60==0) {ip.moveTo(0,y); ip.lineTo(ip.getWidth()-1,y);}
		img = new ImagePlus(hours+" hours", ip);
		img.show();
		int id = img.getID();
		long startTime = System.currentTimeMillis();
		long elapsedTime = 0;
		long lastTime = 0;
		while(elapsedTime<runTime) {
			elapsedTime = (System.currentTimeMillis()-startTime)/1000;
			if (elapsedTime>lastTime) {
				if (WindowManager.getImage(id)==null ) // quit if window has been closed
					break;
				lastTime = elapsedTime;
				IJ.showProgress((double)elapsedTime/runTime);
				showProgress(elapsedTime);
			}
		}
		IJ.beep();
		IJ.showProgress(1.0);
		IJ.showStatus("");
	}

	void showProgress(long time) {
		long sec = time%60;
		long min = (time/60)%60;
		long hour = (time/3600);
		ip.setRoi(0,0,60,20);
		ip.setColor(Color.white);
		ip.fill();
		ip.setRoi(null);
		ip.setColor(Color.black);
		ip.moveTo(5,15);
		ip.drawString(hour+":"+min+":"+sec);
		ip.putPixel((int)sec, (int)hour*60+(int)min+20, 128);
		img.updateAndDraw();
	}

}

