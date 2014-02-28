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

package imagej.patcher;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import javassist.CannotCompileException;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewConstructor;
import javassist.CtNewMethod;
import javassist.LoaderClassPath;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.InstructionPrinter;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.Opcode;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;
import javassist.expr.Handler;
import javassist.expr.MethodCall;
import javassist.expr.NewExpr;

/**
 * The code hacker provides a mechanism for altering the behavior of classes
 * before they are loaded, for the purpose of injecting new methods and/or
 * altering existing ones.
 * <p>
 * In ImageJ, this mechanism is used to provide new seams into legacy ImageJ1
 * code, so that (e.g.) the modern UI is aware of legacy ImageJ events as they
 * occur.
 * </p>
 * 
 * @author Curtis Rueden
 * @author Rick Lentz
 * @author Johannes Schindelin
 */
public class CodeHacker {

	private final ClassPool pool;
	protected final ClassLoader classLoader;
	private final Set<CtClass> handledClasses = new LinkedHashSet<CtClass>();
	private final boolean onlyLogExceptions;

	public CodeHacker(final ClassLoader classLoader, final ClassPool classPool) {
		this.classLoader = classLoader;
		pool = classPool != null ? classPool : ClassPool.getDefault();
		pool.appendClassPath(new ClassClassPath(getClass()));
		pool.appendClassPath(new LoaderClassPath(classLoader));

		onlyLogExceptions = !stackTraceContains("junit.");
	}

	public CodeHacker(final ClassLoader classLoader) {
		this(classLoader, null);
	}

	private void maybeThrow(final IllegalArgumentException e) {
		if (onlyLogExceptions) e.printStackTrace();
		else throw e;
	}

	/**
	 * Modifies a class by injecting the provided code string at the end of the
	 * specified method's body.
	 * 
	 * @param fullClass Fully qualified name of the class to modify.
	 * @param methodSig Method signature of the method to modify; e.g.,
	 *          "public void updateAndDraw()"
	 * @param newCode The string of code to add; e.g., System.out.println(\"Hello
	 *          World!\");
	 */
	public void insertAtBottomOfMethod(final String fullClass,
		final String methodSig, final String newCode)
	{
		try {
			getBehavior(fullClass, methodSig).insertAfter(newCode);
		}
		catch (final Throwable e) {
			maybeThrow(new IllegalArgumentException("Cannot modify method: " +
				methodSig, e));
		}
	}

	/**
	 * Modifies a class by injecting the provided code string at the start of the
	 * specified method's body.
	 * 
	 * @param fullClass Fully qualified name of the class to override.
	 * @param methodSig Method signature of the method to override; e.g.,
	 *          "public void updateAndDraw()"
	 * @param newCode The string of code to add; e.g., System.out.println(\"Hello
	 *          World!\");
	 */
	public void insertAtTopOfMethod(final String fullClass,
		final String methodSig, final String newCode)
	{
		try {
			final CtBehavior behavior = getBehavior(fullClass, methodSig);
			if (behavior instanceof CtConstructor) {
				((CtConstructor) behavior).insertBeforeBody(newCode);
			}
			else {
				behavior.insertBefore(newCode);
			}
		}
		catch (final Throwable e) {
			maybeThrow(new IllegalArgumentException("Cannot modify method: " +
				methodSig, e));
		}
	}

	/**
	 * Modifies a class by injecting the provided code string as a new method.
	 * 
	 * @param fullClass Fully qualified name of the class to override.
	 * @param methodSig Method signature of the method to override; e.g.,
	 *          "public void updateAndDraw()"
	 * @param newCode The string of code to add; e.g., System.out.println(\"Hello
	 *          World!\");
	 */
	public void insertNewMethod(final String fullClass, final String methodSig,
		final String newCode)
	{
		final CtClass classRef = getClass(fullClass);
		final String methodBody = methodSig + " { " + newCode + " } ";
		try {
			final CtMethod methodRef = CtNewMethod.make(methodBody, classRef);
			classRef.addMethod(methodRef);
		}
		catch (final Throwable e) {
			maybeThrow(new IllegalArgumentException(
				"Cannot add method: " + methodSig, e));
		}
	}

	/**
	 * Works around a bug where the horizontal scroll wheel of the mighty mouse is
	 * mistaken for a popup trigger.
	 */
	public void handleMightyMousePressed(final String fullClass) {
		final ExprEditor editor = new ExprEditor() {

			@Override
			public void edit(final MethodCall call) throws CannotCompileException {
				if (call.getMethodName().equals("isPopupTrigger")) {
					call.replace("$_ = $0.isPopupTrigger() && $0.getButton() != 0;");
				}
			}
		};
		final CtClass classRef = getClass(fullClass);
		for (final String methodName : new String[] { "mousePressed",
			"mouseDragged" })
			try {
				final CtMethod method =
					classRef.getMethod(methodName, "(Ljava/awt/event/MouseEvent;)V");
				method.instrument(editor);
			}
			catch (final NotFoundException e) {
				/* ignore */
			}
			catch (final Throwable e) {
				maybeThrow(new IllegalArgumentException("Cannot instrument method: " +
					methodName, e));
			}
	}

	public void insertPrivateStaticField(final String fullClass,
		final Class<?> clazz, final String name)
	{
		insertStaticField(fullClass, Modifier.PRIVATE, clazz, name, null);
	}

	public void insertPublicStaticField(final String fullClass,
		final Class<?> clazz, final String name, final String initializer)
	{
		insertStaticField(fullClass, Modifier.PUBLIC, clazz, name, initializer);
	}

	public void insertStaticField(final String fullClass, final int modifiers,
		final Class<?> clazz, final String name, final String initializer)
	{
		final CtClass classRef = getClass(fullClass);
		try {
			final CtField field =
				new CtField(pool.get(clazz.getName()), name, classRef);
			field.setModifiers(modifiers | Modifier.STATIC);
			classRef.addField(field);
			if (initializer != null) {
				addToClassInitializer(fullClass, name + " = " + initializer + ";");
			}
		}
		catch (final Throwable e) {
			maybeThrow(new IllegalArgumentException("Cannot add field " + name +
				" to " + fullClass, e));
		}
	}

	public void addToClassInitializer(final String fullClass, final String code) {
		final CtClass classRef = getClass(fullClass);
		try {
			CtConstructor method = classRef.getClassInitializer();
			if (method != null) {
				method.insertAfter(code);
			}
			else {
				method =
					CtNewConstructor.make(new CtClass[0], new CtClass[0], code, classRef);
				method.getMethodInfo().setName("<clinit>");
				method.setModifiers(Modifier.STATIC);
				classRef.addConstructor(method);
			}
		}
		catch (final Throwable e) {
			maybeThrow(new IllegalArgumentException("Cannot add " + code +
				" to class initializer of " + fullClass, e));
		}
	}

	public void addCatch(final String fullClass, final String methodSig,
		final String exceptionClassName, final String src)
	{
		try {
			final CtBehavior method = getBehavior(fullClass, methodSig);
			method.addCatch(src, getClass(exceptionClassName), "$e");
		}
		catch (final Throwable e) {
			maybeThrow(new IllegalArgumentException(
				"Cannot add catch for exception of type'" + exceptionClassName +
					" in " + fullClass + "'s " + methodSig, e));
		}
	}

	public void insertAtTopOfExceptionHandlers(final String fullClass,
		final String methodSig, final String exceptionClassName, final String src)
	{
		try {
			final CtBehavior method = getBehavior(fullClass, methodSig);
			new EagerExprEditor() {

				@Override
				public void edit(final Handler handler) throws CannotCompileException {
					try {
						if (handler.getType().getName().equals(exceptionClassName)) {
							handler.insertBefore(src);
							markEdited();
						}
					}
					catch (final Exception e) {
						e.printStackTrace();
					}
				}
			}.instrument(method);
		}
		catch (final Throwable e) {
			maybeThrow(new IllegalArgumentException(
				"Cannot edit exception handler for type'" + exceptionClassName +
					" in " + fullClass + "'s " + methodSig, e));
		}
	}

	/**
	 * Replaces the application name in the given method in the given parameter to
	 * the given constructor call.
	 * <p>
	 * Fails silently if the specified method does not exist (e.g. CommandFinder's
	 * export() function just went away in 1.47i).
	 * </p>
	 * 
	 * @param fullClass Fully qualified name of the class to override.
	 * @param methodSig Method signature of the method to override; e.g.,
	 *          "public void showMessage(String title, String message)"
	 * @param newClassName the name of the class which is to be constructed by the
	 *          new operator
	 * @param parameterIndex the index of the parameter containing the application
	 *          name
	 * @param replacement the code to use instead of the specified parameter
	 */
	protected void replaceAppNameInNew(final String fullClass,
		final String methodSig, final String newClassName,
		final int parameterIndex, final String replacement)
	{
		try {
			final CtBehavior method = getBehavior(fullClass, methodSig);
			new EagerExprEditor() {

				@Override
				public void edit(final NewExpr expr) throws CannotCompileException {
					if (expr.getClassName().equals(newClassName)) try {
						final CtClass[] parameterTypes =
							expr.getConstructor().getParameterTypes();
						if (parameterTypes[parameterIndex - 1] != CodeHacker.this
							.getClass("java.lang.String"))
						{
							maybeThrow(new IllegalArgumentException("Parameter " +
								parameterIndex + " of " + expr.getConstructor() +
								" is not a String!"));
							return;
						}
						final String replace =
							replaceAppName(parameterIndex, parameterTypes.length, replacement);
						expr.replace("$_ = new " + newClassName + replace + ";");
						markEdited();
					}
					catch (final NotFoundException e) {
						maybeThrow(new IllegalArgumentException(
							"Cannot find the parameters of the constructor of " +
								newClassName, e));
					}
				}
			}.instrument(method);
		}
		catch (final Throwable e) {
			maybeThrow(new IllegalArgumentException("Cannot handle app name in " +
				fullClass + "'s " + methodSig, e));
		}
	}

	/**
	 * Replaces the application name in the given method in the given parameter to
	 * the given method call.
	 * <p>
	 * Fails silently if the specified method does not exist (e.g. CommandFinder's
	 * export() function just went away in 1.47i).
	 * </p>
	 * 
	 * @param fullClass Fully qualified name of the class to override.
	 * @param methodSig Method signature of the method to override; e.g.,
	 *          "public void showMessage(String title, String message)"
	 * @param calledMethodName the name of the method to which the application
	 *          name is passed
	 * @param parameterIndex the index of the parameter containing the application
	 *          name
	 * @param replacement the code to use instead of the specified parameter
	 */
	protected void replaceAppNameInCall(final String fullClass,
		final String methodSig, final String calledMethodName,
		final int parameterIndex, final String replacement)
	{
		try {
			final CtBehavior method = getBehavior(fullClass, methodSig);
			new EagerExprEditor() {

				@Override
				public void edit(final MethodCall call) throws CannotCompileException {
					if (call.getMethodName().equals(calledMethodName)) try {
						final boolean isSuper = call.isSuper();
						final CtClass[] parameterTypes =
							isSuper ? ((ConstructorCall) call).getConstructor()
								.getParameterTypes() : call.getMethod().getParameterTypes();
						if (parameterTypes.length < parameterIndex) {
							maybeThrow(new IllegalArgumentException("Index " +
								parameterIndex + " is outside of " + call.getMethod() +
								"'s parameter list!"));
							return;
						}
						if (parameterTypes[parameterIndex - 1] != CodeHacker.this
							.getClass("java.lang.String"))
						{
							maybeThrow(new IllegalArgumentException("Parameter " +
								parameterIndex + " of " + call.getMethod() +
								" is not a String!"));
							return;
						}
						final String replace =
							replaceAppName(parameterIndex, parameterTypes.length, replacement);
						call.replace((isSuper ? "" : "$0.") + calledMethodName + replace +
							";");
						markEdited();
					}
					catch (final NotFoundException e) {
						maybeThrow(new IllegalArgumentException(
							"Cannot find the parameters of the method " + calledMethodName, e));
					}
				}

				@Override
				public void edit(final ConstructorCall call)
					throws CannotCompileException
				{
					edit((MethodCall) call);
				}
			}.instrument(method);
		}
		catch (final Throwable e) {
			maybeThrow(new IllegalArgumentException("Cannot handle app name in " +
				fullClass + "'s " + methodSig, e));
		}
	}

	private String replaceAppName(final int parameterIndex,
		final int parameterCount, final String replacement)
	{
		final StringBuilder builder = new StringBuilder();
		builder.append("(");
		for (int i = 1; i <= parameterCount; i++) {
			if (i > 1) {
				builder.append(", ");
			}
			builder.append("$").append(i);
			if (i == parameterIndex) {
				builder.append(".replace(\"ImageJ\", " + replacement + ")");
			}
		}
		builder.append(")");
		return builder.toString();
	}

	/**
	 * Patches the bytecode of the given method to not return on a null check.
	 * <p>
	 * This is needed to patch support for alternative editors into ImageJ 1.x.
	 * </p>
	 * 
	 * @param fullClass the class of the method to instrument
	 * @param methodSig the signature of the method to instrument
	 */
	public void dontReturnOnNull(final String fullClass, final String methodSig) {
		final CtBehavior behavior = getBehavior(fullClass, methodSig);
		final MethodInfo info = behavior.getMethodInfo();
		final CodeIterator iterator = info.getCodeAttribute().iterator();
		while (iterator.hasNext())
			try {
				int pos = iterator.next();
				final int c = iterator.byteAt(pos);
				if (c == Opcode.IFNONNULL && iterator.byteAt(pos + 3) == Opcode.RETURN)
				{
					iterator.writeByte(Opcode.POP, pos++);
					iterator.writeByte(Opcode.NOP, pos++);
					iterator.writeByte(Opcode.NOP, pos++);
					iterator.writeByte(Opcode.NOP, pos++);
					return;
				}
			}
			catch (final Throwable e) {
				maybeThrow(new IllegalArgumentException(e));
				return;
			}
		maybeThrow(new IllegalArgumentException("Method " + methodSig + " in " +
			fullClass + " does not return on null"));
	}

	/**
	 * Replaces the given methods with stub methods.
	 * 
	 * @param fullClass the class to patch
	 * @param methodNames the names of the methods to replace
	 */
	public void replaceWithStubMethods(final String fullClass,
		final String... methodNames)
	{
		final CtClass clazz = getClass(fullClass);
		final Set<String> override =
			new HashSet<String>(Arrays.asList(methodNames));
		for (final CtMethod method : clazz.getMethods())
			if (override.contains(method.getName())) try {
				final CtMethod stub = makeStubMethod(clazz, method);
				method.setBody(stub, null);
			}
			catch (final Throwable e) {
				maybeThrow(new IllegalArgumentException("Cannot instrument method: " +
					method.getName(), e));
			}
	}

	/**
	 * Replaces the superclass.
	 * 
	 * @param fullClass
	 * @param fullNewSuperclass
	 */
	public void replaceSuperclass(final String fullClass,
		final String fullNewSuperclass)
	{
		final CtClass clazz = getClass(fullClass);
		try {
			final CtClass originalSuperclass = clazz.getSuperclass();
			clazz.setSuperclass(getClass(fullNewSuperclass));
			for (final CtConstructor ctor : clazz.getConstructors())
				ctor.instrument(new ExprEditor() {

					@Override
					public void edit(final ConstructorCall call)
						throws CannotCompileException
					{
						if (call.getMethodName().equals("super")) call.replace("super();");
					}
				});
			letSuperclassMethodsOverride(clazz);
			addMissingMethods(clazz, originalSuperclass);
		}
		catch (final Throwable e) {
			maybeThrow(new IllegalArgumentException(
				"Could not replace superclass of " + fullClass + " with " +
					fullNewSuperclass, e));
		}
	}

	/**
	 * Replaces all instantiations of a subset of AWT classes with nulls.
	 * <p>
	 * This is used by the partial headless support of legacy code.
	 * </p>
	 */
	public void skipAWTInstantiations(final String fullClass) {
		try {
			skipAWTInstantiations(getClass(fullClass));
		}
		catch (final Throwable e) {
			maybeThrow(new IllegalArgumentException(
				"Could not skip AWT class instantiations in " + fullClass, e));
		}
	}

	/**
	 * Replaces every field write access to the specified field in the specified
	 * method.
	 * 
	 * @param fullClass the full class
	 * @param methodSig the signature of the method to instrument
	 * @param fieldName the field whose write access to override
	 * @param newCode the code to run instead of the field access
	 */
	public void overrideFieldWrite(final String fullClass,
		final String methodSig, final String fieldName, final String newCode)
	{
		try {
			final CtBehavior method = getBehavior(fullClass, methodSig);
			new EagerExprEditor() {

				@Override
				public void edit(final FieldAccess access)
					throws CannotCompileException
				{
					if (access.getFieldName().equals(access)) {
						access.replace(newCode);
						markEdited();
					}
				}
			}.instrument(method);
		}
		catch (final Throwable e) {
			maybeThrow(new IllegalArgumentException(
				"Cannot override field access to " + fieldName + " in " + fullClass +
					"'s " + methodSig, e));
		}
	}

	/**
	 * Replaces a call in the given method.
	 * 
	 * @param fullClass the class of the method to edit
	 * @param methodSig the signature of the method to edit
	 * @param calledClass the class of the called method to replace
	 * @param calledMethodName the name of the called method to replace
	 * @param newCode the code to replace the call with
	 */
	public void replaceCallInMethod(final String fullClass,
		final String methodSig, final String calledClass,
		final String calledMethodName, final String newCode)
	{
		replaceCallInMethod(fullClass, methodSig, calledClass, calledMethodName,
			newCode, -1);
	}

	/**
	 * Replaces a call in the given method.
	 * 
	 * @param fullClass the class of the method to edit
	 * @param methodSig the signature of the method to edit
	 * @param calledClass the class of the called method to replace
	 * @param calledMethodName the name of the called method to replace
	 * @param newCode the code to replace the call with
	 * @param onlyNth if positive, only replace the <i>n</i>th call
	 */
	public void replaceCallInMethod(final String fullClass,
		final String methodSig, final String calledClass,
		final String calledMethodName, final String newCode, final int onlyNth)
	{
		try {
			final CtBehavior method = getBehavior(fullClass, methodSig);
			new EagerExprEditor() {

				private int counter = 0;
				private final boolean debug = false;

				@Override
				public void edit(final MethodCall call) throws CannotCompileException {
					if (debug) {
						System.err.println("editing call " + call.getClassName() + "#" +
							call.getMethodName() + " (wanted " + calledClass + "#" +
							calledMethodName + ")");
					}
					if (call.getMethodName().equals(calledMethodName) &&
						call.getClassName().equals(calledClass))
					{
						if (onlyNth > 0 && ++counter != onlyNth) return;
						call.replace(newCode);
						markEdited();
					}
				}

				@Override
				public void edit(final NewExpr expr) throws CannotCompileException {
					if (debug) {
						System.err.println("editing call " + expr.getClassName() + "#" +
							"<init>" + " (wanted " + calledClass + "#" + calledMethodName +
							")");
					}
					if ("<init>".equals(calledMethodName) &&
						expr.getClassName().equals(calledClass))
					{
						if (onlyNth > 0 && ++counter != onlyNth) return;
						expr.replace(newCode);
						markEdited();
					}
				}
			}.instrument(method);
		}
		catch (final Throwable e) {
			maybeThrow(new IllegalArgumentException("Cannot handle replace call to " +
				calledMethodName + " in " + fullClass + "'s " + methodSig, e));
		}
	}

	/**
	 * Loads the given, possibly modified, class.
	 * <p>
	 * This method must be called to confirm any changes made with
	 * {@link #insertAtBottomOfMethod}, {@link #insertAtTopOfMethod}, or
	 * {@link #insertNewMethod}.
	 * </p>
	 * 
	 * @param fullClass Fully qualified class name to load.
	 * @return the loaded class
	 */
	public Class<?> loadClass(final String fullClass) {
		final CtClass classRef = getClass(fullClass);
		return loadClass(classRef);
	}

	/**
	 * Loads the given, possibly modified, class.
	 * <p>
	 * This method must be called to confirm any changes made with
	 * {@link #insertAtBottomOfMethod}, {@link #insertAtTopOfMethod}, or
	 * {@link #insertNewMethod}.
	 * </p>
	 * 
	 * @param classRef class to load.
	 * @return the loaded class
	 */
	public Class<?> loadClass(final CtClass classRef) {
		try {
			return classRef.toClass(classLoader, null);
		}
		catch (final CannotCompileException e) {
			// Cannot use LogService; it will not be initialized by the time the
			// DefaultLegacyService
			// class is loaded, which is when the CodeHacker is run
			if (e.getCause() != null && e.getCause() instanceof LinkageError) {
				throw javaAgentHint("Cannot load class: " + classRef.getName(), e.getCause());
			}
			System.err.println("Warning: Cannot load class: " + classRef.getName() +
				" into " + classLoader);
			e.printStackTrace();
			return null;
		}
		finally {
			classRef.freeze();
		}
	}

	static RuntimeException javaAgentHint(final String message, final Throwable cause)
	{
		final URL url = Utils.getLocation(JavaAgent.class);
		final String path =
			url != null && "file".equals(url.getProtocol()) &&
				url.getPath().endsWith(".jar") ? url.getPath()
				: "/path/to/ij-legacy.jar";

		return new RuntimeException(message + "\n" +
				"It appears that this class was already defined in the class loader!\n" +
				"Please make sure that you initialize the LegacyService before using\n" +
				"any ImageJ 1.x class. You can do that by adding this static initializer:\n\n" +
				"\tstatic {\n" +
				"\t\tLegacyInjector.preinit();\n" +
				"\t}\n\n" +
				"To debug this issue, start the JVM with the option:\n\n" +
				"\t-javaagent:" +
				path +
				"\n\n" +
				"To enforce pre-initialization, start the JVM with the option:\n\n" +
				"\t-javaagent:" + path + "=init\n", cause);
	}

	public void loadClasses() {
		try {
			JavaAgent.stop();
		}
		catch (final Throwable t) {
			// ignore
		}

		final Iterator<CtClass> iter = handledClasses.iterator();
		while (iter.hasNext()) {
			final CtClass classRef = iter.next();
			if (!classRef.isFrozen() && classRef.isModified()) {
				loadClass(classRef);
			}
			iter.remove();
		}
	}

	/** Gets the Javassist class object corresponding to the given class name. */
	private CtClass getClass(final String fullClass) {
		try {
			final CtClass classRef = pool.get(fullClass);
			if (classRef.getClassPool() == pool) handledClasses.add(classRef);
			return classRef;
		}
		catch (final NotFoundException e) {
			throw new IllegalArgumentException("No such class: " + fullClass, e);
		}
	}

	/**
	 * Gets the method or constructor of the specified class and signature.
	 * 
	 * @param fullClass the class containing the method or constructor
	 * @param methodSig the method (or if the name is <code>&lt;init&gt;</code>,
	 *          the constructor)
	 * @return the method or constructor
	 */
	private CtBehavior
		getBehavior(final String fullClass, final String methodSig)
	{
		if (methodSig.indexOf("<init>") < 0) {
			return getMethod(fullClass, methodSig);
		}
		return getConstructor(fullClass, methodSig);
	}

	/**
	 * Gets the Javassist method object corresponding to the given method
	 * signature of the specified class name.
	 */
	private CtMethod getMethod(final String fullClass, final String methodSig) {
		final CtClass cc = getClass(fullClass);
		final String name = getMethodName(methodSig);
		final String[] argTypes = getMethodArgTypes(methodSig);
		final CtClass[] params = new CtClass[argTypes.length];
		for (int i = 0; i < params.length; i++) {
			params[i] = getClass(argTypes[i]);
		}
		try {
			return cc.getDeclaredMethod(name, params);
		}
		catch (final NotFoundException e) {
			throw new IllegalArgumentException("No such method: " + methodSig, e);
		}
	}

	/**
	 * Gets the Javassist constructor object corresponding to the given
	 * constructor signature of the specified class name.
	 */
	private CtConstructor getConstructor(final String fullClass,
		final String constructorSig)
	{
		final CtClass cc = getClass(fullClass);
		final String[] argTypes = getMethodArgTypes(constructorSig);
		final CtClass[] params = new CtClass[argTypes.length];
		for (int i = 0; i < params.length; i++) {
			params[i] = getClass(argTypes[i]);
		}
		try {
			return cc.getDeclaredConstructor(params);
		}
		catch (final NotFoundException e) {
			throw new IllegalArgumentException("No such method: " + constructorSig, e);
		}
	}

	/** Extracts the method name from the given method signature. */
	private String getMethodName(final String methodSig) {
		final int parenIndex = methodSig.indexOf("(");
		final int spaceIndex = methodSig.lastIndexOf(" ", parenIndex);
		return methodSig.substring(spaceIndex + 1, parenIndex);
	}

	private String[] getMethodArgTypes(final String methodSig) {
		final int parenIndex = methodSig.indexOf("(");
		final String methodArgs =
			methodSig.substring(parenIndex + 1, methodSig.length() - 1);
		final String[] args =
			methodArgs.equals("") ? new String[0] : methodArgs.split(",");
		for (int i = 0; i < args.length; i++) {
			args[i] = args[i].trim().split(" ")[0];
		}
		return args;
	}

	/**
	 * Determines whether the specified class has the specified field.
	 * 
	 * @param fullName the class name
	 * @param fieldName the field name
	 * @return whether the field exists
	 */
	public boolean hasField(final String fullName, final String fieldName) {
		final CtClass clazz = getClass(fullName);
		try {
			return clazz.getField(fieldName) != null;
		}
		catch (final Throwable e) {
			return false;
		}
	}

	/**
	 * Determines whether the specified class has the specified method.
	 * 
	 * @param fullClass the class name
	 * @param methodSig the signature of the method
	 * @return whether the class has the specified method
	 */
	public boolean hasMethod(final String fullClass, final String methodSig) {
		try {
			return getBehavior(fullClass, methodSig) != null;
		}
		catch (final Throwable e) {
			return false;
		}
	}

	/**
	 * Determines whether the specified class is known to Javassist.
	 * 
	 * @param fullClass the class name
	 * @return whether the class exists
	 */
	public boolean existsClass(final String fullClass) {
		try {
			return pool.get(fullClass) != null;
		}
		catch (final Throwable e) {
			return false;
		}
	}

	public boolean hasSuperclass(final String fullClass,
		final String fullSuperclass)
	{
		try {
			final CtClass clazz = getClass(fullClass);
			return fullSuperclass.equals(clazz.getSuperclass().getName());
		}
		catch (final Throwable e) {
			return false;
		}
	}

	private static int verboseLevel = 0;

	private static CtMethod makeStubMethod(final CtClass clazz,
		final CtMethod original) throws CannotCompileException, NotFoundException
	{
		// add a stub
		String prefix = "";
		if (verboseLevel > 0) {
			prefix =
				"System.err.println(\"Called " + original.getLongName() + "\\n\"";
			if (verboseLevel > 1) {
				prefix += "+ \"\\t(\" + fiji.Headless.toString($args) + \")\\n\"";
			}
			prefix += ");";
		}

		final CtClass type = original.getReturnType();
		final String body =
			"{" +
				prefix +
				(type == CtClass.voidType ? "" : "return " + defaultReturnValue(type) +
					";") + "}";
		final CtClass[] types = original.getParameterTypes();
		return CtNewMethod.make(type, original.getName(), types, new CtClass[0],
			body, clazz);
	}

	private static String defaultReturnValue(final CtClass type) {
		if (type == CtClass.booleanType) return "false";
		if (type == CtClass.byteType) return "(byte)0";
		if (type == CtClass.charType) return "'\0'";
		if (type == CtClass.doubleType) return "0.0";
		if (type == CtClass.floatType) return "0.0f";
		if (type == CtClass.intType) return "0";
		if (type == CtClass.longType) return "0l";
		if (type == CtClass.shortType) return "(short)0";
		return "null";
	}

	private void addMissingMethods(final CtClass fakeClass,
		final CtClass originalClass) throws CannotCompileException,
		NotFoundException
	{
		if (verboseLevel > 0) {
			System.err.println("adding missing methods from " +
				originalClass.getName() + " to " + fakeClass.getName());
		}
		final Set<String> available = new HashSet<String>();
		for (final CtMethod method : fakeClass.getMethods())
			available.add(stripPackage(method.getLongName()));
		for (final CtMethod original : originalClass.getDeclaredMethods()) {
			if (available.contains(stripPackage(original.getLongName()))) {
				if (verboseLevel > 1) {
					System.err.println("Skipping available method " + original);
				}
				continue;
			}

			final CtMethod method = makeStubMethod(fakeClass, original);
			fakeClass.addMethod(method);
			if (verboseLevel > 1) {
				System.err.println("adding missing method " + method);
			}
		}

		// interfaces
		final Set<CtClass> availableInterfaces = new HashSet<CtClass>();
		for (final CtClass iface : fakeClass.getInterfaces())
			availableInterfaces.add(iface);
		for (final CtClass iface : originalClass.getInterfaces())
			if (!availableInterfaces.contains(iface)) fakeClass.addInterface(iface);

		final CtClass superClass = originalClass.getSuperclass();
		if (superClass != null && !superClass.getName().equals("java.lang.Object"))
		{
			addMissingMethods(fakeClass, superClass);
		}
	}

	private void letSuperclassMethodsOverride(final CtClass clazz)
		throws CannotCompileException, NotFoundException
	{
		for (final CtMethod method : clazz.getSuperclass().getDeclaredMethods()) {
			final CtMethod method2 =
				clazz.getMethod(method.getName(), method.getSignature());
			if (method2.getDeclaringClass().equals(clazz)) {
				// make sure no calls/accesses to GUI components are remaining
				method2.setBody(method, null);
				method2.setName("narf" + method.getName());
			}
		}
	}

	private static String stripPackage(final String className) {
		int lastDot = -1;
		for (int i = 0;; i++) {
			if (i >= className.length()) return className.substring(lastDot + 1);
			final char c = className.charAt(i);
			if (c == '.' || c == '$') lastDot = i;
			else if (c >= 'A' && c <= 'Z') ; // continue
			else if (c >= 'a' && c <= 'z') ; // continue
			else if (i > lastDot + 1 && c >= '0' && c <= '9') ; // continue
			else return className.substring(lastDot + 1);
		}
	}

	private void skipAWTInstantiations(final CtClass clazz)
		throws CannotCompileException
	{
		clazz.instrument(new ExprEditor() {

			@Override
			public void edit(final NewExpr expr) throws CannotCompileException {
				final String name = expr.getClassName();
				if (name.startsWith("java.awt.Menu") ||
					name.equals("java.awt.PopupMenu") ||
					name.startsWith("java.awt.Checkbox") || name.equals("java.awt.Frame"))
				{
					expr.replace("$_ = null;");
				}
				else if (expr.getClassName().equals("ij.gui.StackWindow")) {
					expr.replace("$1.show(); $_ = null;");
				}
			}

			@Override
			public void edit(final MethodCall call) throws CannotCompileException {
				final String className = call.getClassName();
				final String methodName = call.getMethodName();
				if (className.startsWith("java.awt.Menu") ||
					className.equals("java.awt.PopupMenu") ||
					className.startsWith("java.awt.Checkbox")) try {
					final CtClass type = call.getMethod().getReturnType();
					if (type == CtClass.voidType) {
						call.replace("");
					}
					else {
						call.replace("$_ = " + defaultReturnValue(type) + ";");
					}
				}
				catch (final NotFoundException e) {
					e.printStackTrace();
				}
				else if (methodName.equals("put") &&
					className.equals("java.util.Properties"))
				{
					call.replace("if ($1 != null && $2 != null) $_ = $0.put($1, $2);"
						+ "else $_ = null;");
				}
				else if (methodName.equals("get") &&
					className.equals("java.util.Properties"))
				{
					call.replace("$_ = $1 != null ? $0.get($1) : null;");
				}
				else if (className.equals("java.lang.Integer") &&
					methodName.equals("intValue"))
				{
					call.replace("$_ = $0 == null ? 0 : $0.intValue();");
				}
				else if (methodName.equals("addTextListener")) {
					call.replace("");
				}
				else if (methodName.equals("elementAt")) {
					call.replace("$_ = $0 == null ? null : $0.elementAt($$);");
				}
			}
		});
	}

	/**
	 * Augments all <code>startsWith("http://")</code> calls with
	 * <code>|| startsWith("https://")</code>.
	 * 
	 * @param fullClass the class containing the method to instrument
	 * @param methodSig the signature of the method to instrument
	 */
	public void handleHTTPS(final String fullClass, final String methodSig) {
		try {
			final CtBehavior method = getBehavior(fullClass, methodSig);
			new EagerExprEditor() {

				@Override
				public void edit(final MethodCall call) throws CannotCompileException {
					try {
						if (call.getMethodName().equals("startsWith") &&
							"http://".equals(getLastConstantArgument(call, 0)))
						{
							call
								.replace("$_ = $0.startsWith($1) || $0.startsWith(\"https://\");");
							markEdited();
						}
					}
					catch (final BadBytecode e) {
						e.printStackTrace();
					}
				}
			}.instrument(method);
		}
		catch (final Throwable e) {
			maybeThrow(new IllegalArgumentException("Could not handle HTTPS in " +
				methodSig + " in " + fullClass));
		}
	}

	private String getLastConstantArgument(final MethodCall call, final int skip)
		throws BadBytecode
	{
		final int[] indices = new int[skip + 1];
		int counter = 0;

		final MethodInfo info = ((CtMethod) call.where()).getMethodInfo();
		final CodeIterator iterator = info.getCodeAttribute().iterator();
		final int currentPos = call.indexOfBytecode();
		while (iterator.hasNext()) {
			final int pos = iterator.next();
			if (pos >= currentPos) break;
			switch (iterator.byteAt(pos)) {
				case Opcode.LDC:
					indices[(counter++) % indices.length] = iterator.byteAt(pos + 1);
					break;
				case Opcode.LDC_W:
					indices[(counter++) % indices.length] = iterator.u16bitAt(pos + 1);
					break;
			}
		}
		if (counter < skip) {
			return null;
		}
		counter %= indices.length;
		if (skip > 0) {
			counter -= skip;
			if (counter < 0) counter += indices.length;
		}
		return info.getConstPool().getStringInfo(indices[counter]);
	}

	/**
	 * An {@link ExprEditor} that complains when it did not edit anything.
	 * 
	 * @author Johannes Schindelin
	 */
	private abstract static class EagerExprEditor extends ExprEditor {

		private int count = 0;

		protected void markEdited() {
			count++;
		}

		protected boolean wasSuccessful() {
			return count > 0;
		}

		public void instrument(final CtBehavior behavior)
			throws CannotCompileException
		{
			count = 0;
			behavior.instrument(this);
			if (!wasSuccessful()) {
				throw new CannotCompileException("No code replaced!");
			}
		}
	}

	/**
	 * Disassembles all methods of a class.
	 * 
	 * @param fullName the class name
	 * @param out the output stream
	 */
	public void disassemble(final String fullName, final PrintStream out) {
		disassemble(fullName, out, false);
	}

	/**
	 * Disassembles all methods of a class, optionally including superclass
	 * methods.
	 * 
	 * @param fullName the class name
	 * @param out the output stream
	 * @param evenSuperclassMethods whether to disassemble methods defined in
	 *          superclasses
	 */
	public void disassemble(final String fullName, final PrintStream out,
		final boolean evenSuperclassMethods)
	{
		final CtClass clazz = getClass(fullName);
		out.println("Class " + clazz.getName());
		for (final CtConstructor ctor : clazz.getConstructors()) {
			disassemble(ctor, out);
		}
		for (final CtMethod method : clazz.getDeclaredMethods())
			if (evenSuperclassMethods || method.getDeclaringClass().equals(clazz)) disassemble(
				method, out);
	}

	private void disassemble(final CtBehavior method, final PrintStream out) {
		out.println(method.getLongName());
		final MethodInfo info = method.getMethodInfo2();
		final ConstPool constPool = info.getConstPool();
		final CodeAttribute code = info.getCodeAttribute();
		if (code == null) return;

		final CodeIterator iterator = code.iterator();
		while (iterator.hasNext()) {
			int pos;
			try {
				pos = iterator.next();
			}
			catch (final BadBytecode e) {
				throw new RuntimeException(e);
			}

			out.println(pos + ": " +
				InstructionPrinter.instructionString(iterator, pos, constPool));
		}

		out.println("");
	}

	/**
	 * Writes a .jar file with the modified classes.
	 * <p>
	 * This comes in handy e.g. when ImageJ is to be run in an environment where
	 * redefining classes is not allowed. If users need to run, say, the legacy
	 * headless support in such an environment, they need to generate a
	 * <i>headless.jar</i> file using this method and prepend it to the class path
	 * (so that the classes of <i>ij.jar</i> are overridden by
	 * <i>headless.jar</i>'s classes).
	 * </p>
	 * 
	 * @param path the <i>.jar</i> file to write to
	 * @throws IOException
	 */
	public void writeJar(final File path) throws IOException {
		final JarOutputStream jar = new JarOutputStream(new FileOutputStream(path));
		final DataOutputStream dataOut = new DataOutputStream(jar);
		for (final CtClass clazz : handledClasses) {
			final ZipEntry entry =
				new ZipEntry(clazz.getName().replace('.', '/') + ".class");
			jar.putNextEntry(entry);
			clazz.getClassFile().write(dataOut);
			dataOut.flush();
		}
		jar.close();
	}

	private void verify(final CtClass clazz, final PrintWriter output) {
		try {
			final ByteArrayOutputStream stream = new ByteArrayOutputStream();
			final DataOutputStream out = new DataOutputStream(stream);
			clazz.getClassFile().write(out);
			out.flush();
			out.close();
			verify(stream.toByteArray(), output);
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private void verify(final byte[] bytecode, final PrintWriter out) {
		try {
			Collection<URL> urls;
			urls =
				Utils.listContents(new File(System.getProperty("user.home"),
					"fiji/jars/").toURI().toURL());
			if (urls.size() == 0) {
				urls =
					Utils.listContents(new File(System.getProperty("user.home"),
						"Fiji.app/jars/").toURI().toURL());
				if (urls.size() == 0) {
					urls =
						Utils.listContents(new File("/Applications/Fiji.app/jars/")
							.toURI().toURL());
				}
			}
			final ClassLoader loader =
				new java.net.URLClassLoader(urls.toArray(new URL[urls.size()]));
			Class<?> readerClass = null, checkerClass = null;
			for (final String prefix : new String[] { "org.", "jruby.",
				"org.jruby.org." })
			{
				try {
					readerClass = loader.loadClass(prefix + "objectweb.asm.ClassReader");
					checkerClass =
						loader.loadClass(prefix + "objectweb.asm.util.CheckClassAdapter");
					break;
				}
				catch (final ClassNotFoundException e) { /* ignore */}
			}
			final java.lang.reflect.Constructor<?> ctor =
				readerClass.getConstructor(new Class[] { bytecode.getClass() });
			final Object reader = ctor.newInstance(bytecode);
			final java.lang.reflect.Method verify =
				checkerClass.getMethod("verify", new Class[] { readerClass,
					Boolean.TYPE, PrintWriter.class });
			verify.invoke(null, new Object[] { reader, false, out });
		}
		catch (final Throwable e) {
			if (e.getClass().getName().endsWith(".AnalyzerException")) {
				final Pattern pattern =
					Pattern.compile("Error at instruction (\\d+): "
						+ "Argument (\\d+): expected L([^ ,;]+);, but found L(.*);");
				final Matcher matcher = pattern.matcher(e.getMessage());
				if (matcher.matches()) {
					final CtClass clazz1 = getClass(matcher.group(3));
					final CtClass clazz2 = getClass(matcher.group(4));
					try {
						if (clazz2.subtypeOf(clazz1)) return;
					}
					catch (final NotFoundException e1) {
						e1.printStackTrace();
					}
				}
			}
			e.printStackTrace();
		}
	}

	protected void verify(final PrintWriter out) {
		out.println("Verifying " + handledClasses.size() + " classes");
		for (final CtClass clazz : handledClasses) {
			out.println("Verifying class " + clazz.getName());
			out.flush();
			verify(clazz, out);
		}
	}

	/**
	 * Applies legacy patches, optionally including the headless ones.
	 * <p>
	 * Intended to be used in unit tests only, for newly-created class loaders,
	 * via reflection.
	 * </p>
	 * 
	 * @param forceHeadless also apply the headless patches
	 */
	@SuppressWarnings("unused")
	private static void patch(final boolean forceHeadless) {
		final ClassLoader loader = CodeHacker.class.getClassLoader();
		new LegacyInjector().injectHooks(loader, forceHeadless);
	}

	/**
	 * Determines whether the current stack trace contains the specified string.
	 * 
	 * @param needle the text to find
	 * @return whether the stack trace contains the text
	 */
	private static boolean stackTraceContains(String needle) {
		final StringWriter writer = new StringWriter();
		final PrintWriter out = new PrintWriter(writer);
		new Exception().printStackTrace(out);
		out.close();
		return writer.toString().indexOf(needle) >= 0;
	}

	/**
	 * Makes sure that the given class is defined in the class loader.
	 * 
	 * @param clazz the class to commit
	 */
	public void commitClass(Class<?> clazz) {
		getClass(clazz.getName());
	}

}
