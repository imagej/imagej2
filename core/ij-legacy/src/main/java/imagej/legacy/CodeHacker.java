package imagej.legacy;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public class CodeHacker {

	private static final String PATCH_PKG = "imagej.legacy.patches";

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
	 * Overrides the behavior of the specified method for the given class.
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
	 * {@link #overrideMethod}.
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

	private CtClass getClass(String fullClass) {
		try {
			return pool.get(fullClass);
		}
		catch (NotFoundException e) {
			throw new IllegalArgumentException("No such class: " + fullClass, e);
		}		
	}

	private CtMethod getMethod(String fullClass, final String methodSig) {
		final CtClass cc = getClass(fullClass);
		try {
			return cc.getDeclaredMethod(getMethodName(methodSig));
		}
		catch (NotFoundException e) {
			throw new IllegalArgumentException("No such method: " + methodSig, e);
		}
	}

	private String newCode(final String fullClass, final String methodSig) {
		final int dotIndex = fullClass.lastIndexOf(".");
		final String className = fullClass.substring(dotIndex + 1);

		final String methodName = getMethodName(methodSig);

		final int parenIndex = methodSig.indexOf("(");
		final String methodArgs = methodSig.substring(parenIndex + 1,
			methodSig.length() - 1);
		final String[] argList = methodArgs.equals("") ?
			new String[0] : methodArgs.split(",");

		final StringBuilder newCode = new StringBuilder(PATCH_PKG + "." +
			className + "Methods" + "." + methodName + "(this");
		for (String arg : argList) {
			newCode.append(", ");
			newCode.append(arg.split(" ")[1]);
		}
		newCode.append(");");

		return newCode.toString();
	}

	private String getMethodName(final String methodSig) {
		final int parenIndex = methodSig.indexOf("(");
		final int spaceIndex = methodSig.lastIndexOf(" ", parenIndex);
		return methodSig.substring(spaceIndex + 1, parenIndex);
	}

}
