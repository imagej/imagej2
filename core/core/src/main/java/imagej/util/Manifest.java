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

package imagej.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.jar.JarFile;

/**
 * Helper class for working with JAR manifests.
 * 
 * @author Curtis Rueden
 */
public class Manifest {

	/** The JAR manifest backing this object. */
	private final java.util.jar.Manifest manifest;

	/** Creates a new instance wrapping the given JAR manifest. */
	private Manifest(final java.util.jar.Manifest manifest) {
		this.manifest = manifest;
	}

	// -- Manifest methods --

	public String getArchiverVersion() {
		return get("Archiver-Version");
	}

	public String getBuildJdk() {
		return get("Build-Jdk");
	}

	public String getBuiltBy() {
		return get("Built-By");
	}

	public String getCreatedBy() {
		return get("Created-By");
	}

	public String getImplementationBuild() {
		return get("Implementation-Build");
	}

	public String getImplementationDate() {
		return get("Implementation-Date");
	}

	public String getImplementationTitle() {
		return get("Implementation-Title");
	}

	public String getImplementationVendor() {
		return get("Implementation-Vendor");
	}

	public String getImplementationVendorId() {
		return get("Implementation-Vendor-Id");
	}

	public String getImplementationVersion() {
		return get("Implementation-Version");
	}

	public String getManifestVersion() {
		return get("Manifest-Version");
	}

	public String getPackage() {
		return get("Package");
	}

	public String getSpecificationTitle() {
		return get("Specification-Title");
	}

	public String getSpecificationVendor() {
		return get("Specification-Vendor");
	}

	public String getSpecificationVersion() {
		return get("Specification-Version");
	}

	public String get(final String key) {
		return manifest.getMainAttributes().getValue(key);
	}

	public Map<Object, Object> getAll() {
		return Collections.unmodifiableMap(manifest.getMainAttributes());
	}

	// -- Utility methods --

	/** Gets the JAR manifest associated with the given class. */
	public static Manifest getManifest(final Class<?> c) {
		try {
			// try to grab manifest from the JAR
			final URL location = new URL("jar:" + ClassUtils.getLocation(c) + "!/");
			return new Manifest(((JarURLConnection)location.openConnection()).getManifest());
		}
		catch (final IOException e) {
			return null;
		}
	}

}
