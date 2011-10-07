//
// ByteCreator.java
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

package ij;

public class ByteCreator {

	// helper - create a list of bytes in ascending order
	public static byte[] ascending(int count)
	{
		byte[] output = new byte[count];
		for (int i = 0; i < count; i++)
			output[i] = (byte)i;
		return output;
	}
	
	// helper - create a list of bytes in descending order
	public static byte[] descending(int count)
	{
		byte[] output = new byte[count];
		for (int i = 0; i < count; i++)
			output[i] = (byte)(count-i-1);
		return output;
	}
	
	// helper - create a list of repeated bytes
	public static byte[] repeated(int count, int value)
	{
		byte[] output = new byte[count];
		for (int i = 0; i < count; i++)
			output[i] = (byte)value;
		return output;
	}
	
	// helper - create a list of random bytes from 0 .. range-1
	public static byte[] random(int count, int range)
	{
		byte[] output = new byte[count];
		for (int i = 0; i < count; i++)
			output[i] = (byte)Math.floor(range*Math.random());
		return output;
	}
	}
