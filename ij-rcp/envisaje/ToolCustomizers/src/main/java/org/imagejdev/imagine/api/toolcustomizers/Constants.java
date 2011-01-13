/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.imagejdev.imagine.api.toolcustomizers;

import org.openide.util.NbBundle;

/**
 * Standard names for customizers available from Customizers.getCustomizer().
 * Use these to request named customizers so that values are shared across
 * tools.
 *
 * @author Tim Boudreau
 */
public final class Constants {
    private Constants(){}
    public static final String FOREGROUND = NbBundle.getMessage(Constants.class, "foreground"); //NOI18N
    public static final String BACKGROUND = NbBundle.getMessage(Constants.class, "background"); //NOI18N
    public static final String STROKE = NbBundle.getMessage(Constants.class, "stroke"); //NOI18N
    public static final String FONT = NbBundle.getMessage(Constants.class, "font"); //NOI18N
    public static final String TEXT = NbBundle.getMessage(Constants.class, "text"); //NOI18N
    public static final String FILL = NbBundle.getMessage(Constants.class, "fill"); //NOI18N
    public static final String ANGLE = NbBundle.getMessage(Constants.class, "angle"); //NOI18N
    public static final String THRESHOLD = NbBundle.getMessage(Constants.class, "threshold"); //NOI18N
    public static final String FONT_SIZE = NbBundle.getMessage(Constants.class, "fontSize"); //NOI18N
    public static final String FONT_STYLE = NbBundle.getMessage(Constants.class, "fontStyle"); //NOI18N
    public static final String ARC_WIDTH = NbBundle.getMessage(Constants.class, "arcWidth"); //NOI18N
    public static final String ARC_HEIGHT = NbBundle.getMessage(Constants.class, "arcHeight"); //NOI18N
    public static final String ANTIALIAS = NbBundle.getMessage(Constants.class, "antialias"); //NOI18N
    public static final String SIZE = NbBundle.getMessage(Constants.class, "size"); //NOI18N
}
