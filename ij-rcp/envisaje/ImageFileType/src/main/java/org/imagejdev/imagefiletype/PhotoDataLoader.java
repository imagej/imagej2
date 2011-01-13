/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.imagejdev.imagefiletype;

/*******************************************************************************
 *
 * blueMarine - open source photo workflow
 * =======================================
 *
 * Copyright (C) 2003-2008 by Fabrizio Giudici
 * Project home page: http://bluemarine.tidalwave.it
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *******************************************************************************
 *
 * $Id: PhotoDataLoader.java 6541 2008-09-27 14:04:25Z fabriziogiudici $
 *
 ******************************************************************************/
// package it.tidalwave.bluemarine.photo.impl;
//import javax.annotation.CheckForNull;
//import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
//import it.tidalwave.logger.Logger;
//import it.tidalwave.image.jai.ImplementationFactoryJAI;

/*******************************************************************************
 *
 * @author Fabrizio Giudici
 * @version $Id: PhotoDataLoader.java 6541 2008-09-27 14:04:25Z
fabriziogiudici $
 *
 ******************************************************************************/
public class PhotoDataLoader extends MIMEResolver {

    @Override
    public String findMIMEType(FileObject fo) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
//public class PhotoMIMEResolver extends MIMEResolver {

//    private static final String CLASS = PhotoMIMEResolver.class.getName();
//    private static final Logger logger = Logger.getLogger(CLASS);
//    private final Map<String, String> mimeMapByExtension = new HashMap<String, String>();
//    private boolean initialized = false;

// public PhotoMIMEResolver()
// {
// super(getMIMETypes());
// }
    /***************************************************************************
     *
     *

     **************************************************************************/
//    @Override
//    @CheckForNull
//    public synchronized String findMIMEType(@Nonnull final FileObject fileObject) {
//        if (!initialized) {
//            initialize();
//            initialized = true;
//        }
//
//        return mimeMapByExtension.get(fileObject.getExt().toLowerCase(Locale.getDefault()));
//    }
//
//    /***************************************************************************
//     *
//     * Initializes this object. The Java Image I/O API registry
//    (IIORegistry)
//     * is asked for a list of all the available MIME types it can
//    handle, which
//     * is added to the MIME types this loader is able to handle.
//     *
//
//     **************************************************************************/
//    private void initialize() {
////
//// This means that this module must depend on jai-imageio and
//        jrawio,
//// otherwise the plugins could be not available yet.
//// See http://bluemarine.tidalwave.it/issues/browse/BM-455
////
//        ImageIO.scanForPlugins();
//        ImplementationFactoryJAI.getInstance(); // FIXME: why??
//
//        final String[] mimeTypes = ImageIO.getReaderMIMETypes();
//        Arrays.sort(mimeTypes);
//        logger.info("PhotoDataObject will support the following MIME
//
//        types:");
//
//        for (final String mimeType : mimeTypes) {
//            for (final Iterator<ImageReader> i =
//                    ImageIO.getImageReadersByMIMEType(mimeType); i.hasNext();) {
//                final ImageReader imageReader = i.next();
//                final ImageReaderSpi provider =
//                        imageReader.getOriginatingProvider();
//                final String providerClassName =
//                        imageReader.getOriginatingProvider().getClass().getName();
//
//                for (final String suffix : provider.getFileSuffixes()) {
//                    if (!suffix.equals("")) {
//                        final String suffixLC =
//                                suffix.toLowerCase(Locale.getDefault());
//                        registerExtension(suffixLC, mimeType,
//                                providerClassName);
//                        registerExtension(suffixLC + ".gz", mimeType,
//                                providerClassName);
//                    }
//                }
//            }
//        }
//    }

    /***************************************************************************
     *
     *

     **************************************************************************/
//    private void registerExtension(@Nonnull final String suffix,
//            @Nonnull final String mimeType,
//            @Nonnull final String providerClassName) {
//        if (!mimeMapByExtension.containsKey(suffix)) {
//            mimeMapByExtension.put(suffix, mimeType);
//            logger.info(">>>> %s -> %s -> %s", mimeType, suffix,
//                    providerClassName);
//        }
//    }
// private static String[] getMIMETypes()
// {
// ImageIO.scanForPlugins();
// return ImageIO.getReaderMIMETypes();
// }
}
