/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.command;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import imagej.ImageJ;
import imagej.ValidityProblem;
import imagej.log.LogService;
import imagej.module.Module;
import imagej.plugin.Parameter;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

/**
 * Unit tests for verifying that commands are executed (or not) as expected.
 * 
 * @author Curtis Rueden
 */
public class CommandTest {

	@Test
	public void testInvalidCommand() throws InterruptedException,
		ExecutionException
	{
		final ImageJ context = new ImageJ(CommandService.class, LogService.class);
		final CommandService commandService =
			context.getService(CommandService.class);

		// verify that the Command class fails to run
		{
			final CommandModule command =
				commandService.run(InvalidCommand.class).get();

			assertTrue(command.isCanceled());
			assertEquals("asdf", command.getCancelReason());
		}

		// verify that the CommandInfo is flagged as invalid
		final CommandInfo info = new CommandInfo(InvalidCommand.class);
		assertFalse(info.isValid());
		final List<ValidityProblem> problems = info.getProblems();
		assertEquals(2, problems.size());

		final String problem0 = problems.get(0).getMessage();
		assertEquals("Invalid duplicate parameter: private int "
			+ "imagej.command.CommandTest$InvalidCommand.q", problem0);
		final String problem1 = problems.get(1).getMessage();
		assertEquals("Invalid final parameter: private final float "
			+ "imagej.command.CommandTest$InvalidCommand.x", problem1);

		// verify that the CommandInfo fails to run
		{
			final Module module = commandService.run(info).get();
			assertTrue(module instanceof CommandModule);

			final CommandModule command = (CommandModule) module;

//		assertTrue(command.isCanceled());
//		assertEquals("asdf", command.getCancelReason());
		}

	}

	// -- Helper classes --

	public static class ValidCommand implements Command {

		@Parameter
		private int q;

		@Override
		public void run() {
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * Test plugin for verifying that invalid module parameters are dealt with
	 * using proper error handling.
	 * 
	 * @author Curtis Rueden
	 */
	public static class InvalidCommand extends ValidCommand {

		/**
		 * This parameter is invalid because it shadows a private parameter of a
		 * superclass. Such parameters violate the principle of parameter names as
		 * unique keys.
		 */
		@Parameter
		private int q;

		/**
		 * This parameter is invalid because it is declared {@code final} without
		 * being {@link imagej.module.ItemVisibility#MESSAGE} visibility. Java does
		 * not allow such parameter values to be set by the framework.
		 */
		@Parameter
		private final float x = 0;

		@Override
		public void run() {
			throw new IllegalStateException("Test failed");
		}

	}

}
