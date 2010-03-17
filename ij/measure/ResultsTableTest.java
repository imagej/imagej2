package ij.measure;

import static org.junit.Assert.*;
import ij.IJInfo;
import ij.io.Assert;
import ij.plugin.filter.Analyzer;

import org.junit.Test;

public class ResultsTableTest {
	
	ResultsTable r;

	// this method used below to simplify methods
	
	private ResultsTable n() { return new ResultsTable(); }
	
	@Test
	public void testConstants() {
		
		// may be obsolete - delete later if not needed
		assertEquals(150,ResultsTable.MAX_COLUMNS);

		assertEquals(-1,ResultsTable.COLUMN_NOT_FOUND);
		assertEquals(-2,ResultsTable.COLUMN_IN_USE);
		// may be obsolete - delete later if not needed
		assertEquals(-3,ResultsTable.TABLE_FULL);
		
		assertEquals(0,ResultsTable.AREA);
		assertEquals(1,ResultsTable.MEAN);
		assertEquals(2,ResultsTable.STD_DEV);
		assertEquals(3,ResultsTable.MODE);
		assertEquals(4,ResultsTable.MIN);
		assertEquals(5,ResultsTable.MAX);
		assertEquals(6,ResultsTable.X_CENTROID);
		assertEquals(7,ResultsTable.Y_CENTROID);
		assertEquals(8,ResultsTable.X_CENTER_OF_MASS);
		assertEquals(9,ResultsTable.Y_CENTER_OF_MASS);
		assertEquals(10,ResultsTable.PERIMETER);
		assertEquals(11,ResultsTable.ROI_X);
		assertEquals(12,ResultsTable.ROI_Y);
		assertEquals(13,ResultsTable.ROI_WIDTH);
		assertEquals(14,ResultsTable.ROI_HEIGHT);
		assertEquals(15,ResultsTable.MAJOR);
		assertEquals(16,ResultsTable.MINOR);
		assertEquals(17,ResultsTable.ANGLE);
		assertEquals(18,ResultsTable.CIRCULARITY);
		assertEquals(19,ResultsTable.FERET);
		assertEquals(20,ResultsTable.INTEGRATED_DENSITY);
		assertEquals(21,ResultsTable.MEDIAN);
		assertEquals(22,ResultsTable.SKEWNESS);
		assertEquals(23,ResultsTable.KURTOSIS);
		assertEquals(24,ResultsTable.AREA_FRACTION);
		assertEquals(25,ResultsTable.CHANNEL);
		assertEquals(26,ResultsTable.SLICE);
		assertEquals(27,ResultsTable.FRAME);
		assertEquals(28,ResultsTable.FERET_X);
		assertEquals(29,ResultsTable.FERET_Y);
		assertEquals(30,ResultsTable.FERET_ANGLE);
		assertEquals(31,ResultsTable.MIN_FERET);
		assertEquals(32,ResultsTable.ASPECT_RATIO);
		assertEquals(33,ResultsTable.ROUNDNESS);
		assertEquals(34,ResultsTable.SOLIDITY);
		assertEquals(34,ResultsTable.LAST_HEADING);
	}

	@Test
	public void testResultsTable() {
		r = n();
		assertNotNull(r);
	}

	@Test
	public void testGetResultsTable() {
		assertSame(Analyzer.getResultsTable(),ResultsTable.getResultsTable());
	}

	@Test
	public void testIncrementCounter() {
		
		// test on unmodified ResultsTable
		r = n();
		for (int i = 0; i < 1000; i++)
			r.incrementCounter();
		assertEquals(1000,r.getCounter());
		
		// test on a table with some row headings
		r = n();
		for (int j = 0; j < 1000; j++)
		{
			r.incrementCounter();
			r.addLabel(""+j);
		}
		String[] labels = r.getRowLabels();
		for (int i = 0; i < 1000; i++)
			assertEquals(""+i,labels[i]);
		
		// test on a table with some column data
		r = n();
		r.incrementCounter();
		for (int i = 0; i < 1000; i++)
			r.addValue(i, i);
		
		for (int i = 0; i < 1000; i++)
			assertEquals(i,r.getValue(i, 0),Assert.FLOAT_TOL);
	}

	@Test
	public void testAddColumns() {
		r = n();
		
		int startPoint = r.getMaxColumns();
		r.incrementCounter();
		for (int i = 0; i < 1000; i++)
			r.addValue(i,i);
		
		assertTrue(r.getMaxColumns()>startPoint);
		for (int i = 0; i < 1000; i++)
		{
			assertEquals(i,r.getValue(i,0),Assert.DOUBLE_TOL);
			assertEquals("---",r.getColumnHeading(i));
		}
		
	}

	@Test
	public void testGetCounter() {
		r = n();
		assertEquals(0,r.getCounter());
		for (int i = 0; i < 1000; i++)
		{
			r.incrementCounter();
			assertEquals(i+1,r.getCounter());
		}
	}
	
	@Test
	public void testAddValueIntDouble() {
		int startMax;
		
		// try to add a value to a table whose counter == 0
		r = n();
		assertEquals(0,r.getCounter());
		try {
			r.addValue(0, 33);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// access less than zero
		r = n();
		r.incrementCounter();
		try {
			r.addValue(-1,8);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// access just beyond the end of current number of max cols - should allocate more columns
		r = n();
		r.incrementCounter();
		startMax = r.getMaxColumns();
		r.addValue(startMax+1, 9);  // should work
		assertEquals(startMax*2,r.getMaxColumns());
		
		// access beyond the end of current number of max cols - should allocate more but still fail if too big
		r = n();
		r.incrementCounter();
		startMax = r.getMaxColumns();
		try {
			r.addValue(2*startMax,8);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// try general case (column likely null but autorepaired)
		r = n();
		r.incrementCounter();
		r.addValue(44,8);
		assertEquals(8,r.getValue(44,0),Assert.DOUBLE_TOL);
		
		// try general case in middle of table (column likely null but autorepaired)
		r = n();
		r.incrementCounter();
		r.incrementCounter();
		r.incrementCounter();
		r.addValue(13,876);
		assertEquals(876,r.getValue(13,2),Assert.DOUBLE_TOL);
	}

	@Test
	public void testAddValueStringDouble() {

		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// try a null insertion
			r = n();
			r.incrementCounter();
			r.addValue(null,106);
			assertEquals(106,r.getValue(null, 0),Assert.DOUBLE_TOL);
		}
		
		// try a empty insertion
		r = n();
		r.incrementCounter();
		r.addValue("",106);
		assertEquals(106,r.getValue("", 0),Assert.DOUBLE_TOL);
		
		// try basic insertions into unknown column
		r = n();
		r.incrementCounter();
		r.addValue("Arigula",106);
		assertEquals(106,r.getValue("Arigula", 0),Assert.DOUBLE_TOL);
		
		// now insert a known column in same table
		r.incrementCounter();
		r.addValue("Arigula",19);
		assertEquals(106,r.getValue("Arigula", 0),Assert.DOUBLE_TOL);
		assertEquals(19,r.getValue("Arigula", 1),Assert.DOUBLE_TOL);
	}

	@Test
	public void testAddLabelString() {
		// try null case
		r = n();
		r.incrementCounter();
		r.addLabel(null);
		assertEquals(null,r.getLabel(0));

		// try empty case
		r = n();
		r.incrementCounter();
		r.addLabel("");
		assertEquals("",r.getLabel(0));

		// try random case
		r = n();
		r.incrementCounter();
		r.addLabel("Oops");
		assertEquals("Oops",r.getLabel(0));
	}

	@Test
	public void testAddLabelStringString() {
		
		// try with empty table
		try {
			r = n();
			r.addLabel("Plug","Bug");
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// try null,null
		r = n();
		r.incrementCounter();
		r.addLabel(null,null);
		assertEquals(null,r.getLabel(0));
		
		// try null,empty
		r = n();
		r.incrementCounter();
		r.addLabel(null,"");
		assertEquals("",r.getLabel(0));
		
		// try null,valid
		r = n();
		r.incrementCounter();
		r.addLabel(null,"Zoopies");
		assertEquals("Zoopies",r.getLabel(0));

		// try empty,null
		r = n();
		r.incrementCounter();
		r.addLabel("",null);
		assertEquals(null,r.getLabel(0));

		// try empty,empty
		r = n();
		r.incrementCounter();
		r.addLabel("","");
		assertEquals("",r.getLabel(0));

		// try empty,valid
		r = n();
		r.incrementCounter();
		r.addLabel("","Zoopies");
		assertEquals("Zoopies",r.getLabel(0));

		// try valid,null
		r = n();
		r.incrementCounter();
		r.addLabel("Fred",null);
		assertEquals(null,r.getLabel(0));

		// try valid,empty
		r = n();
		r.incrementCounter();
		r.addLabel("Fred","");
		assertEquals("",r.getLabel(0));

		// try valid,valid
		r = n();
		r.incrementCounter();
		r.addLabel("Fred","Zoopies");
		assertEquals("Zoopies",r.getLabel(0));
	}

	@Test
	public void testSetLabel() {
		// try row too small
		try {
			r = n();
			r.setLabel("RowTooLittle",-1);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// try row too big
		try {
			r = n();
			r.setLabel("RowTooBig",0);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// try row just right
		r = n();
		r.incrementCounter();
		r.setLabel("RowJustRight",0);
		assertEquals("RowJustRight",r.getLabel(0));
		
		// try multiple sets and gets
		r = n();
		r.incrementCounter();
		r.incrementCounter();
		r.incrementCounter();
		r.setLabel("First",0);
		r.setLabel("Second",1);
		r.setLabel("Third",2);
		assertEquals("First",r.getLabel(0));
		assertEquals("Second",r.getLabel(1));
		assertEquals("Third",r.getLabel(2));
		r.setLabel("33rd",1);
		assertEquals("First",r.getLabel(0));
		assertEquals("33rd",r.getLabel(1));
		assertEquals("Third",r.getLabel(2));
	}

	@Test
	public void testDisableRowLabels() {
		r = n();
		r.incrementCounter();
		assertNull(r.getRowLabels());
		r.setLabel("Foofang",0);
		assertNotNull(r.getRowLabels());
		r.disableRowLabels();
		assertNull(r.getRowLabels());
	}

	@Test
	public void testGetColumn() {
		float[] results;
		
		// try on a table with no rows
		
		r = n();
		
		try {
			results = r.getColumn(-1);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		results = r.getColumn(149);
		assertEquals(null,results);

		try {
			results = r.getColumn(1000);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// try on a table with one row but no data
		
		r = n();
		r.incrementCounter();
		
		try {
			results = r.getColumn(-1);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		results = r.getColumn(149);
		assertEquals(null,results);

		try {
			results = r.getColumn(1000);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// try on a table of one row with data
		r = n();
		r.incrementCounter();
		for (int i = 0; i < 150; i++)
			r.setValue(i,0,i);

		results = r.getColumn(149);
		Assert.assertFloatArraysEqual(new float[]{149},results);

		// try on a table of 3 rows with data
		r = n();
		r.incrementCounter();
		r.incrementCounter();
		r.incrementCounter();
		for (int i = 0; i < 150; i++)
		{
			r.setValue(i,0,i);
			r.setValue(i,1,i+400);
			r.setValue(i,2,i+800);
		}
		
		results = r.getColumn(44);
		Assert.assertFloatArraysEqual(new float[]{44,444,844},results);
}

	@Test
	public void testGetColumnAsDoubles() {
		double[] results;
		
		// try on a table with no rows
		
		r = n();
		
		try {
			results = r.getColumnAsDoubles(-1);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		results = r.getColumnAsDoubles(149);
		assertEquals(null,results);

		try {
			results = r.getColumnAsDoubles(1000);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// try on a table with one row but no data
		
		r = n();
		r.incrementCounter();
		
		try {
			results = r.getColumnAsDoubles(-1);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		results = r.getColumnAsDoubles(149);
		assertEquals(null,results);

		try {
			results = r.getColumnAsDoubles(1000);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// try on a table of one row with data
		r = n();
		r.incrementCounter();
		for (int i = 0; i < 150; i++)
			r.setValue(i,0,i);

		results = r.getColumnAsDoubles(149);
		Assert.assertDoubleArraysEqual(new double[]{149},results);

		// try on a table of 3 rows with data
		r = n();
		r.incrementCounter();
		r.incrementCounter();
		r.incrementCounter();
		for (int i = 0; i < 150; i++)
		{
			r.setValue(i,0,i);
			r.setValue(i,1,i+400);
			r.setValue(i,2,i+800);
		}
		
		results = r.getColumnAsDoubles(44);
		Assert.assertDoubleArraysEqual(new double[]{44,444,844},results);
	}

	@Test
	public void testColumnExists() {
		// test initial table
		r = n();
		assertFalse(r.columnExists(-1));
		assertFalse(r.columnExists(0));
		assertFalse(r.columnExists(1));
		assertFalse(r.columnExists(14));
		assertFalse(r.columnExists(1000));
		
		// test table with some data in it
		r = n();
		r.incrementCounter();
		r.setValue(14,0,88.7);
		assertFalse(r.columnExists(-1));
		assertFalse(r.columnExists(0));
		assertFalse(r.columnExists(1));
		assertTrue(r.columnExists(14));
		assertFalse(r.columnExists(1000));
	}

	@Test
	public void testGetColumnIndex() {
		// test initial table
		r = n();
		assertEquals(ResultsTable.COLUMN_NOT_FOUND,r.getColumnIndex(null));
		assertEquals(ResultsTable.COLUMN_NOT_FOUND,r.getColumnIndex(""));
		assertEquals(ResultsTable.COLUMN_NOT_FOUND,r.getColumnIndex("TheZeppo"));
		
		// test when a heading is set but not in order
		r = n();
		r.setHeading(13, "TheZeppo");
		assertEquals(ResultsTable.COLUMN_NOT_FOUND,r.getColumnIndex(null));
		assertEquals(ResultsTable.COLUMN_NOT_FOUND,r.getColumnIndex(""));
		// it seems that the headings before 13 must all be non-null for the method to return a good index
		assertEquals(ResultsTable.COLUMN_NOT_FOUND,r.getColumnIndex("TheZeppo"));

		// test when a heading is set and its the first one
		r = n();
		r.setHeading(0, "TheZeppo");
		assertEquals(ResultsTable.COLUMN_NOT_FOUND,r.getColumnIndex(null));
		assertEquals(ResultsTable.COLUMN_NOT_FOUND,r.getColumnIndex(""));
		assertEquals(0,r.getColumnIndex("TheZeppo"));

		// test when a heading is set and its an interior one
		r = n();
		r.setHeading(0, "Buffy");
		r.setHeading(1, "TheZeppo");
		r.setHeading(2, "Willow");
		assertEquals(ResultsTable.COLUMN_NOT_FOUND,r.getColumnIndex(null));
		assertEquals(ResultsTable.COLUMN_NOT_FOUND,r.getColumnIndex(""));
		assertEquals(1,r.getColumnIndex("TheZeppo"));

		// test when a heading is set and its the last one
		r = n();
		r.setHeading(0, "Buffy");
		r.setHeading(1, "Willow");
		r.setHeading(2, "TheZeppo");
		assertEquals(ResultsTable.COLUMN_NOT_FOUND,r.getColumnIndex(null));
		assertEquals(ResultsTable.COLUMN_NOT_FOUND,r.getColumnIndex(""));
		assertEquals(2,r.getColumnIndex("TheZeppo"));
	}

	/*
       Sets the heading of the the first available column and
		returns that column's index. Returns COLUMN_IN_USE
		 if this is a duplicate heading.
	public int getFreeColumn(String heading) {
		for(int i=0; i<headings.length; i++) {
			if (headings[i]==null) {
				columns[i] = new double[maxRows];
				headings[i] = heading;
				if (i>lastColumn) lastColumn = i;
				return i;
			}
			if (headings[i].equals(heading))
				return COLUMN_IN_USE;
		}
		addColumns();
		lastColumn++;
		columns[lastColumn] = new double[maxRows];
		headings[lastColumn] = heading;
		return lastColumn;
	}

	 */
	
	@Test
	public void testGetFreeColumn() {
		// TODO - this test shows that getFreeColumn could be broken
		r = n();
		//r.setHeading(0, null);
		r.setHeading(1,"Fred");
		assertEquals(0,r.getFreeColumn("Fred"));
		assertEquals("Fred",r.getColumnHeading(0));
		assertEquals("Fred",r.getColumnHeading(1));
	}

	@Test
	public void testGetValueAsDouble() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetValueIntInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetValueStringInt() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetLabel() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetValueStringIntDouble() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetValueIntIntDouble() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetColumnHeadings() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetColumnHeading() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetRowAsString() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetHeading() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetDefaultHeadings() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetPrecision() {
		fail("Not yet implemented");
	}

	@Test
	public void testD2s() {
		fail("Not yet implemented");
	}

	@Test
	public void testDeleteRow() {
		fail("Not yet implemented");
	}

	@Test
	public void testReset() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetLastColumn() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddResults() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdateResults() {
		fail("Not yet implemented");
	}

	@Test
	public void testShow() {
		fail("Not yet implemented");
	}

	@Test
	public void testUpdate() {
		fail("Not yet implemented");
	}

	@Test
	public void testOpen() {
		fail("Not yet implemented");
	}

	@Test
	public void testSaveAs() {
		fail("Not yet implemented");
	}

	@Test
	public void testClone() {
		fail("Not yet implemented");
	}

	@Test
	public void testToString() {
		fail("Not yet implemented");
	}

}
