/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imagejdev.sandbox.lookup.photoalbum;


import java.util.Collection;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.Lookup.Template;

public class PhotoService {

    private static PhotoService service;
    private Lookup photoLookup;
    private Collection photos;
    private Template photoTemplate;
    private Result photoResults;

    private PhotoService() {
        photoLookup = Lookup.getDefault();
        photoTemplate = new Template(Photo.class);
        photoResults = photoLookup.lookup(photoTemplate);
        photos = photoResults.allInstances();
    }

    public static synchronized PhotoService getInstance() {
        if (service == null) {
            service = new PhotoService();
        }
        return service;
    }

    public Collection getDefinitions() {
        return photos;
    }

}