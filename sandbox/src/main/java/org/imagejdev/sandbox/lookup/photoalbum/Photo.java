/*
 * From:
 * How to Create a Pluggable Photo Album in Java
 * http://java.dzone.com/news/how-create-pluggable-photo-alb
 */
package org.imagejdev.sandbox.lookup.photoalbum;

import javax.swing.Icon;

public interface Photo {

    Icon[] getPhoto();

    String[] getDescription();
}
