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

package imagej.updater.core;

import java.util.HashMap;
import java.util.List;

import org.scijava.log.LogService;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.plugin.PluginService;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 * Default service for managing available ImageJ upload mechanisms.
 * 
 * @author Johannes Schindelin
 * @author Curtis Rueden
 */
@Plugin(type = Service.class)
public class DefaultUploaderService extends AbstractService implements
	UploaderService
{

	@Parameter
	private LogService log;

	@Parameter
	private PluginService pluginService;

	private HashMap<String, Uploader> uploaderMap;

	// -- UploaderService methods --

	@Override
	public boolean hasUploader(final String protocol) {
		return uploaderMap.containsKey(protocol);
	}

	@Override
	public Uploader getUploader(String protocol)
		throws IllegalArgumentException
	{
		final Uploader uploader = uploaderMap.get(protocol);
		if (uploader == null) {
			throw new IllegalArgumentException("No uploader found for protocol " +
				protocol);
		}
		return uploader;
	}

	// -- Service methods --

	@Override
	public void initialize() {
		// ask the plugin service for the list of available upload mechanisms
		uploaderMap = new HashMap<String, Uploader>();
		final List<? extends Uploader> uploaders =
			pluginService.createInstancesOfType(Uploader.class);
		for (final Uploader uploader : uploaders) {
			uploaderMap.put(uploader.getProtocol(), uploader);
		}
		log.info("Found " + uploaderMap.size() + " upload mechanisms.");
	}

}
