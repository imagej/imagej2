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

package imagej.util;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import org.junit.Test;

/**
 * Unit tests for {@link Prefs}.
 * 
 * @author Curtis Rueden
 * @author Grant Harris
 */
public class PrefsTest {

	@Test
	public void testMap() {
		final Preferences prefs = Preferences.userNodeForPackage(String.class);
		final Map<String, String> map = new HashMap<String, String>();
		map.put("0", "A");
		map.put("1", "B");
		map.put("2", "C");
		map.put("3", "D");
		map.put("5", "f");
		final String mapKey = "MapKey";
		Prefs.putMap(prefs, map, mapKey);
		final Map<String, String> result = Prefs.getMap(prefs, mapKey);
		assertEquals(map, result);
	}

	@Test
	public void testList() {
		final Preferences prefs = Preferences.userNodeForPackage(String.class);
		final String recentFilesKey = "RecentFiles";
		final List<String> recentFiles = new ArrayList<String>();
		recentFiles.add("some/path1");
		recentFiles.add("some/path2");
		recentFiles.add("some/path3");
		Prefs.putList(prefs, recentFiles, recentFilesKey);
		final List<String> result = Prefs.getList(prefs, recentFilesKey);
		assertEquals(recentFiles, result);
	}

}
