//
// Dimensions.java
//

/*
Annotations for automatic plugins.
Copyright (C) 2010, UW-Madison LOCI

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package imagej.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.java.sezpoz.Indexable;

/**
 * Annotation interface used on IAutomaticPlugin classes.
 * Specifies required and optional dimension names.  If a
 * newly opened image's dimensions are all either required
 * or optional (and no required dimensions are missing)
 * the plugin is automatically invoked.
 *
 * Syntax:
 *  @Dimensions(required="X,Y,Z", optional="Lifetime")
 *
 * @author Aivar Grislis
 */
//TODO optional syntax using arrays:
// @Dimensions(required={"X","Y","Z"}, optional={"Lifetime"})
//
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE)
@Indexable(type=IAutoPlugin.class)
public @interface Dimensions {
    String required();
    String optional() default "";
}


