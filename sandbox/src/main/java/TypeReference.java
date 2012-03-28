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

import java.lang.reflect.Type;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Courtesy of Neil Gafter's blog.
 * Thanks to Curt Cox for the pointer.
 */
public abstract class TypeReference<T> {

 private final Type type;
 private volatile Constructor<?> constructor;

 protected TypeReference() {
     Type superclass = getClass().getGenericSuperclass();
     if (superclass instanceof Class) {
         throw new RuntimeException("Missing type parameter.");
     }
     this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
 }

 /**
  * @return a new instance of {@code T} using the default, no-arg
  * constructor.
  * @throws IllegalAccessException on security reflection issues
  * @throws NoSuchMethodException there's not getRawType on the type
  * @throws java.lang.reflect.InvocationTargetException if a reflective call causes an exception in the underlying instance
  * @throws InstantiationException if the instance cannot be instantiated
  */
 @SuppressWarnings("unchecked")
 public T newInstance()
         throws NoSuchMethodException, IllegalAccessException,
         InvocationTargetException, InstantiationException {
     if (constructor == null) {
         Class<?> rawType = type instanceof Class<?>
             ? (Class<?>) type
             : (Class<?>) ((ParameterizedType) type).getRawType();
         constructor = rawType.getConstructor();
     }
     return (T) constructor.newInstance();
 }

 /**
  * @return the referenced type.
  */
 public Type getType() {
     return this.type;
 }
 
     public static void main(String[] args) throws Exception {
        List<String> l1 = new TypeReference<ArrayList<String>>() {}.newInstance();
        List l2 = new TypeReference<ArrayList>() {}.newInstance();
    }
}
