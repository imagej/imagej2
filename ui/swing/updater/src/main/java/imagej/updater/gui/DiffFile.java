package imagej.updater.gui;

import imagej.log.LogService;
import imagej.updater.core.Diff;
import imagej.updater.core.Diff.Mode;
import imagej.updater.core.FileObject;
import imagej.updater.core.FilesCollection;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JFrame;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

/**
 * A {@link JFrame} to show the differences between the remote and local
 * versions of a file known to the ImageJ Updater.
 * 
 * @author Johannes Schindelin
 */
public class DiffFile extends JFrame {
	private static final long serialVersionUID = 1L;
	protected LogService log;
	protected String filename;
	protected URL remote, local;
	protected DiffView diffView;
	protected Diff diff;
	protected int diffOffset;
	protected Thread worker;

	/**
	 * Initialize the frame.
	 * 
	 * @param files
	 *            the collection of files, including information about the
	 *            update site from which we got the file
	 * @param file
	 *            the file to diff
	 * @param mode
	 *            the diff mode
	 * @throws MalformedURLException
	 */
	public DiffFile(final FilesCollection files, final FileObject file, final Mode mode) throws MalformedURLException {
		super(file.getLocalFilename() + " differences");

		log = files.log;
		filename = file.getLocalFilename();
		remote = new URL(files.getURL(file));
		local = files.prefix(file.getLocalFilename()).toURI().toURL();

		diffView = new DiffView();
		addModeLinks();
		diffOffset = diffView.getDocument().getLength();
		diff = new Diff(diffView.getPrintStream());
		show(mode);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().add(diffView);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (worker != null)
					worker.interrupt();
			}
		});
		pack();
	}

	/**
	 * Switch to a different diff mode.
	 * 
	 * @param mode
	 *            the mode to diff to
	 */
	protected synchronized void show(final Mode mode) {
		if (worker != null)
			worker.interrupt();
		worker = new Thread() {
			@Override
			public void run() {
				try {
					final Document doc = diffView.getDocument();
					try {
						doc.remove(diffOffset, doc.getLength() - diffOffset);
					} catch (BadLocationException e) {
						log.error(e);
					}
					diff.showDiff(filename, remote, local, mode);
				} catch (RuntimeException e) {
					if (!(e.getCause() instanceof InterruptedException))
						log.error(e);
				} catch (MalformedURLException e) {
					log.error(e);
				} catch (IOException e) {
					log.error(e);
				} catch (Error e) {
					log.error(e);
				}
				synchronized(DiffFile.this) {
					worker = null;
				}
			}
		};
		worker.start();
	}

	/**
	 * Add the action links for the available diff modes.
	 */
	private void addModeLinks() {
		for (final Mode mode : Mode.values()) {
			if (diffView.getDocument().getLength() > 0)
				diffView.normal(" ");
			diffView.link(mode.toString(), new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					show(mode);
				}
			});
		}
	}
}
