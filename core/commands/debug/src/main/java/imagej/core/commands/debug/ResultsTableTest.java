package imagej.core.commands.debug;

import imagej.command.Command;
import imagej.data.table.DefaultResultsTable;
import imagej.data.table.ResultsTable;
import imagej.event.StatusService;
import imagej.module.ItemIO;
import imagej.plugin.Parameter;
import imagej.plugin.Plugin;

/** A test of {@link ResultsTable}. */
@Plugin(menuPath = "Plugins>Sandbox>Results Table Test", headless = true)
public class ResultsTableTest implements Command {

	@Parameter
	private StatusService statusService;

	@Parameter(label = "Paul Molitor Baseball Statistics", type = ItemIO.OUTPUT)
	private ResultsTable baseball;

	@Parameter(label = "Big Table", type = ItemIO.OUTPUT)
	private ResultsTable big;

	@Override
	public void run() {
		statusService.showStatus("Creating a small table...");
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
		baseball = new DefaultResultsTable(data[0].length, data.length);
		baseball.setColumnHeader("Year", 0);
		baseball.setColumnHeader("Age", 1);
		baseball.setColumnHeader("BA", 2);
		for (int row = 0; row < data.length; row++) {
			for (int col = 0; col < data[row].length; col++) {
				baseball.setValue(col, row, data[row][col]);
			}
		}

		// create a larger table with 10 million elements
		statusService.showStatus("Creating a large table...");
		final int colCount = 10, rowCount = 1000000;
		big = new DefaultResultsTable(colCount, rowCount);
		for (int col = 0; col < colCount; col++) {
			statusService.showProgress(col, colCount);
			for (int row = 0; row < rowCount; row++) {
				big.setValue(col, row, row + col);
			}
		}
		statusService.clearStatus();
	}

}
