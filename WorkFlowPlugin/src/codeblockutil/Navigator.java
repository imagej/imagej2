package codeblockutil;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.Timer;


/**
 * A navigator navigates between different Explorers.
 * Though the UI holds a common theme, each explorer
 * may represent a unique subset of the whole.
 * 
 * Basically, say we have a bunch of canvases (100).
 * Say that we group the canvases into 5 explorers
 * based on some quality and have each individual explorer
 * independently control and display its own group
 * of canvases.  The problem we run into is how do we switch
 * from one explorer to the next.  There are many ways
 * to do this.  We can have buttons that makes unused
 * explorers hidden.  Or we have shrink them.  This
 * particular implementation chooses to slide them to
 * the side.
 * 
 * Now that you know why we need a navigator, let's talk
 * about what a Navigator is.  A Navigator lines up
 * a set of explorers along the x-axis.  It slides between
 * the explorers until it reaches the end.  That's it. Simple?
 * 
 * Note however, that each explorer must be UNIQUE.  NO EXCEPTIONS!
 * That is, the name of a particular explorer may not be
 * shared by any other explorers.  This check rep. is made when 
 * adding new explorer.
 * 
 * A Navigator unfortunately makes a once dangerous assumption: the name of
 * each explorer must remain the same forever.  If the name changes, then
 * the invariant described in the previous paragraph no longer holds.
 * 
 * @author An Ho
 *
 */
final public class Navigator{
	/** The UI options */
	public enum Type{GLASS, MAGIC, POPUP, STACK, TABBED, WINDOW};
	/** UI Type */
	private Type explorerModel;;
	/** The index of the active explorer being viewed.  0<=position<explorers.size */
	private int position;
	/** Ordered set of explorers */
	private List<Explorer> explorers;
	/** The UI used to navigate between explorers */
	private JScrollPane scroll;
	/** The viewport that holds all the explorers. Explorers should be lined up in order within this "view" */
	private JComponent view;
	/** The UI used to switch between different explorers */
	private ExplorerSwitcher switcher;
	/** Displays the sliding action when moving from one explorer to the next */
	private NavigationAnimator animator;
	/**
	 * Constructs new navigator with an empty collection of canvases.
	 */
	public Navigator () {
		this(Type.GLASS);
	}
	public Navigator (Type UIModel) {
		explorerModel = UIModel;
		animator = new NavigationAnimator();
		explorers = new ArrayList<Explorer>();
		view = new JPanel();
		position = 0;
		view.setBackground(Color.darkGray);
		view.setLayout(null);
		this.scroll = new JScrollPane(view,
				ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
				ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		this.switcher = new ExplorerSwitcher();
	}
	/**
	 * prints an error message for debugging purposes
	 * @param m
	 */
	private void printError(String m){
		System.out.println(m);
		//new RuntimeException(m).printStackTrace();
	}
	/**
	 * Adds a new Explorer
	 * @param name - the name of the new explorer
	 * 
	 * @requires NONE
	 * @effects If the name is null, do nothing.
	 * 			If the name is not unique, do nothing
	 * 			If the name is not null, is unique, then add it to this
	 * 			navigators set of explorers and update the UI.
	 */
	final public void addExlorer(String name){
		if(name == null){
			this.printError("Name of explorer may not be assigned as null");
			return;
		}
		for(Explorer explorer : explorers){
			if (explorer.getName().equals(name)){
				this.printError("May not add duplicate explorers named: "+name);
				return;
			}
		}
		Explorer explorer;
		if (explorerModel == Type.GLASS){
			explorer = new GlassExplorer();
		}else if (explorerModel ==  Type.MAGIC){
			explorer = new MagicExplorer();
		}else if (explorerModel == Type.POPUP){
			explorer = new PopupExplorer();
		}else if (explorerModel == Type.WINDOW){
			explorer = new WindowExplorer();
		}else if (explorerModel == Type.TABBED){
			explorer = new TabbedExplorer();
		}else {
			explorer = new StackExplorer();
		}
		explorer.setName(name);
		explorers.add(explorer);
		view.add(explorer.getJComponent());
		this.reformView();
		
	}
	/**
	 * Removes the explorer with the specified name.  If no
	 * explorer is found with that name, then do nothing.
	 * If the specified name is null, then do nothing.
	 * @param name
	 * 
	 * @requires NONE
	 * @effects If the name is null, do nothing.
	 * 			If the name is not unique, do nothing.
	 * 			If no explorer with the specified name, do nothing.
	 * 			If the name is not null, is unique, and there
	 * 			exists a corresponding explorer, then remove it from
	 * 			navigator's set of explorers and update the UI.
	 */
	final public void removeExplorer(String name){
		if(name == null){
			this.printError("Name of explorer may not be assigned as null");
			return;
		}
		Explorer explorer = null;
		for(Explorer ex : explorers){
			if (ex.getName().equals(name)){
				if(explorer == null){
					explorer = ex;
				}else{
					this.printError("Navigator has duplicate explorers named: "+name);
					return;
				}
			}
		}
		if(explorer == null){
			this.printError("Navigator does not have a explorer named: "+name);
			return;
		}
		explorers.remove(explorer);
		view.remove(explorer.getJComponent());
		this.reformView();
	}
	
	public List<Explorer> getExplorers(){
		return this.explorers;
	}
	/**
	 * @param name
	 * @return true iff there exists an explorer whose name is
	 * equal to the specified name.  If name is null, return false.
	 */
	public boolean hasExplorer(String name){
		if(name == null) return false;
		for(Explorer explorer : explorers){
			if (explorer.getName().equals(name)){
				return true;
			}
		}
		return false;
	}
	/**
	 * redraws and revalidates the size and preferred size of the viewport
	 * and scroll pane to reflect the current collection of explorers.
	 */
	public void reformView(){
		int accumWidth = 0;
		int height = scroll.getHeight();
		int width = scroll.getWidth();
		for(Explorer explorer : explorers){
			explorer.getJComponent().setBounds(accumWidth, 0, width, height);
			explorer.reformView();
			accumWidth += width;
		}
		view.setPreferredSize(new Dimension(accumWidth, height));
		view.setBounds(0, 0, accumWidth, height);
		scroll.revalidate();
		scroll.repaint();
		scroll.getHorizontalScrollBar().setValue(explorers.get(position).getJComponent().getX());
	}
	/**
	 * Reassigns the canvases to the explorer with the specified name.  If
	 * no explorer is found to have that name, or if the name is null,
	 * then do nothing.
	 * @param canvases
	 * @param explorer
	 * 
	 * @requires canvases!= null
	 */
	public void setCanvas(List<? extends Canvas> canvases, String explorer){
		for(Explorer ex : explorers){
			if(ex.getName().equals(explorer)){
				ex.setDrawersCard(canvases);
			}
		}
		this.reformView();
	}
	/**
	 * Sets the view to the explorer with the specified name.
	 * If no explorers are found with a matching name, or if the
	 * name is null, then do nothing.
	 * @param name
	 */
	public void setView(String name){
		for(int i=0; i<explorers.size(); i++){
			Explorer ex = explorers.get(i);
			if(ex.getName().equals(name)){
				setView(i);
			}
		}
		
	}
	/**
	 * Sets the view to the explorer with the specified
	 * index.  The index is first transformed such that:
	 * 		(1) if index < 0 , then index = explorers.size;
	 * 		(2) if index >= explorers.size, then index =0;
	 * 		(3) if explorer.isEmpty == true, then do nothing.
	 * We must change the state and GUI to reflect this change.
	 * @param index
	 */
	private void setView(int index){
		
		//if the explorers are empty, make no changes.
		if(!explorers.isEmpty()){
			//first we determine what middlePosition is (the explorer we're currently viewing)
			
			int middlePosition = index<0 ? explorers.size()-1 : index;
			middlePosition = index>=explorers.size() ? 0 : middlePosition;
			
			int leftPosition = middlePosition-1;
			int rightPosition = middlePosition+1;
			
			if(leftPosition<0){
				leftPosition = explorers.size()-1;
			}
			if(rightPosition>explorers.size()-1){
				rightPosition = 0;
			}
			
			//then we set the abstract position state
			this.position = middlePosition;
			
			//then we change the GUI
			switcher.switchView(
					explorers.get(leftPosition).getName(),
					explorers.get(position).getName(),
					explorers.get(rightPosition).getName());
			animator.start(explorers.get(position).getJComponent().getX());
		}
	}
	/**
	 * @return the JCOmponent representation of this.  MAY NOT BE NULL
	 */
	public JComponent getJComponent(){
		return scroll;
	}
	/**
	 * @return the JComponent representation of the switching tool pane.
	 */
	public JComponent getSwitcher(){
		return this.switcher;
	}
	
	/**
	 * Switching tool pane that provides the graphical interface for switching
	 * between different explorers within this navigator
	 */
	private class ExplorerSwitcher extends JPanel{
		private static final long serialVersionUID = 328149080295L;
		private int LABEL_HEIGHT = 9;
    	private JLabel mainLabel;
    	private JLabel leftLabel;
    	private CArrowButton leftArrow;
    	private JLabel rightLabel;
    	private CArrowButton rightArrow;
    	private ExplorerSwitcher(){
    		leftLabel = new JLabel("", SwingConstants.LEFT);
    		leftLabel.setForeground(Color.white);
    		leftLabel.setFont(new Font("Ariel", Font.PLAIN, LABEL_HEIGHT));
    		leftArrow = new CArrowButton(CArrowButton.Direction.WEST){
    			private static final long serialVersionUID = 328149080296L;
    			public void triggerAction(){
    				setView(position-1);
    			}
    		};
    		
    		rightLabel = new JLabel("",SwingConstants.RIGHT);
    		rightLabel.setForeground(Color.white);
    		rightLabel.setFont(new Font("Ariel", Font.PLAIN, LABEL_HEIGHT));
    		rightArrow = new CArrowButton(CArrowButton.Direction.EAST){
    			private static final long serialVersionUID = 328149080297L;
    			public void triggerAction(){
    				setView(position+1);
    			}
    		};
    		
    		mainLabel = new JLabel("", SwingConstants.CENTER);
    		mainLabel.setFont(new Font("Ariel", Font.BOLD, 15));
    		mainLabel.setForeground(Color.white);
    		mainLabel.setOpaque(false);
    		
    		double[][] constraints = {{15,15,15,TableLayoutConstants.FILL,15,15,15},
					  {TableLayoutConstants.FILL,20,10,5}};
    		this.setLayout(new TableLayout(constraints));
    		this.setOpaque(false);
    		this.add(leftLabel,"0, 2, 2, 2");
    		this.add(leftArrow,"1, 1");
    		this.add(rightLabel,"4, 2, 6, 2");
    		this.add(rightArrow,"5, 1");
    		this.add(mainLabel,"3, 0, 3, 2");
    	}
    	/**
    	 * Switches the switcher UI to reflect the current state of the navigator
    	 * 
    	 * @param newIndex
    	 */
    	void switchView(String left, String middle, String right){
			leftLabel.setText(left);
			mainLabel.setText(middle);
			rightLabel.setText(right);
    	}
    }
	
	
	/**
	 * This Animator is responsible for providing the sliding action that occurs
	 * when the navigator needs to switch from one explorer to the next.
	 * @author An Ho
	 */
	private class NavigationAnimator implements ActionListener{
		/** The maximum number of times we can scroll.  A higher value gives it a higher frames/second */
		private final int partitions = 10;
		/** An internal timer that manages incremental scrolling */
		private Timer timer;
		/** The new x-location of the view after scrolling is complete */
		private int value;
		/** The scrolling unit we wish to scroll for every incremental time step */
		private int dx;
		/** How many times have we scroll? */
		private int count;
		/** Instantiates a single time for everything. */
		private NavigationAnimator () {
			timer = new Timer(50, this);
			value = 0;
		}
		/**
		 * This method takes in a x-coordinate that represents the new x location
		 * that we wish to view.  The x-coordinate should be with respect to this.scroll
		 * and should be in terms of pixels.  The animator will then animate a sliding
		 * motion from the current view to the view seen at location x.
		 * @param x - The x location to view.
		 */
		private void start(int x){
			this.count = partitions;
			this.value = x;
			this.dx = ((x-scroll.getHorizontalScrollBar().getValue())/partitions);
			timer.start();
		}
		/** Keep scrolling until be get to x */
		public void actionPerformed(ActionEvent e){
			if(count<0){
				timer.stop();
				scroll.getHorizontalScrollBar().setValue(value);
				scroll.revalidate();
				scroll.repaint();
			}else{
				scroll.getHorizontalScrollBar().setValue(
						value-dx*count);
				count--;		
				scroll.revalidate();
				scroll.repaint();
			}
		}
	}
	/** Testing purposes */
	public static void main(String[] args) {
		class CC extends DefaultCanvas{
			private static final long serialVersionUID = 328149080298L;
			public CC(String label){
				super();
				super.setName(label);
			}
			public JComponent getJComponent(){return new JButton(this.getName());}
		}
		final Navigator n = new Navigator();
		for(int i = 0 ; i<8; i++){
			List<Canvas> c1 = new ArrayList<Canvas>();
			for(int j= 0; j<10; j++){
				c1.add(new CC("# "+j));
			}
			n.addExlorer("Ex"+i);
			n.setCanvas(c1,"Ex"+i);
		}
		JFrame f = new JFrame();
		f.addComponentListener(new ComponentAdapter(){
			public void componentResized(ComponentEvent e){
				n.reformView();
			}
		});
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setLayout(new BorderLayout());
		f.setSize(400,600);
		f.add(n.getJComponent(), BorderLayout.CENTER);
		f.add(n.getSwitcher(), BorderLayout.NORTH);
		f.setVisible(true);
	}
}