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

import java.lang.reflect.Array;
import java.lang.reflect.Field;

/**
 * Diagnostics Utilities 
 * 
 * @author GBH
 */
public class Diagnostics {

	/**
	 * Dumps the field values for an object, including arrays.
	 * Based on ideas from : http://www.techrepublic.com/html/tr/sidebars/1050855-3.html,
	 * http://www.techrepublic.com/article/put-the-java-reflection-api-to-work-in-your-apps/1050855
	 */
	
	public static String dump(Object o) {
		StringBuffer buffer = new StringBuffer();
		Class<?> oClass = o.getClass();
		if (oClass.isArray()) {
			buffer.append("[");
			for (int i = 0; i < Array.getLength(o); i++) {
				if (i > 0) {
					buffer.append(",");
				}
				Object value = Array.get(o, i);
				buffer.append(value.getClass().isArray() ? dump(value) : value);
			}
			buffer.append("]");
		} else {
			buffer.append("{ ");
			while (oClass != null) {
				Field[] fields = oClass.getDeclaredFields();
				for (int i = 0; i < fields.length; i++) {
					if (buffer.length() > 1) {
						buffer.append(", \n");
					}
					fields[i].setAccessible(true);
					buffer.append(fields[i].getName());
					buffer.append(": ");
					try {
						Object value = fields[i].get(o);
						if (value != null) {
							buffer.append(value.getClass().isArray() ? dump(value) : value);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				oClass = oClass.getSuperclass();
			}
			buffer.append("\n}");
		}
		return buffer.toString();
	}

}
