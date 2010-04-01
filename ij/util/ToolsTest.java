package ij.util;

import java.awt.Color;

import static org.junit.Assert.*;

import org.junit.Test;


public class ToolsTest {

	@Test
	public void testHexDigitConstants()
	{
		// hexDigits is a public static array of character constants
		//   test that the values don't change between releases
		assertEquals(16,Tools.hexDigits.length);
		assertEquals('0',Tools.hexDigits[0]);
		assertEquals('1',Tools.hexDigits[1]);
		assertEquals('2',Tools.hexDigits[2]);
		assertEquals('3',Tools.hexDigits[3]);
		assertEquals('4',Tools.hexDigits[4]);
		assertEquals('5',Tools.hexDigits[5]);
		assertEquals('6',Tools.hexDigits[6]);
		assertEquals('7',Tools.hexDigits[7]);
		assertEquals('8',Tools.hexDigits[8]);
		assertEquals('9',Tools.hexDigits[9]);
		assertEquals('A',Tools.hexDigits[10]);
		assertEquals('B',Tools.hexDigits[11]);
		assertEquals('C',Tools.hexDigits[12]);
		assertEquals('D',Tools.hexDigits[13]);
		assertEquals('E',Tools.hexDigits[14]);
		assertEquals('F',Tools.hexDigits[15]);
	}
	
	@Test
	public void testC2hex() {
		// c2hex encodes java.awt.Colors into 7 character strings
		assertEquals("#000000",Tools.c2hex(Color.BLACK));     // test 0 channels, known colors
		assertEquals("#FF0000",Tools.c2hex(Color.RED));       // test 1 channel, known colors
		assertEquals("#00FF00",Tools.c2hex(Color.GREEN));
		assertEquals("#0000FF",Tools.c2hex(Color.BLUE));
		assertEquals("#00FFFF",Tools.c2hex(Color.CYAN));      // test 2 channels, known colors
		assertEquals("#FF00FF",Tools.c2hex(Color.MAGENTA));
		assertEquals("#FFFF00",Tools.c2hex(Color.YELLOW));
		assertEquals("#FFFFFF",Tools.c2hex(Color.WHITE));     // test 3 channels, known colors
		assertEquals("#F34A81",Tools.c2hex(new Color(0xf3,0x4a,0x81)));    // test arbitrary values
	}

	@Test
	public void testF2hex() {
		// f2hex encodes floats into 9 character strings
		//   expected values taken from working implementation
		assertEquals("#00000000",Tools.f2hex(0.0f));
		assertEquals("#BF800000",Tools.f2hex(-1.0f));
		assertEquals("#3F800000",Tools.f2hex(1.0f));
		assertEquals("#42C80000",Tools.f2hex(100.0f));
		assertEquals("#49742400",Tools.f2hex(1000000.0f));
		assertEquals("#40490E56",Tools.f2hex(3.1415f));
		assertEquals("#3FD70A3D",Tools.f2hex(1.68f));
		assertEquals("#C5898400",Tools.f2hex(-4400.5f));
		assertEquals("#7F7FFFFF",Tools.f2hex(Float.MAX_VALUE));
		assertEquals("#00000001",Tools.f2hex(Float.MIN_VALUE));
		assertEquals("#7FC00000",Tools.f2hex(Float.NaN));
		assertEquals("#FF800000",Tools.f2hex(Float.NEGATIVE_INFINITY));
		assertEquals("#7F800000",Tools.f2hex(Float.POSITIVE_INFINITY));
	}

	// helper method for the next test below
	//   does all the work with passed parameters
	private void testMinMaxDouble(double eMin, double eMax, double[] input){
		double[] minMax = Tools.getMinMax(input);
		assertTrue(minMax.length == 2);
		assertEquals(eMin,minMax[0],0.00001);
		assertEquals(eMax,minMax[1],0.00001);
	}
	
	@Test
	public void testGetMinMaxDoubleArray() {
		//minMaxDouble(0.0, 0.0, null); -> crashes
		testMinMaxDouble(Double.MAX_VALUE, -Double.MAX_VALUE, new double[0]);
		testMinMaxDouble(0.0, 0.0, new double[] {0.0});
		testMinMaxDouble(0.0, 1.0, new double[] {0.0, 1.0});
		testMinMaxDouble(0.0, 1.0, new double[] {1.0, 0.0});
		testMinMaxDouble(-7.0, -1.0, new double[] {-7.0, -3.0, -1.0});
		testMinMaxDouble(20.0, 100.0, new double[] {100.0, 80.0, 60.0, 40.0, 20.0});
		testMinMaxDouble(Double.MIN_VALUE, Double.MAX_VALUE, new double[] {Double.MAX_VALUE,Double.MIN_VALUE});
		testMinMaxDouble(Double.MAX_VALUE, -Double.MAX_VALUE, new double[] {Double.NaN});
		testMinMaxDouble(1.0, 8.0, new double[] {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0});
		testMinMaxDouble(-454.0, 10000.0, new double[] {42.0, 3.1415, -73.0, 1.68, 10000.0, -454.0});
	}

	// helper method for the next test below
	//   does all the work with passed parameters
	private void testMinMaxDouble(double eMin, double eMax, float[] input){
		double[] minMax = Tools.getMinMax(input);
		assertTrue(minMax.length == 2);
		assertEquals(eMin,minMax[0],0.00001);
		assertEquals(eMax,minMax[1],0.00001);
	}
	
	@Test
	public void testGetMinMaxFloatArray() {
		//minMaxDouble(0.0, 0.0, null); -> crashes
		testMinMaxDouble(Double.MAX_VALUE, -Double.MAX_VALUE, new float[0]);
		testMinMaxDouble(0.0, 0.0, new float[] {0.0f});
		testMinMaxDouble(0.0, 1.0, new float[] {0.0f, 1.0f});
		testMinMaxDouble(0.0, 1.0, new float[] {1.0f, 0.0f});
		testMinMaxDouble(-7.0, -1.0, new float[] {-7.0f, -3.0f, -1.0f});
		testMinMaxDouble(20.0, 100.0, new float[] {100.0f, 80.0f, 60.0f, 40.0f, 20.0f});
		testMinMaxDouble((double)Float.MIN_VALUE, (double)Float.MAX_VALUE, new float[] {Float.MAX_VALUE,Float.MIN_VALUE});
		testMinMaxDouble(Double.MAX_VALUE, -Double.MAX_VALUE, new float[] {Float.NaN});
		testMinMaxDouble(1.0, 8.0, new float[] {1.0f, 2.0f, 3.0f, 4.0f, 5.0f, 6.0f, 7.0f, 8.0f});
		testMinMaxDouble(-454.0, 10000.0, new float[] {42.0f, 3.1415f, -73.0f, 1.68f, 10000.0f, -454.0f});
	}

	private void toDoubleConversion(float[] floats)	{
		assertNotNull(floats);
		double[] doubles = Tools.toDouble(floats);
		assertNotNull(doubles);
		assertTrue(floats.length == doubles.length);
		for (int i = 0; i < floats.length; i++)
			assertEquals((double)floats[i],doubles[i],0.0000001);
	}

	@Test
	public void testToDouble() {
		// test that the routine that converts a float array to a double array works
		toDoubleConversion(new float[] {1.0f});
		toDoubleConversion(new float[] {-3.0f, 0.0f, 3.0f});
		toDoubleConversion(new float[] {-99f, -98f, -106f, 0.0f});
		toDoubleConversion(new float[] {1f, 10f, 100f, 1000f, 10000f, 100000f});
		toDoubleConversion(new float[] {Float.MAX_VALUE});
		toDoubleConversion(new float[] {Float.MIN_VALUE});
		toDoubleConversion(new float[] {Float.NaN});
		toDoubleConversion(new float[] {0.0f});
	}


	private void toFloatConversion(double[] doubles)	{
		assertNotNull(doubles);
		float[] floats = Tools.toFloat(doubles);
		assertNotNull(floats);
		assertTrue(floats.length == doubles.length);
		for (int i = 0; i < doubles.length; i++)
			assertEquals((float)doubles[i],floats[i],0.0000001);
	}

	@Test
	public void testToFloat() {
		// test that the routine that converts a float array to a double array works
		toFloatConversion(new double[] {1.0});
		toFloatConversion(new double[] {-3.0, 0.0, 3.0});
		toFloatConversion(new double[] {-99, -98, -106, 0.0});
		toFloatConversion(new double[] {1, 10, 100, 1000, 10000, 100000});
		toFloatConversion(new double[] {Double.MAX_VALUE});
		toFloatConversion(new double[] {Double.MIN_VALUE});
		toFloatConversion(new double[] {Double.NaN});
		toFloatConversion(new double[] {0.0});
	}

	@Test
	public void testFixNewLines() {
		// assertEquals(null,Tools.fixNewLines(null)); -> runtime error
		assertEquals("",Tools.fixNewLines(""));
		assertEquals("\n",Tools.fixNewLines("\r"));
		assertEquals("a\n",Tools.fixNewLines("a\r"));
		assertEquals("\na",Tools.fixNewLines("\ra"));
		assertEquals("\n\n",Tools.fixNewLines("\r\n"));
		assertEquals("\n\n",Tools.fixNewLines("\n\r"));
		assertEquals("aaaa",Tools.fixNewLines("aaaa"));
		assertEquals("Fred",Tools.fixNewLines("Fred"));
		assertEquals("Fred\nJoe",Tools.fixNewLines("Fred\rJoe"));
		assertEquals("\n\n\n\n",Tools.fixNewLines("\r\r\r\r"));
	}

	@Test
	public void testParseDoubleStringDouble() {
		assertEquals(5.0,Tools.parseDouble(null,5.0),0.00001);
		assertEquals(5.0,Tools.parseDouble("",5.0),0.00001);
		assertEquals(5.0,Tools.parseDouble("fred",5.0),0.00001);
		assertEquals(4.33,Tools.parseDouble("4.33",5.0),0.00001);
		assertEquals(99.0,Tools.parseDouble("99",5.0),0.00001);
		assertEquals(-6.2,Tools.parseDouble("-6.2",5.0),0.00001);
		assertEquals(1.3e17,Tools.parseDouble("1.3e17",5.0),0.00001);
	}

	@Test
	public void testParseDoubleString() {
		assertEquals(Double.NaN,Tools.parseDouble(null),0.00001);
		assertEquals(Double.NaN,Tools.parseDouble(""),0.00001);
		assertEquals(Double.NaN,Tools.parseDouble("Himalayas"),0.00001);
		assertEquals(Double.NaN,Tools.parseDouble("%$^&UYTGH"),0.00001);
		assertEquals(42e42,Tools.parseDouble("42e42"),0.00001);
		assertEquals(1.4,Tools.parseDouble("1.4"),0.00001);
		assertEquals(73,Tools.parseDouble("73"),0.00001);
		assertEquals(-6.7,Tools.parseDouble("-6.7"),0.00001);
		assertEquals(0.0,Tools.parseDouble("0"),0.00001);
	}

	@Test
	public void testGetDecimalPlaces() {
		assertEquals(2,Tools.getDecimalPlaces(Double.MAX_VALUE, Double.MAX_VALUE));
		assertEquals(7,Tools.getDecimalPlaces(Double.MIN_VALUE, Double.MIN_VALUE));
		assertEquals(2,Tools.getDecimalPlaces(Double.NaN, Double.NaN));
		assertEquals(7,Tools.getDecimalPlaces(Double.MAX_VALUE, Double.MIN_VALUE));
		assertEquals(2,Tools.getDecimalPlaces(Double.MAX_VALUE, Double.NaN));
		assertEquals(2,Tools.getDecimalPlaces(Double.MIN_VALUE, Double.NaN));
		assertEquals(0,Tools.getDecimalPlaces(0.0, 0.0));
		assertEquals(0,Tools.getDecimalPlaces(88.0, 88.0));
		assertEquals(0,Tools.getDecimalPlaces(-3.0, -3.0));
		assertEquals(0,Tools.getDecimalPlaces(-3.0, 3.0));
		assertEquals(3,Tools.getDecimalPlaces(17.1, 4.1));
		assertEquals(3,Tools.getDecimalPlaces(4.1, 17.1));
		assertEquals(3,Tools.getDecimalPlaces(0.0, 1.4));
		assertEquals(7,Tools.getDecimalPlaces(1.4, 0.0));
		assertEquals(3,Tools.getDecimalPlaces(0.25, 1000.25));
		assertEquals(3,Tools.getDecimalPlaces(1000.25, 0.25));
		assertEquals(3,Tools.getDecimalPlaces(-0.25, -1000.25));
		assertEquals(3,Tools.getDecimalPlaces(-1000.25, -0.25));
		assertEquals(3,Tools.getDecimalPlaces(0.6, 100000.6));
		assertEquals(3,Tools.getDecimalPlaces(-100000.6, -0.6));
		assertEquals(3,Tools.getDecimalPlaces(3.2, 7.55));
		assertEquals(3,Tools.getDecimalPlaces(7.55, 3.2));
		assertEquals(3,Tools.getDecimalPlaces(1001.25,983.7563));
		assertEquals(3,Tools.getDecimalPlaces(983.7563, 1001.25));
		assertEquals(2,Tools.getDecimalPlaces(1000.4, 2444.0));
		assertEquals(3,Tools.getDecimalPlaces(99.4, 200.1));
		assertEquals(4,Tools.getDecimalPlaces(0.05, 200.1));
		assertEquals(5,Tools.getDecimalPlaces(0.005, 200.1));
		assertEquals(6,Tools.getDecimalPlaces(0.0005, 200.1));
		assertEquals(7,Tools.getDecimalPlaces(0.00005, 200.1));
		assertEquals(7,Tools.getDecimalPlaces(0.0000001, 200.1));
		assertEquals(2,Tools.getDecimalPlaces(1000.4, 1444.0));
		assertEquals(3,Tools.getDecimalPlaces(1000.4, 1001.45));
		assertEquals(4,Tools.getDecimalPlaces(1000.4, 1000.45));
		assertEquals(5,Tools.getDecimalPlaces(1000.4, 1000.405));
		assertEquals(6,Tools.getDecimalPlaces(1000.4, 1000.4005));
		assertEquals(7,Tools.getDecimalPlaces(1000.4, 1000.40005));
		assertEquals(7,Tools.getDecimalPlaces(1000.4, 1000.400005));
	}

	// should ideally do a comparison between our split and String.split(regex)
	//   but there are some differences that I needed to special code for and it
	//   was easier to just send in expected array
	
	void testSplit(String str, String[] expectedArray)
	{
		String[] ijWay = Tools.split(str);
		
		assertArrayEquals(expectedArray,ijWay);
	}
	
	@Test
	public void testSplitString() {
		testSplit("", new String[] {""});
		testSplit("abc", new String[] {"abc"});
		testSplit("a\tb\tc\t", new String[] {"a", "b", "c"});
		testSplit("a\rb\rc\r", new String[] {"a", "b", "c"});
		testSplit("a\nb\nc\n", new String[] {"a", "b", "c"});
		testSplit("a b c", new String[] {"a", "b", "c"});
		testSplit("a\n\n\nb", new String[] {"a", "b"});
		testSplit("\na\r", new String[] {"a"});
		testSplit(" a ", new String[] {"a"});
		testSplit("a\tb\rc\nd", new String[] {"a", "b", "c", "d"});
		testSplit("\n\n", new String[] {"\n\n"}); // BDZ: expected array value nonintuitive
		testSplit("\r\r", new String[] {"\r\r"}); // BDZ: expected array value nonintuitive
		testSplit("\t\t", new String[] {"\t\t"}); // BDZ: expected array value nonintuitive
		testSplit("  ", new String[] {"  "});     // BDZ: expected array value nonintuitive
		testSplit("a\r\t\n \n\t\rz", new String[] {"a", "z"});
	}

	void testSplit2(String str, String delims, String[] expectedArray)
	{
		String[] ijWay = Tools.split(str,delims);
		
		assertArrayEquals(expectedArray,ijWay);
	}
	
	@Test
	public void testSplitStringString() {
		testSplit2("","#",new String[] {""});
		testSplit2("###agh###", "#", new String[] {"agh"});
		testSplit2("#k#o#pppp#","#", new String[] {"k","o","pppp"});
		testSplit2("c:\\", ":", new String[] {"c", "\\"});
		testSplit2("> m < bob == fred", "<>=", new String[] {" m ", " bob ", " fred"});
		testSplit2("a?b?c","#",new String[] {"a?b?c"});
		testSplit2("a?b?c","?",new String[] {"a","b","c"});
		testSplit2("!!","!",new String[] {"!!"});  // BDZ: expected array value nonintuitive
		testSplit2("7777m7s77d777f7777g","7",new String[] {"m","s","d","f","g"});
		testSplit2("12366a","6",new String[] {"123","a"});
	}

}
