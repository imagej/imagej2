package imagedisplay;

public class Equalizer {

    static int[] histogram;
    static int max,  range;
    static int slice;
    static boolean canceled;
    static int width = 0;
    static int height = 0;
    static boolean equalize;
    static boolean normalize;
    static double saturated = 0.5;
    static boolean classicEqualization = false;

    //public Equalizer() {}

    /*  byte reverse[] = new byte[256];
    for (int j=0; j<200; j++){
    reverse[j]=(byte)(256-j);
    }
    ByteLookupTable blut=new ByteLookupTable(0, reverse);
    LookupOp lop = new LookupOp(blut, null);
    lop.filter(bi,bimg);
     */
    public static short[] equalize(short[] img, int w, int h) {
        width = w;
        height = h;
        getHistogram(img);
        if (histogram == null) {
            return null;
        }
        max = 4095;
        range = 4095;

        double sum;
        sum = getWeightedValue(0);
        for (int i = 1; i < max; i++) {
            sum += 2 * getWeightedValue(i);
        }
        sum += getWeightedValue(max);
        double scale = range / sum;
        short[] lut = new short[range + 1];
        lut[0] = 0;
        sum = getWeightedValue(0);
        for (int i = 1; i < max; i++) {
            double delta = getWeightedValue(i);
            sum += delta;
            lut[i] = (short) Math.round(sum * scale);
            sum += delta;
        }
        lut[max] = (short) max;
        return lut;
    }

    public static byte[] equalize(byte[] img, int w, int h) {
        width = w;
        height = h;
        getHistogram(img);
        if (histogram == null) {
            return null;
        }
        max = 255;
        range = 255;
        double sum;
        sum = getWeightedValue(0);
        for (int i = 1; i < max; i++) {
            sum += 2 * getWeightedValue(i);
        }
        sum += getWeightedValue(max);
        double scale = range / sum;
        byte[] lut = new byte[range + 1];
        lut[0] = 0;
        sum = getWeightedValue(0);
        for (int i = 1; i < max; i++) {
            double delta = getWeightedValue(i);
            sum += delta;
            lut[i] = (byte) Math.round(sum * scale);
            sum += delta;
        }
        lut[max] = (byte) max;
        return lut;
    }

    private static double getWeightedValue(int i) {
        int h = histogram[i];
        if (h < 2 || classicEqualization) {
            return (double) h;
        }
        return Math.sqrt((double) (h));
    }

    public static void getHistogram(Object img) {
        if (img instanceof byte[]) {
            histogram = new int[256];
        } else {
            histogram = new int[4096];
        }
        int v = 0;
        for (int y = 0; y < height; y++) {
            int i = y * width;
            for (int x = 0; x < (width); x++) {
                if (img instanceof byte[]) {
                    v = ((byte[]) img)[i++] & 0xff;
                } else {
                    v = ((short[]) img)[i++] & 0xffff;
                }
                histogram[v]++;
            }
        }
        return;
    }
}