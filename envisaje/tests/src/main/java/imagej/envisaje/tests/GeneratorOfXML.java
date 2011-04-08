/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.envisaje.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.StringBuilder;
import org.openide.util.Exceptions;

/**
 *
 * @author GBH
 */
public class GeneratorOfXML {

    private static File file;

    public static void generate(File f) {
        file = f;
        String actionClass = "imagej.envisaje.foo.BarAction";
        String label = "Label";
        String catagory = "Edit";
        String menuPathTop = "Menu";
        String menuPathSub = "Edit";
        String iconPath = "imagej/envisaje/tests/help.png";
        
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(file));
            generate(out, actionClass, label, catagory, menuPathTop, menuPathSub, iconPath);
            out.close();
            System.out.println("file.toURL():" + file.toURL());
        } catch (IOException e) {
        }

    }

    public static void generate(
            BufferedWriter out,
            String actionClass,
            String label,
            String catagory,
            String menuPathTop,
            String menuPathSub,
            String iconPath) {

        String actionClassDash = actionClass.replace('.', '-');
        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<!DOCTYPE filesystem PUBLIC \"-//NetBeans//DTD Filesystem 1.2//EN\"\n");
        sb.append("                            \"http://www.netbeans.org/dtds/filesystem-1_2.dtd\">\n");
        sb.append("<filesystem>\n");
        sb.append("    <folder name=\"Actions\">\n");
        sb.append("        <file name=\"" + actionClassDash + ".instance\">\n");
        sb.append("            <!--" + actionClass + "-->\n");
        sb.append("            <attr name=\"delegate\" newvalue=\"" + actionClass + "\"/>\n");
        sb.append("            <attr name=\"displayName\" stringvalue=\"" + label + "\"/>\n");
        sb.append("            <attr name=\"iconBase\" stringvalue=\"" + iconPath + "\"/>\n");
        sb.append("            <attr methodvalue=\"org.openide.awt.Actions.alwaysEnabled\" name=\"instanceCreate\"/>\n");
        sb.append("        </file>\n");
        sb.append("    </folder>\n");
        sb.append("    <folder name=\"" + menuPathTop + "\">\n");
        sb.append("        <folder name=\"" + menuPathSub + "\">\n");
        sb.append("            <file name=\"" + actionClassDash + ".shadow\">\n");
        sb.append("            <!--" + actionClass + "-->\n");
        sb.append("                <attr name=\"originalFile\" stringvalue=\"Actions/" + actionClassDash + ".instance\"/>\n");
        sb.append("                <attr intvalue=\"1\" name=\"position\"/>\n");
        sb.append("            </file>\n");
        sb.append("        </folder>\n");
        sb.append("    </folder>\n");
        //        sb.append("    <folder name=\"Toolbars\">\n");
        //        sb.append("        <folder name=\"File\">\n");
        //        sb.append("            <file name=\"imagej-envisaje-tests-DemoDynamicAction.shadow\">\n");
        //        sb.append("                <!--imagej.envisaje.tests.DemoDynamicAction-->\n");
        //        sb.append("                <attr name=\"originalFile\" stringvalue=\"Actions/imagej-envisaje-tests-DemoDynamicAction.instance\"/>\n");
        //        sb.append("                <attr intvalue=\"1\" name=\"position\"/>\n");
        //        sb.append("            </file>\n");
        //        sb.append("        </folder>\n");
        //        sb.append("    </folder>\n");
        sb.append("</filesystem>\n");

        System.out.println(sb.toString());
        byte[] barray = sb.toString().getBytes();
        try {
            out.write(sb.toString());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
            // FileObject newMenuItem = Repository.getDefault().getDefaultFileSystem().getRoot().
            //         getFileObject("Menu/Some/org-netbeans-modules-test-TestAction.shadow");
    //        try {
    //            FileSystem fragment = FileUtil.createMemoryFileSystem();
    //            FileObject f = FileUtil.createData(fragment.getRoot(), "");
    //            System.out.println(f.toString());
    //            FileLock lock = f.lock();
    //            OutputStream out = f.getOutputStream(lock);
    //            //In the shadow file, write the reference to the instance file,
    //            //which must match the registration in the module's layer.xml file:
    //            OutputStreamWriter out1 = new OutputStreamWriter(out, "UTF-8");
    //            out.write(barray);
    //            out1.flush();
    //            out1.close();
    //            out.close();
    //            lock.releaseLock();
    //            URL url = URLMapper.findURL(f, URLMapper.INTERNAL);
    //            System.out.println("URL: " + url.toString() + " path: " + url.getPath());
    //            XMLFileSystem xmlF = new XMLFileSystem(url);
    //        } catch (Exception ex) {
    //            Exceptions.printStackTrace(ex);
    //        }
            //        StringBuilder sb = new StringBuilder();
            //        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            //        sb.append("<!DOCTYPE filesystem PUBLIC \\\"-//NetBeans//DTD Filesystem 1.2//EN\\\"\n");
            //        sb.append("                            \"http://www.netbeans.org/dtds/filesystem-1_2.dtd\">\n");
            //        sb.append("<filesystem>\n");
            //        sb.append("    <folder name=\"Actions\">\n");
            //        sb.append("        <file name=\"imagej-envisaje-tests-DemoDynamicAction.instance\">\n");
            //        sb.append("            <!--imagej.envisaje.tests.DemoDynamicAction-->\n");
            //        sb.append("            <attr name=\"delegate\" newvalue=\"imagej.envisaje.tests.DemoDynamicAction\"/>\n");
            //        sb.append("            <attr name=\"displayName\" stringvalue=\"key\"/>\n");
            //        sb.append("            <attr name=\"iconBase\" stringvalue=\"imagej/envisaje/tests/help.png\"/>\n");
            //        sb.append("            <attr methodvalue=\"org.openide.awt.Actions.alwaysEnabled\" name=\"instanceCreate\"/>\n");
            //        sb.append("        </file>\n");
            //        sb.append("    </folder>\n");
            //        sb.append("    <folder name=\"Menu\">\n");
            //        sb.append("        <folder name=\"File\">\n");
            //        sb.append("            <file name=\"imagej-envisaje-tests-DemoDynamicAction.shadow\">\n");
            //        sb.append("                <!--imagej.envisaje.tests.DemoDynamicAction-->\n");
            //        sb.append("                <attr name=\"originalFile\" stringvalue=\"Actions/imagej-envisaje-tests-DemoDynamicAction.instance\"/>\n");
            //        sb.append("                <attr intvalue=\"1\" name=\"position\"/>\n");
            //        sb.append("            </file>\n");
            //        sb.append("        </folder>\n");
            //        sb.append("    </folder>\n");
            //        sb.append("    <folder name=\"Toolbars\">\n");
            //        sb.append("        <folder name=\"File\">\n");
            //        sb.append("            <file name=\"imagej-envisaje-tests-DemoDynamicAction.shadow\">\n");
            //        sb.append("                <!--imagej.envisaje.tests.DemoDynamicAction-->\n");
            //        sb.append("                <attr name=\"originalFile\" stringvalue=\"Actions/imagej-envisaje-tests-DemoDynamicAction.instance\"/>\n");
            //        sb.append("                <attr intvalue=\"1\" name=\"position\"/>\n");
            //        sb.append("            </file>\n");
            //        sb.append("        </folder>\n");
            //        sb.append("    </folder>\n");
            //        sb.append("</filesystem>\n");
            //        System.out.println(sb.toString());


            //        StringBuilder sb = new StringBuilder();
    //        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    //        sb.append("<!DOCTYPE filesystem PUBLIC \\\"-//NetBeans//DTD Filesystem 1.2//EN\\\"\n");
    //        sb.append("                            \"http://www.netbeans.org/dtds/filesystem-1_2.dtd\">\n");
    //        sb.append("<filesystem>\n");
    //        sb.append("    <folder name=\"Actions\">\n");
    //        sb.append("        <file name=\"imagej-envisaje-tests-DemoDynamicAction.instance\">\n");
    //        sb.append("            <!--imagej.envisaje.tests.DemoDynamicAction-->\n");
    //        sb.append("            <attr name=\"delegate\" newvalue=\"imagej.envisaje.tests.DemoDynamicAction\"/>\n");
    //        sb.append("            <attr name=\"displayName\" stringvalue=\"key\"/>\n");
    //        sb.append("            <attr name=\"iconBase\" stringvalue=\"imagej/envisaje/tests/help.png\"/>\n");
    //        sb.append("            <attr methodvalue=\"org.openide.awt.Actions.alwaysEnabled\" name=\"instanceCreate\"/>\n");
    //        sb.append("        </file>\n");
    //        sb.append("    </folder>\n");
    //        sb.append("    <folder name=\"Menu\">\n");
    //        sb.append("        <folder name=\"File\">\n");
    //        sb.append("            <file name=\"imagej-envisaje-tests-DemoDynamicAction.shadow\">\n");
    //        sb.append("                <!--imagej.envisaje.tests.DemoDynamicAction-->\n");
    //        sb.append("                <attr name=\"originalFile\" stringvalue=\"Actions/imagej-envisaje-tests-DemoDynamicAction.instance\"/>\n");
    //        sb.append("                <attr intvalue=\"1\" name=\"position\"/>\n");
    //        sb.append("            </file>\n");
    //        sb.append("        </folder>\n");
    //        sb.append("    </folder>\n");
    //        sb.append("    <folder name=\"Toolbars\">\n");
    //        sb.append("        <folder name=\"File\">\n");
    //        sb.append("            <file name=\"imagej-envisaje-tests-DemoDynamicAction.shadow\">\n");
    //        sb.append("                <!--imagej.envisaje.tests.DemoDynamicAction-->\n");
    //        sb.append("                <attr name=\"originalFile\" stringvalue=\"Actions/imagej-envisaje-tests-DemoDynamicAction.instance\"/>\n");
    //        sb.append("                <attr intvalue=\"1\" name=\"position\"/>\n");
    //        sb.append("            </file>\n");
    //        sb.append("        </folder>\n");
    //        sb.append("    </folder>\n");
    //        sb.append("</filesystem>\n");
    //        System.out.println(sb.toString());


    }
}
