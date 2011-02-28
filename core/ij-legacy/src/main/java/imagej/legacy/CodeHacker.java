package imagej.legacy;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

/**
 * The code hacker provides a mechanism for altering the behavior of classes
 * before they are loaded, for the purpose of injecting new methods and/or
 * altering existing ones.
 *
 * In ImageJ, this mechanism is used to provide new seams into legacy ImageJ1
 * code, so that (e.g.) the modern GUI is aware of IJ1 events as they occur.
 *
 * @author Curtis Rueden
 * @author Rick Lentz
 */
public class CodeHacker {

	private static final String PATCH_PKG = "imagej.legacy.patches";
	private static final String PATCH_SUFFIX = "Methods";

	private ClassPool pool;
	
	public CodeHacker() {
		pool = ClassPool.getDefault();
	}

	/**
	 * TODO
	 *
	 * @param fullClass Fully qualified name of the class to override.
	 * @param methodSig Method signature of the method to override;
	 *   e.g., "public void updateAndDraw()"
	 */
	public void insertAfterMethod(final String fullClass,
		final String methodSig)
	{
		insertAfterMethod(fullClass, methodSig, newCode(fullClass, methodSig));
	}

	/**
	 * Modifies a class by injecting the provided code string
	 * at the end of the provided method's body.
	 *
	 * @param fullClass Fully qualified name of the class to override.
	 * @param methodSig Method signature of the method to override;
	 *   e.g., "public void updateAndDraw()"
	 * @param newCode The string of code to add;
	 *   e.g., System.out.println(\"Change Me!\");
	 */
	public void insertAfterMethod(final String fullClass,
		final String methodSig, final String newCode)
	{
		try {
			getMethod(fullClass, methodSig).insertAfter(newCode);
		}
		catch (CannotCompileException e) {
			throw new IllegalArgumentException("Cannot modify method: " +
				methodSig, e);
		}
	}

	/**
	 * TODO
	 *
	 * @param fullClass Fully qualified name of the class to override.
	 * @param methodSig Method signature of the method to override;
	 *   e.g., "public void updateAndDraw()"
	 */
	public void insertBeforeMethod(final String fullClass,
		final String methodSig)
	{
		insertBeforeMethod(fullClass, methodSig, newCode(fullClass, methodSig));
	}

	/**
	 * Modifies a class by injecting the provided code string
	 * at the start of the provided method's body.
	 *
	 * @param fullClass Fully qualified name of the class to override.
	 * @param methodSig Method signature of the method to override;
	 *   e.g., "public void updateAndDraw()"
	 * @param newCode The string of code to add;
	 *   e.g., System.out.println(\"Change Me!\");
	 */
	public void insertBeforeMethod(final String fullClass,
		final String methodSig, final String newCode)
	{
		try {
			getMethod(fullClass, methodSig).insertBefore(newCode);
		}
		catch (CannotCompileException e) {
			throw new IllegalArgumentException("Cannot modify method: " +
				methodSig, e);
		}
	}

	/**
	 * Inserts the specified method into the given class.
	 * The new method implementation should be declared in the
	 * imagej.legacy.patches package, with the same name as the original
	 * class plus "Methods"; e.g., overridden ij.gui.ImageWindow methods should
	 * be placed in the imagej.legacy.patches.ImageWindowMethods class.
	 *
	 * New method implementations must be public static, with an additional
	 * first parameter: the instance of the class on which to operate.
	 *
	 * @param fullClass Fully qualified name of the class to override.
	 * @param methodSig Method signature of the method to override;
	 *   e.g., "public void setVisible(boolean vis)"
	 */
	public void insertMethod(final String fullClass, final String methodSig) {
		insertMethod(fullClass, methodSig, newCode(fullClass, methodSig));
	}

	public void insertMethod(final String fullClass,
		final String methodSig, final String newCode)
	{
		final CtClass classRef = getClass(fullClass);
		final String methodBody = methodSig + " { " + newCode + " } ";
		try {
			final CtMethod methodRef = CtNewMethod.make(methodBody, classRef);
			classRef.addMethod(methodRef);
		}
		catch (CannotCompileException e) {
			throw new IllegalArgumentException(
				"Cannot add method: " + methodSig, e);
		}
	}

	/**
	 * Loads the given, possibly modified, class.
	 *
	 * This method must be called to confirm any changes made with
	 * {@link #insertAfterMethod}, {@link #insertBeforeMethod} or
	 * {@link #insertMethod}.
	 *
	 * @param fullClass fully qualified class name to load
	 * @return the loaded class
	 */
	public Class<?> loadClass(String fullClass) {
		final CtClass classRef = getClass(fullClass);
		try {
			return classRef.toClass();
		}
		catch (CannotCompileException e) {
			throw new IllegalArgumentException("Cannot load class: " + fullClass, e);
		}
	}

	/** Gets the Javassist class object corresponding to the given class name. */
	private CtClass getClass(String fullClass) {
		try {
			return pool.get(fullClass);
		}
		catch (NotFoundException e) {
			throw new IllegalArgumentException("No such class: " + fullClass, e);
		}		
	}

	/**
	 * Gets the Javassist method object corresponding to the
	 * given method signature of the specified class name.
	 */
	private CtMethod getMethod(String fullClass, final String methodSig) {
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
		catch (NotFoundException e) {
			throw new IllegalArgumentException("No such method: " + methodSig, e);
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
		final String[] argNames = getMethodArgNames(methodSig);
		final boolean isStatic = isStatic(methodSig);

		final StringBuilder newCode = new StringBuilder(PATCH_PKG + "." +
			className + PATCH_SUFFIX + "." + methodName + "(");
		boolean firstArg = true;
		if (!isStatic) {
			newCode.append("this");
			firstArg = false;
		}
		for (String arg : argNames) {
			if (firstArg) firstArg = false;
			else newCode.append(", ");
			newCode.append(arg);
		}
		newCode.append(");");

		return newCode.toString();
	}

	/** Extracts the method name from the given method signature. */
	private String getMethodName(final String methodSig) {
		final int parenIndex = methodSig.indexOf("(");
		final int spaceIndex = methodSig.lastIndexOf(" ", parenIndex);
		return methodSig.substring(spaceIndex + 1, parenIndex);
	}

	private String[] getMethodArgTypes(final String methodSig) {
		return getMethodArgs(methodSig, 0);
	}

	private String[] getMethodArgNames(final String methodSig) {
		return getMethodArgs(methodSig, 1);
	}

	private String[] getMethodArgs(final String methodSig, final int index) {
		assert index >= 0 && index <= 1;
		final int parenIndex = methodSig.indexOf("(");
		final String methodArgs = methodSig.substring(parenIndex + 1,
			methodSig.length() - 1);
		final String[] args = methodArgs.equals("") ?
			new String[0] : methodArgs.split(",");
		for (int i = 0; i < args.length; i++) {
			args[i] = args[i].trim().split(" ")[index];
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

}
