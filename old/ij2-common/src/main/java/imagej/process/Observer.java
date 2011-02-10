package imagej.process;

/** the Observer interface is designed for use by classes that observe processes. It has more fine grained control
 * than java.util.Observer. When a process is initiated it calls init() on its observers. At each change it calls
 * update() on its observers. Finally when the process is done it calls done() on its observers. Users of this
 * interface include the imagej.process.operation classes and the ProgressTracker class. 
 */
public interface Observer
{
	void init();
	void update();
	void done();
}
