package ij;

	/** Plugins that implement this interface are notified when
		an image window is opened, closed or updated. */
	public interface ImageListener {

	public void imageOpened(ImagePlus imp);

	public void imageClosed(ImagePlus imp);

	public void imageUpdated(ImagePlus imp);

}
