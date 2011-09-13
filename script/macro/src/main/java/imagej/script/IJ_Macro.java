//
// IJ_Macro.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2011, ImageJDev.org.
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

package imagej.script;

import imagej.plugin.Plugin;
import imagej.util.Log;

import java.io.Reader;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CodeConverter;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.Modifier;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.MethodCall;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

import bsh.EvalError;
import bsh.Interpreter;

/**
 * A re-implementation of the IJ1 Macro interpreter on top of the Beanshell
 * interpreter For this to work, we need to use Javassist to adapt Beanshell's
 * parser, since Beanshell does not allow for passing variables by reference, as
 * needed e.g. for getDimensions(width, height, channels, slices, frames). The
 * real implementations are in the class MacroFunctions.
 * 
 * @author Johannes Schindelin
 * @see MacroFunctions
 */
@Plugin(type = ScriptLanguage.class)
public class IJ_Macro extends AbstractScriptEngineFactory {

	final static protected Class<Interpreter> interpreterClass;
	final protected static Set<String> functionNames;
	final protected static Map<String, Method> functions;

	static {
		final ClassPool pool = ClassPool.getDefault();

		Class<Interpreter> newClass = null;
		try {
			final CtClass stringClass = pool.get("java.lang.String");
			final CtClass nameSpaceClass = pool.get("bsh.NameSpace");

			CtClass clazz = pool.makeClass("bsh.MacroVariable");
			clazz.addInterface(pool.get("imagej.script.MacroFunctions$Variable"));
			clazz.addField(new CtField(stringClass, "name", clazz));
			clazz.addField(new CtField(nameSpaceClass, "nameSpace", clazz));
			clazz.addConstructor(CtNewConstructor.make(new CtClass[] {
				nameSpaceClass, stringClass }, new CtClass[0],
				"{ nameSpace = $1; name = $2; }", clazz));
			clazz.addMethod(CtNewMethod.make(
				"public String getName() { return name; }", clazz));
			clazz.addMethod(CtNewMethod.make(
				"public Object getValue() { return nameSpace.getVariable(name); }",
				clazz));
			clazz
				.addMethod(CtNewMethod
					.make(
						"public void setValue(Object value) {\n"
							+ "    this.nameSpace.setTypedVariable(this.name, value.getClass(), value, false);\n"
							+ "}", clazz));
			pool.toClass(clazz);

			final CtClass originalMethodInvocation =
				pool.get("bsh.BSHMethodInvocation");
			clazz =
				pool.getAndRename("bsh.BSHMethodInvocation",
					"bsh.IJMacroBSHMethodInvocation");
			clazz.setSuperclass(originalMethodInvocation);
			clazz.addConstructor(CtNewConstructor.make(
				new CtClass[] { originalMethodInvocation }, new CtClass[0], "{\n"
					+ "    super($1.id);\n" + "    parent = $1.parent;\n"
					+ "    children = $1.children;\n"
					+ "    firstToken = $1.firstToken;\n"
					+ "    lastToken = $1.lastToken;\n"
					+ "    sourceFile = $1.sourceFile;\n" + "}", clazz));
			CtMethod method =
				clazz.getMethod("eval",
					"(Lbsh/CallStack;Lbsh/Interpreter;)Ljava/lang/Object;");
			method
				.insertBefore("bsh.NameSpace nameSpace = $1.top();\n"
					+ "bsh.BSHAmbiguousName nameNode = (bsh.BSHAmbiguousName)jjtGetChild(0);\n"
					+ "String functionName = nameNode.getName(nameSpace).toString();\n"
					+ "if (imagej.script.IJ_Macro.handlesFunction(functionName)) {\n"
					+ "    final bsh.BSHArguments arguments = (bsh.BSHArguments)jjtGetChild(1);"
					+ "    final Object[] parameters = new Object[arguments.jjtGetNumChildren()];\n"
					+ "    final java.lang.reflect.Method function =\n"
					+ "        imagej.script.IJ_Macro.getFunction(functionName, parameters.length);\n"
					+ "    final Class[] types = function.getParameterTypes();"
					+ "    for (int i = 0; i < types.length; i++) {\n"
					+ "        if (types[i].getName().equals(\"imagej.script.MacroFunctions$Variable\")) {\n"
					+ "	           final String name = ((bsh.SimpleNode)arguments.jjtGetChild(i)).firstToken.image;\n"
					+ "            final bsh.MacroVariable variable = new bsh.MacroVariable(nameSpace, name);\n"
					+ "            parameters[i] = variable;\n"
					+ "        } else {\n"
					+ "            Object value = ((bsh.SimpleNode)arguments.jjtGetChild(i)).eval($1, $2);\n"
					+ "            parameters[i] = bsh.Types.castObject(value, types[i], 0 /* ASSIGNMENT */);"
					+ "            parameters[i] = bsh.Primitive.unwrap(parameters[i]);\n"
					+ "        }\n"
					+ "    }\n"
					+ "    try {\n"
					+ "        return function.invoke(null, parameters);\n"
					+ "    } catch (java.lang.reflect.InvocationTargetException e) {\n"
					+ "        throw new bsh.EvalError(e.getTargetException().getMessage(), $0, $1);\n"
					+ "    }\n" + "}");
			pool.toClass(clazz);

			final CtClass originalInterpreterClass = pool.get("bsh.Interpreter");
			clazz =
				pool.makeClass("bsh.IJMacroInterpreter", originalInterpreterClass);
			clazz.addConstructor(CtNewConstructor.make(
				"public IJMacroInterpreter() {\n" + "    super();\n" + "}", clazz));
			clazz
				.addConstructor(CtNewConstructor
					.make(
						"public IJMacroInterpreter(java.io.Reader in,\n"
							+ "        java.io.PrintStream out, java.io.PrintStream err, boolean interactive,\n"
							+ "        bsh.NameSpace nameSpace, bsh.IJMacroInterpreter parent, String sourceFileInfo) {\n"
							+ "    super(in, out, err, interactive, nameSpace, parent, sourceFileInfo);\n"
							+ "}", clazz));

			final CtMethod ijReplace =
				CtNewMethod
					.make(
						"protected void ijReplace(bsh.SimpleNode node) {\n"
							+ "    if (node == null || node.children == null) return;\n"
							+ "    for (int i = 0; i < node.children.length; i++) {\n"
							+ "        if (node.children[i].getClass() == bsh.BSHMethodInvocation.class) {\n"
							+ "            node.children[i] =\n"
							+ "                new bsh.IJMacroBSHMethodInvocation((bsh.BSHMethodInvocation)node.children[i]);\n"
							+ "        }\n"
							+ "        if (node.children[i] instanceof bsh.SimpleNode) {\n"
							+ "            ijReplace((bsh.SimpleNode)node.children[i]);\n"
							+ "        }\n" + "     }\n" + "}", clazz);
			clazz.addMethod(ijReplace);
			clazz
				.addMethod(CtNewMethod
					.make(
						"public boolean Line() throws bsh.ParseException {\n"
							+ "    boolean eof = parser.Line();\n"
							+ "    if (parser != null && parser.jjtree != null && parser.jjtree.nodeArity() > 0) {\n"
							+ "        ijReplace((bsh.SimpleNode)(parser.jjtree.rootNode()));\n"
							+ "    }\n" + "    return eof;\n" + "}", clazz));
			clazz.addMethod(CtNewMethod.make(
				"public bsh.JJTParserState get_jjtree() {\n"
					+ "    return parser.jjtree;\n" + "}", clazz));

			final CtMethod originalEval =
				originalInterpreterClass
					.getMethod("eval",
						"(Ljava/io/Reader;Lbsh/NameSpace;Ljava/lang/String;)Ljava/lang/Object;");
			method = CtNewMethod.copy(originalEval, clazz, null);
			method.instrument(new ExprEditor() {

				@Override
				public void edit(final ConstructorCall call) {
					if (call.getClassName().equals("bsh.Interpreter")) {
						try {
							call.replace("$_ = new bsh.IJMacroInterpreter($1, $2, $3);");
						}
						catch (final CannotCompileException e) {
							e.printStackTrace();
						}
					}
				}

				@Override
				public void edit(final MethodCall call) {
					if (call.getMethodName().equals("eval")) {
						try {
							// otherwise it tries to find eval(CallStack,
							// IJMacroInterpreter)
							call.replace("$_ = $0.eval($1, (bsh.Interpreter)$2);");
						}
						catch (final CannotCompileException e) {
							e.printStackTrace();
						}
					}
				}

				@Override
				public void edit(final FieldAccess access) {
					if (access.getFieldName().equals("showResults")) {
						try {
							access.replace("$_ = $0.getShowResults();");
						}
						catch (final CannotCompileException e) {
							e.printStackTrace();
						}
					}
				}

			});

			/*
			 * Javassist is a little bit overzealous and replaces Interpreter
			 * with IJMacroInterpreter in a call to SimpleNode's eval() method
			 */
			final CodeConverter converter = new CodeConverter();
			converter.redirectMethodCall("eval", pool.get("bsh.SimpleNode")
				.getMethod("eval",
					"(Lbsh/CallStack;Lbsh/Interpreter;)Ljava/lang/Object;"));
			method.instrument(converter);
			clazz.addMethod(method);

			newClass = toClass(pool, clazz);
		}
		catch (final Exception e) {
			Log.error("Got exception while initializing IJ Macro interpreter class");
			e.printStackTrace();
		}
		interpreterClass = newClass;

		functionNames = new HashSet<String>();
		functions = new HashMap<String, Method>();
		for (final Method method : MacroFunctions.class.getMethods()) {
			if ((method.getModifiers() & Modifier.STATIC) != 0) {
				final String key =
					method.getName() + ";" + method.getParameterTypes().length;
				if (functions.containsKey(key)) {
					throw new IllegalArgumentException(
						"Duplicate function implementations: " + key);
				}
				functionNames.add(method.getName());
				functions.put(key, method);
			}
		}
	}

	/*
	 * A simple wrapper of Javassist's toClass() method to work around a warning
	 * stemming from Javassist's non-use of generics.
	 */
	@SuppressWarnings("unchecked")
	private static Class<Interpreter> toClass(final ClassPool pool,
		final CtClass clazz) throws CannotCompileException
	{
		return pool.toClass(clazz);
	}

	@Override
	public final List<String> getExtensions() {
		return Arrays.asList("ijm", "txt");
	}

	@Override
	public ScriptEngine getScriptEngine() {
		final Interpreter interpreter;
		try {
			interpreter = interpreterClass.newInstance();
		}
		catch (final InstantiationException e) {
			Log.error(e);
			throw new RuntimeException("Could not instantiate the macro interpreter");
		}
		catch (final IllegalAccessException e) {
			Log.error(e);
			throw new RuntimeException("Access error");
		}
		return new AbstractScriptEngine() {

			@Override
			public Object eval(final String script) throws ScriptException {
				try {
					return interpreter.eval(script);
				}
				catch (final EvalError e) {
					throw new ScriptException(e);
				}
			}

			@Override
			public Object eval(final Reader reader) throws ScriptException {
				try {
					return interpreter.eval(reader);
				}
				catch (final EvalError e) {
					throw new ScriptException(e);
				}
			}
		};
	}

	public static boolean handlesFunction(final String name) {
		return functionNames.contains(name);
	}

	public static Method getFunction(final String functionName,
		final int parameterCount)
	{
		return functions.get(functionName + ";" + parameterCount);
	}

}
