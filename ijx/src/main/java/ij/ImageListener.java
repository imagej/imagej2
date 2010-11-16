package ij;

import ijx.IjxImagePlus;

	/** Plugins that implement this interface are notified when
		an image window is opened, closed or updated. */
	public interface ImageListener {

	public void imageOpened(IjxImagePlus imp);

	public void imageClosed(IjxImagePlus imp);

	public void imageUpdated(IjxImagePlus imp);

}
