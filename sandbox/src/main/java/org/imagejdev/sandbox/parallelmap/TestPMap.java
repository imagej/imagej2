/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.imagejdev.sandbox.parallelmap;

import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 *
 * @author GBH
 */
public class TestPMap {

    Function<URI, BufferedImage> getImage = new Function<URI, BufferedImage>() {

        @Override
        public BufferedImage apply(URI uri) throws Exception {
            return ImageIO.read(uri.toURL());
        }
    };

    public void testPMap() throws Exception {
        Function<URI, BufferedImage> getImage = new Function<URI, BufferedImage>() {

            @Override
            public BufferedImage apply(URI uri) throws Exception {
                return ImageIO.read(uri.toURL());
            }
        };

        Set<URI> input = new HashSet<URI>();

        // Add image urls
        input.add(URI.create("http://www.example.com/image1.jpg"));
        input.add(URI.create("http://www.example.com/image2.jpg"));
        input.add(URI.create("http://www.example.com/image3.jpg"));
        input.add(URI.create("http://www.example.com/image4.jpg"));

        List<BufferedImage> output = new ArrayList<BufferedImage>(input.size());

        ExecutorService e = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        try {
            PMap.map(getImage, input, output, e);
        } finally {
            e.shutdown();
        }

        // do something with output
    }

    public static void main(String[] args) {
        try {
            new TestPMap().testPMap();
        } catch (Exception ex) {
            Logger.getLogger(TestPMap.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
