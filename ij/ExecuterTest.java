package ij;

import static org.junit.Assert.*;

import java.awt.event.KeyEvent;

import org.junit.Test;

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
		fail("Not yet implemented");
	}

	@Test
	public void testGetCommand() {
		// already tested above
	}

	@Test
	public void testAddCommandListener() {
		
		FakeListener listener1 = new FakeListener(Behavior.RunOther,"Crop");
		FakeListener listener2 = new FakeListener(Behavior.RunOther,"Clear");
		
		// passing a null command listener now causes a NullPtrExcept later in run()
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			Executer.addCommandListener(null);
		}
		Executer.addCommandListener(listener1);
		Executer.addCommandListener(listener2);

		ex = new Executer("Wojokowski",null);
		
		// give Executer's hatched threads time to terminate
		try {
			Thread.sleep(100000L);
		} catch (Exception e) {
		}
		
		Executer.removeCommandListener(listener1);
		Executer.removeCommandListener(listener2);
	}

	@Test
	public void testRemoveCommandListener() {
		fail("Not yet implemented");
	}

}
