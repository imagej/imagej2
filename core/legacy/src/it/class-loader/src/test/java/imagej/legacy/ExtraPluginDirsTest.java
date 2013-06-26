package imagej.legacy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import ij.IJ;
import ij.ImageJ;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import org.junit.Before;
import org.junit.Test;


public class ExtraPluginDirsTest {

	static {
		DefaultLegacyService.preinit();
	}

	@Before
	public void makeJar() throws IOException {
		final String ij1PluginsDir = System.getProperty("ij1.plugin.dirs");
		assertNotNull(ij1PluginsDir);
		final File jarFile = new File(ij1PluginsDir, "Set_Property.jar");
		final String path = "Set_Property.class";
		final InputStream in = getClass().getResourceAsStream("/" + path);
		final byte[] buffer = new byte[16384];
		final JarOutputStream jar = new JarOutputStream(new FileOutputStream(jarFile));
		final ZipEntry entry = new ZipEntry(path);
		jar.putNextEntry(entry);
		for (;;) {
			int count = in.read(buffer);
			if (count < 0) break;
			jar.write(buffer,  0, count);
		}
		jar.close();
		in.close();
	}

	@Test
	public void findsExtraPluginDir() {
		final String key = "random-" + Math.random();
		System.setProperty(key, "321");
		new ImageJ(ImageJ.NO_SHOW);
		IJ.run("Set Property", "key=" + key + " value=123");
		assertEquals("123", System.getProperty(key));
	}
}
