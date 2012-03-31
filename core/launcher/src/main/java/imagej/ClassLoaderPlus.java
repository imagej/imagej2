package imagej;

/**
 * A classloader whose classpath can be augmented after instantiation
 */

import java.io.File;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import java.util.ArrayList;
import java.util.List;

public class ClassLoaderPlus extends URLClassLoader {
	// A frozen ClassLoaderPlus will add only to the urls array
	protected boolean frozen;
	protected List<URL> urls = new ArrayList<URL>();

	public static ClassLoaderPlus getInFijiDirectory(String... relativePaths) {
		try {
			File directory = new File(getFijiDir());
			URL[] urls = new URL[relativePaths.length];
			for (int i = 0; i < urls.length; i++)
				urls[i] = new File(directory, relativePaths[i]).toURI().toURL();
			return get(urls);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Uh oh: " + e.getMessage());
		}
	}

	public static ClassLoaderPlus get(File... files) {
		try {
			URL[] urls = new URL[files.length];
			for (int i = 0; i < urls.length; i++)
				urls[i] = files[i].toURI().toURL();
			return get(urls);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Uh oh: " + e.getMessage());
		}
	}

	public static ClassLoaderPlus get(URL... urls) {
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		if (classLoader instanceof ClassLoaderPlus) {
			ClassLoaderPlus classLoaderPlus = (ClassLoaderPlus)classLoader;
			for (URL url : urls)
				classLoaderPlus.add(url);
			return classLoaderPlus;
		}
		return new ClassLoaderPlus(urls);
	}

	public static ClassLoaderPlus getRecursivelyInFijiDirectory(String... relativePaths) {
		return getRecursivelyInFijiDirectory(false, relativePaths);
	}

	public static ClassLoaderPlus getRecursivelyInFijiDirectory(boolean onlyJars, String... relativePaths) {
		try {
			File directory = new File(getFijiDir());
			ClassLoaderPlus classLoader = null;
			File[] files = new File[relativePaths.length];
			for (int i = 0; i < files.length; i++)
				classLoader = getRecursively(onlyJars, new File(directory, relativePaths[i]));
			return classLoader;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Uh oh: " + e.getMessage());
		}
	}

	public static ClassLoaderPlus getRecursively(File directory) {
		return getRecursively(false, directory);
	}

	public static ClassLoaderPlus getRecursively(boolean onlyJars, File directory) {
		try {
			ClassLoaderPlus classLoader = onlyJars ? null : get(directory);
			File[] list = directory.listFiles();
			if (list != null)
				for (File file : list)
					if (file.isDirectory())
						classLoader = getRecursively(onlyJars, file);
					else if (file.getName().endsWith(".jar"))
						classLoader = get(file);
			return classLoader;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Uh oh: " + e.getMessage());
		}
	}


	public ClassLoaderPlus() {
		this(new URL[0]);
	}

	public ClassLoaderPlus(URL... urls) {
		super(urls, Thread.currentThread().getContextClassLoader());
		Thread.currentThread().setContextClassLoader(this);
	}

	public void addInFijiDirectory(String relativePath) {
		try {
			add(new File(getFijiDir(), relativePath));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Uh oh: " + e.getMessage());
		}
	}

	public void add(String path) throws MalformedURLException {
		add(new File(path));
	}

	public void add(File file) throws MalformedURLException {
		add(file.toURI().toURL());
	}

	public void add(URL url) {
		urls.add(url);
		if (!frozen)
			addURL(url);
	}

	public void freeze() {
		frozen = true;
	}

	public String getClassPath() {
		StringBuilder builder = new StringBuilder();
		String sep = "";
		for (URL url : urls)
			if (url.getProtocol().equals("file")) {
				builder.append(sep).append(url.getPath());
				sep = File.pathSeparator;
			}
		return builder.toString();
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getName()).append("(");
		for (URL url : getURLs())
			builder.append(" ").append(url.toString());
		builder.append(" )");
		return builder.toString();
	}

	public static String getFijiDir() throws ClassNotFoundException {
		String path = System.getProperty("ij.dir");
		if (path != null)
			return path;
		final String prefix = "file:";
		final String suffix = "/jars/ij-launcher.jar!/fiji/ClassLoaderPlus.class";
		path = Class.forName("fiji.ClassLoaderPlus")
			.getResource("ClassLoaderPlus.class").getPath();
		if (path.startsWith(prefix))
			path = path.substring(prefix.length());
		if (path.endsWith(suffix))
			path = path.substring(0,
				path.length() - suffix.length());
		return path;
	}

	public String getJarPath(String className) {
		try {
			Class clazz = loadClass(className);
			String path = clazz.getResource("/" + className.replace('.', '/') + ".class").getPath();
			if (path.startsWith("file:"))
				path = path.substring(5);
			int bang = path.indexOf("!/");
			if (bang > 0)
				path = path.substring(0, bang);
			return path;
		} catch (Throwable t) {
			t.printStackTrace();
			return null;
		}
	}
}