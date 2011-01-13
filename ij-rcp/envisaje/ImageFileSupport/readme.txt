To use this module:

1. Install it (and the included JGoodies DataBinding module) into the NetBeans 5.0 IDE.

2. Open or create a project in the IDE (for example, a Java project)

3. Right click on some directory within that project and choose New->File/Folder...

4. Choose "Other" in the Dialog box and select "emptydog.dog"

5.  Specify a name and location for the file to be created.

You should then see a node representing the new (empty) dog file you have just created. 
Right-click on the node, edit the dog using the custom editor.  You might also open
the custom viewer (also from the context menu) at the same time so you can see how
it keeps in synch with the editor.  

After you have a feel for how it works from the outside, look at the code behind it
to see how it was done.  The inline comments hopefully explain things pretty well, 
but you might also consult the NetBeans Javadoc for more information.

Comments, suggestions and improvements are welcome.

    Tom Wheeler
    e-mail: tom@tomwheeler.com


NOTE: This module uses the enum feature of JDK 1.5, so it is not compatible with JDK 1.4.