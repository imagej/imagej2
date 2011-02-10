package imagedisplay;


/**
 * <p>Title: PixelChangeEvent</p>
 * Emitted when cursor is moved on an image.

 */
public class PixelChangeEvent extends java.util.EventObject {

    public int x, y, value;

  public PixelChangeEvent(Object source, int value, int x, int y) {
    super(source);
    this.value = value;
    this.x = x;
    this.y = y;
  }
}
