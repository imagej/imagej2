
package imagej.data.table;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests {@link DefaultResultsTable}.
 * 
 * @author Curtis Rueden
 */
public class DefaultResultsTableTest {

	public ResultsTable createTable() {
		// Paul Molitor
		final double[][] data = {
			{1978, 21, .273},
			{1979, 22, .322},
			{1980, 23, .304},
			{1981, 24, .267},
			{1982, 25, .302},
			{1983, 26, .270},
			{1984, 27, .217},
			{1985, 28, .297},
			{1986, 29, .281},
			{1987, 30, .353},
			{1988, 31, .312},
			{1989, 32, .315},
			{1990, 33, .285},
			{1991, 34, .325},
			{1992, 35, .320},
			{1993, 36, .332},
			{1994, 37, .341},
			{1995, 38, .270},
			{1996, 39, .341},
			{1997, 40, .305},
			{1998, 41, .281},
		};
		final ResultsTable table =
			new DefaultResultsTable(data[0].length, data.length);
		table.setColumnHeader("Year", 0);
		table.setColumnHeader("Age", 1);
		table.setColumnHeader("BA", 2);
		for (int row=0; row<data.length; row++) {
			for (int col=0; col<data[row].length; col++) {
				table.setValue(col, row, data[row][col]);
			}
		}
		return table;
	}

	@Test
	public void testStructure() {
		final ResultsTable table = createTable();
		assertEquals(3, table.getColumnCount());
		assertEquals(21, table.getRowCount());
		for (DoubleColumn column : table) {
			assertEquals(21, column.size());
		}
	}

	// TODO - Add more tests.

}
