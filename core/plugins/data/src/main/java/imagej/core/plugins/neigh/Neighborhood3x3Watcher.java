//
// Neighborhood3x3Watcher.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.core.plugins.neigh;

/**
 * This interface is implemented by classes who want to do a 3x3 neighborhood
 * operation of some sort.
 * 
 * @author Barry DeZonia
 */
public interface Neighborhood3x3Watcher {

	/**
	 * Called once before the neighborhood iterations take place to allow
	 * implementer to initialize state.
	 */
	void setup();

	/**
	 * Called once each time a neighborhood is visited for the first time to allow
	 * implementer to initialize local neighborhood state.
	 */
	void initializeNeighborhood(long[] position);

	/**
	 * Called 9 times (3x3), once for each value at a location so implementer can
	 * update state for the local neighborhood.
	 */
	void visitLocation(int dx, int dy, double value);

	/**
	 * Called after neighborhood is completely visited. Allows implementer to
	 * calculate the output value for that neighborhood.
	 */
	double calcOutputValue();

}
