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
 * #L%
 */

package imagej.data.display;

import imagej.data.overlay.Overlay;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Stores data that is used by various OverlayManager implementations. There
 * is one {@link OverlayInfoList} per {@link OverlayService}.
 * 
 * @author Barry DeZonia
 */
public class OverlayInfoList {

	private final LinkedList<OverlayInfo> list;
	
	public OverlayInfoList() {
		list = new LinkedList<OverlayInfo>();
	}

	public int getOverlayInfoCount() {
		return list.size();
	}
	
	public OverlayInfo getOverlayInfo(int i) {
		return list.get(i);
	}
	
	public boolean addOverlayInfo(int i, OverlayInfo info) {
		if (findIndex(info) >= 0) return false;
		list.add(i, info);
		//info.overlay.incrementReferences();
		return true;
	}
	
	public boolean addOverlayInfo(OverlayInfo info) {
		int last = list.size();
		return addOverlayInfo(last,info);
	}
	
	public boolean addOverlay(int i, Overlay overlay) {
		if (findIndex(overlay) >= 0) return false;
		final OverlayInfo info = new OverlayInfo(overlay);
		return addOverlayInfo(i,info);
	}

	public boolean addOverlay(Overlay overlay) {
		int last = list.size();
		return addOverlay(last, overlay);
	}
	
	public boolean replaceOverlayInfo(int i, OverlayInfo info) {
		if (deleteOverlayInfo(i))
			return addOverlayInfo(i, info);
		return false;
	}
	
	public boolean replaceOverlay(int i, Overlay overlay) {
		final OverlayInfo info = new OverlayInfo(overlay);
		return replaceOverlayInfo(i, info);
	}
	
	public boolean deleteOverlayInfo(int i) {
		final OverlayInfo info = list.remove(i);
		if (info == null) return false;
		//info.overlay.decrementReferences();
		return true;
	}
	
	public boolean deleteOverlayInfo(OverlayInfo info) {
		final int index = findIndex(info);
		if (index < 0) return false;
		return deleteOverlayInfo(index);
	}

	public boolean deleteOverlay(Overlay overlay) {
		final int index = findIndex(overlay);
		if (index < 0) return false;
		return deleteOverlayInfo(index);
	}

	public void deleteAll() {
		final int num = list.size();
		for (int i = 0; i < num; i++)
			deleteOverlayInfo(0);
	}
	
	public int findIndex(OverlayInfo info) {
		for (int i = 0; i < list.size(); i++)
			if (info == list.get(i))
				return i;
		return -1;
	}
	
	public int findIndex(Overlay overlay) {
		for (int i = 0; i < list.size(); i++)
			if (overlay == list.get(i).getOverlay())
				return i;
		return -1;
	}

	public void sort() {
		Collections.sort(list, new Comparator<OverlayInfo>() {
			@Override
			public int compare(OverlayInfo arg0, OverlayInfo arg1) {
				return arg0.toString().compareTo(arg1.toString());
			}});
	}
	
	public int[] selectedIndices() {
		int selCount = 0;
		for (int i = 0; i < getOverlayInfoCount(); i++) {
			if (getOverlayInfo(i).isSelected()) selCount++;
		}
		int[] selectedIndices = new int[selCount];
		int index = 0;
		for (int i = 0; i < getOverlayInfoCount(); i++) {
			if (getOverlayInfo(i).isSelected()) selectedIndices[index++] = i;
		}
		return selectedIndices;
	}
	
	public void deselectAll() {
		for (int i = 0; i < getOverlayInfoCount(); i++) {
			getOverlayInfo(i).setSelected(false);
		}
	}
	
	public List<Overlay> selectedOverlays() {
		ArrayList<Overlay> overlays = new ArrayList<Overlay>();
		for (int i = 0; i < getOverlayInfoCount(); i++) {
			OverlayInfo info = getOverlayInfo(i);
			if (info.isSelected())
				overlays.add(info.getOverlay());
		}
		return overlays;
	}
}
