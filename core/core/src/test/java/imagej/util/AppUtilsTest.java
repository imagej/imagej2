package imagej.util;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class AppUtilsTest {

	@Test
	public void testBaseDirectory() {
		assertNull(AppUtils.getBaseDirectory(new File(
			"/home/blub/.m2/repository/org/dscho/secret/1.0/secret-1.0.jar"), null));
		assertEquals(new File("/tmp"), AppUtils.getBaseDirectory(new File(
			"/tmp/app/target/classes"), "app"));
		assertEquals(new File("/tmp"), AppUtils.getBaseDirectory(new File(
			"/tmp/app/target/ij-app-1.57.jar"), "app"));
	}

}
