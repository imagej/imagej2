package imagedisplay.stream;

import imagedisplay.util.StaticSwingUtils;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.IndexColorModel;
import java.awt.image.MemoryImageSource;

import java.util.ArrayList;

public class StreamSource {

    StreamGenerator generator;
    public MemoryImageSource mis;
    public byte[] imageArrayByte = null;
    int w = 0;
    int h = 0;
    int frames = 0;
    byte startIntensity = 10;
    private ArrayList consumers = new ArrayList();
    long last = System.nanoTime();
    StreamMonitorFPS mon;

    public StreamSource(int w, int h, StreamGenerator generator) {
        this.generator = generator;
        this.w = w;
        this.h = h;
        initializeStream();
    }

    void initializeStream() {
        // create a ColorModel with a gray scale palette:
        byte[] gray = new byte[256];
        for (int i = 0; i < 256; i++) {
            gray[i] = (byte) i;
        }
        IndexColorModel cm = new IndexColorModel(8, 256, gray, gray, gray);

        // create imageArray to be passed to JNI with LuCamInterface.setDisplayArray8A()
        imageArrayByte = new byte[h * w];
        for (int i = 0; i < imageArrayByte.length; i++) {
            imageArrayByte[i] = startIntensity;
        }

        // construct a MemoryImageSource that can be used to create the image
        mis = new MemoryImageSource(w, h, cm, imageArrayByte, 0, w);
        mis.setAnimated(true);
        mis.setFullBufferUpdates(true);
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
    }

    public void attachToStream(ImageConsumer consumer) {
        if (mis == null) {
            initializeStream();
        }
        consumers.add(consumer);
        mis.addConsumer(consumer);
    }

    public void detachFromStream(ImageConsumer consumer) {
        // @todo What if in opposite order?
        if (consumers.contains(consumer)) {
            consumers.remove(consumer);
            mis.removeConsumer(consumer);
        }
        if (consumers.isEmpty()) {
            // stop streaming from device
            generator.stopStream();
            generator.closeStreamSource();
        //mis = null;
        }
    }

    public void startProduction(ImageConsumer consumer) {
        mis.startProduction(consumer);
    }

    public ImageProducer getImageProducer() {
        return mis;
    }

    public void attachMonitor(StreamMonitorFPS mon) {
        this.mon = mon;
    }

    public void detachMonitor() {
        this.mon = null;
    }

    // Callback
    // this is called from the streaming mode callback function of the camera
    public void callBack() {
        //long dur = (System.nanoTime() - last); // nanoseconds per frame
        //double fps = 1000000000.0 / dur;
        frames++;
        //      if(cameraModel!=null) {
        //          cameraModel.setCurrentFPS(fps);
        //      }
        //System.out.println("calledback.");

        //if ((frames % 3) == 0) {

        if (true) {
            StaticSwingUtils.dispatchToEDT(new Runnable() {
                public void run() {
                    if (mis != null) {
                        mis.newPixels();
                        if (mon != null) {
                            mon.update(frames);
                        }
                    }
                }
            });
        }

    //last = System.nanoTime();
    }

}
