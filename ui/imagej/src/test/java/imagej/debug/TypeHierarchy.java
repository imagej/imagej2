//
// TypeHierarchy.java
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

package imagej.debug;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Dumps the type hierarchy of the classes given as arguments.
 * 
 * @author Curtis Rueden
 */
public class TypeHierarchy {

	private final HashMap<Class<?>, TypeNode> classes;

	public TypeHierarchy(final String[] classNames) {
		classes = loadClasses(classNames);
		parseRelationships();
	}

	public void printTree() {
		for (final TypeNode node : classes.values()) {
			if (node.isRoot()) System.out.println(node);
		}
	}

	public static void main(String[] args) {
		final TypeHierarchy typeHierarchy = new TypeHierarchy(args);
		typeHierarchy.printTree();
	}

	// -- Helper methods --

	private HashMap<Class<?>, TypeNode> loadClasses(final String[] classNames) {
		final HashMap<Class<?>, TypeNode> list = new HashMap<Class<?>, TypeNode>();
		for (final String className : classNames) {
			try {
				final Class<?> c = Class.forName(className);
				list.put(c, new TypeNode(c));
			}
			catch (ClassNotFoundException exc) {
				System.err.println("Ignoring invalid class: " + className);
			}
		}
		return list;
	}

	private void parseRelationships() {
		for (final TypeNode node : classes.values()) {
			parseAncestors(null, node.getClassObject());
		}
	}

	private void parseAncestors(final TypeNode child, final Class<?> c) {
		if (c == null) return;
		final TypeNode node = classes.get(c);
		if (node == null) {
			if (c != Object.class) {
				System.err.println("Ignoring irrelevant class: " + c.getName());
			}
			return;
		}
		if (child != null) node.addChild(child);
		parseAncestors(node, c.getSuperclass());
		for (Class<?> iface : c.getInterfaces()) {
			parseAncestors(node, iface);
		}
	}

	// -- Helper classes --

	public class TypeNode {

		private final Class<?> c;
		private final HashSet<TypeNode> children = new HashSet<TypeNode>();
		private final HashSet<TypeNode> parents = new HashSet<TypeNode>();

		public TypeNode(final Class<?> c) {
			this.c = c;
		}

		public Class<?> getClassObject() {
			return c;
		}

		public void addChild(final TypeNode node) {
			children.add(node);
			node.parents.add(this);
		}

		public boolean isRoot() {
			return parents.isEmpty();
		}

		public boolean isLeaf() {
			return children.isEmpty();
		}

		@Override
		public String toString() {
			return toString(0);
		}

		private String toString(final int indent) {
			final StringBuilder sb = new StringBuilder();
			for (int i = 0; i < indent; i++) {
				sb.append(' ');
			}
			sb.append(c.getName());
			sb.append("\n");
			for (final TypeNode child : children) {
				sb.append(child.toString(indent + 2));
			}
			return sb.toString();
		}

	}

}
