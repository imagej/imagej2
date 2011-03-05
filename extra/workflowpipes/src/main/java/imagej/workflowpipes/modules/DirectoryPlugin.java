//
// DirectoryPlugin.java
//

/*
ImageJ software for multidimensional image processing and analysis.

Copyright (c) 2010, ImageJDev.org.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the names of the ImageJDev.org developers nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
*/

package imagej.workflowpipes.modules;

import java.io.File;

import imagej.workflow.plugin.AbstractPlugin;
import imagej.workflow.plugin.IPlugin;
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
        System.out.println("directory is " + directory);
        if (null != files) {
            for (File file : files) {
                System.out.println("file is " + file.getName());
                if (!file.isDirectory()) {
                    String fileName = file.getName();
                    if (fileName.endsWith(suffix)) {
                        System.out.println("found " + fileName);
                        put(FILE, fileName);
                    }
                }
                String fileName = file.getName();
            }
        }
    }
}

