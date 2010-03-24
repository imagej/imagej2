package ij.measure;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.*;
import java.util.*;
import java.awt.*;

import ij.IJInfo;
import ij.io.Assert;
import ij.plugin.filter.Analyzer;
import ij.process.*;
import ij.gui.Roi;
import ij.*;
import ij.measure.*;

public class ResultsTableTest {
	
	ResultsTable r;

	// this method used below to simplify methods
	
	private ResultsTable n() { return new ResultsTable(); }
	
	@Test
	public void testConstants() {
		
		// TODO - maybe obsolete - delete later if not needed
		assertEquals(150,ResultsTable.MAX_COLUMNS);

		assertEquals(-1,ResultsTable.COLUMN_NOT_FOUND);
		assertEquals(-2,ResultsTable.COLUMN_IN_USE);
		// TODO - maybe obsolete - delete later if not needed
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
			assertEquals(i,r.getValueAsDouble(i, 0),Assert.DOUBLE_TOL);
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
			assertEquals(i,r.getValueAsDouble(i,0),Assert.DOUBLE_TOL);
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
		assertEquals(8,r.getValueAsDouble(44,0),Assert.DOUBLE_TOL);
		
		// try general case in middle of table (column likely null but autorepaired)
		r = n();
		r.incrementCounter();
		r.incrementCounter();
		r.incrementCounter();
		r.addValue(13,876);
		assertEquals(876,r.getValueAsDouble(13,2),Assert.DOUBLE_TOL);
	}

	@Test
	public void testAddValueStringDouble() {

		// try a null insertion
		r = n();
		r.incrementCounter();
		try {
			r.addValue(null,106);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
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
		r = n();
		try {
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
		r = n();
		try {
			r.setLabel("RowTooLittle",-1);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// try row too big
		r = n();
		try {
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
		Assert.assertFloatArraysEqual(new float[]{149},results,Assert.FLOAT_TOL);

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
		Assert.assertFloatArraysEqual(new float[]{44,444,844},results,Assert.FLOAT_TOL);
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
		Assert.assertDoubleArraysEqual(new double[]{149},results,Assert.FLOAT_TOL);

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
		Assert.assertDoubleArraysEqual(new double[]{44,444,844},results,Assert.FLOAT_TOL);
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
		
		// test when a heading is set and its the first one
		r = n();
		r.incrementCounter();
		r.addValue("TheZeppo",42);
		assertEquals(ResultsTable.COLUMN_NOT_FOUND,r.getColumnIndex(null));
		assertEquals(ResultsTable.COLUMN_NOT_FOUND,r.getColumnIndex(""));
		assertEquals(0,r.getColumnIndex("TheZeppo"));

		// test when a heading is set and its an interior one
		r = n();
		r.incrementCounter();
		r.addValue("Buffy",42);
		r.addValue("TheZeppo",42);
		r.addValue("Willow",42);
		assertEquals(ResultsTable.COLUMN_NOT_FOUND,r.getColumnIndex(null));
		assertEquals(ResultsTable.COLUMN_NOT_FOUND,r.getColumnIndex(""));
		assertEquals(1,r.getColumnIndex("TheZeppo"));

		// test when a heading is set and its the last one
		r = n();
		r.incrementCounter();
		r.addValue("Buffy",42);
		r.addValue("Willow",42);
		r.addValue("TheZeppo",42);
		assertEquals(ResultsTable.COLUMN_NOT_FOUND,r.getColumnIndex(null));
		assertEquals(ResultsTable.COLUMN_NOT_FOUND,r.getColumnIndex(""));
		assertEquals(2,r.getColumnIndex("TheZeppo"));
	}

	@Test
	public void testGetFreeColumn() {
		
		// we find a free column in allocated space and we allocate its data
		// test null case
		r = n();
		r.incrementCounter();
		assertEquals(0,r.getFreeColumn(null));
		//assertEquals(0.0,r.getValue(null,0),Assert.DOUBLE_TOL);
		
		// we find a free column in allocated space and we allocate its data
		// test empty case
		r = n();
		r.incrementCounter();
		assertEquals(0,r.getFreeColumn(""));
		assertEquals(0.0,r.getValue("",0),Assert.DOUBLE_TOL);
		
		// we find a free column in allocated space and we allocate its data
		// test a general case - one entry in list
		r = n();
		r.incrementCounter();
		assertEquals(0,r.getFreeColumn("Flubb"));
		assertEquals(0.0,r.getValue("Flubb",0),Assert.DOUBLE_TOL);
		
		// we find a column with that name already exists
		// try to get same col name twice
		r = n();
		r.incrementCounter();
		assertEquals(0,r.getFreeColumn("Flubb"));
		assertEquals(0.0,r.getValue("Flubb",0),Assert.DOUBLE_TOL);
		assertEquals(ResultsTable.COLUMN_IN_USE,r.getFreeColumn("Flubb"));
		
		// we find a free column in allocated space and we allocate its data
		// try to get a second column
		r = n();
		r.incrementCounter();
		assertEquals(0,r.getFreeColumn("Flubb"));
		assertEquals(0.0,r.getValue("Flubb",0),Assert.DOUBLE_TOL);
		assertEquals(1,r.getFreeColumn("Grubb"));
		assertEquals(0.0,r.getValue("Grubb",0),Assert.DOUBLE_TOL);
		
		// we find a free column in allocated space and we allocate its data
		// try to get a second column after a failure
		r = n();
		r.incrementCounter();
		assertEquals(0,r.getFreeColumn("Flubb"));
		assertEquals(0.0,r.getValue("Flubb",0),Assert.DOUBLE_TOL);
		assertEquals(ResultsTable.COLUMN_IN_USE,r.getFreeColumn("Flubb"));
		assertEquals(1,r.getFreeColumn("Grubb"));
		assertEquals(0.0,r.getValue("Grubb",0),Assert.DOUBLE_TOL);

		// otherwise we allocate some new columns and assign the new column and allocate its data
		int spaceToAllocate;
		r = n();
		r.incrementCounter();
		spaceToAllocate = r.getMaxColumns();
		for (int i = 0; i < spaceToAllocate; i++)
		{
			r.getFreeColumn(""+i);
			assertEquals(0.0,r.getValue(""+i,0),Assert.DOUBLE_TOL);
		}
		assertEquals(spaceToAllocate,r.getMaxColumns());
		assertEquals(spaceToAllocate,r.getFreeColumn("Bigelow"));
		assertFalse(spaceToAllocate == r.getMaxColumns());
		assertEquals(0.0,r.getValue("Bigelow",0),Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetValueAsDouble() {
		int maxAllocated;
		
		// untested now as broken
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// try to access a value with row too little
			r = n();
			r.incrementCounter();
			r.setValue("Porchlight", 0, 63.3);
			try {
				r.getValueAsDouble(0, -1);
				fail();
			} catch (IllegalArgumentException e) {
				assertTrue(true);
			}
		}

		// untested now as broken
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// try to access a value with column too little
			r = n();
			r.incrementCounter();
			r.setValue("Porchlight", 0, 63.3);
			try {
				r.getValueAsDouble(-1, 0);
				fail();
			} catch (IllegalArgumentException e) {
				assertTrue(true);
			}
		}
		
		// col too big
		r = n();
		r.incrementCounter();
		r.setValue("Porchlight", 0, 63.3);
		maxAllocated = r.getMaxColumns();
		try {
			r.getValueAsDouble(maxAllocated+1, 0);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// row too big
		r = n();
		r.incrementCounter();
		r.setValue("Porchlight", 0, 63.3);
		try {
			r.getValueAsDouble(0, 1);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// col not allocated
		r = n();
		r.incrementCounter();
		try {
			r.getValueAsDouble(0, 0);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// otherwise return value
		r = n();
		r.incrementCounter();
		r.setValue("Porchlight", 0, 63.3);
		assertEquals(63.3,r.getValueAsDouble(0, 0),Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetValueStringInt() {
		// row < 0
		r = n();
		try {
			r.getValue("Soopupple", -1);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// row >= curr counter
		r = n();
		r.incrementCounter();
		try {
			r.getValue("Soopupple", 1);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// column not found
		r = n();
		r.incrementCounter();
		r.addValue("Freeptoo", 66.8);
		try {
			r.getValue("Soopupple", 0);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// otherwise should be safe
		
		r = n();
		r.incrementCounter();
		r.addValue("ScoobySnacks",177);
		r.incrementCounter();
		r.addValue("SnickerDoodles",144);
		assertEquals(177,r.getValue("ScoobySnacks", 0),Assert.DOUBLE_TOL);
		assertEquals(144,r.getValue("SnickerDoodles", 1),Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetLabel() {
		// row < 0
		r = n();
		try {
			r.getLabel(-1);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// row >= curr counter
		r = n();
		r.incrementCounter();
		try {
			r.getLabel(1);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// row labels == null -- get back null
		r = n();
		r.incrementCounter();
		assertNull(r.getRowLabels());
		assertNull(r.getLabel(0));
		
		// rowLabels[row] == null -- get back null
		r = n();
		r.incrementCounter();
		r.addLabel("Florp");
		r.incrementCounter();
		r.addLabel("Porp");
		r.incrementCounter();
		assertNotNull(r.getRowLabels());
		assertNull(r.getLabel(2));

		// else get back label that was put in
		r = n();
		r.incrementCounter();
		r.addLabel("Florp");
		r.incrementCounter();
		r.addLabel("Porp");
		assertEquals("Florp",r.getLabel(0));
		assertEquals("Porp",r.getLabel(1));
	}

	@Test
	public void testSetValueStringIntDouble() {
		// null input
		r = n();
		r.incrementCounter();
		try {
			r.setValue(null,0,123);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}

		// empty input
		r = n();
		r.incrementCounter();
		r.setValue("",0,123);
		assertEquals(123,r.getValue("", 0),Assert.DOUBLE_TOL);
		
		// valid input
		r = n();
		r.incrementCounter();
		r.setValue("Sweep",0,123);
		assertEquals(123,r.getValue("Sweep", 0),Assert.DOUBLE_TOL);
	}

	@Test
	public void testSetValueIntIntDouble() {
		int maxCols;
		
		// column < 0
		r = n();
		r.incrementCounter();
		try {
			r.setValue(-1,0,106.4);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		if (IJInfo.RUN_ENHANCED_TESTS)
		{
			// row < 0
			r = n();
			r.incrementCounter();
			r.setValue(0,-1,106.4);
		}
		
		// column < maxColumns
		r = n();
		r.incrementCounter();
		maxCols = r.getMaxColumns();
		for (int i = 0; i < maxCols; i++)
			r.setValue(""+i,0,i);
		r.setValue(15,0,173.3);
		assertEquals(173.3,r.getValueAsDouble(15,0),Assert.DOUBLE_TOL);
		
		// maxColumns <= column < 2*maxColumns
		r = n();
		r.incrementCounter();
		maxCols = r.getMaxColumns();
		for (int i = 0; i < maxCols; i++)
			r.setValue(""+i,0,i);
		r.setValue(maxCols+1,0,19.7);
		assertEquals(19.7,r.getValueAsDouble(maxCols+1,0),Assert.DOUBLE_TOL);
		
		// column >= 2*maxColumns
		r = n();
		r.incrementCounter();
		maxCols = r.getMaxColumns();
		try {
			r.setValue(2*maxCols,0,19.7);
			fail();
		} catch(IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// row too big
		r = n();
		r.incrementCounter();
		try {
			r.setValue(0,1,19.7);
			fail();
		} catch(IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// if column is null allocate space for it
		r = n();
		r.incrementCounter();
		assertNull(r.getColumn(0));
		r.setValue(0, 0, 44.4);
		assertNotNull(r.getColumn(0));
		assertEquals(44.4,r.getValueAsDouble(0, 0),Assert.DOUBLE_TOL);
		
		// default : set value
		r = n();
		r.incrementCounter();
		r.setValue(0, 0, 93.5);
		assertEquals(93.5,r.getValueAsDouble(0, 0),Assert.DOUBLE_TOL);
		r.setValue(1, 0, 88.1);
		assertEquals(88.1,r.getValueAsDouble(1, 0),Assert.DOUBLE_TOL);
		r.setValue(2, 0, 77.3);
		assertEquals(77.3,r.getValueAsDouble(2, 0),Assert.DOUBLE_TOL);
	}

	@Test
	public void testGetColumnHeadings() {
		
		// null rowLabels, null headings
		r = n();
		r.incrementCounter();
		r.addValue(0, 1);
		r.addValue(1, 2);
		r.addValue(2, 3);
		r.addValue(3, 4);
		assertNull(r.getRowLabels());
		assertEquals(" \t---\t---\t---\t---",r.getColumnHeadings());
		
		// null rowLabels, valid headings
		r = n();
		r.incrementCounter();
		r.addValue("Fred", 1);
		r.addValue("Jim", 2);
		r.addValue("Bob", 3);
		r.addValue("Ray", 4);
		assertNull(r.getRowLabels());
		assertEquals(" \tFred\tJim\tBob\tRay",r.getColumnHeadings());

		// valid rowLabels, null headings
		r = n();
		r.incrementCounter();
		r.addValue(0, 1);
		r.addValue(1, 2);
		r.addValue(2, 3);
		r.addValue(3, 4);
		r.setLabel("XXXX", 0);
		assertNotNull(r.getRowLabels());
		assertEquals(" \tLabel\t---\t---\t---\t---",r.getColumnHeadings());

		// valid rowLabels, valid headings
		r = n();
		r.incrementCounter();
		r.addValue("Fred", 1);
		r.addValue("Jim", 2);
		r.addValue("Bob", 3);
		r.addValue("Ray", 4);
		r.setLabel("XXXX", 0);
		assertNotNull(r.getRowLabels());
		assertEquals(" \tLabel\tFred\tJim\tBob\tRay",r.getColumnHeadings());
	}

	@Test
	public void testGetColumnHeading() {
		int max;
		r = n();
		r.incrementCounter();
		max = r.getMaxColumns();
		
		// test -1
		try {
			r.getColumnHeading(-1);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		// test max
		try {
			r.getColumnHeading(max);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// test 0..max-1
		for (int i = 0; i < max; i++)
			r.setValue(""+i,0,55);
		assertEquals(max,r.getMaxColumns());
		for (int i = 0; i < max; i++)
			assertEquals(""+i,r.getColumnHeading(i));
	}

	@Test
	public void testGetRowAsString() {
		
		// try row index < 0
		r = n();
		r.incrementCounter();
		try {
			r.getRowAsString(-1);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// try row index >= numRows
		r = n();
		r.incrementCounter();
		try {
			r.getRowAsString(1);
			fail();
		} catch (IllegalArgumentException e) {
			assertTrue(true);
		}
		
		// else try valid entries of row index
		//   rowLabels null
		r = n();
		r.incrementCounter();
		r.setValue("Hoopta",0,1.1);
		r.setValue("Doopta",0,2.22);
		r.setValue("Loopta",0,3.333);
		r.setValue("Zoopta",0,4.4444);
		assertEquals("1\t1.100\t2.220\t3.333\t4.444",r.getRowAsString(0));
		
		// else try valid entries of row index
		//   rowLabels not null
		r = n();
		r.incrementCounter();
		r.setLabel("XXXX", 0);
		r.setValue("Hoopta",0,1.1);
		r.setValue("Doopta",0,2.22);
		r.setValue("Loopta",0,3.333);
		r.setValue("Zoopta",0,4.4444);
		assertEquals("1\tXXXX\t1.100\t2.220\t3.333\t4.444",r.getRowAsString(0));
	}

	@Test
	public void testSetDefaultHeadings() {
		r = n();
		for (int i = 0; i < r.getMaxColumns(); i++)
			r.getFreeColumn(""+i);
		r.setDefaultHeadings();
		assertEquals("Area",r.getColumnHeading(0));
		assertEquals("Mean",r.getColumnHeading(1));
		assertEquals("StdDev",r.getColumnHeading(2));
		assertEquals("Mode",r.getColumnHeading(3));
		assertEquals("Min",r.getColumnHeading(4));
		assertEquals("Max",r.getColumnHeading(5));
		assertEquals("X",r.getColumnHeading(6));
		assertEquals("Y",r.getColumnHeading(7));
		assertEquals("XM",r.getColumnHeading(8));
		assertEquals("YM",r.getColumnHeading(9));
		assertEquals("Perim.",r.getColumnHeading(10));
		assertEquals("BX",r.getColumnHeading(11));
		assertEquals("BY",r.getColumnHeading(12));
		assertEquals("Width",r.getColumnHeading(13));
		assertEquals("Height",r.getColumnHeading(14));
		assertEquals("Major",r.getColumnHeading(15));
		assertEquals("Minor",r.getColumnHeading(16));
		assertEquals("Angle",r.getColumnHeading(17));
		assertEquals("Circ.",r.getColumnHeading(18));
		assertEquals("Feret",r.getColumnHeading(19));
		assertEquals("IntDen",r.getColumnHeading(20));
		assertEquals("Median",r.getColumnHeading(21));
		assertEquals("Skew",r.getColumnHeading(22));
		assertEquals("Kurt",r.getColumnHeading(23));
		assertEquals("%Area",r.getColumnHeading(24));
		assertEquals("Ch",r.getColumnHeading(25));
		assertEquals("Slice",r.getColumnHeading(26));
		assertEquals("Frame",r.getColumnHeading(27));
		assertEquals("FeretX",r.getColumnHeading(28));
		assertEquals("FeretY",r.getColumnHeading(29));
		assertEquals("FeretAngle",r.getColumnHeading(30));
		assertEquals("MinFeret",r.getColumnHeading(31));
		assertEquals("AR",r.getColumnHeading(32));
		assertEquals("Round",r.getColumnHeading(33));
		assertEquals("Solidity",r.getColumnHeading(34));
		assertEquals("35",r.getColumnHeading(35));
	}

	@Test
	public void testSetPrecision() {
		r = n();
		r.incrementCounter();
		r.setValue("Fred", 0, 9.9999999999);

		// try crazy val : -1
		r.setPrecision(-1);
		assertEquals("1\t1.0E1",r.getRowAsString(0));
		
		// try crazy val : big num
		r.setPrecision(808);
		assertEquals("1\t10.000000000",r.getRowAsString(0));

		// try reasonable values
		r.setPrecision(0);
		assertEquals("1\t10",r.getRowAsString(0));
		r.setPrecision(1);
		assertEquals("1\t10.0",r.getRowAsString(0));
		r.setPrecision(2);
		assertEquals("1\t10.00",r.getRowAsString(0));
		r.setPrecision(3);
		assertEquals("1\t10.000",r.getRowAsString(0));
		r.setPrecision(4);
		assertEquals("1\t10.0000",r.getRowAsString(0));
		r.setPrecision(5);
		assertEquals("1\t10.00000",r.getRowAsString(0));
		r.setPrecision(6);
		assertEquals("1\t10.000000",r.getRowAsString(0));
		r.setPrecision(7);
		assertEquals("1\t10.0000000",r.getRowAsString(0));
		r.setPrecision(8);
		assertEquals("1\t10.00000000",r.getRowAsString(0));
	}

	@Test
	public void testD2s() {
		// try the edge cases
		assertEquals("NaN",ResultsTable.d2s(Double.NaN, 0));
		assertEquals("-Infinity",ResultsTable.d2s(Double.NEGATIVE_INFINITY, 0));
		assertEquals("-Infinity",ResultsTable.d2s(Float.NEGATIVE_INFINITY, 0));
		assertEquals("Infinity",ResultsTable.d2s(Double.POSITIVE_INFINITY, 0));
		assertEquals("Infinity",ResultsTable.d2s(Float.POSITIVE_INFINITY, 0));
		assertEquals("3.4e38",ResultsTable.d2s(Float.MAX_VALUE, 5));
		assertEquals("1.798E308",ResultsTable.d2s(Double.MAX_VALUE, 5));
		assertEquals("1.401E-45",ResultsTable.d2s(Float.MIN_VALUE, 5));
		assertEquals("4.900E-324",ResultsTable.d2s(Double.MIN_VALUE, 5));
		
		// try negative numbers
		assertEquals("-1.100",ResultsTable.d2s(-1.1, 3));
		assertEquals("-25.4500",ResultsTable.d2s(-25.45, 4));
		assertEquals("-341.65400",ResultsTable.d2s(-341.654, 5));
		assertEquals("-4502.379900",ResultsTable.d2s(-4502.3799, 6));
		
		// try positive numbers
		assertEquals("1",ResultsTable.d2s(1.4, 0));
		assertEquals("1.6",ResultsTable.d2s(1.6, 1));
		assertEquals("25.44",ResultsTable.d2s(25.44, 2));
		assertEquals("25.7",ResultsTable.d2s(25.66, 1));
		assertEquals("188.333",ResultsTable.d2s(188.333, 3));
		assertEquals("188.6660",ResultsTable.d2s(188.666, 4));
		
		// weird decimal places
		assertEquals("4.50000E0",ResultsTable.d2s(4.5,-5));
		assertEquals("4.5E0",ResultsTable.d2s(4.5,-1));
		assertEquals("4",ResultsTable.d2s(4.5,0));
		assertEquals("4.500000000",ResultsTable.d2s(4.5,9));
		assertEquals("4.500000000",ResultsTable.d2s(4.5,10));
		assertEquals("4.500000000",ResultsTable.d2s(4.5,1000));
		
		// try the special cases the routine tests for
		assertEquals("0.0001000",ResultsTable.d2s(0.0001,7));
		assertEquals("1.000E-4",ResultsTable.d2s(0.0001,3));
		assertEquals("1.000E13",ResultsTable.d2s(9999999999991d,3));
		}

	@Test
	public void testDeleteRow() {

		// delete the -1th row
		r = n();
		r.incrementCounter();
		r.deleteRow(-1);
		assertEquals(1,r.getCounter());
		
		// no rows to delete
		r = n();
		assertEquals(0,r.getCounter());
		r.deleteRow(0);
		assertEquals(0,r.getCounter());
		
		// trying to delete beyond end
		r = n();
		r.incrementCounter();
		assertEquals(1,r.getCounter());
		r.deleteRow(1);
		assertEquals(1,r.getCounter());
		
		// if rowLabels not null - make sure they are rearranged correctly
		// also test that all row data is moved up in the columns
		
		// delete row at beginning of list
		r = n();
		r.incrementCounter();
		r.incrementCounter();
		r.incrementCounter();
		r.setValue("Zoops", 0, 100);
		r.setValue("Zoops", 1, 200);
		r.setValue("Zoops", 2, 300);
		r.setLabel("X", 0);
		r.setLabel("Y", 1);
		r.setLabel("Z", 2);
		r.deleteRow(0);
		assertEquals("Y",r.getLabel(0));
		assertEquals("Z",r.getLabel(1));
		assertEquals(200,r.getValue("Zoops", 0),Assert.DOUBLE_TOL);
		assertEquals(300,r.getValue("Zoops", 1),Assert.DOUBLE_TOL);
		assertEquals(2,r.getCounter());
		
		// delete row in middle of list
		r = n();
		r.incrementCounter();
		r.incrementCounter();
		r.incrementCounter();
		r.setValue("Zoops", 0, 100);
		r.setValue("Zoops", 1, 200);
		r.setValue("Zoops", 2, 300);
		r.setLabel("X", 0);
		r.setLabel("Y", 1);
		r.setLabel("Z", 2);
		r.deleteRow(1);
		assertEquals("X",r.getLabel(0));
		assertEquals("Z",r.getLabel(1));
		assertEquals(100,r.getValue("Zoops", 0),Assert.DOUBLE_TOL);
		assertEquals(300,r.getValue("Zoops", 1),Assert.DOUBLE_TOL);
		assertEquals(2,r.getCounter());
		
		// delete row at end of list
		r = n();
		r.incrementCounter();
		r.incrementCounter();
		r.incrementCounter();
		r.setValue("Zoops", 0, 100);
		r.setValue("Zoops", 1, 200);
		r.setValue("Zoops", 2, 300);
		r.setLabel("X", 0);
		r.setLabel("Y", 1);
		r.setLabel("Z", 2);
		r.deleteRow(2);
		assertEquals("X",r.getLabel(0));
		assertEquals("Y",r.getLabel(1));
		assertEquals(100,r.getValue("Zoops", 0),Assert.DOUBLE_TOL);
		assertEquals(200,r.getValue("Zoops", 1),Assert.DOUBLE_TOL);
		assertEquals(2,r.getCounter());
	}

	@Test
	public void testReset() {
		// populate a table as much as possible
		r = n();
		for (int i = 0; i < 1000; i++)
		{
			r.incrementCounter();
			r.setLabel(""+i,i);
			for (int j = 0; j < 500; j++)
				r.setValue(""+j,i,i+j);
		}
		// run reset
		r.reset();
		// see that all the values have changed
		assertEquals(0,r.getCounter());
		assertEquals(-1,r.getLastColumn());
		assertNull(r.getRowLabels());
		for (int j = 0; j < 500; j++)
		{
			assertNull(r.getColumnHeading(j));
			assertNull(r.getColumn(j));
		}
	}

	@Test
	public void testGetLastColumn() {
		r = n();
		r.incrementCounter();
		assertEquals(-1,r.getLastColumn());
		r.addValue("Fweek",66);
		assertEquals(0,r.getLastColumn());
	}

	@Test
	public void testAddResults() {
		if (IJInfo.RUN_GUI_TESTS)
		{
			// this method simply updates the UI in some way
		}
	}

	@Test
	public void testUpdateResults() {
		if (IJInfo.RUN_GUI_TESTS)
		{
			// this method simply updates the UI in some way
		}
	}

	@Test
	public void testShow() {
		if (IJInfo.RUN_GUI_TESTS)
		{
			// this method simply updates the UI in some way
		}
	}

	@Test
	public void testUpdate() {
		int measurements =
			Measurements.AREA+Measurements.MEAN+Measurements.STD_DEV+Measurements.MODE+Measurements.MIN_MAX+
			Measurements.CENTROID+Measurements.CENTER_OF_MASS+Measurements.PERIMETER+Measurements.RECT+
			Measurements.ELLIPSE+Measurements.SHAPE_DESCRIPTORS+Measurements.FERET+Measurements.INTEGRATED_DENSITY+
			Measurements.MEDIAN+Measurements.SKEWNESS+Measurements.KURTOSIS+Measurements.AREA_FRACTION;

		ByteProcessor proc = new ByteProcessor(2,3,new byte[] {1,2,3,4,5,6},null);
		ImagePlus ip = new ImagePlus("ZutAlors",proc);
		Roi roi = new Roi(new Rectangle(0,0,1,1));  // should be {1,2,3,4} of {1,2,3,4,5,6}
		r = n();
		r.incrementCounter();
		for (int i = 0; i < 35; i++)
			assertNull(r.getColumnHeading(i));
		
		r.update(measurements, ip, roi);

		assertEquals(34,r.getLastColumn());
		assertNull(r.getLabel(0));
		assertEquals("Area",r.getColumnHeading(0));
		assertEquals("Mean",r.getColumnHeading(1));
		assertEquals("StdDev",r.getColumnHeading(2));
		assertEquals("Mode",r.getColumnHeading(3));
		assertEquals("Min",r.getColumnHeading(4));
		assertEquals("Max",r.getColumnHeading(5));
		assertEquals("X",r.getColumnHeading(6));
		assertEquals("Y",r.getColumnHeading(7));
		assertEquals("XM",r.getColumnHeading(8));
		assertEquals("YM",r.getColumnHeading(9));
		assertEquals("Perim.",r.getColumnHeading(10));
		assertEquals("BX",r.getColumnHeading(11));
		assertEquals("BY",r.getColumnHeading(12));
		assertEquals("Width",r.getColumnHeading(13));
		assertEquals("Height",r.getColumnHeading(14));
		assertEquals("Major",r.getColumnHeading(15));
		assertEquals("Minor",r.getColumnHeading(16));
		assertEquals("Angle",r.getColumnHeading(17));
		assertEquals("Circ.",r.getColumnHeading(18));
		assertEquals("Feret",r.getColumnHeading(19));
		assertEquals("IntDen",r.getColumnHeading(20));
		assertEquals("Median",r.getColumnHeading(21));
		assertEquals("Skew",r.getColumnHeading(22));
		assertEquals("Kurt",r.getColumnHeading(23));
		assertEquals("%Area",r.getColumnHeading(24));
		assertNull(r.getColumnHeading(25));
		assertNull(r.getColumnHeading(26));
		assertNull(r.getColumnHeading(27));
		assertEquals("FeretX",r.getColumnHeading(28));
		assertEquals("FeretY",r.getColumnHeading(29));
		assertEquals("FeretAngle",r.getColumnHeading(30));
		assertEquals("MinFeret",r.getColumnHeading(31));
		assertEquals("AR",r.getColumnHeading(32));
		assertEquals("Round",r.getColumnHeading(33));
		assertEquals("Solidity",r.getColumnHeading(34));
	}

	@Test
	public void testOpen() {
		// unknown file
		try {
			r = ResultsTable.open("data/Nonexistent.txt");
			fail();
		} catch (IOException e) {
			//System.out.println("Exception: "+e.getMessage());
			assertTrue(true);
		}
		
		// empty file
		try {
			r = ResultsTable.open("data/EmptyFile.txt");
			fail();
		} catch (IOException e) {
			//System.out.println("Exception: "+e.getMessage());
			assertTrue(true);
		}

		// not a table
		try {
			r = ResultsTable.open("data/BogusTable.txt");
			fail();
		} catch (IOException e) {
			//System.out.println("Exception: "+e.getMessage());
			assertTrue(true);
		}
		
		// else a real table

		// try a basic table
		try {
			r = ResultsTable.open("data/BasicTable.txt");
			assertEquals(2,r.getCounter());
			assertEquals("Jones",r.getLabel(0));
			assertEquals(3,r.getValueAsDouble(0, 0),Assert.DOUBLE_TOL);
			assertEquals(5,r.getValueAsDouble(1, 0),Assert.DOUBLE_TOL);
			assertEquals(7,r.getValueAsDouble(2, 0),Assert.DOUBLE_TOL);
			assertEquals("Bob",r.getLabel(1));
			assertEquals(1,r.getValueAsDouble(0, 1),Assert.DOUBLE_TOL);
			assertEquals(2,r.getValueAsDouble(1, 1),Assert.DOUBLE_TOL);
			assertEquals(8,r.getValueAsDouble(2, 1),Assert.DOUBLE_TOL);
			assertEquals("Smiles",r.getColumnHeading(0));
			assertEquals("Winks",r.getColumnHeading(1));
			assertEquals("Nods",r.getColumnHeading(2));
		} catch (Exception e) {
			//System.out.println("excep: "+e.getMessage());
			fail();
		}
		// if all headings numeric then cols renamed C1, C2, etc.
		// note - can't test this subcase because if all headings are numeric is true then labels is false
		//   and a lower level routine gets called to load data that is gui dependent and returns null
		/*
		try {
			r = ResultsTable.open("data/AllNumericTable.txt");
			assertEquals("C1",r.getColumnHeading(0));
			assertEquals("C2",r.getColumnHeading(1));
			assertEquals("C3",r.getColumnHeading(2));
		} catch (Exception e) {
			System.out.println("excep: "+e.getMessage());
			fail();
		}
		*/
	}

	@Test
	public void testSaveAs() {
		// note - can only test nonGui portion

		String baseFileName = System.getProperty("java.io.tmpdir") + "IJRTTjunk";
		
		// an empty table
		r = n();
		try {
			r.saveAs(baseFileName+".txt");
			fail();
		} catch (java.io.IOException e) {
			assertTrue(true);
		}
		
		// a valid table
		r = n();
		r.incrementCounter();
		r.setValue("Pigs", 0, 1);
		r.setValue("Chickens", 0, 2);
		r.setValue("Bunnies", 0, 3);

		// save as a txt file (delimited by tabs)
		try {
			r.saveAs(baseFileName+".txt");
		} catch (java.io.IOException e) {
			fail();
		}
		
		// test that it saved correctly
		try {
			FileReader fr = new FileReader(baseFileName+".txt");
			BufferedReader rdr = new BufferedReader(fr);
			ArrayList<String> data = new ArrayList<String>();
			while (rdr.ready())
				data.add(rdr.readLine());
			assertEquals(" \tPigs\tChickens\tBunnies",data.get(0));
			assertEquals("1\t1\t2\t3",data.get(1));
		} catch (Exception e) {
			fail();
		}

		// save as csv file (delimited by commas)
		try {
			r.saveAs(baseFileName+".csv");
		} catch (java.io.IOException e) {
			fail();
		}
		
		// test that it saved correctly
		try {
			FileReader fr = new FileReader(baseFileName+".csv");
			BufferedReader rdr = new BufferedReader(fr);
			ArrayList<String> data = new ArrayList<String>();
			while (rdr.ready())
				data.add(rdr.readLine());
			assertEquals(" ,Pigs,Chickens,Bunnies",data.get(0));
			assertEquals("1,1,2,3",data.get(1));
		} catch (Exception e) {
			fail();
		}
	}

	@Test
	public void testClone() {
		ResultsTable c;
		
		// setup table
		r = n();
		r.incrementCounter();
		r.setValue("Toes",0,77);
		r.setValue("Fingers",0,13);
		r.setLabel("Count", 0);
		
		c = (ResultsTable)r.clone();
		
		// see that all rows copied, all labels, headings, and values the same
		assertNotNull(c);
		assertEquals(r.getCounter(),c.getCounter());
		assertEquals(r.getColumnHeading(0),c.getColumnHeading(0));
		assertEquals(r.getColumnHeading(1),c.getColumnHeading(1));
		assertEquals(r.getLabel(0),c.getLabel(0));
		assertEquals(r.getValue(0, 0),c.getValue(0, 0),Assert.DOUBLE_TOL);
		assertEquals(r.getValue(1, 0),c.getValue(1, 0),Assert.DOUBLE_TOL);
	}

	@Test
	public void testToString() {
		r = n();
		r.incrementCounter();
		r.incrementCounter();
		r.incrementCounter();
		r.setValue("Flower", 0, 22);
		r.setValue("Leaf", 0, 55);
		r.setValue("Stem", 0, 88);
		
		String str = "ctr="+r.getCounter()+", hdr="+r.getColumnHeadings();
		assertEquals(str,r.toString());
	}

}
