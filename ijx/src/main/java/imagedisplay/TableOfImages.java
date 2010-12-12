/*
 * TableOfImages.java
 *
 * Created on June 29, 2006, 2:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package imagedisplay;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class TableOfImages
    extends JPanel // implements ListSelectionListener
{

    JTable table;
    JPopupMenu popup;
    JMenuItem menuItem;
    SeriesOfImages series;

    public TableOfImages(String FILE, int zSections, int sampling, int sizeOfThumbs)
      {

        int colWidth = sizeOfThumbs;
        int rowHeight = sizeOfThumbs;


        series = new SeriesOfImages(FILE, zSections);
        int nT = series.getTimeIntervals();
        int nZ = series.getZSections();

        Dimension dim = series.getImageDimensions();
        if (dim.getWidth() >= dim.getHeight()) {
            sampling = 1 + (int) dim.getWidth() / sizeOfThumbs;

            colWidth = sizeOfThumbs;
            rowHeight = (int) (sizeOfThumbs * dim.getHeight() /
                (float) dim.getWidth());
        } else {
            sampling = (int) dim.getHeight() / sizeOfThumbs;
            rowHeight = sizeOfThumbs;
            colWidth = (int) (sizeOfThumbs * dim.getWidth() /
                (float) dim.getHeight());
        }

        System.out.println(nT + " : " + nZ);
        Object[][] data =
            new Object[nZ][nT];

        for (int t = 0; t < series.getTimeIntervals(); t++) {
            for (int z = 0; z < series.getZSections(); z++) {
                data[z][t] =
                    new ImageIcon(series.getAsThumbnail(t * nZ + z,
                    sampling));
            }
        }

        // Column headers
        Object[] column = new Object[nT];
        for (int i = 0; i < nT; i++) {
            column[i] = "t=" + String.valueOf(i + 1);
        }
        // Row headers
        String[] rowHeaders = new String[nZ];
        for (int i = 0; i < nZ; i++) {
            rowHeaders[i] = "z=" + String.valueOf(i + 1);
        }



        createTableOfImages(data, column, colWidth,
            rowHeaders, rowHeight);
      }

    private void createTableOfImages(final Object[][] data, final Object[] column,
        int colWidth, final String[] rowHeaders, int rowHeight)
      {

        AbstractTableModel model = new AbstractTableModel() {

            public int getColumnCount()
              {
                return column.length;
              }

            public int getRowCount()
              {
                return data.length;
              }

            public String getColumnName(int col)
              {
                return (String) column[col];
              }

            public Object getValueAt(int row, int col)
              {
                return data[row][col];
              }

            public Class getColumnClass(int col)
              {
                return ImageIcon.class;
              }
        };

        table = new JTable(model);
        setImageObserver(table);
        // Size
        table.setRowHeight(rowHeight + 1);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // makes horiz scrollable
        // set all columns fixed width
        colWidth = colWidth + 1;
        int numCols = table.getColumnModel().getColumnCount();
        for (int i = 0; i < numCols; i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(colWidth);
            table.getColumnModel().getColumn(i).setMinWidth(colWidth);
            table.getColumnModel().getColumn(i).setMaxWidth(colWidth);
        }
        table.setIntercellSpacing(new Dimension(0, 0));

        // Selection
        table.setSelectionBackground(Color.gray);
        table.setCellSelectionEnabled(true);
        table.setRowSelectionAllowed(true);
        table.setColumnSelectionAllowed(true);

        //table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        //table.setDefaultRenderer(Object.class, new BorderCellRenderer());

        // Add responses to actions
        //ListSelectionModel listMod = table.getSelectionModel();
        //listMod.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //listMod.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        //listMod.addListSelectionListener(this);

        SelectionListener listener = new SelectionListener(table);
        table.getSelectionModel().addListSelectionListener(listener);
        table.getColumnModel().getSelectionModel().addListSelectionListener(listener);

        // Mouse Events
        table.addMouseListener(new MouseAdapter() { // add DoubleClickability

            public void mouseClicked(MouseEvent e)
              {
                if (e.getClickCount() == 2) {
                    System.out.println(" double click");
                    int r = table.getSelectedRow();
                    int c = table.getSelectedColumn();
                    System.out.println(r + "," + c);
                    // Open the image in viewer...
                    openImageViewer(series, r, c);
                }
                // Popup Menu
                if (SwingUtilities.isRightMouseButton(e)) {
                    //int index = list.locationToIndex(e.getPoint());

                    popup.show(e.getComponent(), e.getX(), e.getY());
                }

              }
        });
        // PopUp Menus
        ActionListener popupActionListener =
            new ActionListener() {

                public void actionPerformed(ActionEvent e)
                  {
                    JMenuItem source = (JMenuItem) (e.getSource());
                    String action = source.getText();
                    if (action == "Open") {
                        //opPanel.openInViewer();
                    }
                    if (action == "Delete") {
                        //opPanel.delete();
                    }
                  }
            };
        popup = new JPopupMenu();
        menuItem = new JMenuItem("Open");
        menuItem.addActionListener(popupActionListener);
        popup.add(menuItem);
        menuItem = new JMenuItem("Delete");
        menuItem.addActionListener(popupActionListener);
        popup.add(menuItem);

        // Row Headers
        ListModel lmRHeader = new AbstractListModel() {
//            String headers[] = {
//                    "a", "b", "c", "d", "e", "f", "g", "h", "i"};
            public int getSize()
              {
                return rowHeaders.length;
              }

            public Object getElementAt(int index)
              {
                return rowHeaders[index];
              }
        };


        JList rowHeader = new JList(lmRHeader);
        rowHeader.setFixedCellWidth(20);
        rowHeader.setFixedCellHeight(table.getRowHeight() + table.getRowMargin());
//                             + table.getIntercellSpacing().height);
        rowHeader.setCellRenderer(new RowHeaderRenderer(table));

        // Scroll Panel
        JScrollPane pane = new JScrollPane(table);
        pane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        pane.setRowHeaderView(rowHeader);
        this.setLayout(new BorderLayout());
        add(pane, BorderLayout.CENTER);
      }

    private void openImageViewer(SeriesOfImages series, int r, int c)
      {
        int n = r + c *  series.getZSections();
        String title = "" + series.getFilename() + ": " + r + ", " + c + " (" + n +")";
        BufferedImage img = series.getImage(n);
                (new FrameImageDisplay(img, title)).setVisible(true);      
    }
//------------------------------------------------------
    private void setImageObserver(JTable table)
      {
        TableModel model = table.getModel();
        int colCount = model.getColumnCount();
        int rowCount = model.getRowCount();
        for (int col = 0; col < colCount; col++) {
            if (ImageIcon.class == model.getColumnClass(col)) {
                for (int row = 0; row < rowCount; row++) {
                    ImageIcon icon = (ImageIcon) model.getValueAt(row, col);
                    if (icon != null) {
                        icon.setImageObserver(new CellImageObserver(table, row,
                            col));
                    }
                }
            }
        }
      }

//--------------------------------------------------------------------
    class CellImageObserver
        implements ImageObserver {

        JTable table;
        int row, col;

        CellImageObserver(JTable table, int row, int col)
          {
            this.table = table;
            this.row = row;
            this.col = col;
          }

        public boolean imageUpdate(Image img, int flags,
            int x, int y, int w, int h)
          {
            if ((flags & (FRAMEBITS | ALLBITS)) != 0) {
                Rectangle rect = table.getCellRect(row, col, false);
                table.repaint(rect);
            }
            return (flags & (ALLBITS | ABORT)) == 0;
          }
    }
//------------------------------------------------------
//    public void valueChanged (ListSelectionEvent e) {
//        int maxRows;
//        int[] selRows;
//        int[] selCols;
//        Object value;
//        System.out.println(e.toString());
//        if (!e.getValueIsAdjusting()) {
//            selRows = table.getSelectedRows();
//            selCols = table.getSelectedColumns();
//            for (int i = 0; i < selRows.length; i++) {
//                System.out.print(selRows[i] + ", ");
//            }
//            System.out.println("");
//            for (int i = 0; i < selCols.length; i++) {
//                System.out.println(selCols[i] + ", ");
//            }
//
//        }
//      if (selRows.length > 0) {
//        for (int i = 0; i < 3; i++) {
//          // get Table data
//          TableModel tm = table.getModel();
//          value = tm.getValueAt(selRows[0], i);
//          System.out.println("Selection : " + value);
//        }
//        System.out.println();
//      }
//    }
//-------------------------------------------------------------------
    class SelectionListener
        implements ListSelectionListener {

        JTable table;
        // It is necessary to keep the table since it is not possible
        // to determine the table from the event's source
        SelectionListener(final JTable table)
          {
            this.table = table;
          }

        public void valueChanged(ListSelectionEvent e)
          {
            // If cell selection is enabled, both row and column change events are fired
            System.out.println(e.toString());
            if (e.getSource() == table.getSelectionModel() && table.getRowSelectionAllowed()) {
                // Column selection changed
                int first = e.getFirstIndex();
                int last = e.getLastIndex();
                System.out.println("Col: " + first + " - " + last);
            } else if (e.getSource() == table.getColumnModel().getSelectionModel() && table.getColumnSelectionAllowed()) {
                // Row selection changed
                int first = e.getFirstIndex();
                int last = e.getLastIndex();
                System.out.println("Row: " + first + " - " + last);
            }

            if (e.getValueIsAdjusting()) {
                // The mouse button has not yet been released
            }
          }
    }
//--------------------------------------------------------------------------
    class RowHeaderRenderer
        extends JLabel
        implements ListCellRenderer {

        RowHeaderRenderer(JTable table)
          {
            JTableHeader header = table.getTableHeader();
            setOpaque(true);
            setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            setHorizontalAlignment(CENTER);
            setForeground(header.getForeground());
            setBackground(header.getBackground());
            setFont(header.getFont());
          }

        public Component getListCellRendererComponent(JList list,
            Object value, int index, boolean isSelected,
            boolean cellHasFocus)
          {
            setText((value == null) ? "" : value.toString());
            return this;
          }
    }
//--------------------------------------------------------------------
    public static void main(String[] args)
      {

        // @todo open selected image on double click.

        String FILE = "xyzt-200x200x10x15.tif";
        //"Series_TZ\\STAPS_04_0621_1451_54.tif";
        //"Series_TZ\\xyzt-200x200x10x15_b.tif";
        // "Series_TZ\\31Aug95.Newt3Lamellap.tif";

        int zSections = 10; // Number of Z-sections

        int sampling = 3; // for thumbnails
        int sizeOfThumbs = 256;

        TableOfImages table = new TableOfImages(FILE, zSections, sampling, sizeOfThumbs);

        JFrame frame = new JFrame("ImageTableTest");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(table, BorderLayout.CENTER);
        frame.setSize(300, 150);
        frame.setVisible(true);
      }
}//=================================================================================
/*
e941. Programmatically Making Selections in a JTable Component
To select columns, setColumnSelectionInterval(), addColumnSelectionInterval(), and removeColumnSelectionInterval() are available. However, these only work if columnSelectionAllowed is true and rowSelectionAllowed is false. This also applies to rows.

To select individual cells or blocks of cells, use changeSelection(). However, both columnSelectionAllowed and rowSelectionAllowed must be false.

These selection methods observe the setting of the selection mode. For example, if the selection mode is SINGLE_SELECTION, only a single row, column, or cell can be made. See e940 Enabling Single or Multiple Selections in a JTable Component for more information on selection modes.

int rows = 10;
int cols = 5;
JTable table = new JTable(rows, cols);

// Use this mode to demonstrate the following examples
table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

// The following column selection methods work only if these
// properties are set this way
table.setColumnSelectionAllowed(true);
table.setRowSelectionAllowed(false);

// Select a column - column 0
table.setColumnSelectionInterval(0, 0);

// Select an additional range of columns - columns 1 to 2
table.addColumnSelectionInterval(1, 2);

// Deselect a range of columns - columns 0 to 1
table.removeColumnSelectionInterval(0, 1);


// The following row selection methods work only if these
// properties are set this way
table.setColumnSelectionAllowed(false);
table.setRowSelectionAllowed(true);

// Select a row - row 0
table.setRowSelectionInterval(0, 0);

// Select an additional range of rows - rows 1 to 2
table.addRowSelectionInterval(1, 2);

// Deselect a range of rows - rows 0 to 1
table.removeRowSelectionInterval(0, 1);


// The following cell selection methods work only if these
// properties are set this way
table.setColumnSelectionAllowed(true);
table.setRowSelectionAllowed(true);

// Select a cell: cell (2,1)
int row = 2;
int col = 1;
boolean toggle = false;
boolean extend = false;
table.changeSelection(row, col, toggle, extend);

// Extend the selection to include all cells between (2,1) to (5,3)
row = 5;
col = 3;
toggle = false;
extend = true;
table.changeSelection(row, col, toggle, extend);

// Deselect a cell: cell (3,2)
// All cells in the row and column containing (3,2) are deselected.
row = 3;
col = 2;
toggle = true;
extend = false;
table.changeSelection(row, col, toggle, extend);

// This method actually toggles the selection state so that
// if it were called again, it exactly reverses the first call.
// Select cell (3,2) as well as the other cells that
// were deselected in the first call.
toggle = true;
extend = false;
table.changeSelection(row, col, toggle, extend);

// Select all cells
table.selectAll();

// Deselect all cells
table.clearSelection();
 */

/*
e964. Listening for Selection Events in a JTable Component
To listen for selection changes, you need to add a listener to both the JTable's table model and to the table column model.

A selection change event has a first and last property that specifies the range of affected rows (or columns). For example, if the 5th row is selected and the user selects the 7th row, the first property is 4 and the last is property 6. There is not enough information in the event to determine exactly which cells have changed. See e942 Getting the Selected Cells in a JTable Component for an example of how to determine the selected cells.

Changing the selection programmatically (see e941 Programmatically Making Selections in a JTable Component) causes selection change events to be fired.

SelectionListener listener = new SelectionListener(table);
table.getSelectionModel().addListSelectionListener(listener);
table.getColumnModel().getSelectionModel()
.addListSelectionListener(listener);

public class SelectionListener implements ListSelectionListener {
JTable table;

// It is necessary to keep the table since it is not possible
// to determine the table from the event's source
SelectionListener(JTable table) {
this.table = table;
}
public void valueChanged(ListSelectionEvent e) {
// If cell selection is enabled, both row and column change events are fired
if (e.getSource() == table.getSelectionModel()
&& table.getRowSelectionAllowed()) {
// Column selection changed
int first = e.getFirstIndex();
int last = e.getLastIndex();
} else if (e.getSource() == table.getColumnModel().getSelectionModel()
&& table.getColumnSelectionAllowed() ){
// Row selection changed
int first = e.getFirstIndex();
int last = e.getLastIndex();
}

if (e.getValueIsAdjusting()) {
// The mouse button has not yet been released
}
}
}
 */

/*
e967. Listening for Clicks on a Column Header in a JTable Component

int rows = 10;
int cols = 5;
JTable table = new JTable(rows, cols);
JTableHeader header = table.getTableHeader();

header.addMouseListener(new ColumnHeaderListener());

public class ColumnHeaderListener extends MouseAdapter {
public void mouseClicked(MouseEvent evt) {
JTable table = ((JTableHeader)evt.getSource()).getTable();
TableColumnModel colModel = table.getColumnModel();

// The index of the column whose header was clicked
int vColIndex = colModel.getColumnIndexAtX(evt.getX());
int mColIndex = table.convertColumnIndexToModel(vColIndex);

// Return if not clicked on any column header
if (vColIndex == -1) {
return;
}

// Determine if mouse was clicked between column heads
Rectangle headerRect = table.getTableHeader().getHeaderRect(vColIndex);
if (vColIndex == 0) {
headerRect.width -= 3;    // Hard-coded constant
} else {
headerRect.grow(-3, 0);   // Hard-coded constant
}
if (!headerRect.contains(evt.getX(), evt.getY())) {
// Mouse was clicked between column heads
// vColIndex is the column head closest to the click

// vLeftColIndex is the column head to the left of the click
int vLeftColIndex = vColIndex;
if (evt.getX() < headerRect.x) {
vLeftColIndex--;
}
}
}
}
 */

/*
e942. Getting the Selected Cells in a JTable Component
The method for determining the selected cells depends on whether column, row, or cell selection is enabled.

JTable table = new JTable();

if (table.getColumnSelectionAllowed()
&& !table.getRowSelectionAllowed()) {
// Column selection is enabled
// Get the indices of the selected columns
int[] vColIndices = table.getSelectedColumns();
} else if (!table.getColumnSelectionAllowed()
&& table.getRowSelectionAllowed()) {
// Row selection is enabled
// Get the indices of the selected rows
int[] rowIndices = table.getSelectedRows();
} else if (table.getCellSelectionEnabled()) {
// Individual cell selection is enabled

// In SINGLE_SELECTION mode, the selected cell can be retrieved using
table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
int rowIndex = table.getSelectedRow();
int colIndex = table.getSelectedColumn();

// In the other modes, the set of selected cells can be retrieved using
table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

// Get the min and max ranges of selected cells
int rowIndexStart = table.getSelectedRow();
int rowIndexEnd = table.getSelectionModel().getMaxSelectionIndex();
int colIndexStart = table.getSelectedColumn();
int colIndexEnd = table.getColumnModel().getSelectionModel()
.getMaxSelectionIndex();

// Check each cell in the range
for (int r=rowIndexStart; r<=rowIndexEnd; r++) {
for (int c=colIndexStart; c<=colIndexEnd; c++) {
if (table.isCellSelected(r, c)) {
// cell is selected
}
}
}
}

 */
/*
e940. Enabling Single or Multiple Selections in a JTable Component
By default, a table component allows multiple selections. To control the selection behavior, see e939 Enabling Row, Column, or Cell Selections in a JTable Component.

JTable table = new JTable();

// Get default selection mode
int selMode = table.getSelectionModel().getSelectionMode();
// MULTIPLE_INTERVAL_SELECTION

// Allow only single a selection
table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

// Allow selection to span one contiguous set of rows,
// visible columns, or block of cells
table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

// Allow multiple selections of rows, visible columns, or cell blocks (default)
table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 *///---------------------------------------------------------------------------
// Row Headers
/*
package tame.examples;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

// derived from @author Nobuo Tamemasa @version 1.0 11/09/98

public class RowHeaderExample extends JFrame {

public RowHeaderExample() {
super( "Row Header Example" );
setSize( 300, 150 );

ListModel lm = new AbstractListModel() {
String headers[] = {"a", "b", "c", "d", "e", "f", "g", "h", "i"};
public int getSize() { return headers.length; }
public Object getElementAt(int index) {
return headers[index];
}
};

DefaultTableModel dm = new DefaultTableModel(lm.getSize(),10);
JTable table = new JTable( dm );
table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

JList rowHeader = new JList(lm);
rowHeader.setFixedCellWidth(50);

rowHeader.setFixedCellHeight(table.getRowHeight()
+ table.getRowMargin());
//                             + table.getIntercellSpacing().height);
rowHeader.setCellRenderer(new RowHeaderRenderer(table));

JScrollPane scroll = new JScrollPane( table );
scroll.setRowHeaderView(rowHeader);
getContentPane().add(scroll, BorderLayout.CENTER);
}

public static void main(String[] args) {
RowHeaderExample frame = new RowHeaderExample();
frame.addWindowListener( new WindowAdapter() {
public void windowClosing( WindowEvent e ) {
System.exit(0);
}
});
frame.setVisible(true);
}
}


class RowHeaderRenderer extends JLabel implements ListCellRenderer {

RowHeaderRenderer(JTable table) {
JTableHeader header = table.getTableHeader();
setOpaque(true);
setBorder(UIManager.getBorder("TableHeader.cellBorder"));
setHorizontalAlignment(CENTER);
setForeground(header.getForeground());
setBackground(header.getBackground());
setFont(header.getFont());
}

public Component getListCellRendererComponent( JList list,
Object value, int index, boolean isSelected, boolean cellHasFocus) {
setText((value == null) ? "" : value.toString());
return this;
}
}
 */
