package ijx.util;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: joshmarinacci
 * Date: Jun 23, 2010
 * Time: 8:06:39 PM
 * To change this template use File | Settings | File Templates.
 */
public class OSUtil {
    private OSUtil() {
    }

    public static boolean isMac() {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Mac OS")) {
            return true;
        }
        return false;
    }

    public static boolean isJava6() {
//            return true;
        return (System.getProperty("java.version").startsWith("1.6"));
    }

    public static String getClipboardAsString() {
      String result = "";
      Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      //odd: the Object param of getContents is not currently used
      Transferable contents = clipboard.getContents(null);
      boolean hasTransferableText =
        (contents != null) &&
        contents.isDataFlavorSupported(DataFlavor.stringFlavor)
      ;
      if ( hasTransferableText ) {
        try {
          result = (String)contents.getTransferData(DataFlavor.stringFlavor);
        }
        catch (UnsupportedFlavorException ex){
          //highly unlikely since we are using a standard DataFlavor
          System.out.println(ex);
          ex.printStackTrace();
        }
        catch (IOException ex) {
          System.out.println(ex);
          ex.printStackTrace();
        }
      }
      return result;
    }

    public static File getJavaWSExecutable() {
        println("java.home = " + System.getProperty("java.home"));
        if(isMac()) {
            File javaws6 = new File("/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home/bin/javaws");
            if(javaws6.exists()) {
                return javaws6;
            }
            return new File("/System/Library/Frameworks/JavaVM.framework/Versions/1.5/Home/bin/javaws");
        }
        return new File(System.getProperty("java.home"),"bin/javaws");
    }
    /*
    public static void launchWebstart(String url) {
        openBrowser(url);
    }

    public static void log(Throwable ex) {
        System.out.println(ex.getMessage());
        ex.printStackTrace(System.out);
    } */


    // launching code from http://www.centerkey.com/java/browser/
    public static void openBrowser(String url) {
        String os = System.getProperty("os.name");
        println("os = " + os);
        String osName = System.getProperty("os.name");
        try {
            if (osName.startsWith("Mac OS")) {
                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL",
                        new Class[]{String.class});
                openURL.invoke(null, new Object[]{url});
            } else if (osName.startsWith("Windows")) {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            } else { //assume Unix or Linux
                String[] browsers = {
                    "firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++) {
                    if (Runtime.getRuntime().exec(
                            new String[]{"which", browsers[count]}).waitFor() == 0) {
                        browser = browsers[count];
                    }
                }
                if (browser == null) {
                    throw new Exception("Could not find web browser");
                } else {
                    Runtime.getRuntime().exec(new String[]{browser, url});
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void println(String string) {
        System.out.println(string);
    }
        /*
    public static File getSupportDir() {
        String base = getBaseStorageDir();
        return new File(base,"MaiTai");
    }*/

    public static String getBaseStorageDir() {
        String os = System.getProperty("os.name").toLowerCase();
        StringBuffer filepath = new StringBuffer(System.getProperty("user.home"));
        System.out.println("os = " + os);
        System.out.println("user.home = " + filepath);
        if(os.indexOf("windows xp") != -1) {
            u.p("doing xp");
            filepath.append(File.separator);
            filepath.append("Local Settings");
            filepath.append(File.separator);
            filepath.append("Application Data");
        } else if (os.indexOf("vista") != -1) {
            u.p("doing vista");
            filepath.append(File.separator);
            filepath.append("appdata");
            filepath.append(File.separator);
            filepath.append("locallow");
        } else if (os.startsWith("mac")) {
            u.p("doing mac");
            filepath.append(File.separator);
            filepath.append("Library");
            filepath.append(File.separator);
            filepath.append("Preferences");
        }
        filepath.append(File.separator);
        System.out.println("final filepath = " + filepath.toString());
      return filepath.toString();
    }
}