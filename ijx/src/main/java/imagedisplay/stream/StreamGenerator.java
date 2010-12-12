/*
 * StreamGenerator.java
 * Created on November 8, 2006, 11:17 AM
 */
package imagedisplay.stream;

/**
 *
 * @author GBH
 */
public interface StreamGenerator {

    /*
     *  implemented by Cameras and IntensityMeasurer
     * /
    
    /*
     * int width;
     * int hieght;
     * boolean streaming;
     * StreamSource  source;
     */   
    // Only 8-bit supported
    
    public StreamSource getStreamSource();

    public int getWidth();

    public int getHeight();

    public void startStream();

    public void stopStream();

    public boolean isStreaming();

    public void closeStreamSource();

}
