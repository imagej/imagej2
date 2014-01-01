/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2014 Board of Regents of the University of
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

import imagej.updater.util.Util;

/**
 * Update sites for the updater.
 * 
 * <p>
 * An update site is a set of files served by a web server that conforms to a
 * certain set of rules defined by the ImageJ updater: a <i>db.xml.gz</i> file
 * in the root directory contains detailed information about available file
 * versions, past file versions, descriptions, authors, links, etc.
 * 
 * @author Johannes Schindelin
 */
public class UpdateSite implements Cloneable, Comparable<UpdateSite> {

	boolean active;
	private String name;
	private String url;
	private String host;
	private String uploadDirectory;
	private String description;
	private String maintainer;
	private long timestamp;
	int rank;

	public UpdateSite(final String name, String url, final String sshHost, String uploadDirectory,
		final String description, final String maintainer, final long timestamp)
	{
		setName(name);
		setURL(url);
		setUploadDirectory(uploadDirectory);
		setHost(sshHost);
		setDescription(description);
		setMaintainer(maintainer);
		setTimestamp(timestamp);
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(final boolean active) {
		this.active = active;
	}

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getURL() {
		return url;
	}

	public void setURL(final String url) {
		if (url == null || url.equals("") || url.endsWith("/")) this.url = url;
		else this.url = url + "/";
	}

	public String getHost() {
		return host;
	}

	public void setHost(final String host) {
		this.host = host;
	}

	public String getUploadDirectory() {
		return uploadDirectory;
	}

	public void setUploadDirectory(final String uploadDirectory) {
		if (uploadDirectory == null || uploadDirectory.equals("") || uploadDirectory.endsWith("/")) this.uploadDirectory = uploadDirectory;
		else this.uploadDirectory = uploadDirectory + "/";
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getMaintainer() {
		return maintainer;
	}

	public void setMaintainer(final String maintainer) {
		this.maintainer = maintainer;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getRank() {
		return rank;
	}

	@Override
	public Object clone() {
		final UpdateSite clone = new UpdateSite(name, url, host, uploadDirectory, description, maintainer, timestamp);
		clone.setActive(isActive());
		return clone;
	}

	public boolean isLastModified(final long lastModified) {
		return timestamp == Long.parseLong(Util.timestamp(lastModified));
	}

	public void setLastModified(final long lastModified) {
		timestamp = Long.parseLong(Util.timestamp(lastModified));
	}

	public boolean isUploadable() {
		return (uploadDirectory != null && !uploadDirectory.equals("")) ||
				(host != null && host.indexOf(':') > 0);
	}

	@Override
	public String toString() {
		return url + (host != null ? ", " + host : "") +
			(uploadDirectory != null ? ", " + uploadDirectory : "");
	}

	@Override
	public int compareTo(UpdateSite other) {
		return rank - other.rank;
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof UpdateSite)
			return rank == ((UpdateSite)other).rank;
		return false;
	}

	@Override
	public int hashCode() {
		return rank;
	}

	public String getUploadProtocol() {
		if (host == null)
			throw new RuntimeException("Missing upload information for site " + url);
		final int at = host.indexOf('@');
		final int colon = host.indexOf(':');
		if (colon > 0 && (at < 0 || colon < at)) return host.substring(0, colon);
		return "ssh";
	}
}
