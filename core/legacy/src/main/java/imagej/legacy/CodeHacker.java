/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2013 Board of Regents of the University of
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

package imagej.legacy;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.scijava.util.ClassUtils;

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
import javassist.bytecode.InstructionPrinter;
import javassist.expr.ConstructorCall;
import javassist.expr.ExprEditor;
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

	private static final String PATCH_PKG = "imagej.legacy.patches";
	private static final String PATCH_SUFFIX = "Methods";

	private final ClassPool pool;
	protected final ClassLoader classLoader;
	private final Set<CtClass> handledClasses = new LinkedHashSet<CtClass>();

	public CodeHacker(final ClassLoader classLoader, final ClassPool classPool) {
		this.classLoader = classLoader;
		pool = classPool != null ? classPool : ClassPool.getDefault();
		pool.appendClassPath(new ClassClassPath(getClass()));
		pool.appendClassPath(new LoaderClassPath(classLoader));

		// the CodeHacker offers the LegacyService instance, therefore it needs to add that field here
		if (!hasField("ij.IJ", "_legacyService")) {
			insertPrivateStaticField("ij.IJ", LegacyService.class, "_legacyService");
			insertNewMethod("ij.IJ",
				"public static imagej.legacy.LegacyService getLegacyService()",
				"return _legacyService;");
		}
	}

	public CodeHacker(ClassLoader classLoader) {
		this(classLoader, null);
	}

	/**
	 * Modifies a class by injecting additional code at the end of the specified
	 * method's body.
	 * <p>
	 * The extra code is defined in the imagej.legacy.patches package, as
	 * described in the documentation for {@link #insertNewMethod(String, String)}.
	 * </p>
	 * 
	 * @param fullClass Fully qualified name of the class to modify.
	 * @param methodSig Method signature of the method to modify; e.g.,
	 *          "public void updateAndDraw()"
	 */
	public void
		insertAtBottomOfMethod(final String fullClass, final String methodSig)
	{
		insertAtBottomOfMethod(fullClass, methodSig, newCode(fullClass, methodSig));
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
			getMethod(fullClass, methodSig).insertAfter(expand(newCode));
		}
		catch (final CannotCompileException e) {
			throw new IllegalArgumentException("Cannot modify method: " + methodSig,
				e);
		}
	}

	/**
	 * Modifies a class by injecting additional code at the start of the specified
	 * method's body.
	 * <p>
	 * The extra code is defined in the imagej.legacy.patches package, as
	 * described in the documentation for {@link #insertNewMethod(String, String)}.
	 * </p>
	 * 
	 * @param fullClass Fully qualified name of the class to override.
	 * @param methodSig Method signature of the method to override; e.g.,
	 *          "public void updateAndDraw()"
	 */
	public void
		insertAtTopOfMethod(final String fullClass, final String methodSig)
	{
		insertAtTopOfMethod(fullClass, methodSig, newCode(fullClass, methodSig));
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
			getMethod(fullClass, methodSig).insertBefore(expand(newCode));
		}
		catch (final CannotCompileException e) {
			throw new IllegalArgumentException("Cannot modify method: " + methodSig,
				e);
		}
	}

	/**
	 * Modifies a class by injecting a new method.
	 * <p>
	 * The body of the method is defined in the imagej.legacy.patches package, as
	 * described in the {@link #insertNewMethod(String, String)} method
	 * documentation.
	 * <p>
	 * The new method implementation should be declared in the
	 * imagej.legacy.patches package, with the same name as the original class
	 * plus "Methods"; e.g., overridden ij.gui.ImageWindow methods should be
	 * placed in the imagej.legacy.patches.ImageWindowMethods class.
	 * </p>
	 * <p>
	 * New method implementations must be public static, with an additional first
	 * parameter: the instance of the class on which to operate.
	 * </p>
	 * 
	 * @param fullClass Fully qualified name of the class to override.
	 * @param methodSig Method signature of the method to override; e.g.,
	 *          "public void setVisible(boolean vis)"
	 */
	public void insertNewMethod(final String fullClass, final String methodSig) {
		insertNewMethod(fullClass, methodSig, newCode(fullClass, methodSig));
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
		final String methodBody = methodSig + " { " + expand(newCode) + " } ";
		try {
			final CtMethod methodRef = CtNewMethod.make(methodBody, classRef);
			classRef.addMethod(methodRef);
		}
		catch (final CannotCompileException e) {
			throw new IllegalArgumentException("Cannot add method: " + methodSig, e);
		}
	}

	/*
	 * Works around a bug where the horizontal scroll wheel of the mighty mouse is mistaken for a popup trigger.
	 */
	public void handleMightyMousePressed(final String fullClass) {
		ExprEditor editor = new ExprEditor() {
			@Override
			public void edit(MethodCall call) throws CannotCompileException {
				if (call.getMethodName().equals("isPopupTrigger"))
					call.replace("$_ = $0.isPopupTrigger() && $0.getButton() != 0;");
			}
		};
		final CtClass classRef = getClass(fullClass);
		for (final String methodName : new String[] { "mousePressed", "mouseDragged" }) try {
			final CtMethod method = classRef.getMethod(methodName, "(Ljava/awt/event/MouseEvent;)V");
			method.instrument(editor);
		} catch (NotFoundException e) {
			/* ignore */
		} catch (CannotCompileException e) {
			throw new IllegalArgumentException("Cannot instrument method: " + methodName, e);
		}
	}


	public void insertPrivateStaticField(final String fullClass,
			final Class<?> clazz, final String name) {
		insertStaticField(fullClass, Modifier.PRIVATE, clazz, name, null);
	}

	public void insertPublicStaticField(final String fullClass,
			final Class<?> clazz, final String name, final String initializer) {
		insertStaticField(fullClass, Modifier.PUBLIC, clazz, name, initializer);
	}

	public void insertStaticField(final String fullClass, int modifiers,
			final Class<?> clazz, final String name, final String initializer) {
		final CtClass classRef = getClass(fullClass);
		try {
			final CtField field = new CtField(pool.get(clazz.getName()), name,
					classRef);
			field.setModifiers(modifiers | Modifier.STATIC);
			classRef.addField(field);
			if (initializer != null) {
				addToClassInitializer(fullClass, name + " = " + initializer + ";");
			}
		} catch (CannotCompileException e) {
			throw new IllegalArgumentException("Cannot add field " + name
					+ " to " + fullClass, e);
		} catch (NotFoundException e) {
			throw new IllegalArgumentException("Cannot add field " + name
					+ " to " + fullClass, e);
		}
	}

	public void addToClassInitializer(final String fullClass, final String code) {
		final CtClass classRef = getClass(fullClass);
		try {
			CtConstructor method = classRef.getClassInitializer();
			if (method != null) {
				method.insertAfter(code);
			} else {
				method = CtNewConstructor.make(new CtClass[0], new CtClass[0], code, classRef);
				method.getMethodInfo().setName("<clinit>");
				method.setModifiers(Modifier.STATIC);
				classRef.addConstructor(method);
			}
		} catch (CannotCompileException e) {
			throw new IllegalArgumentException("Cannot add " + code
					+ " to class initializer of " + fullClass, e);
		}
	}

	public void addCatch(final String fullClass, final String methodSig, final String exceptionClassName, final String src) {
		try {
			final CtMethod method = getMethod(fullClass, methodSig);
			method.addCatch(src, getClass(exceptionClassName), "$e");
		} catch (CannotCompileException e) {
			throw new IllegalArgumentException("Cannot add catch for exception of type'"
				+ exceptionClassName + " in " + fullClass		+ "'s " + methodSig, e);
		}
	}

	public void insertAtTopOfExceptionHandlers(final String fullClass, final String methodSig, final String exceptionClassName, final String src) {
		try {
			final CtMethod method = getMethod(fullClass, methodSig);
			method.instrument(new ExprEditor() {
				@Override
				public void edit(Handler handler) throws CannotCompileException {
					try {
						if (handler.getType().getName().equals(exceptionClassName)) {
							handler.insertBefore(src);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
		} catch (CannotCompileException e) {
			throw new IllegalArgumentException("Cannot edit exception handler for type'"
				+ exceptionClassName + " in " + fullClass		+ "'s " + methodSig, e);
		}
	}
	/**
	 * Replaces the application name.
	 * 
	 * @param appName the new application name
	 */
	public void setAppName(final String appName) {
		insertPublicStaticField("ij.IJ", String.class, "_appName", "\"" + appName + "\"");
		final String appNameField = "ij.IJ._appName");
		insertAtTopOfMethod("ij.IJ", "public void error(java.lang.String title, java.lang.String msg)",
				"if ($1 == null || $1.equals(\"ImageJ\")) $1 = " + appNameField + ";");
		insertAtBottomOfMethod("ij.ImageJ", "public java.lang.String version()", "$_ = $_.replace(\"ImageJ\", " + appNameField + ");");
		replaceParameterInCall("ij.ImageJ", "public <init>(java.applet.Applet applet, int mode)", "super", 1, appNameField);
		replaceParameterInNew("ij.ImageJ", "public void run()", "ij.gui.GenericDialog", 1, appNameField);
		replaceParameterInCall("ij.ImageJ", "public void run()", "addMessage", 1, appNameField);
		replaceParameterInNew("ij.plugin.CommandFinder", "public void export()", "ij.text.TextWindow", 1, appNameField);
		replaceParameterInCall("ij.plugin.Hotkeys", "public void removeHotkey()", "addMessage", 1, appNameField);
		replaceParameterInCall("ij.plugin.Hotkeys", "public void removeHotkey()", "showStatus", 1, appNameField);
		replaceParameterInCall("ij.plugin.Options", "public void appearance()", "showMessage", 2, appNameField);
		replaceParameterInCall("ij.gui.YesNoCancelDialog", "public <init>(java.awt.Frame parent, java.lang.String title, java.lang.String msg)", "super", 2, appNameField);
		replaceParameterInCall("ij.gui.Toolbar", "private void showMessage(int toolId)", "showStatus", 1, appNameField);
	}

	/**
	 * Replaces the application name in the given method in the given parameter
	 * to the given constructor call.
	 * 
	 * Fails silently if the specified method does not exist (e.g.
	 * CommandFinder's export() function just went away in 1.47i).
	 * 
	 * @param fullClass
	 *            Fully qualified name of the class to override.
	 * @param methodSig
	 *            Method signature of the method to override; e.g.,
	 *            "public void showMessage(String title, String message)"
	 * @param newClassName
	 *            the name of the class which is to be constructed by the new
	 *            operator
	 * @param parameterIndex
	 *            the index of the parameter containing the application name
	 * @param replacement
	 *            the code to use instead of the specified parameter
	 * @throws CannotCompileException
	 */
	private void replaceParameterInNew(final String fullClass,
			final String methodSig, final String newClassName,
			final int parameterIndex, final String replacement) {
		try {
			final CtMethod method = getMethod(fullClass, methodSig);
			method.instrument(new ExprEditor() {
				@Override
				public void edit(NewExpr expr) throws CannotCompileException {
					if (expr.getClassName().equals(newClassName))
						try {
							final CtClass[] parameterTypes = expr
									.getConstructor().getParameterTypes();
							if (parameterTypes[parameterIndex] != CodeHacker.this
									.getClass("java.lang.String")) {
								throw new IllegalArgumentException("Parameter "
										+ parameterIndex + " of "
										+ expr.getConstructor() + " is not a String!");
							}
							final String replace = replaceParameter(
									parameterIndex, parameterTypes.length, replacement);
							expr.replace("$_ = new " + newClassName + replace
									+ ";");
						} catch (NotFoundException e) {
							throw new IllegalArgumentException(
									"Cannot find the parameters of the constructor of "
											+ newClassName, e);
					}
				}
			});
		} catch (IllegalArgumentException e) {
			// ignore: the method was not found
		} catch (CannotCompileException e) {
			throw new IllegalArgumentException("Cannot handle app name in " + fullClass
					+ "'s " + methodSig, e);
		}
	}

	/**
	 * Replaces the application name in the given method in the given parameter
	 * to the given method call.
	 * 
	 * Fails silently if the specified method does not exist (e.g.
	 * CommandFinder's export() function just went away in 1.47i).
	 * 
	 * @param fullClass
	 *            Fully qualified name of the class to override.
	 * @param methodSig
	 *            Method signature of the method to override; e.g.,
	 *            "public void showMessage(String title, String message)"
	 * @param calledMethodName
	 *            the name of the method to which the application name is passed
	 * @param parameterIndex
	 *            the index of the parameter containing the application name
	 * @param replacement
	 *            the code to use instead of the specified parameter
	 * @throws CannotCompileException
	 */
	private void replaceParameterInCall(final String fullClass,
			final String methodSig, final String calledMethodName,
			final int parameterIndex, final String replacement) {
		try {
			final CtBehavior method;
			if (methodSig.indexOf("<init>") < 0) {
				method = getMethod(fullClass, methodSig);
			} else {
				method = getConstructor(fullClass, methodSig);
			}
			method.instrument(new ExprEditor() {
				@Override
				public void edit(MethodCall call) throws CannotCompileException {
					if (call.getMethodName().equals(calledMethodName)) try {
						final boolean isSuper = call.isSuper();
						final CtClass[] parameterTypes = isSuper ?
								((ConstructorCall) call).getConstructor().getParameterTypes() :
								call.getMethod().getParameterTypes();
						if (parameterTypes.length < parameterIndex) {
								throw new IllegalArgumentException("Index " + parameterIndex + " is outside of " + call.getMethod() + "'s parameter list!");
						}
						if (parameterTypes[parameterIndex - 1] != CodeHacker.this.getClass("java.lang.String")) {
							throw new IllegalArgumentException("Parameter " + parameterIndex + " of "
									+ call.getMethod() + " is not a String!");
						}
						final String replace = replaceParameter(
								parameterIndex, parameterTypes.length, replacement);
						call.replace((isSuper ? "" : "$0.") + calledMethodName + replace + ";");
					} catch (NotFoundException e) {
						throw new IllegalArgumentException(
								"Cannot find the parameters of the method "
										+ calledMethodName, e);
					}
				}

				@Override
				public void edit(final ConstructorCall call) throws CannotCompileException {
					edit((MethodCall)call);
				}
			});
		} catch (IllegalArgumentException e) {
			// ignore: the method was not found
		} catch (CannotCompileException e) {
			throw new IllegalArgumentException("Cannot handle app name in " + fullClass
					+ "'s " + methodSig, e);
		}
	}

	private String replaceParameter(final int parameterIndex, final int parameterCount, final String replacement) {
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
	 * Replaces the given methods with stub methods.
	 * 
	 * @param fullClass the class to patch
	 * @param methodNames the names of the methods to replace
	 * @throws NotFoundException
	 * @throws CannotCompileException
	 */
	public void replaceWithStubMethods(final String fullClass, final String... methodNames) {
		final CtClass clazz = getClass(fullClass);
		final Set<String> override = new HashSet<String>(Arrays.asList(methodNames));
		for (final CtMethod method : clazz.getMethods())
			if (override.contains(method.getName())) try {
				final CtMethod stub = makeStubMethod(clazz, method);
				method.setBody(stub, null);
			} catch (NotFoundException e) {
				// ignore
			} catch (CannotCompileException e) {
				throw new IllegalArgumentException("Cannot instrument method: " + method.getName(), e);
			}
	}

	/**
	 * Replaces the superclass.
	 * 
	 * @param fullClass
	 * @param fullNewSuperclass
	 * @throws NotFoundException
	 */
	public void replaceSuperclass(String fullClass, String fullNewSuperclass) {
		final CtClass clazz = getClass(fullClass);
		try {
			CtClass originalSuperclass = clazz.getSuperclass();
			clazz.setSuperclass(getClass(fullNewSuperclass));
			for (final CtConstructor ctor : clazz.getConstructors())
				ctor.instrument(new ExprEditor() {
					@Override
					public void edit(final ConstructorCall call) throws CannotCompileException {
						if (call.getMethodName().equals("super"))
							call.replace("super();");
					}
				});
			letSuperclassMethodsOverride(clazz);
			addMissingMethods(clazz, originalSuperclass);
		} catch (NotFoundException e) {
			throw new IllegalArgumentException("Could not replace superclass of " + fullClass + " with " + fullNewSuperclass, e);
		} catch (CannotCompileException e) {
			throw new IllegalArgumentException("Could not replace superclass of " + fullClass + " with " + fullNewSuperclass, e);
		}
	}

	/**
	 * Replaces all instantiations of a subset of AWT classes with nulls.
	 * 
	 * This is used by the partial headless support of legacy code.
	 * 
	 * @param fullClass
	 * @throws CannotCompileException
	 * @throws NotFoundException
	 */
	public void skipAWTInstantiations(String fullClass) {
		try {
			skipAWTInstantiations(getClass(fullClass));
		} catch (CannotCompileException e) {
			throw new IllegalArgumentException("Could not skip AWT class instantiations in " + fullClass, e);
		} catch (NotFoundException e) {
			throw new IllegalArgumentException("Could not skip AWT class instantiations in " + fullClass, e);
		}
	}

	/**
	 * Loads the given, possibly modified, class.
	 * <p>
	 * This method must be called to confirm any changes made with
	 * {@link #insertAfterMethod}, {@link #insertBeforeMethod},
	 * or {@link #insertMethod}.
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
	 * {@link #insertAfterMethod}, {@link #insertBeforeMethod},
	 * or {@link #insertMethod}.
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
			// Cannot use LogService; it will not be initialized by the time the DefaultLegacyService
			// class is loaded, which is when the CodeHacker is run
			if (e.getCause() != null && e.getCause() instanceof LinkageError) {
				final URL url = ClassUtils.getLocation(LegacyJavaAgent.class);
				final String path = url != null && "file".equals(url.getProtocol()) && url.getPath().endsWith(".jar")?
						url.getPath() : "/path/to/ij-legacy.jar";

				throw new RuntimeException("Cannot load class: " + classRef.getName() + "\n"
						+ "It appears that this class was already defined in the class loader!\n"
						+ "Please make sure that you initialize the LegacyService before using\n"
						+ "any ImageJ 1.x class. You can do that by adding this static initializer:\n\n"
						+ "\tstatic {\n"
						+ "\t\tDefaultLegacyService.preinit();\n"
						+ "\t}\n\n"
						+ "To debug this issue, start the JVM with the option:\n\n"
						+ "\t-javaagent:" + path + "\n\n"
						+ "To enforce pre-initialization, start the JVM with the option:\n\n"
						+ "\t-javaagent:" + path + "=init\n", e.getCause());
			}
			System.err.println("Warning: Cannot load class: " + classRef.getName() + " into " + classLoader);
			e.printStackTrace();
			return null;
		} finally {
			classRef.freeze();
		}
	}

	public void loadClasses() {
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
	 * Gets the Javassist constructor object corresponding to the given constructor
	 * signature of the specified class name.
	 */
	private CtConstructor getConstructor(final String fullClass, final String constructorSig) {
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

	/**
	 * Generates a new line of code calling the {@link imagej.legacy.patches}
	 * class and method corresponding to the given method signature.
	 */
	private String newCode(final String fullClass, final String methodSig) {
		final int dotIndex = fullClass.lastIndexOf(".");
		final String className = fullClass.substring(dotIndex + 1);

		final String methodName = getMethodName(methodSig);
		final boolean isStatic = isStatic(methodSig);
		final boolean isVoid = isVoid(methodSig);

		final String patchClass = PATCH_PKG + "." + className + PATCH_SUFFIX;
		for (final CtMethod method : getClass(patchClass).getMethods()) try {
			if ((method.getModifiers() & Modifier.STATIC) == 0) continue;
			final CtClass[] types = method.getParameterTypes();
			if (types.length == 0 || !types[0].getName().equals("imagej.legacy.LegacyService")) {
				throw new UnsupportedOperationException("Method " + method + " of class " + patchClass + " has wrong type!");
			}
		} catch (NotFoundException e) {
			e.printStackTrace();
		}

		final StringBuilder newCode =
			new StringBuilder((isVoid ? "" : "return ") + patchClass + "." + methodName + "(");
		newCode.append("$service");
		if (!isStatic) {
			newCode.append(", this");
		}
		final int argCount = getMethodArgTypes(methodSig).length;
		for (int i = 1; i <= argCount; i++) {
			newCode.append(", $" + i);
		}
		newCode.append(");");

		return newCode.toString();
	}

	/** Patches in the current legacy service for '$service' */
	private String expand(final String code) {
		return code
			.replace("$isLegacyMode()", "$service.isLegacyMode()")
			.replace("$service", "ij.IJ.getLegacyService()");
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

	/** Returns true if the given method signature is static. */
	private boolean isStatic(final String methodSig) {
		final int parenIndex = methodSig.indexOf("(");
		final String methodPrefix = methodSig.substring(0, parenIndex);
		for (final String token : methodPrefix.split(" ")) {
			if (token.equals("static")) return true;
		}
		return false;
	}

	/** Returns true if the given method signature returns void. */
	private boolean isVoid(final String methodSig) {
		final int parenIndex = methodSig.indexOf("(");
		final String methodPrefix = methodSig.substring(0, parenIndex);
		return methodPrefix.startsWith("void ") ||
			methodPrefix.indexOf(" void ") > 0;
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
		} catch (NotFoundException e) {
			return false;
		}
	}

	private static int verboseLevel = 0;

	private static CtMethod makeStubMethod(CtClass clazz, CtMethod original) throws CannotCompileException, NotFoundException {
		// add a stub
		String prefix = "";
		if (verboseLevel > 0) {
			prefix = "System.err.println(\"Called " + original.getLongName() + "\\n\"";
			if (verboseLevel > 1) {
				prefix += "+ \"\\t(\" + fiji.Headless.toString($args) + \")\\n\"";
			}
			prefix += ");";
		}

		CtClass type = original.getReturnType();
		String body = "{" +
			prefix +
			(type == CtClass.voidType ? "" : "return " + defaultReturnValue(type) + ";") +
			"}";
		CtClass[] types = original.getParameterTypes();
		return CtNewMethod.make(type, original.getName(), types, new CtClass[0], body, clazz);
	}

	private static String defaultReturnValue(CtClass type) {
		return (type == CtClass.booleanType ? "false" :
			(type == CtClass.byteType ? "(byte)0" :
			 (type == CtClass.charType ? "'\0'" :
			  (type == CtClass.doubleType ? "0.0" :
			   (type == CtClass.floatType ? "0.0f" :
			    (type == CtClass.intType ? "0" :
			     (type == CtClass.longType ? "0l" :
			      (type == CtClass.shortType ? "(short)0" : "null"))))))));
	}

	private void addMissingMethods(CtClass fakeClass, CtClass originalClass) throws CannotCompileException, NotFoundException {
		if (verboseLevel > 0)
			System.err.println("adding missing methods from " + originalClass.getName() + " to " + fakeClass.getName());
		Set<String> available = new HashSet<String>();
		for (CtMethod method : fakeClass.getMethods())
			available.add(stripPackage(method.getLongName()));
		for (CtMethod original : originalClass.getDeclaredMethods()) {
			if (available.contains(stripPackage(original.getLongName()))) {
				if (verboseLevel > 1)
					System.err.println("Skipping available method " + original);
				continue;
			}

			CtMethod method = makeStubMethod(fakeClass, original);
			fakeClass.addMethod(method);
			if (verboseLevel > 1)
				System.err.println("adding missing method " + method);
		}

		// interfaces
		Set<CtClass> availableInterfaces = new HashSet<CtClass>();
		for (CtClass iface : fakeClass.getInterfaces())
			availableInterfaces.add(iface);
		for (CtClass iface : originalClass.getInterfaces())
			if (!availableInterfaces.contains(iface))
				fakeClass.addInterface(iface);

		CtClass superClass = originalClass.getSuperclass();
		if (superClass != null && !superClass.getName().equals("java.lang.Object"))
			addMissingMethods(fakeClass, superClass);
	}

	private void letSuperclassMethodsOverride(CtClass clazz) throws CannotCompileException, NotFoundException {
		for (CtMethod method : clazz.getSuperclass().getDeclaredMethods()) {
			CtMethod method2 = clazz.getMethod(method.getName(), method.getSignature());
			if (method2.getDeclaringClass().equals(clazz)) {
				method2.setBody(method, null); // make sure no calls/accesses to GUI components are remaining
				method2.setName("narf" + method.getName());
			}
		}
	}

	private static String stripPackage(String className) {
		int lastDot = -1;
		for (int i = 0; ; i++) {
			if (i >= className.length())
				return className.substring(lastDot + 1);
			char c = className.charAt(i);
			if (c == '.' || c == '$')
				lastDot = i;
			else if (c >= 'A' && c <= 'Z')
				; // continue
			else if (c >= 'a' && c <= 'z')
				; // continue
			else if (i > lastDot + 1 && c >= '0' && c <= '9')
				; // continue
			else
				return className.substring(lastDot + 1);
		}
	}

	private void skipAWTInstantiations(CtClass clazz) throws CannotCompileException, NotFoundException {
		clazz.instrument(new ExprEditor() {
			@Override
			public void edit(NewExpr expr) throws CannotCompileException {
				String name = expr.getClassName();
				if (name.startsWith("java.awt.Menu") || name.equals("java.awt.PopupMenu") ||
						name.startsWith("java.awt.Checkbox") || name.equals("java.awt.Frame")) {
					expr.replace("$_ = null;");
				} else if (expr.getClassName().equals("ij.gui.StackWindow")) {
					expr.replace("$1.show(); $_ = null;");
				}
			}

			@Override
			public void edit(MethodCall call) throws CannotCompileException {
				final String className = call.getClassName();
				final String methodName = call.getMethodName();
				if (className.startsWith("java.awt.Menu") || className.equals("java.awt.PopupMenu") ||
						className.startsWith("java.awt.Checkbox")) try {
					CtClass type = call.getMethod().getReturnType();
					if (type == CtClass.voidType) {
						call.replace("");
					} else {
						call.replace("$_ = " + defaultReturnValue(type) + ";");
					}
				} catch (NotFoundException e) {
					e.printStackTrace();
				} else if (methodName.equals("put") && className.equals("java.util.Properties")) {
					call.replace("if ($1 != null && $2 != null) $_ = $0.put($1, $2); else $_ = null;");
				} else if (methodName.equals("get") && className.equals("java.util.Properties")) {
					call.replace("$_ = $1 != null ? $0.get($1) : null;");
				} else if (className.equals("java.lang.Integer") && methodName.equals("intValue")) {
					call.replace("$_ = $0 == null ? 0 : $0.intValue();");
				} else if (methodName.equals("addTextListener")) {
					call.replace("");
				} else if (methodName.equals("elementAt")) {
					call.replace("$_ = $0 == null ? null : $0.elementAt($$);");
				}
			}
		});
	}

	/**
	 * Disassembles all methods of a class.
	 * 
	 * @param fullName
	 *            the class name
	 * @param out
	 *            the output stream
	 */
	public void disassemble(final String fullName, final PrintStream out) {
		disassemble(fullName, out, false);
	}

	/**
	 * Disassembles all methods of a class, optionally including superclass
	 * methods.
	 * 
	 * @param fullName
	 *            the class name
	 * @param out
	 *            the output stream
	 * @param evenSuperclassMethods
	 *            whether to disassemble methods defined in superclasses
	 */
	public void disassemble(final String fullName, final PrintStream out, final boolean evenSuperclassMethods) {
		CtClass clazz = getClass(fullName);
		out.println("Class " + clazz.getName());
		for (CtConstructor ctor : clazz.getConstructors()) try {
			disassemble(ctor.toMethod(ctor.getName(), clazz), out);
		} catch (CannotCompileException e) {
			e.printStackTrace(out);
		}
		for (CtMethod method : clazz.getDeclaredMethods())
			if (evenSuperclassMethods || method.getDeclaringClass().equals(clazz))
				disassemble(method, out);
	}

	private void disassemble(CtMethod method, PrintStream out) {
		out.println(method.getLongName());
		new InstructionPrinter(out).print(method);
		out.println("");
	}

	public void writeJar(final File path) throws IOException {
		final JarOutputStream jar = new JarOutputStream(new FileOutputStream(path));
		final DataOutputStream dataOut = new DataOutputStream(jar);
		for (final CtClass clazz : handledClasses) {
			final ZipEntry entry = new ZipEntry(clazz.getName().replace('.', '/') + ".class");
			jar.putNextEntry(entry);
			clazz.getClassFile().write(dataOut);
			dataOut.flush();
		}
		jar.close();
	}

}
