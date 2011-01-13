/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imagejdev.imagefiletype;

import org.openide.cookies.CloseCookie;
import org.openide.cookies.OpenCookie;
import org.openide.loaders.OpenSupport;
import org.openide.windows.CloneableTopComponent;

/**
 *
 * @author GBH
 */
public class ImgOpenSupport extends OpenSupport implements OpenCookie, CloseCookie {

    public ImgOpenSupport(TiffDataObject.Entry entry) {
        super(entry);
    }

    @Override
    protected CloneableTopComponent createCloneableTopComponent() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

//  @Override
//    protected CloneableTopComponent createCloneableTopComponent() {
//        TiffDataObject dobj = (TiffDataObject) entry.getDataObject();
//        imagej.imageframe.ImgDisplayTopComponent =     tc = new ImgTopComponent();
//        tc.setDisplayName(dobj.getName());
//        dobj.
//        tc.add(new DisplayJAI());
//        return tc;
//    }

}
