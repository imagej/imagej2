package imagej;

import java.io.File;
import java.io.IOException;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.jar.JarFile;

public class JarLauncher {
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Missing argument");
			System.exit(1);
		}
		String[] shifted = new String[args.length - 1];
		System.arraycopy(args, 1, shifted, 0, shifted.length);
		launchJar(args[0], shifted);
	}

	// helper to launch .jar files (by inspecting their Main-Class
	// attribute).
	public static void launchJar(String jarPath, String[] arguments) {
		JarFile jar = null;
		try {
			jar = new JarFile(jarPath);
		} catch (IOException e) {
			System.err.println("Could not read '" + jarPath + "'.");
			System.exit(1);
		}
		Manifest manifest = null;
		try {
			manifest = jar.getManifest();
		} catch (IOException e) { }
		if (manifest == null) {
			System.err.println("No manifest found in '"
					+ jarPath + "'.");
			System.exit(1);
		}
		Attributes attributes = manifest.getMainAttributes();
		String className = attributes == null ? null :
			attributes.getValue("Main-Class");
		if (className == null) {
			System.err.println("No main class attribute found in '"
					+ jarPath + "'.");
			System.exit(1);
		}
		ClassLoaderPlus loader = ClassLoaderPlus.get(new File(jarPath));
		ClassLauncher.launch(loader, className, arguments);
	}
}
