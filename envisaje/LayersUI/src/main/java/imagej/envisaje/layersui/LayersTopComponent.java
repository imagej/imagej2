package imagej.envisaje.layersui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import imagej.envisaje.api.editing.LayerFactory;
import imagej.envisaje.api.image.Layer;
import imagej.envisaje.api.image.Picture;
import imagej.envisaje.spi.image.LayerImplementation;
import imagej.envisaje.misccomponents.PopupSliderUI;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.Mode;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Top component which displays something.
 */

final class LayersTopComponent extends TopComponent implements LookupListener,
			ChangeListener,
			ActionListener,
			Runnable {

		private static final long serialVersionUID = -1323452L;
		private static LayersTopComponent instance;
		private final JPanel innerPanel = new JPanel();
		private final JToolBar bar = new JToolBar();
		private final InstanceContent content = new InstanceContent();
		private final AbstractLookup lkp = new AbstractLookup(content);
		private final JScrollPane scrollPane = new JScrollPane(innerPanel);
		private final JButton newButton;

		private LayersTopComponent() {
			initComponents();
			scrollPane.setBorder(BorderFactory.createEmptyBorder());
			scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
			setName(NbBundle.getMessage(LayersTopComponent.class,
					"CTL_LayersTopComponent"));
			setToolTipText(NbBundle.getMessage(LayersTopComponent.class,
					"HINT_LayersTopComponent"));
			innerPanel.setLayout(new LM());
			// innerPanel.setLayout (new BoxLayout (innerPanel, BoxLayout.Y_AXIS));
			add(scrollPane, BorderLayout.CENTER);
			Dimension bdim = new Dimension(25, 25);
			newButton = new JButton();

			newButton.setPreferredSize(bdim);
			newButton.setToolTipText(NbBundle.getMessage(LayersTopComponent.class,
					"LBL_New"));
			newButton.setIcon(new ImageIcon(Utilities.loadImage("imagej/envisaje/layersui/resources/newlayer.png")));
			newButton.setName(NAME_NEW);
			newButton.addActionListener(this);
			newButton.setEnabled(false);
			bar.add(newButton);
			JButton delButton = new JButton();

			delButton.setToolTipText(NbBundle.getMessage(LayersTopComponent.class,
					"LBL_Delete"));
			delButton.setIcon(new ImageIcon(Utilities.loadImage("imagej/envisaje/layersui/resources/delete.png")));
			delButton.setName(NAME_DELETE);
			delButton.addActionListener(this);
			delButton.setEnabled(false);
			delButton.setPreferredSize(bdim);
			bar.add(delButton);
			JButton upButton = new JButton();

			upButton.setPreferredSize(bdim);
			upButton.setToolTipText(NbBundle.getMessage(LayersTopComponent.class,
					"LBL_Up"));
			upButton.setIcon(new ImageIcon(Utilities.loadImage("imagej/envisaje/layersui/resources/up.png")));
			upButton.setName(NAME_UP);
			upButton.setEnabled(false);
			upButton.addActionListener(this);
			bar.add(upButton);
			JButton downButton = new JButton();

			downButton.setPreferredSize(bdim);
			downButton.setToolTipText(NbBundle.getMessage(LayersTopComponent.class,
					"LBL_Down"));
			downButton.setIcon(new ImageIcon(Utilities.loadImage("imagej/envisaje/layersui/resources/down.png")));
			downButton.setName(NAME_DOWN);
			downButton.setEnabled(false);
			downButton.addActionListener(this);
			bar.add(downButton);
			JButton dupButton = new JButton();

			dupButton.setPreferredSize(bdim);
			dupButton.setToolTipText(NbBundle.getMessage(LayersTopComponent.class,
					"LBL_Duplicate"));
			dupButton.setIcon(new ImageIcon(Utilities.loadImage("imagej/envisaje/layersui/resources/clone.png")));
			dupButton.setName(NAME_DUP);
			dupButton.setEnabled(false);
			dupButton.addActionListener(this);
			bar.add(dupButton);
			JButton flattenButton = new JButton();

			flattenButton.setPreferredSize(bdim);
			flattenButton.setToolTipText(NbBundle.getMessage(LayersTopComponent.class,
					"LBL_Flatten"));
			flattenButton.setIcon(new ImageIcon(Utilities.loadImage("imagej/envisaje/layersui/resources/flatten.png")));
			flattenButton.setName(NAME_FLATTEN);
			flattenButton.setEnabled(false);
			flattenButton.addActionListener(this);
			bar.add(flattenButton);
			add(bar, BorderLayout.SOUTH);
			associateLookup(lkp);
			// Show the empty label
			updateContents();
		}
		private static final String NAME_NEW = "new";
		private static final String NAME_DELETE = "delete";
		private static final String NAME_UP = "up";
		private static final String NAME_DOWN = "down";
		private static final String NAME_DUP = "dup";
		private static final String NAME_FLATTEN = "flatten";

		@Override
		public void open() {
			Mode m = WindowManager.getDefault().findMode("layers");

			if (m != null) {
				m.dockInto(this);
			}
			super.open();
		}
		// public Dimension getPreferredSize () {
		// Handles a bug that makes main window layout go insane
		// return new Dimension (200, 300);
		// }

		/**
		 * This method is called from within the constructor to
		 * initialize the form.
		 * WARNING: Do NOT modify this code. The content of this method is
		 * always regenerated by the Form Editor.
		 */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents

    private void initComponents() {
        setLayout(new BorderLayout());
    }
    // </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

		/**
		 *
		 * Gets default instance. Don't use directly, it reserved for '.settings' file only,
		 * i.e. deserialization routines, otherwise you can get non-deserialized instance.
		 */
		public static synchronized LayersTopComponent getDefault() {
			if (instance == null) {
				instance = new LayersTopComponent();
			}
			return instance;
		}

		@Override
		public int getPersistenceType() {
			return TopComponent.PERSISTENCE_ALWAYS;
		}
		Result pictureLookupResult = null;

		@Override
		public void componentOpened() {
			pictureLookupResult = Utilities.actionsGlobalContext().lookupResult(Picture.class);
			pictureLookupResult.addLookupListener(this);
			pictureLookupResult.allInstances(); //initialize
			updatePicture();
			startTimer();
		}

		private void updatePicture() {
			Picture curr =
					Utilities.actionsGlobalContext().lookup(Picture.class);

			setPicture(curr);
		}

		@Override
		public void componentClosed() {
			setPicture(null);
			if (pictureLookupResult != null) {
				pictureLookupResult.removeLookupListener(this);
				pictureLookupResult = null;
			}
			stopTimer();
		}
		private Picture picture = null;
		private final Timer timer = new Timer(5000, this);

		private void startTimer() {
			timer.setRepeats(true);
			timer.start();
		}

		private void stopTimer() {
			timer.stop();
		}

		private void setPicture(Picture layers) {
			if (this.picture == layers) {
				return;
			}
			if (this.picture != null) {
				this.picture.removeChangeListener(this);
			}
			this.picture = layers;
			if (this.picture != null) {
				this.picture.addChangeListener(this);
			}
			Component[] c = bar.getComponents();

			for (int i = 0; i < c.length; i++) {
				c[i].setEnabled(layers != null);
			}
			content.set(layers == null ? Collections.EMPTY_SET
					: Collections.singleton(layers), null);
			updateContents();
		}

		private void updateContents() {
			EventQueue.invokeLater(this);
		}

		public void run() {
			innerPanel.removeAll();
			if (picture != null) {
				List l = picture.getLayers();

				for (Iterator i = l.iterator(); i.hasNext();) {
					Layer layer = (Layer) i.next();
					LayerPanel pnl = new LayerPanel(layer);

					innerPanel.add(pnl, 0);
				}
			} else {
				JLabel lbl = new JLabel(NbBundle.getMessage(LayersTopComponent.class,
						"LBL_Empty"));

				lbl.setHorizontalTextPosition(SwingConstants.CENTER);
				lbl.setEnabled(false);
				innerPanel.add(lbl);
			}
			scrollPane.invalidate();
			scrollPane.revalidate();
			scrollPane.repaint();
		}

		/**
		 * replaces this in object stream
		 */
		@Override
		public Object writeReplace() {
			return new ResolvableHelper();
		}

		@Override
		protected String preferredID() {
			return "LayersTopComponent";
		}

		public void resultChanged(LookupEvent lookupEvent) {
			updatePicture();
		}

		public void stateChanged(ChangeEvent e) {
			updateContents();
		}

		public void actionPerformed(ActionEvent e) {
			if (picture == null) {
				return;
			}
			if (e.getSource() instanceof Timer) {
				repaint();
				return;
			}
			JButton jb = (JButton) e.getSource();

			if (NAME_DELETE.equals(jb.getName())) {
				Layer l = picture.getActiveLayer();

				if (l != null) {
					picture.delete(l);
				}
			} else if (NAME_NEW.equals(jb.getName())) {
				Collection<? extends LayerFactory> factories =
						Lookup.getDefault().lookupAll(LayerFactory.class);
				if (factories.size() == 1) {
					Layer layer = Utilities.actionsGlobalContext().lookup(Layer.class);
					int pos;
					if (layer != null) {
						List<Layer> layers = picture.getLayers();
						int ix = layers.indexOf(layer);
						assert ix != -1 : "Selected layer from lookup not part of picture";
						pos = ix + 1;
					} else {
						pos = Picture.POSITION_BOTTOM;
					}
					picture.add(pos);
				} else {
					class AA extends AbstractAction {

						private LayerFactory f;

						public AA(LayerFactory f) {
							this.f = f;
							String name = f.getDisplayName();
							String s = NbBundle.getMessage(
									LayersTopComponent.class, "FMT_NewLayer", name); //NOI18N
							putValue(Action.NAME, s);
						}

						public void actionPerformed(ActionEvent ae) {
							int ct = picture.getLayers().size();
							Set<String> usedNames = new HashSet<String>(ct);
							for (Layer l : picture.getLayers()) {
								usedNames.add(l.getName());
							}
							String newName = null;
							int ix = 1;
							while (newName == null || usedNames.contains(newName)) {
								newName = NbBundle.getMessage(LayersTopComponent.class,
										"NEW_LAYER_NAME", "" + (ix++));
							}
							LayerImplementation impl = f.createLayer(newName,
									picture.getRepaintHandle(),
									picture.getSize());
							Layer layer = Utilities.actionsGlobalContext().lookup(Layer.class);
							int pos;
							if (layer != null) {
								List<Layer> layers = picture.getLayers();
								int index = layers.indexOf(layer);
								assert index != -1 : "Selected layer from lookup not part of picture";
								pos = index + 1;
							} else {
								pos = Picture.POSITION_BOTTOM;
							}
							picture.add(pos,
									impl.getLayer());
						}
					}
					JPopupMenu popup = new JPopupMenu();
					for (LayerFactory factory : factories) {
						popup.add(new AA(factory));
					}
					EventObject event = EventQueue.getCurrentEvent();
					if (event instanceof MouseEvent) {
						MouseEvent me = (MouseEvent) event;
						Point p = me.getPoint();
						popup.show((Component) me.getSource(),
								p.x, p.y);
					} else {
						Point p = new Point(newButton.getWidth() / 2,
								newButton.getHeight() / 2);
						popup.show(newButton, p.x, p.y);
					}
				}
			} else if (NAME_DOWN.equals(jb.getName())) {
				Layer l = picture.getActiveLayer();
				int ix = picture.getLayers().indexOf(l);

				if (ix != 0) {
					picture.move(l, ix - 1);
				}
			} else if (NAME_UP.equals(jb.getName())) {
				Layer l = picture.getActiveLayer();
				int ix = picture.getLayers().indexOf(l);

				if (ix != picture.getLayers().size() - 1) {
					picture.move(l, ix + 2);
				}
			} else if (NAME_DUP.equals(jb.getName())) {
				Layer l = picture.getActiveLayer();

				picture.duplicate(l);
			} else if (NAME_FLATTEN.equals(jb.getName())) {
				picture.flatten();
			}
			stateChanged(null);
		}

		static final class ResolvableHelper implements java.io.Serializable {

			private static final long serialVersionUID = 1L;

			public Object readResolve() {
				return LayersTopComponent.getDefault();
			}
		}

		private class LayerPanel extends JPanel implements PropertyChangeListener,
				MouseListener,
				FocusListener,
				ActionListener,
				KeyListener,
				ChangeListener {

			private final Layer layer;
			private final JCheckBox box;
			private final JLabel lbl;
			private final JSlider opacity;
			private final Thumbnail thumbnail;

			public LayerPanel(Layer layer) {
				this.layer = layer;
				add(thumbnail = new Thumbnail());
				box = new JCheckBox();
				box.addActionListener(this);
				add(box);
				lbl = new JLabel();
				// So the editor will have enough room
				add(lbl);
				refresh();
				setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,
						0,
						1,
						0,
						Color.GRAY),
						BorderFactory.createEmptyBorder(0,
						12,
						0,
						0)));
				box.addMouseListener(this);
				lbl.addMouseListener(this);
				addMouseListener(this);
				box.setBorder(BorderFactory.createMatteBorder(0, 5, 0, 1, Color.GRAY));
				opacity = new JSlider();
				opacity.setMinimum(0);
				opacity.setMaximum(100);
				opacity.setValue(Math.max(100,
						(int) (layer.getOpacity() * 100.0F)));
				add(opacity);
				opacity.addChangeListener(this);
				opacity.setUI((javax.swing.plaf.SliderUI) PopupSliderUI.createUI(opacity));
				opacity.setOpaque(false);
				opacity.addMouseListener(this);
				box.setOpaque(false);
			}
			private static final int GAP = 3;

			@Override
			public void doLayout() {
				Insets ins = getInsets();
				Dimension tPrefSize = thumbnail.getPreferredSize();
				Dimension opPrefSize = opacity.getPreferredSize();
				Dimension ckPrefSize = box.getPreferredSize();
				Dimension lblPrefSize = lbl.getPreferredSize();
				int x = ins.left;
				int y = center(tPrefSize.height, ins);

				thumbnail.setBounds(x, y, tPrefSize.width, tPrefSize.height);
				x += tPrefSize.width + GAP;
				y = center(ckPrefSize.height, ins);
				box.setBounds(x, y, ckPrefSize.width, ckPrefSize.height);
				x += ckPrefSize.width + GAP;
				int opX = (getWidth() - ins.right) - opPrefSize.width;

				y = center(opPrefSize.height, ins);
				opacity.setBounds(opX, y, opPrefSize.width, opPrefSize.height);
				y = center(lblPrefSize.height, ins);
				int w = (opX - GAP) - x;

				lbl.setBounds(x, y, w, lblPrefSize.height);
			}

			@Override
			public Dimension getPreferredSize() {
				Dimension result = new Dimension();
				Component[] c = getComponents();

				for (int i = 0; i < c.length; i++) {
					result.width += c[i].getPreferredSize().width;
					result.height = Math.max(result.height,
							c[i].getPreferredSize().height);
				}
				result.width += GAP * (c.length - 2);
				result.height += GAP * 2;
				result.width = Math.max(120, result.width);
				return result;
			}

			@Override
			public Dimension getMinimumSize() {
				return new Dimension(30, getPreferredSize().height);
			}

			@Override
			public Dimension getMaximumSize() {
				return getPreferredSize();
			}

			private int center(int height, Insets ins) {
				return Math.max(0,
						((getHeight() - (ins.top + ins.bottom)) / 2)
						- (height / 2));
			}

			public void stateChanged(ChangeEvent e) {
				JSlider opacitySlider = (JSlider) e.getSource();
				float val = opacitySlider.getValue();

				layer.setOpacity(val / 100.0F);
			}

			private class Thumbnail extends JComponent {

				@Override
				public boolean isOpaque() {
					return true;
				}
				// XXX how to repaint it when the image changes?

				@Override
				public void paint(Graphics gr) {
					Graphics2D g = (Graphics2D) gr;
					g.setColor(Color.WHITE);
					g.fillRect(0, 0, getWidth(), getHeight());
					g.setColor(Color.GRAY);
					g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
					Rectangle r = new Rectangle(1, 1, getWidth() - 1,
							getHeight() - 1);
					Shape clip = g.getClip();

					g.setClip(r);
					layer.paint(g, r, true);
					g.setClip(clip);
				}

				@Override
				public Dimension getPreferredSize() {
					Dimension result = new Dimension();
					Dimension lsize = picture.getSize();
					int ht = 32;

					if (lsize.height != 0) {
						float htFactor = (float) (ht - 4)
								/ (float) picture.getSize().height;

						result.width = (int) (lsize.width * htFactor);
						result.height = ht - 4;
					}
					return result;
				}

				@Override
				public Dimension getMaximumSize() {
					return getPreferredSize();
				}
			}

			@Override
			public void addNotify() {
				super.addNotify();
				layer.addPropertyChangeListener(this);
			}

			@Override
			public void removeNotify() {
				super.removeNotify();
				layer.removePropertyChangeListener(this);
				stopEditing(false);
			}

			private void refresh() {
				lbl.setText(layer.getName());
				box.setSelected(layer.isVisible());
				setBackground(picture.getActiveLayer() == layer ? Color.LIGHT_GRAY
						: Color.WHITE);
			}

			public void propertyChange(PropertyChangeEvent evt) {
				refresh();
			}

			public void mouseClicked(MouseEvent e) {
				if (!e.isPopupTrigger()) {
					if (e.getClickCount() == 1 && !(e.getSource() instanceof JCheckBox)) {
						picture.setActiveLayer(layer);
						e.consume();
					} else if (e.getSource() != box) {
						startEditing();
					}
				}
			}
			private JTextField editor = null;

			private void startEditing() {
				editor = new JTextField();
				lbl.add(editor);
				editor.setBounds(0, 0, lbl.getWidth(), lbl.getHeight());
				editor.addFocusListener(this);
				editor.addKeyListener(this);
				editor.setText(lbl.getText());
				editor.setSelectionStart(0);
				editor.setSelectionEnd(lbl.getText().length());
				editor.requestFocus();
				invalidate();
				revalidate();
				repaint();
			}

			private void stopEditing(boolean takeValue) {
				if (editor == null) {
					return;
				}
				if (takeValue && editor.getText().trim().length() > 0) {
					layer.setName(editor.getText().trim());
					layer.commitLastPropertyChangeToUndoHistory();
				}
				editor.removeFocusListener(this);
				editor.removeActionListener(this);
				lbl.remove(editor);
				repaint();
				editor = null;
			}

			public void keyReleased(KeyEvent e) {
				int code = e.getKeyCode();

				if (code == KeyEvent.VK_ENTER || code == KeyEvent.VK_TAB) {
					stopEditing(true);
				} else if (code == KeyEvent.VK_ESCAPE) {
					stopEditing(false);
				}
			}

			public void focusLost(FocusEvent e) {
				stopEditing(true);
			}

			public void actionPerformed(ActionEvent e) {
				if (e.getSource() instanceof JCheckBox) {
					JCheckBox checkbox = (JCheckBox) e.getSource();

					layer.setVisible(checkbox.isSelected());
					layer.commitLastPropertyChangeToUndoHistory();
					if (!checkbox.isSelected()) {
						Picture picture = Utilities.actionsGlobalContext().lookup(Picture.class);
						//Find a layer that is not invisible, so we don't
						//accidentally edit an invisible layer
						if (layer == picture.getActiveLayer()) {
							List<Layer> l = picture.getLayers();
							int ix = l.indexOf(layer);
							boolean found = false;
							for (int i = ix; i < l.size(); i++) {
								Layer nue = l.get(i);
								if (nue.isVisible()) {
									picture.setActiveLayer(nue);
									found = true;
									break;
								}
							}
							if (!found) {
								for (int i = ix; i >= 0; i--) {
									Layer nue = l.get(i);
									if (nue.isVisible()) {
										picture.setActiveLayer(nue);
										found = true;
										break;
									}
								}
							}
							if (!found) {
								picture.setActiveLayer(null);
							}
						}
					}
					stopEditing(true);
				} else {
					stopEditing(true);
				}
			}

			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					System.err.println("Got popup trigger");
					Point p = e.getPoint();
					Component src = (Component) e.getSource();
					getPopupMenu().show(src, p.x, p.y);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.getSource() instanceof JSlider && !e.isPopupTrigger()) {
					layer.commitLastPropertyChangeToUndoHistory();
				} else if (e.isPopupTrigger()) {
					System.err.println("Got popup trigger");
					Point p = e.getPoint();
					Component src = (Component) e.getSource();
					getPopupMenu().show(src, p.x, p.y);
				}
			}

			public void mouseEntered(MouseEvent e) {
				thumbnail.repaint();
			}

			public void mouseExited(MouseEvent e) {
			}

			public void focusGained(FocusEvent e) {
			}

			public void keyTyped(KeyEvent e) {
			}

			public void keyPressed(KeyEvent e) {
			}

			private JPopupMenu getPopupMenu() {
				JPopupMenu menu = new JPopupMenu();
				Action[] a = getPopupActions(layer);
				for (int i = 0; i < a.length; i++) {
					menu.add(a[i]);
				}
				return menu;
			}
		}

		private static Action[] getPopupActions(Layer layer) {
			Collection<? extends Action> actions =
					layer.getLookup().lookupAll(Action.class);

			List<Action> result = new ArrayList<Action>(actions.size() + 3);
			result.addAll(actions);
			Collection<? extends LayerFactory> factories =
					Lookup.getDefault().lookupAll(LayerFactory.class);
			for (LayerFactory factory : factories) {
				if (factory.canConvert(layer)) {
					result.add(new ConvertAction(factory, layer));
				}
			}
			Action[] a =
					(Action[]) result.toArray(new Action[result.size()]);
			return a;
		}

		private static final class ConvertAction extends AbstractAction {

			private final LayerFactory factory;
			private final Layer layer;

			ConvertAction(LayerFactory factory, Layer layer) {
				this.factory = factory;
				this.layer = layer;
				String s = factory.getConversionActionDisplayName();
				putValue(Action.NAME, s);
			}

			public void actionPerformed(ActionEvent ae) {
				Picture picture = Utilities.actionsGlobalContext().lookup(Picture.class);
				if (picture == null) {
					throw new IllegalStateException("No Picture in " + //NOI18N
							"action context"); //NOI18N
				}
				if (picture != null) {
					List<Layer> layers = picture.getLayers();
					int index = layers.indexOf(layer);
					if (index != -1) {
						Layer nue = factory.convert(layer).getLayer();
						picture.delete(layer);
						picture.add(index, nue);
						return;
					}
				}
				Toolkit.getDefaultToolkit().beep();
			}
		}

		private static final class LM implements LayoutManager {

			public void addLayoutComponent(String name, Component comp) {
			}

			public void removeLayoutComponent(Component comp) {
			}

			public Dimension preferredLayoutSize(Container parent) {
				// Avoid a beta bug by which the layers window keeps resizing itself
				// to hide the editor area every time it gets focus.  We
				// ought to be calculating it properly
				// Dimension result = new Dimension (300, 200);
				// return result;
				Component[] c = parent.getComponents();
				Dimension result = new Dimension();

				for (int i = 0; i < c.length; i++) {
					Dimension d = c[i].getPreferredSize();

					result.height += d.height;
					result.width = d.width;
				}
				return result;
			}

			public Dimension minimumLayoutSize(Container parent) {
				return preferredLayoutSize(parent);
			}

			public void layoutContainer(Container parent) {
				Component[] c = parent.getComponents();
				Insets ins = parent.getInsets();
				int y = ins.top;
				int l = ins.left;
				int w = parent.getWidth() - (ins.left + ins.right);

				for (int i = 0; i < c.length; i++) {
					if (c[i] instanceof JLabel && c.length == 1) {
						c[i].setBounds(12, 0, parent.getWidth(), parent.getHeight());
						break;
					}
					int h = Math.max(24, c[i].getPreferredSize().height);

					c[i].setBounds(l, y, w, h);
					y += h;
				}
			}
		}
	}
