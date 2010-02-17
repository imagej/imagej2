package ij.io;

public class Images {

	// ********************** PRIVATE DATA  ***************************************
	
	private static final long[][] BaseImage1x1 = {{77}};
	private static final long[][] BaseImage3x3 = {{11,12,13},{21,22,23},{31,32,33}};
	private static final long[][] BaseImage1x9 = {{11,12,13,14,15,16,17,18,19}};
	private static final long[][] BaseImage7x2 = {{11,12},{21,22},{31,32},{41,42},{51,52},{61,62},{71,72}};
	private static final long[][] BaseImage5x4 = {{255,255,255,255},{127,127,127,127},{63,63,63,63},{31,31,31,31},{15,15,15,15}};
	private static final long[][] BaseImage4x6 =
		{	{0,255,100,200,77,153},
			{255,254,253,252,251,250},
			{1,2,3,4,5,6},
			{0,0,0,0,0,0},
			{67,67,67,67,67,67},
			{8,99,8,99,8,255}
		};
	private static final long[][] Base24BitImage5x5 =
		{	{0xffffff,0xff0000,0x00ff00,0x0000ff, 0},
			{16777216,100000,5999456,7070708,4813},
			{1,10,100,1000,10000},
			{0,0,0,0,0},
			{88,367092,1037745,88,4}
		};
	private static final long[][] Base48BitImage6x6 = 
		{	{0xffffffffffffL, 0xffffffffff00L, 0xffffffff0000L, 0xffffff000000L, 0xffff00000000L, 0xff0000000000L},
			{0,0xffffffffffffL,0,0xffffffffffffL,0,0xffffffffffffL},
			{1,2,3,4,5,6},
			{0xff0000000000L,0x00ff00000000L, 0x0000ff000000,0x000000ff0000,0x00000000ff00,0x0000000000ff},
			{111111111111L,222222222222L,333333333333L,444444444444L,555555555555L,666666666666L},
			{0,567,0,582047483,0,1},
			{12345,554224678,90909090,9021,666666,3145926}
		};

	private static long[][] Image1x1sub1 = new long[][] {{83}};
	private static long[][] Image1x1sub2 = new long[][] {{121}};
	private static long[][] Image1x1sub3 = new long[][] {{415}};

	private static long[][][] ImageSet1x1 = {Image1x1sub1, Image1x1sub2, Image1x1sub3 };

	private static long[][] Image3x1sub1 = new long[][] {{1,2},{3,4},{5,6}};
	private static long[][] Image3x1sub2 = new long[][] {{6,5},{4,3},{2,1}};
	private static long[][] Image3x1sub3 = new long[][] {{4,6},{9,8},{1,7}};

	private static long[][][] ImageSet3x1 = {Image3x1sub1, Image3x1sub2, Image3x1sub3 };
	
	private static long[][] Image2x4sub1 = new long[][] {{1,2,3,4},{5,6,7,8}};
	private static long[][] Image2x4sub2 = new long[][] {{8,7,6,5},{4,3,2,1}};
	private static long[][] Image2x4sub3 = new long[][] {{1,0,1,0},{0,1,0,1}};

	private static long[][][] ImageSet2x4 = {Image2x4sub1, Image2x4sub2, Image2x4sub3 };
	
	private static long[][][] ImageSet6x6 = {Base48BitImage6x6, Base48BitImage6x6};

	// ********************** PUBLIC ACCESS POINTS  ***************************************
	
	public static long[][][] Images = {BaseImage1x1, BaseImage3x3, BaseImage1x9, BaseImage7x2,
		BaseImage5x4, BaseImage4x6, Base24BitImage5x5, Base48BitImage6x6};
	
	public static long[][][][] ImageSets = {ImageSet1x1, ImageSet3x1, ImageSet2x4, ImageSet6x6};
}
