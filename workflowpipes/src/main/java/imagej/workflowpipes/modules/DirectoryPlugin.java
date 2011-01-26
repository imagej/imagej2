/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package imagej.workflowpipes.modules;

import java.io.File;

import imagej.workflow.plugin.AbstractPlugin;
import imagej.workflow.plugin.IPlugin;
import imagej.workflow.plugin.ItemWrapper;
import imagej.workflow.plugin.annotations.Item;
import imagej.workflow.plugin.annotations.Input;
import imagej.workflow.plugin.annotations.Output;

/**
 * Plugin that checks a given directory for files of a given suffix and puts
 * out file names.
 *
 * @author aivar
 */
@Input({
    @Item(name=DirectoryPlugin.DIR, type=Item.Type.STRING),
    @Item(name=DirectoryPlugin.SUFFIX, type=Item.Type.STRING)
})
@Output({
    @Item(name=DirectoryPlugin.FILE, type=Item.Type.STRING)
})
public class DirectoryPlugin extends AbstractPlugin implements IPlugin {
    static final String DIR = "Directory";
    static final String SUFFIX = "File suffix";
    static final String FILE = "File name";

    public void process() {
        System.out.println("in DirectoryPlugin");
        String directoryName = (String) get(DIR);
        String suffix = (String) get(SUFFIX);
        File directory = new File(directoryName);
        File[] files = directory.listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                String fileName = file.getName();
                if (fileName.endsWith(SUFFIX)) {
                    System.out.println("found " + fileName);
                    put(FILE, fileName);
                }
            }
            String fileName = file.getName();
        }
    }
}

