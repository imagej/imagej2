/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import imagej.Context;
import imagej.command.DefaultCommandService;
import imagej.event.DefaultEventService;
import imagej.log.StderrLogService;
import imagej.module.DefaultModuleService;
import imagej.platform.DefaultAppService;
import imagej.platform.DefaultPlatformService;
import imagej.platform.PlatformService;
import imagej.plugin.DefaultPluginService;
import imagej.thread.DefaultThreadService;

import java.util.List;

import org.junit.Test;

/**
 * Tests {@link ServiceIndex}.
 * 
 * @author Curtis Rueden
 */
public class ServiceIndexTest {

	@Test
	public void testGetAll() {
		final Context context = new Context(PlatformService.class);
		final ServiceIndex serviceIndex = context.getServiceIndex();
		final List<Service> all = serviceIndex.getAll();
		assertEquals(8, all.size());
		assertSame(DefaultEventService.class, all.get(0).getClass());
		assertSame(DefaultCommandService.class, all.get(1).getClass());
		assertSame(DefaultModuleService.class, all.get(2).getClass());
		assertSame(DefaultAppService.class, all.get(3).getClass());
		assertSame(DefaultPlatformService.class, all.get(4).getClass());
		assertSame(DefaultPluginService.class, all.get(5).getClass());
		assertSame(DefaultThreadService.class, all.get(6).getClass());
		assertSame(StderrLogService.class, all.get(7).getClass());
	}

}
