package ij;

import static org.junit.Assert.*;

import java.awt.event.KeyEvent;

import org.junit.Test;

import ij.process.*;
import ij.macro.*;
import ij.measure.*;
import ij.plugin.filter.*;

public class ExecuterTest {

	enum Behavior {RunAsAsked, RunOther, RunNothing};
	
	Executer ex;
	
	private class FakeListener implements CommandListener
	{
		Behavior b;
		String otherCommand;
		
		FakeListener(Behavior b, String otherCommand)
		{
			this.b = b;
			this.otherCommand = otherCommand;
		}
		
		public String commandExecuting(String command)
		{
			if (b == Behavior.RunAsAsked)
			{
				//System.out.println("Listener - Returning command as is: ("+command+")");
				return command;
			}
			else if (b == Behavior.RunOther)
			{
				//System.out.println("Listener - Returning other command: ("+otherCommand+")");
				return otherCommand;
			}
			else  // RunNothing
			{
				//System.out.println("Listener - Returning null command");
				return null;
			}
		}
	}
	
	@Test
	public void testExecuterString() {
		
		// pass in null
		ex = new Executer(null);
		assertNotNull(ex);
		assertNull(Executer.getCommand());
		
		// pass in empty string
		ex = new Executer("");
		assertNotNull(ex);
		assertNull(Executer.getCommand());
		
		// pass in something legit
		ex = new Executer("VisitHookahBarn");
		assertNotNull(ex);
		assertNull(Executer.getCommand());
	}

	@Test
	public void testExecuterStringImagePlus() {
		ImagePlus ignored = null;
		FakeListener listener;

		listener = new FakeListener(Behavior.RunNothing,null);
		Executer.addCommandListener(listener);
		
		// pass in null
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			ex = new Executer(null,ignored);
			assertNotNull(ex);
			assertNull(Executer.getCommand());
		}

		// pass in empty string
		IJ.setKeyDown(KeyEvent.VK_ESCAPE);
		ex = new Executer("",ignored);
		assertNotNull(ex);
		assertEquals("",Executer.getCommand());
		assertFalse(IJ.escapePressed());
		
		// pass in something non null
		IJ.setKeyDown(KeyEvent.VK_ESCAPE);
		ex = new Executer("EatCheeseburger",ignored);
		assertNotNull(ex);
		assertEquals("EatCheeseburger",Executer.getCommand());
		assertFalse(IJ.escapePressed());
		
		// pass in something real w/ repeat
		IJ.setKeyDown(KeyEvent.VK_ESCAPE);
		IJ.setKeyDown(KeyEvent.VK_SHIFT);
		ex = new Executer("RepeatMyself",ignored);
		assertNotNull(ex);
		assertEquals("EatCheeseburger",Executer.getCommand());
		assertFalse(IJ.shiftKeyDown());
		assertFalse(IJ.escapePressed());
		
		// pass in "Undo"
		IJ.setKeyDown(KeyEvent.VK_ESCAPE);
		ex = new Executer("Undo",ignored);
		assertNotNull(ex);
		assertEquals("EatCheeseburger",Executer.getCommand());
		assertFalse(IJ.escapePressed());
		
		// pass in "Close"
		IJ.setKeyDown(KeyEvent.VK_ESCAPE);
		ex = new Executer("Close",ignored);
		assertNotNull(ex);
		assertEquals("EatCheeseburger",Executer.getCommand());
		assertFalse(IJ.escapePressed());

		// pass in something real
		IJ.setKeyDown(KeyEvent.VK_ESCAPE);
		ex = new Executer("Help",ignored);
		assertNotNull(ex);
		assertEquals("Help",Executer.getCommand());
		assertFalse(IJ.escapePressed());
		
		// give Executer's hatched threads time to terminate
		try {
			Thread.sleep(1000L);
		} catch (Exception e) {
		}
		
		Executer.removeCommandListener(listener);
	}

	@Test
	public void testRun() {
		// note - can't find a way to test. No images are open and so I can't test results of running some
		//   command. I tried some commands but nothing seems to change any state I can test. This is
		//   primarily because no image is noticed as loaded. The GUI is not running and it keeps track of
		//   images only when they are loaded in a window. Using IJ.openImage() does not fix this. The
		//   multithreaded nature of Executer makes faking out IJ that an image is loaded impossible.
	}

	@Test
	public void testGetCommand() {
		// already tested above
	}

	@Test
	public void testAddCommandListener() {
		// note - somewhat tested in above methods
		// note - Executer's addCommandListener method sets private vars that are only referenced in run().
		//   I've tried to get run() to do things to test this method but it always complains that there is no
		//   Window open. It requires a GUI window to be open (via WindowManager). I tried to fake it out but
		//   Executer's multithreaded implementation gets in the way of the workaround.
		
		/*

		// must init menubar and commands structures in IJ before executer::run() is invoked 
		IJ.init();
		
		//FakeListener listener = new FakeListener(Behavior.RunOther,"Crop"); : null ptrExcep with this one
		FakeListener listener = new FakeListener(Behavior.RunOther,"Clear");
		
		// passing a null command listener now causes a NullPtrExcept later in run()
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			Executer.addCommandListener(null);
		}
		
		Executer.addCommandListener(listener);

		ImagePlus ip = IJ.openImage("data/head8bit.tif");

		assertNotEquals(0,ip.getPixel(0,0));
		
		// this doesn't work - currTempImage tied to this thread. run() hatches its own thread past here
		WindowManager.setTempCurrentImage(ip);
		
		ex = new Executer("AnyOldThing",null);  // should run the listener activated commands
		
		// give Executer's hatched threads time to terminate
		try {
			Thread.sleep(4000L);
		} catch (Exception e) {
		}
		
		// test that image got Cleared
		assertEquals(0,ip.getPixel(0,0));
		
		Executer.removeCommandListener(listener);

		 */
	}

	@Test
	public void testRemoveCommandListener() {
		// note - can't test. See previous method's explanation. I'm invoking it above but no way to test if it
		//   actually works.
	}

}
