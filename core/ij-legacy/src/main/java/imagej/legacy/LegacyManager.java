package imagej.legacy;

import ij.IJ;
import ij.ImageJ;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

public final class LegacyManager {

	private static final String PATCH_PKG = "imagej.legacy.patches";

	/** Mapping between datasets and legacy image objects. */
	private static LegacyImageMap imageMap;

	private LegacyManager() {
		// prevent instantiation of utility class
	}

	static {
		// NB: Override class behavior before class loading gets too far along.

		// override ImageWindow behavior
		overrideMethod("ij.gui.ImageWindow", "public void setVisible(boolean vis)");
		overrideMethod("ij.gui.ImageWindow", "public void show()");
		loadClass("ij.gui.ImageWindow");
		
		insertAfterMethod( "ij.ImagePlus","updateAndDraw", "imagej.legacy.patches.ImagePlusMethods.updateAndDraw(this);" );
	}
	
	/**
	 * Modifies a class by injecting the provided code string at the end of the provided method's body
	 * @param qualifiedClassName - The name of the class to be modified E.g. ij.ImagePlus
	 * @param methodName - The name of the method to be modified E.g. updateAndDraw
	 * @param codeToAdd - The string of code to add E.g. System.out.println(\"Change Me!\");
	 */
	public static void insertAfterMethod( String qualifiedClassName, String methodName, String codeToAdd )
	{
		// Get the class in question
		ClassPool pool = ClassPool.getDefault();
		try {
			CtClass cc = pool.get( qualifiedClassName );
			CtMethod cm = cc.getDeclaredMethod( methodName );
			cm.insertAfter(codeToAdd);
			cc.toClass();
		} catch ( Exception e ) {
			throw new IllegalArgumentException("Error changing class: " + qualifiedClassName, e);
		}
	}

	/**
	 * Modifies a class by injecting the provided code string at the start of the provided method's body
	 * @param qualifiedClassName - The name of the class to be modified E.g. ij.ImagePlus
	 * @param methodName - The name of the method to be modified E.g. updateAndDraw
	 * @param codeToAdd - The string of code to add E.g. System.out.println(\"Change Me!\");
	 */
	public static void insertBeforeMethod( String qualifiedClassName, String methodName, String codeToAdd )
	{
		// Get the class in question
		ClassPool pool = ClassPool.getDefault();
		try {
			CtClass cc = pool.get( qualifiedClassName );
			CtMethod cm = cc.getDeclaredMethod( methodName );
			cm.insertBefore(codeToAdd);
			cc.toClass();
		} catch ( Exception e ) {
			throw new IllegalArgumentException("Error changing class: " + qualifiedClassName, e);
		}
	}
	
	public static ImageJ initialize() {
		final ImageJ ij = IJ.getInstance();
		if (ij != null) return ij;

		imageMap = new LegacyImageMap();

		// initialize legacy ImageJ application
		return new ImageJ(ImageJ.NO_SHOW);
	}

	public static LegacyImageMap getImageMap() {
		return imageMap;
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
	private static void overrideMethod( String fullClass, String methodSig ) {
		final int dotIndex = fullClass.lastIndexOf(".");
		final String className = fullClass.substring(dotIndex + 1);

		final int parenIndex = methodSig.indexOf("(");
		final int spaceIndex = methodSig.lastIndexOf(" ", parenIndex);
		final String methodName = methodSig.substring(spaceIndex + 1, parenIndex);
		final String methodArgs = methodSig.substring(parenIndex + 1,
			methodSig.length() - 1);
		final String[] argList = methodArgs.equals("") ?
			new String[0] : methodArgs.split(",");

		final StringBuilder methodBody = new StringBuilder();
		methodBody.append(methodSig);
		methodBody.append(" { ");
		methodBody.append(PATCH_PKG);
		methodBody.append(".");
		methodBody.append(className);
		methodBody.append("Methods");
		methodBody.append(".");
		methodBody.append(methodName);
		methodBody.append("(this");
		for (String arg : argList) {
			methodBody.append(", ");
			methodBody.append(arg.split(" ")[1]);
		}
		methodBody.append("); }");

		final ClassPool pool = ClassPool.getDefault();
		final CtClass classRef;
		try {
			classRef = pool.get(fullClass);
		}
		catch (NotFoundException e) {
			throw new IllegalArgumentException("No such class: " + fullClass, e);
		}
		final CtMethod methodRef;
		try {
			methodRef = CtNewMethod.make(methodBody.toString(), classRef);
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
	private static Class<?> loadClass(String fullClass) {
		
		//Create a hashtable container of compile time class objects
		final ClassPool pool = ClassPool.getDefault();
		
		//get the compile time class handle
		final CtClass classRef;
		try {
			classRef = pool.get( fullClass );
		}
		catch (NotFoundException e) {
			throw new IllegalArgumentException("No such class: " + fullClass, e);
		}
		try {
			// directly load the class
			return classRef.toClass();
		}
		catch (CannotCompileException e) {
			throw new IllegalArgumentException("Cannot alter class: " + fullClass, e);
		}
	}

}
