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

package imagej.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * Helper class for working with Maven POMs.
 *
 * @author Curtis Rueden
 */
public class POM extends XML {

	/** Parses a POM from the given file. */
	public POM(final File file) throws ParserConfigurationException,
		SAXException, IOException
	{
		super(file);
	}

	/** Parses a POM from the given input stream. */
	public POM(final InputStream in) throws ParserConfigurationException,
		SAXException, IOException
	{
		super(in);
	}

	/** Parses a POM from the given string. */
	public POM(final String s) throws ParserConfigurationException,
		SAXException, IOException
	{
		super(s);
	}

	// -- POM methods --

	/** Gets the POM's groupId. */
	public String getGroupId() {
		final String groupId = cdata("//project/groupId");
		if (groupId != null) return groupId;
		return cdata("//project/parent/groupId");
	}

	/** Gets the POM's artifactId. */
	public String getArtifactId() {
		return cdata("//project/artifactId");
	}

	/** Gets the POM's version. */
	public String getVersion() {
		final String version = cdata("//project/version");
		if (version != null) return version;
		return cdata("//project/parent/version");
	}

	// -- Utility methods --

	/**
	 * Gets the Maven POM associated with the given class.
	 *
	 * @param c The class to use as a base when searching for a pom.xml.
	 * @param groupId The Maven groupId of the desired POM.
	 * @param artifactId The Maven artifactId of the desired POM.
	 */
	public static POM getPOM(final Class<?> c, final String groupId,
		final String artifactId)
	{
		try {
			final URL location = ClassUtils.getLocation(c);
			if (!location.getProtocol().equals("file") || location.toString().endsWith(".jar")) {
				// look for pom.xml in JAR's META-INF/maven subdirectory
				final String pomPath =
					"META-INF/maven/" + groupId + "/" + artifactId + "/pom.xml";
                String locationString = location.toString();
                if (!location.getProtocol().equals("jar")) {

                }
                String protocolPrefix = location.getProtocol().equals("jar") ? "" : "jar:";
                String jarPathPrefix = location.toString().endsWith("!/") ? "" : "!/";
                final URL pomURL = new URL(protocolPrefix +
                        location.toString() +
                        jarPathPrefix +
                        pomPath);
				final InputStream pomStream = pomURL.openStream();
				return new POM(pomStream);
			}
			// look for the POM in the class's base directory
			final File file = FileUtils.urlToFile(location);
			final File baseDir = AppUtils.getBaseDirectory(file, null);
			final File pomFile = new File(baseDir, "pom.xml");
			return new POM(pomFile);
		}
		catch (final IOException e) {
			return null;
		}
		catch (final ParserConfigurationException e) {
			return null;
		}
		catch (final SAXException e) {
			return null;
		}
	}

}
