/*
 * #%L
 * ImageJ software for multidimensional image processing and analysis.
 * %%
 * Copyright (C) 2009 - 2012 Board of Regents of the University of
 * Wisconsin-Madison, Broad Institute of MIT and Harvard, and Max Planck
 * Institute of Molecular Cell Biology and Genetics.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * The views and conclusions contained in the software and documentation are
 * those of the authors and should not be interpreted as representing official
 * policies, either expressed or implied, of any organization.
 * #L%
 */

package imagej.updater.gui;

import imagej.updater.util.Canceled;
import imagej.updater.util.Progress;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

@SuppressWarnings("serial")
public class ProgressDialog extends JDialog implements Progress {

	JProgressBar progress;
	JButton detailsToggle;
	int toggleHeight = -1;
	JScrollPane detailsScrollPane;
	Details details;
	Detail latestDetail;
	String title;
	boolean canceled;
	protected long latestUpdate;

	public ProgressDialog(final Frame owner) {
		this(owner, null);
	}

	public ProgressDialog(final Frame owner, final String title) {
		super(owner);

		final Container root = getContentPane();
		root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
		progress = new JProgressBar();
		progress.setMinimum(0);
		root.add(progress);

		final JPanel buttons = new JPanel();
		detailsToggle = new JButton("Show Details");
		detailsToggle.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent event) {
				toggleDetails();
			}
		});
		buttons.add(detailsToggle);
		final JButton cancel = new JButton("Cancel");
		cancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {
				canceled = true;
				ProgressDialog.this.dispose();
			}
		});
		buttons.add(cancel);
		buttons.setMaximumSize(buttons.getMinimumSize());
		root.add(buttons);

		details = new Details();
		detailsScrollPane =
			new JScrollPane(details,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		detailsScrollPane.setVisible(false);
		root.add(detailsScrollPane);

		if (title != null) setTitle(title);
		pack();

		if (owner != null) {
			final Dimension o = owner.getSize();
			final Dimension size = getSize();
			if (size.width < o.width / 2) {
				size.width = o.width / 2;
				setSize(size);
			}
			setLocation(owner.getX() + (o.width - size.width) / 2, owner.getY() +
				(o.height - size.height) / 2);
		}

		final KeyAdapter keyAdapter = new KeyAdapter() {

			@Override
			public void keyReleased(final KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) cancel();
			}
		};
		root.addKeyListener(keyAdapter);
		detailsToggle.addKeyListener(keyAdapter);
		cancel.addKeyListener(keyAdapter);

		setLocationRelativeTo(null);
		setVisible(true);
	}

	public void cancel() {
		canceled = true;
	}

	protected void checkIfCanceled() {
		if (canceled) throw new Canceled();
	}

	@Override
	public void setTitle(final String title) {
		this.title = title;
		progress.setStringPainted(true);
		setTitle();
	}

	protected void setTitle() {
		checkIfCanceled();
		if (detailsScrollPane.isVisible() || latestDetail == null) progress
			.setString(title);
		else progress.setString(title + ": " + latestDetail.getString());
		repaint();
	}

	@Override
	public void setCount(final int count, final int total) {
		checkIfCanceled();
		if (updatesTooFast()) return;
		progress.setMaximum(total);
		progress.setValue(count);
		repaint();
	}

	@Override
	public void addItem(final Object item) {
		checkIfCanceled();
		details.addDetail(item.toString());
		if (updatesTooFast() && !detailsScrollPane.isVisible()) return;
		setTitle();
		validate();
		repaint();
	}

	@Override
	public void setItemCount(final int count, final int total) {
		checkIfCanceled();
		if (updatesTooFast()) return;
		latestDetail.setMaximum(total);
		latestDetail.setValue(count);
		repaint();
	}

	@Override
	public void itemDone(final Object item) {
		checkIfCanceled();
		latestDetail.setValue(latestDetail.getMaximum());
	}

	@Override
	public void done() {
		if (latestDetail != null) latestDetail.setValue(latestDetail.getMaximum());
		progress.setValue(progress.getMaximum());
		dispose();
	}

	public void toggleDetails() {
		final boolean show = !detailsScrollPane.isVisible();
		detailsScrollPane.setVisible(show);
		detailsScrollPane.invalidate();
		detailsToggle.setText(show ? "Hide Details" : "Show Details");
		setTitle();

		final Dimension dimension = getSize();
		if (toggleHeight == -1) toggleHeight = dimension.height + 100;
		setSize(new Dimension(dimension.width, toggleHeight));
		toggleHeight = dimension.height;
	}

	class Details extends JPanel {

		Details() {
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		}

		public void addDetail(final String title) {
			addDetail(new Detail(title));
		}

		public void addDetail(final Detail detail) {
			add(detail);
			final JScrollBar vertical = detailsScrollPane.getVerticalScrollBar();
			vertical.setValue(vertical.getMaximum());
			latestDetail = detail;
		}
	}

	class Detail extends JProgressBar {

		Detail(final String text) {
			setStringPainted(true);
			setString(text);
		}
	}

	protected boolean updatesTooFast() {
		if (System.currentTimeMillis() - latestUpdate < 50) return true;
		latestUpdate = System.currentTimeMillis();
		return false;
	}

	public static void main(final String[] args) {
		final ProgressDialog dialog = new ProgressDialog(null, "Hello");
		dialog.addItem("Bello");
		dialog.setVisible(true);
	}
}
