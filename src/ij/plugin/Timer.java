package ij.plugin;
import java.awt.*;
import ij.*;

/**ImageJ plugin for measuring the speed of various Java operations.*/
public class Timer implements PlugIn {
	int j=0;
	long startTime, nullLoopTime;
	int numLoops;


	public void run(String arg) {
		int j=0, k;
		int[] a = new int[10];
		long endTime;

		/*
		startTime = System.currentTimeMillis();
		//for (int i=0; i<100; i++) IJ.wait(10);
		for (int i=0; i<100; i++) Thread.yield();
		long elapsedTime = System.currentTimeMillis() - startTime;
		IJ.write(elapsedTime + " ms");
		*/
                
		numLoops = 10000;
		do {
			numLoops = (int)(numLoops*1.33);
			startTime = System.currentTimeMillis();
			for (int i=0; i<numLoops; i++) {}
			nullLoopTime = System.currentTimeMillis() - startTime;
			//IJ.write("loops=" + numLoops + ",  nullLoopTime=" + nullLoopTime);
		} while (nullLoopTime<250);

		IJ.write("");
		IJ.write("Timer: " + numLoops + " iterations (" + nullLoopTime + "ms)");
		Timer2 o = new Timer2();

		// null loop
		startTime = System.currentTimeMillis();
		for (int i=0; i<numLoops; i++) {}
		showTime("null loop");

		// i = o.getJ()
		startTime = System.currentTimeMillis();
		for (int i=0; i<numLoops; i++) {k = o.getJ();}
		showTime("i=o.getJ()");

		// i = o.getJFinal()
		startTime = System.currentTimeMillis();
		for (int i=0; i<numLoops; i++) {k = o.getJFinal();}
		showTime("i=o.getJ() (final)");

		// i = o.getJClass()
		startTime = System.currentTimeMillis();
		for (int i=0; i<numLoops; i++) {k = o.getJClass();}
		showTime("i=o.getJ() (static)");

		// i=o.j
		startTime = System.currentTimeMillis();
		for (int i=0; i<numLoops; i++) {k = o.j;}
		showTime("i=o.j");

		// i=o.jStatic
		startTime = System.currentTimeMillis();
		for (int i=0; i<numLoops; i++) {k = Timer2.k;}
		showTime("i=o.j (static)");

		// i=j
		startTime = System.currentTimeMillis();
		for (int i=0; i<numLoops; i++) {k = j;}
		showTime("i=j");

		// i=a[j]
		startTime = System.currentTimeMillis();
		for (int i=0; i<numLoops; i++) {k = a[j];}
		showTime("i=a[j]");

		/*
		long startTime = System.currentTimeMillis();
		for (int i=0; i<=numLoops; i++) {
			IJ.wait(51);
			if (i%50 == 0 )
			IJ.showProgress(i/(double)numLoops);
		}
		long endTime = System.currentTimeMillis();
		IJ.write("  showProgress(): " + (endTime - startTime) + "msecs");

		startTime = System.currentTimeMillis();
		for (int i=0; i<=numLoops; i++) {
			long time = System.currentTimeMillis();
		}
		endTime = System.currentTimeMillis();
		IJ.write("  System.currentTimeMillis(): " + (endTime - startTime) + "msecs");
		*/
	}


	void showTime(String s) {
		long elapsedTime = System.currentTimeMillis() - startTime - nullLoopTime;
		IJ.write("  " + s + ": " + (elapsedTime*1000000)/numLoops + " ns");
	}
	
	
	/*
	void test() {
		//IJ.showMessage("Available for testing");
		//timer();
		//barTest();

		new Main();
	}

	void memoryTest() {
		int i=0;
		MemTest foo=null;
		try {
			while (true) {
				foo=new MemTest(foo,100000);
				i++;
			}
		} catch (OutOfMemoryError e) {
			IJ.log("out of memory at "+i);
		}
	}
	*/

}


	class Timer2 {
		int j=0;
		static int k=0;

		public int getJ() {return j;}
		public final int getJFinal() {return j;}
		public static int getJClass() {return k;}
	}


/*
class MemTest{
	MemTest last; //to keep last one from being garbage collected
	byte[] buf;

	public MemTest(MemTest last, int size) {
		this.last=last;
		buf=new byte[size];
	}
}


class Main extends Frame {
    Main() {
        super("Dialog Example");
        add("West", new Button("Modal"));
        add("East", new Button("Modeless"));
        pack();
        show();
    }
    
    public boolean action(Event evt, Object what) {
        if ("Modal".equals(what)) {
            new MainDialog(this, true);
            return true;
        } else if ("Modeless".equals(what)) {
            new MainDialog(this, false);
            return true;
        }
        return false;
    }

    static public void main(String[] args) {
        new Main();
    }
}


class MainDialog extends Dialog {
    // These two integers hold the location of the last window.
    // New windows are created at an offset to the previous one.
    static int offsetX, offsetY;

    MainDialog(Frame frame, boolean modal) {
        super(frame, modal);
        setTitle(isModal() ? "Modal" : "Modeless");
        add("Center", new Button("Quit"));
        offsetX += 20;
        offsetY += 20;
        setLocation(offsetX, offsetY);
        pack();
        show();
    }

    public boolean action(Event evt, Object what) {
        if ("Quit".equals(what)) {
            dispose();
            return true;
        }
        return false;
    }
}
*/


