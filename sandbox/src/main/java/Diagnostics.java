/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
