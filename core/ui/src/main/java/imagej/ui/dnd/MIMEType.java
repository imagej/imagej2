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

package imagej.ui.dnd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * A Multipurpose Internet Mail Extension (MIME) type, as defined in RFC 2045
 * and 2046.
 * <p>
 * This class is similar to e.g. {@code java.awt.datatransfer.MimeType} and
 * {@code org.apache.pivot.util.MIMEType}. We reinvent the wheel here since
 * there is no public MIME type class in core Java excluding AWT, which we could
 * use cross-environment in e.g. Android.
 * </p>
 * 
 * @author Curtis Rueden
 */
public class MIMEType {

	/** The parameter name of the MIME type's fully qualified Java class. */
	private static final String CLASS_PARAM = "class";

	/** The base MIME type, without parameters. */
	private final String base;

	/** List of parameter names, in the order they appeared in the MIME string. */
	private final List<String> paramNames;

	/** Table of parameter names and values. */
	private final Map<String, String> params;

	/**
	 * Constructs a new MIME type object from the given MIME type string.
	 * 
	 * @param mimeType The MIME type string, which may optionally include a list
	 *          of semicolon-separated parameters.
	 */
	public MIMEType(final String mimeType) {
		this(mimeType, null);
	}

	/**
	 * Constructs a new MIME type object from the given MIME type string.
	 * 
	 * @param mimeType The MIME type string, which may optionally include a list
	 *          of semicolon-separated parameters.
	 * @param javaType The associated Java class of the MIME type. If non-null, a
	 *          "class" parameter is guaranteed to exist with the MIME type
	 *          indicating compatibility with the given Java class.
	 * @throws IllegalArgumentException if the {@code mimeType} includes a
	 *           different Java class parameter than the {@code javaType}.
	 */
	public MIMEType(final String mimeType, final Class<?> javaType) {
		final StringTokenizer st = new StringTokenizer(mimeType, ";");
		base = st.nextToken().trim();

		// parse parameters
		final ArrayList<String> names = new ArrayList<String>();
		final HashMap<String, String> map = new HashMap<String, String>();
		while (st.hasMoreTokens()) {
			final String param = st.nextToken();
			final int equals = param.indexOf("=");
			if (equals < 0) continue; // ignore invalid parameter
			final String name = param.substring(0, equals).trim();
			final String value = param.substring(equals + 1).trim();
			names.add(name);
			map.put(name, value);
		}

		// ensure Java class (if given) is on the parameter list
		if (javaType != null) {
			final String mimeClassName = map.get(CLASS_PARAM);
			final String javaClassName = javaType.getName();
			if (mimeClassName == null) {
				map.put(CLASS_PARAM, javaClassName);
			}
			else if (!mimeClassName.equals(javaClassName)) {
				throw new IllegalArgumentException("MIME class (" + mimeClassName +
					") and Java class (" + javaClassName + ") do not match");
			}
		}

		paramNames = Collections.unmodifiableList(names);
		params = Collections.unmodifiableMap(map);
	}

	// -- MIMEType methods --

	/** Gets the MIME type with no parameter list. */
	public String getBase() {
		return base;
	}

	/** Gets the value of the parameter with the given name, or null if none. */
	public String getParameter(final String name) {
		return params.get(name);
	}

	/** Gets the parameter names associated with this MIME type. */
	public List<String> getParameters() {
		return paramNames;
	}

	/** Gets whether this MIME type matches the given MIME type string. */
	public boolean isCompatible(final String mimeType) {
		// TODO: This logic may be too simple in some cases. E.g., we probably
		// don't want to require all parameters to match and in the same order.
		return toString().equals(mimeType);
	}

	/** Gets whether this MIME type represents objects of the given Java class. */
	public boolean isCompatible(final Class<?> javaType) {
		return javaType.getName().equals(getParameter(CLASS_PARAM));
	}

	// -- Object methods --

	@Override
	public int hashCode() {
		return toString().hashCode();
	}

	@Override
	public boolean equals(final Object o) {
		return toString().equals(o.toString());
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(getBase());
		for (final String name : getParameters()) {
			sb.append("; ");
			sb.append(name);
			sb.append("=");
			sb.append(getParameter(name));
		}
		return sb.toString();
	}

}
