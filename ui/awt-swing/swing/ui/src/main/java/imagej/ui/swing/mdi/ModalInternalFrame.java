/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package imagej.ui.swing.mdi;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.beans.*;

/*
 * From 
 */
public class ModalInternalFrame extends JInternalFrame {

	public ModalInternalFrame(String title, JRootPane rootPane, Component desktop, JOptionPane pane) {
		super(title);

		// create opaque glass pane
		final JPanel glass = new JPanel();
		glass.setOpaque(false);

		// Attach mouse listeners
		MouseInputAdapter adapter =	new MouseInputAdapter() { };
		glass.addMouseListener(adapter);
		glass.addMouseMotionListener(adapter);

		// Add in option pane
		getContentPane().add(pane, BorderLayout.CENTER);

		// Define close behavior
		PropertyChangeListener pcl =
				new PropertyChangeListener() {

					public void propertyChange(PropertyChangeEvent event) {
						if (isVisible()
								&& (event.getPropertyName().equals(
								JOptionPane.VALUE_PROPERTY)
								|| event.getPropertyName().equals(
								JOptionPane.INPUT_VALUE_PROPERTY))) {
							try {
								setClosed(true);
							} catch (PropertyVetoException ignored) {
							}
							ModalInternalFrame.this.setVisible(false);
							glass.setVisible(false);
						}
					}

				};
		pane.addPropertyChangeListener(pcl);

		// Change frame border
		putClientProperty("JInternalFrame.frameType", "optionDialog");

		// Size frame
		Dimension size = getPreferredSize();
		Dimension rootSize = desktop.getSize();

		setBounds((rootSize.width - size.width) / 2,
				(rootSize.height - size.height) / 2,
				size.width, size.height);
		desktop.validate();
		try {
			setSelected(true);
		} catch (PropertyVetoException ignored) {
		}

		// Add modal internal frame to glass pane
		glass.add(this);

		// Change glass pane to our panel
		rootPane.setGlassPane(glass);

		// Show glass pane, then modal dialog
		glass.setVisible(true);
	}

	public void setVisible(boolean value) {
		super.setVisible(value);
		if (value) {
			startModal();
		} else {
			stopModal();
		}
	}

	private synchronized void startModal() {
		try {
			if (SwingUtilities.isEventDispatchThread()) {
				EventQueue theQueue =
						getToolkit().getSystemEventQueue();
				while (isVisible()) {
					AWTEvent event = theQueue.getNextEvent();
					Object source = event.getSource();
					if (event instanceof ActiveEvent) {
						((ActiveEvent) event).dispatch();
					} else if (source instanceof Component) {
						((Component) source).dispatchEvent(event);
					} else if (source instanceof MenuComponent) {
						((MenuComponent) source).dispatchEvent(event);
					} else {
						System.err.println("Unable to dispatch: " + event);
					}
				}
			} else {
				while (isVisible()) {
					wait();
				}
			}
		} catch (InterruptedException ignored) {
		}
	}

	private synchronized void stopModal() {
		notifyAll();
	}

	public static void main(String args[]) {
		final JFrame frame = new JFrame("Modal Internal Frame");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final JDesktopPane desktop = new JDesktopPane();

		ActionListener showModal = new ActionListener() {

			Integer ZERO = new Integer(0);
			Integer ONE = new Integer(1);

			public void actionPerformed(ActionEvent e) {

				// Manually construct an input popup
				JOptionPane optionPane = new JOptionPane(
						"Print?", JOptionPane.QUESTION_MESSAGE,
						JOptionPane.YES_NO_OPTION);

				// Construct a message internal frame popup
				JInternalFrame modal =
						new ModalInternalFrame("Really Modal",
						frame.getRootPane(), desktop, optionPane);

				modal.setVisible(true);

				Object value = optionPane.getValue();
				if (value.equals(ZERO)) {
					System.out.println("Selected Yes");
				} else if (value.equals(ONE)) {
					System.out.println("Selected No");
				} else {
					System.err.println("Input Error");
				}
			}

		};

		JInternalFrame internal =
				new JInternalFrame("Opener");
		desktop.add(internal);

		JButton button = new JButton("Open");
		button.addActionListener(showModal);

		Container iContent = internal.getContentPane();
		iContent.add(button, BorderLayout.CENTER);
		internal.setBounds(25, 25, 200, 100);
		internal.setVisible(true);

		Container content = frame.getContentPane();
		content.add(desktop, BorderLayout.CENTER);
		frame.setSize(500, 300);
		frame.setVisible(true);
	}

	/**
	J  D  C    T  E  C  H    T  I  P  S
	
	
	TIPS, TECHNIQUES, AND SAMPLE CODE
	
	
	
	WELCOME to the Java Developer Connection(sm) (JDC) Tech Tips, 
	December 20, 2001. This issue covers two approaches for
	creating modal internal frames:
	
	 * Creating Modal Internal Frames -- Approach 1 
	 * Creating Modal Internal Frames -- Approach 2 
	
	These tips were developed using Java(tm) 2 SDK, Standard Edition, 
	v 1.3 and Java 2 Enterprise Edition, v 1.3. 
	
	This issue of the JDC Tech Tips is written by John Zukowski, 
	president of JZ Ventures, Inc. (http://www.jzventures.com).
	
	You can view this issue of the Tech Tips on the Web at
	http://java.sun.com/jdc/JDCTechTips/2001/tt1220.html
	
	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
	CREATING MODAL INTERNAL FRAMES -- APPROACH 1
	
	The Java Foundation Classes (JFC) Project Swing component set 
	provides the JOptionPane component to display simple, standard 
	dialogs. A dialog is a window that displays information, but also 
	expects a response from the user. For example, a dialog might 
	warn the user of a potential problem, and also display an OK
	button so that the user can acknowledge the warning. You can 
	display a dialog in either a popup window or in an internal frame, 
	that is, a window within another window. 
	
	To display a dialog in a popup window, you use one of the 
	JOptionPane showXXXDialog methods such as showMessageDialog.
	The popup window in this case is modal. This means that the user 
	must respond to the popup before the program continues running.
	But there's more -- modal also means that the user can't interact 
	with other parts of the program. To display a dialog in an 
	internal frame, you use one of the showInternalXXXDialog methods
	in JOptionPane. These internal frame dialogs are not modal. There 
	are times however when you might want a dialog in an internal 
	frame to be modal. This Tech Tip will show you how to create 
	a modal dialog in an internal frame.
	
	There is limited support for modality in the internal frames 
	created by a JOptionPane. To take advantage of this limited 
	support, you need to place the internal frame in the glass pane 
	of the frame where the desktop pane appears. 
	
	If you've worked with internal frames, you know that normally
	you add internal frames, that is, instances of JInternalFrame, to
	a desktop pane, that is, an instance of JDesktopPane. A desktop
	pane is a layered pane that manages multiple overlapping internal 
	frames. A glass pane is part of the root pane that a top-level 
	window such as a frame relies on. A root pane is comprised of 
	three parts (the glass pane, layered pane, and content pane), and 
	an optional fourth part (the menu bar). The content pane contains
	the root pane's visible components. The optional menu bar contains
	the root pane's menus. And the layered pane positions the contents
	of the content pane and the optional menu bar. The glass pane 
	is useful in intercepting events that would otherwise pass through 
	to the underlying components.
	
	So, to repeat, you can create a somewhat modal dialog in an 
	internal frame created by JOptionPane. To do this, you put the 
	internal frame in the glass pane of the frame where the desktop 
	pane appears. This technique restricts input to only that 
	specific frame. The internal frame in this case isn't truly 
	modal. To be truly modal, an internal frame needs to block once it 
	is shown. If you follow this technique, the internal frame doesn't 
	do that. However, the approach does restrict input to the single 
	component. 
	
	The first step in this technique is to create a dialog within an
	internal frame. To do this, you need to create the JOptionPane, 
	and then use one of the createInternalXXX methods to create
	and show the necessary message component. For instance, the 
	following creates a message dialog within an internal frame:
	
	JOptionPane optionPane = new JOptionPane();
	optionPane.setMessage("Hello, World");
	optionPane.setMessageType(
	JOptionPane.INFORMATION_MESSAGE);
	JInternalFrame modal = 
	optionPane.createInternalFrame(desktop, "Modal");
	
	The next step is to place the component in the glass pane of the 
	window where the desktop pane is located. The glass pane can be 
	any component. So, the easiest way to do this is to create a 
	transparent JPanel:
	
	JPanel glass = new JPanel();
	glass.setOpaque(false);
	glass.add(modal);
	frame.setGlassPane(glass);
	glass.setVisible(true);
	modal.setVisible(true);
	
	The last steps are to set up the glass pane to intercept events,
	and to hide the glass pane when the internal frame closes. In
	order for the glass pane to intercept events, you have to attach 
	a MouseListener and MouseMotionListener. To hide the glass pane 
	when the internal frame closes, you need to attach an 
	InternalFrameListener to the internal frame:
	
	class ModalAdapter extends InternalFrameAdapter {
	Component glass;
	
	public ModalAdapter(Component glass) {
	this.glass = glass;
	
	// Associate dummy mouse listeners
	// Otherwise mouse events pass through
	MouseInputAdapter adapter = new MouseInputAdapter(){};
	glass.addMouseListener(adapter);
	glass.addMouseMotionListener(adapter);
	}
	
	public void internalFrameClosed(InternalFrameEvent e) {
	glass.setVisible(false);
	}
	}
	
	Here's a program that puts all the pieces together. It creates 
	a JDesktopPane with a single internal frame. On that frame is
	a button. When that button is pressed, the blocking internal 
	frame message dialog appears. While that is visible, you cannot 
	press the first button. Once the OK button is pressed on the 
	message window, you can then interact with the first internal 
	frame.
	
	import java.awt.*;
	import java.awt.event.*;
	import javax.swing.*;
	import javax.swing.event.*;
	
	public class Modal  {
	
	static class ModalAdapter 
	extends InternalFrameAdapter {
	Component glass;
	
	public ModalAdapter(Component glass) {
	this.glass = glass;
	
	// Associate dummy mouse listeners
	// Otherwise mouse events pass through
	MouseInputAdapter adapter = 
	new MouseInputAdapter(){};
	glass.addMouseListener(adapter);
	glass.addMouseMotionListener(adapter);
	}
	
	public void internalFrameClosed(
	InternalFrameEvent e) {
	glass.setVisible(false);
	}
	}
	
	public static void main(String args[]) {
	final JFrame frame = new JFrame(
	"Modal Internal Frame");
	frame.setDefaultCloseOperation(
	JFrame.EXIT_ON_CLOSE);
	
	final JDesktopPane desktop = new JDesktopPane();
	
	ActionListener showModal = 
	new ActionListener() {
	public void actionPerformed(ActionEvent e) {
	
	// Manually construct a message frame popup
	JOptionPane optionPane = new JOptionPane();
	optionPane.setMessage("Hello, World");
	optionPane.setMessageType(
	JOptionPane.INFORMATION_MESSAGE);
	JInternalFrame modal = optionPane.
	createInternalFrame(desktop, "Modal");
	
	// create opaque glass pane
	JPanel glass = new JPanel();
	glass.setOpaque(false);
	
	// Attach modal behavior to frame
	modal.addInternalFrameListener(
	new ModalAdapter(glass));
	
	// Add modal internal frame to glass pane
	glass.add(modal);
	
	// Change glass pane to our panel
	frame.setGlassPane(glass);
	
	// Show glass pane, then modal dialog
	glass.setVisible(true);
	modal.setVisible(true);
	
	System.out.println("Returns immediately");
	}
	};
	
	JInternalFrame internal = 
	new JInternalFrame("Opener");
	desktop.add(internal);
	
	JButton button = new JButton("Open");
	button.addActionListener(showModal);
	
	Container iContent = internal.getContentPane();
	iContent.add(button, BorderLayout.CENTER);
	internal.setBounds(25, 25, 200, 100);
	internal.setVisible(true);
	
	Container content = frame.getContentPane();
	content.add(desktop, BorderLayout.CENTER);
	frame.setSize(500, 300);
	frame.setVisible(true);
	}
	}
	
	- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
	CREATING MODAL INTERNAL FRAMES -- APPROACH 2
	
	While the approach shown in the Tech Tip "Creating Modal Internal 
	Frames -- Approach 1" provides internal frames that block input 
	to other internal frames, the frames are not truly modal. To be
	truly modal, an internal frame needs to block once it is shown.
	The internal frame in the above tip doesn't do that.
	
	In order to make an internal frame truly modal, you must
	take over the event dispatching yourself when the frame is shown. 
	This would be in addition to showing the frame in the glass pane. 
	You can still use the JOptionPane to create the message and input 
	dialogs, but you must also add some behavior that is normally 
	handled for you when using one of the showInternalXXX methods. 
	Because of the needed custom behavior, and when this custom 
	behavior is needed, it is necessary to create a JInternalFrame 
	subclass. Doing this also allows you to move into the subclass 
	much of the behavior that was done in the prior ActionListener.
	
	Most of the work you need to do in creating a truly modal 
	internal frame involves completing the subclass constructor. 
	Simply copying the ActionListener code from the previous tip
	to the constructor provides a framework in which you can build. 
	By passing in the JRootPane, you can use this modal 
	JInternalFrame in both a JApplet as well as a JFrame.
	
	public ModalInternalFrame(String title, 
	JRootPane rootPane, Component desktop, 
	JOptionPane pane) {
	super(title);
	
	// create opaque glass pane
	final JPanel glass = new JPanel();
	glass.setOpaque(false);
	
	// Attach mouse listeners
	MouseInputAdapter adapter = 
	new MouseInputAdapter(){};
	glass.addMouseListener(adapter);
	glass.addMouseMotionListener(adapter);
	
	// Add in option pane
	getContentPane().add(pane, BorderLayout.CENTER);
	
	// *** Remaining code to be added here ***
	
	// Add modal internal frame to glass pane
	glass.add(this);
	
	// Change glass pane to our panel
	rootPane.setGlassPane(glass);
	
	// Show glass pane, then modal dialog
	glass.setVisible(true);
	}
	
	Notice that the only code not copied from the ActionListener is 
	the final setVisible(true) call for the internal frame.
	
	Some of the other tasks that the showInternalXXX methods of 
	JOptionPane perform include (1) setting up the closing of the 
	dialog once one of the buttons is selected (or input is entered),
	and (2) some appearance-related tasks mostly having to do with 
	sizing. Because you aren't using the showInternalXXX method, you 
	must perform these other tasks yourself.
	
	The way to set up the closing of the internal frame is to attach 
	a PropertyChangeListener to the option pane. In the JOptionPane, 
	when a button is selected or input is entered, it triggers the 
	generation of a PropertyChangeEvent. You can close the frame when 
	that event happens. Here's what the code for that behavior looks 
	like:
	
	// Define close behavior
	PropertyChangeListener pcl = 
	new PropertyChangeListener() {
	public void propertyChange(
	PropertyChangeEvent event) {
	if (isVisible() && 
	(event.getPropertyName().equals(
	JOptionPane.VALUE_PROPERTY) ||
	event.getPropertyName().equals(
	JOptionPane.INPUT_VALUE_PROPERTY))) {
	try {
	setClosed(true);
	} catch (PropertyVetoException ignored) {
	}
	ModalInternalFrame.this.setVisible(false);
	glass.setVisible(false);
	}
	}
	};
	pane.addPropertyChangeListener(pcl);
	
	There are three appearance-related tasks that you need to perform. 
	Internal frame dialogs are defined to have a different border 
	than regular internal frames. So, you need to set a client 
	property for the frame. The second task is to initialize the size 
	and position of the internal frame. You could just hard-code 
	a size (however, the following code centers the frame). The final 
	task is to mark the internal frame as the selected one. Here's 
	what the code for these appearance-related tasks looks like:
	
	// Change frame border
	putClientProperty("JInternalFrame.frameType", 
	"optionDialog");
	
	// Size frame
	Dimension size = getPreferredSize();
	Dimension rootSize = desktop.getSize();
	
	setBounds((rootSize.width - size.width) / 2,
	(rootSize.height - size.height) / 2,
	size.width, size.height); 
	desktop.validate(); 
	try {
	setSelected(true);
	} catch (PropertyVetoException ignored) {
	}
	
	Adding these two code blocks to the middle of your constructor
	completes the initialization of the JInternalFrame subclass.
	
	The final thing you have to do is take over the event dispatching 
	after the internal frame is shown.  Normally, event dispatching 
	is handled in the EventQueue class. However, because you are 
	blocking the event handling thread when you make the internal 
	frame modal, the EventQueue will never see events. So you have to 
	replace the functionality. 
	
	In order to dispatch events yourself, all you have to do is copy 
	the code in the dispatchEvent() method of the EventQueue. If the 
	internal frame is made visible from a thread other than the event 
	dispatching thread, you don't even have to copy the code in the 
	dispatchEvent() method. In that case, all you have to do is 
	wait() to block. Then, when the frame is closed, it needs to be 
	notified. Here's what the event dispatching code looks like.
	
	public void setVisible(boolean value) {
	super.setVisible(value);
	if (value) {
	startModal();
	} else {
	stopModal();
	}
	}
	
	private synchronized void startModal() {
	try {
	if (SwingUtilities.isEventDispatchThread()) {
	EventQueue theQueue = 
	getToolkit().getSystemEventQueue();
	while (isVisible()) {
	AWTEvent event = theQueue.getNextEvent();
	Object source = event.getSource();
	if (event instanceof ActiveEvent) {
	((ActiveEvent)event).dispatch();
	} else if (source instanceof Component) {
	((Component)source).dispatchEvent(
	event);
	} else if (source instanceof 
	MenuComponent) {
	((MenuComponent)source).dispatchEvent(
	event);
	} else {
	System.err.println(
	"Unable to dispatch: " + event);
	}
	}
	} else {
	while (isVisible()) {
	wait();
	}
	}
	} catch (InterruptedException ignored) {
	}
	}
	
	private synchronized void stopModal() {
	notifyAll();
	}
	
	Here's an example that puts everything together. Instead of 
	simply displaying a message dialog, it prompts the user to answer 
	a Yes/No question when the modal internal frame is shown. Notice 
	that all you have to do after creating the internal frame is show 
	it.
	
	import java.awt.*;
	import java.awt.event.*;
	import javax.swing.*;
	import javax.swing.event.*;
	import java.beans.*;
	
	public class ModalInternalFrame extends JInternalFrame {
	
	public ModalInternalFrame(String title, JRootPane 
	rootPane, Component desktop, JOptionPane pane) {
	super(title);
	
	// create opaque glass pane
	final JPanel glass = new JPanel();
	glass.setOpaque(false);
	
	// Attach mouse listeners
	MouseInputAdapter adapter = 
	new MouseInputAdapter(){};
	glass.addMouseListener(adapter);
	glass.addMouseMotionListener(adapter);
	
	// Add in option pane
	getContentPane().add(pane, BorderLayout.CENTER);
	
	// Define close behavior
	PropertyChangeListener pcl = 
	new PropertyChangeListener() {
	public void propertyChange(PropertyChangeEvent 
	event) {
	if (isVisible() && 
	(event.getPropertyName().equals(
	JOptionPane.VALUE_PROPERTY) ||
	event.getPropertyName().equals(
	JOptionPane.INPUT_VALUE_PROPERTY))) {
	try {
	setClosed(true);
	} catch (PropertyVetoException ignored) {
	}
	ModalInternalFrame.this.setVisible(false);
	glass.setVisible(false);
	}
	}
	};
	pane.addPropertyChangeListener(pcl);
	
	// Change frame border
	putClientProperty("JInternalFrame.frameType",
	"optionDialog");
	
	// Size frame
	Dimension size = getPreferredSize();
	Dimension rootSize = desktop.getSize();
	
	setBounds((rootSize.width - size.width) / 2,
	(rootSize.height - size.height) / 2,
	size.width, size.height); 
	desktop.validate(); 
	try {
	setSelected(true);
	} catch (PropertyVetoException ignored) {
	}
	
	// Add modal internal frame to glass pane
	glass.add(this);
	
	// Change glass pane to our panel
	rootPane.setGlassPane(glass);
	
	// Show glass pane, then modal dialog
	glass.setVisible(true);
	}
	
	public void setVisible(boolean value) {
	super.setVisible(value);
	if (value) {
	startModal();
	} else {
	stopModal();
	}
	}
	
	private synchronized void startModal() {
	try {
	if (SwingUtilities.isEventDispatchThread()) {
	EventQueue theQueue = 
	getToolkit().getSystemEventQueue();
	while (isVisible()) {
	AWTEvent event = theQueue.getNextEvent();
	Object source = event.getSource();
	if (event instanceof ActiveEvent) {
	((ActiveEvent)event).dispatch();
	} else if (source instanceof Component) {
	((Component)source).dispatchEvent(
	event);
	} else if (source instanceof MenuComponent) {
	((MenuComponent)source).dispatchEvent(
	event);
	} else {
	System.err.println(
	"Unable to dispatch: " + event);
	}
	}
	} else {
	while (isVisible()) {
	wait();
	}
	}
	} catch (InterruptedException ignored) {
	}
	}
	
	private synchronized void stopModal() {
	notifyAll();
	}
	
	public static void main(String args[]) {
	final JFrame frame = new JFrame(
	"Modal Internal Frame");
	frame.setDefaultCloseOperation(
	JFrame.EXIT_ON_CLOSE);
	
	final JDesktopPane desktop = new JDesktopPane();
	
	ActionListener showModal = 
	new ActionListener() {
	Integer ZERO = new Integer(0);
	Integer ONE = new Integer(1);
	public void actionPerformed(ActionEvent e) {
	
	// Manually construct an input popup
	JOptionPane optionPane = new JOptionPane(
	"Print?", JOptionPane.QUESTION_MESSAGE, 
	JOptionPane.YES_NO_OPTION);
	
	// Construct a message internal frame popup
	JInternalFrame modal = 
	new ModalInternalFrame("Really Modal", 
	frame.getRootPane(), desktop, optionPane);
	
	modal.setVisible(true);
	
	Object value = optionPane.getValue();
	if (value.equals(ZERO)) {
	System.out.println("Selected Yes");
	} else if (value.equals(ONE)) {
	System.out.println("Selected No");
	} else {
	System.err.println("Input Error"); 
	}
	}
	};
	
	JInternalFrame internal = 
	new JInternalFrame("Opener");
	desktop.add(internal);
	
	JButton button = new JButton("Open");
	button.addActionListener(showModal);
	
	Container iContent = internal.getContentPane();
	iContent.add(button, BorderLayout.CENTER);
	internal.setBounds(25, 25, 200, 100);
	internal.setVisible(true);
	
	Container content = frame.getContentPane();
	content.add(desktop, BorderLayout.CENTER);
	frame.setSize(500, 300);
	frame.setVisible(true);
	}
	}
	
	To learn more about internal frames, see the Java Tutorial lesson 
	"How to Use Internal Frames" at 
	http://java.sun.com/tutorial/uiswing/components/internalframe.html.
	
	To learn more about the root pane and its glass pane, read the 
	Java Tutorial lesson "How to Use Root Panes" at 
	http://java.sun.com/tutorial/uiswing/components/rootpane.html.
	
	.  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .  .
	
	IMPORTANT: Please read our Terms of Use and Privacy policies:
	http://www.sun.com/share/text/termsofuse.html
	http://www.sun.com/privacy/ 
	
	 * FEEDBACK
	Comments? Send your feedback on the JDC Tech Tips to: 
	jdc-webmaster@sun.com
	
	 * SUBSCRIBE/UNSUBSCRIBE
	- To subscribe, go to the subscriptions page,
	(http://developer.java.sun.com/subscription/), choose
	the newsletters you want to subscribe to and click "Update".
	- To unsubscribe, go to the subscriptions page,
	(http://developer.java.sun.com/subscription/), uncheck the
	appropriate checkbox, and click "Update".
	- To use our one-click unsubscribe facility, see the link at 
	the end of this email:
	
	- ARCHIVES
	You'll find the JDC Tech Tips archives at:
	
	http://java.sun.com/jdc/TechTips/index.html
	
	
	- COPYRIGHT
	Copyright 2001 Sun Microsystems, Inc. All rights reserved.
	901 San Antonio Road, Palo Alto, California 94303 USA.
	
	This document is protected by copyright. For more information, see:
	
	http://java.sun.com/jdc/copyright.html
	
	
	JDC Tech Tips 
	December 20, 2001
	
	Sun, Sun Microsystems, Java, and Java Developer Connection are
	trademarks or registered trademarks of Sun Microsystems, Inc. 
	in the United States and other countries.
	
	
	To use our one-click unsubscribe facility, select the following URL:
	http://sunmail.sun.com/unsubscribe?7171485-492267985
	 */
}
