/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package imagej.envisaje.imagenb;

import javax.imageio.ImageIO;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 * Data loader which recognizes image files.
 * @author Petr Hamernik, Jaroslav Tulach
 * @author Marian Petras
 */
public class ImageDataLoader extends UniFileLoader {

    /** Generated serial version UID. */
    static final long serialVersionUID =-8188309025795898449L;
    
    /** MIME-type of BMP files */
    private static final String BMP_MIME_TYPE = "image/bmp";            //NOI18N
    /** is BMP format support status known? */
    private static boolean bmpSupportStatusKnown = false;
    
    /** Creates new image loader. */
    public ImageDataLoader() {
        // Set the representation class.
        super("org.netbeans.modules.image.ImageDataObject"); // NOI18N
        
        ExtensionList ext = new ExtensionList();
        ext.addMimeType("image/gif");                                   //NOI18N
        ext.addMimeType("image/jpeg");                                  //NOI18N
        ext.addMimeType("image/png");                                   //NOI18N
        setExtensions(ext);
    }
    
    protected FileObject findPrimaryFile(FileObject fo){
        FileObject primFile = super.findPrimaryFile(fo);
        
        if ((primFile == null)
                && !bmpSupportStatusKnown 
                && !fo.isFolder()
                && fo.getMIMEType().equals(BMP_MIME_TYPE)) {
            try {
                if (ImageIO.getImageReadersByMIMEType(BMP_MIME_TYPE).hasNext()){
                    getExtensions().addMimeType(BMP_MIME_TYPE);
                    primFile = fo;
                }
            } finally {
                bmpSupportStatusKnown = true;
            }
        }
        
        return primFile;
    }
    
    /** Gets default display name. Overrides superclass method. */
    protected String defaultDisplayName() {
        return NbBundle.getBundle(ImageDataLoader.class).getString("PROP_ImageLoader_Name");
    }
    
    /**
     * This methods uses the layer action context so it returns
     * a non-<code>null</code> value.
     *
     * @return  name of the context on layer files to read/write actions to
     */
    protected String actionsContext () {
        return "Loaders/image/png-gif-jpeg-bmp/Actions/";               //NOI18N
    }
    
    /** Create the image data object.
     * @param primaryFile the primary file (e.g. <code>*.gif</code>)
     * @return the data object for this file
     * @exception DataObjectExistsException if the primary file already has a data object
     * @exception java.io.IOException should not be thrown
     */
    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, java.io.IOException {
        return new ImageDataObject(primaryFile, this);
    }

}
