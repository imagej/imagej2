package src.plugin;

import ij.IJ;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import src.opencl.OpenCLManager;


import com.nativelibs4java.opencl.CLBuildException;
import com.nativelibs4java.opencl.CLByteBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLFloatBuffer;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.util.NIOUtils;

public class FHT3DopenCL 
{
	public FHT3DopenCL()
	{
		initialize();
	}
	
	static final String[] openCLsourceArray = {"/bitrev1.cl","/bitrev2.cl","/rad4but1.cl","/rad4but2.cl","/rowtemp1.cl","/rowtemp2.cl"};
	static final String[] programNamesArray = { "bitrev1","bitrev2","rad4but1","rad4but2","rowtemp1","rowtemp2" };
	
	private static OpenCLManager oclm;
	private boolean useCL;
    public boolean isUseOpenCL() {
    	return useCL;
	}
    
    void initialize()
    {
    	//Get the OpenCL devices and validate them
    	oclm = new OpenCLManager();

    	//Set the source object
    	try {
    		useCL = oclm.setSource( openCLsourceArray, programNamesArray );
		} catch (Exception e2) 
		{
			oclm = null;
			IJ.log( "Exception caused by " + e2.toString() );
			IJ.handleException(e2);
		}

    }
    public static void p(String s)
    {
    	IJ.log(s);
    }
    
    public static void FHT3DopenCL(float[][] data, int w, int h, int d, boolean inverse) 
    {
    	p("Starting FHT3DopenCL portion ");
    	//p("Data is a 2D float array of dimensions " + data[0].length + " " + data.length + " w is " + w + " h is " + h + " d is " + d + " and boolean inverse is " + inverse);
    	//p("created float array sw, cw, sh, ch for sin cos tables size " + (w/4) );
    	
    	float[] sw = new float[w/4];
    	float[] cw = new float[w/4];
    	float[] sh = new float[h/4];
    	float[] ch = new float[h/4];
    	makeSinCosTables( w,sw,cw );
    	makeSinCosTables( h,sh,ch );
    	
    	//copy in the data TODO: replace make sin cos tables
    	FloatBuffer swFloatBuffer = NIOUtils.directFloats( sw.length, oclm.getContext().getByteOrder() ); 
		FloatBuffer cwFloatBuffer = NIOUtils.directFloats( cw.length, oclm.getContext().getByteOrder() );
    	FloatBuffer shFloatBuffer = NIOUtils.directFloats( sh.length, oclm.getContext().getByteOrder() ); 
		FloatBuffer chFloatBuffer = NIOUtils.directFloats( ch.length, oclm.getContext().getByteOrder() );
		
		//assign the data
		swFloatBuffer.put( sw );
		cwFloatBuffer.put( cw );
		shFloatBuffer.put( sh ); 
		chFloatBuffer.put( ch ); 
		
		//create card buffers
		CLFloatBuffer swCLFloat = oclm.getContext().createFloatBuffer( CLMem.Usage.Input, swFloatBuffer, true );
		CLFloatBuffer cwCLFloat = oclm.getContext().createFloatBuffer( CLMem.Usage.Input, cwFloatBuffer, true );
		CLFloatBuffer shCLFloat = oclm.getContext().createFloatBuffer( CLMem.Usage.Input, shFloatBuffer, true );
		CLFloatBuffer chCLFloat = oclm.getContext().createFloatBuffer( CLMem.Usage.Input, chFloatBuffer, true );		
		
		//these host buffers can be replaced by mapping TODO: abstract
		FloatBuffer xFloatBuffer = NIOUtils.directFloats( data[0].length, oclm.getContext().getByteOrder() ); 

		//Allocate memory on the GPU device for input and output
		CLFloatBuffer dataCLFloat = oclm.getContext().createFloatBuffer( CLMem.Usage.InputOutput, xFloatBuffer, true );
		CLFloatBuffer tempCLFloat = oclm.getContext().createFloatBuffer( CLMem.Usage.InputOutput, data[0].length );
		CLFloatBuffer tempCLFloat2 = oclm.getContext().createFloatBuffer( CLMem.Usage.InputOutput, data[0].length );
		
    	//p("finished populating sw, cw, sh, and ch.");
    	
    	for (int i = 0; i < d; i++)
    	{
    		p("OpenCl  " + i + " of " + d + " complete.");
    		
    		//assign the data
    		xFloatBuffer.rewind();
    		xFloatBuffer.put( data[i] );
    		
    		//copy to card
    		dataCLFloat =  oclm.getContext().createFloatBuffer(CLMem.Usage.InputOutput, xFloatBuffer, true );
    		
    		//p("starting rc2DFHT");
    		//rc2DFHT(dataCLFloat, w, h, swCLFloat, cwCLFloat, shCLFloat, chCLFloat, tempCLFloat);
    		
    		/** Row-column Fast Hartley Transform */
    		//static void rc2DFHT(CLFloatBuffer x, int w, int h, CLFloatBuffer swFloatBuffer, CLFloatBuffer cwFloatBuffer, CLFloatBuffer shFloatBuffer, CLFloatBuffer chFloatBuffer, CLFloatBuffer tempCLFloat) 
    		{
    			for (int row=0; row<h; row++)
    			{
    				dfht3CL( dataCLFloat, row*w, w, swCLFloat, cwCLFloat, tempCLFloat);
    			}

    			for(int col = 0; col < w; col++)
    			{
    				//Allow for blocking execution
    				CLEvent kernelCompletion;

    				synchronized( oclm.kernel[4] )
    				{
    					oclm.kernel[4].setArgs( dataCLFloat, tempCLFloat2, col, w );
    					kernelCompletion = oclm.kernel[4].enqueueNDRange( oclm.getQueue(), new int[]{ h }, null );
    				}  kernelCompletion.waitFor();
    				
    				
    				dfht3CL( tempCLFloat2, 0, h, shCLFloat, chCLFloat, tempCLFloat);
    				
    				synchronized( oclm.kernel[5] )
    				{
    					oclm.kernel[5].setArgs( dataCLFloat, tempCLFloat2, col, w );
    					kernelCompletion = oclm.kernel[5].enqueueNDRange( oclm.getQueue(), new int[]{ h }, null );
    				}  kernelCompletion.waitFor();
    			}
    		}
    		
    		
			//write the data to the host buffer
    		dataCLFloat.read( oclm.getQueue(), xFloatBuffer, true );
			xFloatBuffer.rewind();
			xFloatBuffer.get( data[i] );
			
    		//p("ending rc2DFHT");
    	}
    	
    	
    	//Release temp
    	tempCLFloat.release();
    	tempCLFloat2.release();
    	dataCLFloat.release();
    	
    	p("Ending FHT3DopenCL portion ");

    	//p("creating array u of size " + d);
    	float[] u = new float[d];
    	
    	
    	if(powerOf2Size(d))
    	{
    		p("Starting Power of 2 openCL");
    		
    		//p("creating float array s and c of size" + (d/4));
    		float[] s = new float[d/4];
    		float[] c = new float[d/4];

    		makeSinCosTables(d,s,c);
    		
    		//copy in the data TODO: replace make sin cos tables
        	FloatBuffer sFloatBuffer = NIOUtils.directFloats( s.length, oclm.getContext().getByteOrder() ); 
    		FloatBuffer cFloatBuffer = NIOUtils.directFloats( c.length, oclm.getContext().getByteOrder() );
    		FloatBuffer uFloatBuffer = NIOUtils.directFloats( u.length, oclm.getContext().getByteOrder() );
    		
    		//create device buffers
    		CLFloatBuffer sCLFloat = oclm.getContext().createFloatBuffer( CLMem.Usage.Input, sFloatBuffer, true );
    		CLFloatBuffer cCLFloat = oclm.getContext().createFloatBuffer( CLMem.Usage.Input, cFloatBuffer, true );
    		
    		//assign the data
    		sFloatBuffer.put( s );
    		cFloatBuffer.put( c );
    		
    		//Allocate memory on the GPU device for input and output
    		CLFloatBuffer xCLFloat = oclm.getContext().createFloatBuffer( CLMem.Usage.InputOutput, xFloatBuffer, true );
    		CLFloatBuffer uCLFloat = oclm.getContext().createFloatBuffer( CLMem.Usage.InputOutput, d );
    		CLFloatBuffer temp2CLFloat = oclm.getContext().createFloatBuffer( CLMem.Usage.InputOutput, d );

    		
    		//p("done populating s and c arrays with sin cos tables");
    		for(int k2 = 0; k2 < h; k2++)
    		{
    			p("Power of k2 loop # " + k2 + " of " + h);
    			for(int k1 = 0; k1 < w; k1++)
    			{
    				int ind = k1 + k2*w;

    				for(int k3 = 0; k3 < d; k3++)
    				{
    					u[k3] = data[k3][ind];
    				}
    				
    				//copy u to card
    				uFloatBuffer.rewind();
    				uFloatBuffer.put( u );
    				uCLFloat = oclm.getContext().createFloatBuffer(CLMem.Usage.InputOutput, uFloatBuffer, true );

    				//p("starting dfht3 with u, d, s, and c paramaters");
    				dfht3CL(uCLFloat, 0, d, sCLFloat, cCLFloat, temp2CLFloat );
    				
    				//write the data to the host buffer
    				uCLFloat.read( oclm.getQueue(), uFloatBuffer, true );
    	    		uFloatBuffer.rewind();
    	    		uFloatBuffer.get( u );
    				
    				//p("populate array data for all " + d + " dimensions.");
    				for(int k3 = 0; k3 < d; k3++)
    				{
    					data[k3][ind] = u[k3];
    				}
    			}
    		}
    		
    		//release CL
    		xCLFloat.release();
    		uCLFloat.release();
    		temp2CLFloat.release();
    		
    	}
    	else
    	{
    		float[] cas = hartleyCoefs(d);
    		float[] work = new float[d];
    		//p("done populating s and c arrays with sin cos tables");
    		for(int k2 = 0; k2 < h; k2++)
    		{
    			for(int k1 = 0; k1 < w; k1++)
    			{
    				int ind = k1 + k2*w;
    				for(int k3 = 0; k3 < d; k3++)
    				{
    					u[k3] = data[k3][ind];
    				}

    				//p("starting slowHT since d is not power of 2");
    				slowHT(u,cas,d,work);

    				//p("populate array data for all " + d + " dimensions.");
    				for(int k3 = 0; k3 < d; k3++)
    				{
    					data[k3][ind] = u[k3];
    				}
    			}
    		}
    	}
    	p("Done with opencl portion...");
    	
    	//Convert to actual Hartley transform
    	//p("starting the Hartley transform");
    	float A,B,C,D,E,F,G,H;
    	int k1C,k2C,k3C;
    	for(int k3 = 0; k3 <= d/2; k3++)
    	{
    		k3C = (d - k3) % d;
    		for(int k2 = 0; k2 <= h/2; k2++)
    		{
    			k2C = (h - k2) % h;
    			for (int k1 = 0; k1 <= w/2; k1++)
    			{
    				k1C = (w - k1) % w;
    				A = data[k3][k1 + w*k2C];
    				B = data[k3][k1C + w*k2];
    				C = data[k3C][k1 + w*k2];
    				D = data[k3C][k1C + w*k2C];
    				E = data[k3C][k1 + w*k2C];
    				F = data[k3C][k1C + w*k2];
    				G = data[k3][k1 + w*k2];
    				H = data[k3][k1C + w*k2C];
    				data[k3][k1 + w*k2] = (A+B+C-D)/2;
    				data[k3C][k1 + w*k2] = (E+F+G-H)/2;
    				data[k3][k1 + w*k2C] = (G+H+E-F)/2;
    				data[k3C][k1 + w*k2C] = (C+D+A-B)/2;
    				data[k3][k1C + w*k2] = (H+G+F-E)/2;
    				data[k3C][k1C + w*k2] = (D+C+B-A)/2;
    				data[k3][k1C + w*k2C] = (B+A+D-C)/2;
    				data[k3C][k1C + w*k2C] = (F+E+H-G)/2;
    			}
    		}
    	}
    	//p("finished with the Hartley transform");
    	if(inverse)
    	{
    		//float norm = (float)Math.sqrt(d*h*w);
    		float norm = d*h*w;
    		for(int k3 = 0; k3 < d; k3++)
    		{
    			for(int k2 = 0; k2 < h; k2++)
    			{
    				for (int k1 = 0; k1 < w; k1++)
    				{
    					data[k3][k1 + w*k2] /= norm;
    				}
    			}
    		}
    	}
    	
		
		
    	p("Ending FHT3DopenCL Routine with parameters data, w, h, d, and inverse");
    }

	private static int BitRevX (int  x, int bitlen) 
	{
		int  temp = 0;
		for (int i=0; i<=bitlen; i++)
			if ((x & (1<<i)) !=0)
				temp  |= (1<<(bitlen-i-1));
		return temp & 0x0000ffff;
	}
	
	static float[] hartleyCoefs(int max)
	{
		float[] cas = new float[max*max];
		int ind = 0;
		for(int n = 0; n < max; n++)
		{
			for (int k = 0; k < max; k++)
			{
				double arg = (2*Math.PI*k*n)/max;
				cas[ind++] = (float)(Math.cos(arg) + Math.sin(arg));
			}
		}
		return cas;
	}
	
	static void slowHT(float[] u, float[] cas, int max, float[] work)
	{
		int ind = 0;
		for(int k = 0; k < max; k++)
		{
			float sum = 0;
			for(int n = 0; n < max; n++)
			{
				sum += u[n]*cas[ind++];
			}
			work[k] = sum;
		}
		for (int k = 0; k < max; k++)
		{
			u[k] = work[k];
		}
	}
	
	static void makeSinCosTables(int maxN, float[] s, float[] c) 
	{
		int n = maxN/4;
		double theta = 0.0;
		double dTheta = 2.0 * Math.PI/maxN;
		for (int i=0; i<n; i++) 
		{
			c[i] = (float)Math.cos(theta);
			s[i] = (float)Math.sin(theta);
			theta += dTheta;
		}
	}
	
	/* An optimized real FHT */
	public static void dfht3CL ( CLFloatBuffer dataCLFloat, int base, int maxN, CLFloatBuffer s, CLFloatBuffer c, CLFloatBuffer tempCLFloat ) 
	{
		/* Do all the things that OpenCL will not be good at */
		int Nlog2 = log2(maxN);
		int gpSize = 2;     //first & second stages - do radix 4 butterflies once thru
		int numGps = maxN / 4;
		
		try
		{
			//Allow for blocking execution
			CLEvent kernelCompletion;

			synchronized( oclm.kernel[0] )
			{
				oclm.kernel[0].setArgs( dataCLFloat, tempCLFloat, base, Nlog2 );
				kernelCompletion = oclm.kernel[0].enqueueNDRange( oclm.getQueue(), new int[]{ maxN }, null );
			}  kernelCompletion.waitFor();
			
			synchronized( oclm.kernel[1] )
			{
				oclm.kernel[1].setArgs( dataCLFloat, tempCLFloat, base );
				kernelCompletion = oclm.kernel[1].enqueueNDRange( oclm.getQueue(), new int[]{ maxN }, null );
			} kernelCompletion.waitFor();
			
			synchronized ( oclm.kernel[2] ) 
			{
				oclm.kernel[2].setArgs( dataCLFloat, gpSize, base );
				kernelCompletion = oclm.kernel[2].enqueueNDRange( oclm.getQueue(), new int[]{ numGps }, null );
			} kernelCompletion.waitFor();
			

			
			int stage, numBfs;
			if (Nlog2 > 2) 
			{
				gpSize = 4;
				numBfs = 2;
				numGps = numGps / 2;
				//IJ.write("FFT: dfht3 "+Nlog2+" "+numGps+" "+numBfs);
				for (stage=2; stage<Nlog2; stage++) 
				{
					//__global float* x, __global float* c, __global float* s, int gpSize, int numGps, int base, int numBfs )
					synchronized ( oclm.kernel[3] ) 
					{
						oclm.kernel[3].setArgs( dataCLFloat, c, s, gpSize, numGps, base, numBfs );
						kernelCompletion = oclm.kernel[3].enqueueNDRange( oclm.getQueue(), new int[]{ numGps }, null );
					} kernelCompletion.waitFor();
					
					gpSize *= 2;
					numBfs *= 2;
					numGps = numGps / 2;
				}
			}
			
			//IJ.log( "OpenCL generated results for radix4 and bitrev" );

		} catch(Exception e)
		{ 	
			IJ.log( "OpenCL generated exception " + e.toString() + "\n Switching to Java" );
			IJ.handleException( e );
		} 
	}
	
	static int log2 (int x) 
	{
		int count = 15;
		while (!btst(x, count))
			count--;
		return count;
	}
	
	private static boolean btst (int  x, int bit) 
	{
		//int mask = 1;
		return ((x & (1<<bit)) != 0);
	}
	
	static void BitRevRArr (float[] x, int base, int bitlen, int maxN) 
	{
		int    l;
		float[] tempArr = new float[maxN];
		for (int i=0; i<maxN; i++)
		{
			l = BitRevX (i, bitlen);  //i=1, l=32767, bitlen=15
			tempArr[i] = x[base+l];
		}
		
		for (int i=0; i<maxN; i++)
			x[base+i] = tempArr[i];
	}

	static boolean powerOf2Size(int w) 
	{
        int i=2;
        while( i < w ) i *= 2;
        return i == w;
    }
		
	/* An optimized real FHT */
	public static void dfht3 (float[] x, int base, int maxN, float[] s, float[] c) 
	{
		int i, stage, gpNum, gpIndex, gpSize, numGps, Nlog2;
		int bfNum, numBfs;
		int Ad0, Ad1, Ad2, Ad3, Ad4, CSAd;
		float rt1, rt2, rt3, rt4;

		Nlog2 = log2(maxN);
		BitRevRArr(x, base, Nlog2, maxN);	//bitReverse the input array
		gpSize = 2;     //first & second stages - do radix 4 butterflies once thru
		numGps = maxN / 4;
		for (gpNum=0; gpNum<numGps; gpNum++)  
		{
			Ad1 = gpNum * 4;
			Ad2 = Ad1 + 1;
			Ad3 = Ad1 + gpSize;
			Ad4 = Ad2 + gpSize;
			rt1 = x[base+Ad1] + x[base+Ad2];   // a + b
			rt2 = x[base+Ad1] - x[base+Ad2];   // a - b
			rt3 = x[base+Ad3] + x[base+Ad4];   // c + d
			rt4 = x[base+Ad3] - x[base+Ad4];   // c - d
			x[base+Ad1] = rt1 + rt3;      // a + b + (c + d)
			x[base+Ad2] = rt2 + rt4;      // a - b + (c - d)
			x[base+Ad3] = rt1 - rt3;      // a + b - (c + d)
			x[base+Ad4] = rt2 - rt4;      // a - b - (c - d)
		}
		
		if (Nlog2 > 2) 
		{
			 // third + stages computed here
			gpSize = 4;
			numBfs = 2;
			numGps = numGps / 2;
			//IJ.write("FFT: dfht3 "+Nlog2+" "+numGps+" "+numBfs);
			for (stage=2; stage<Nlog2; stage++) 
			{
				for (gpNum=0; gpNum<numGps; gpNum++) 
				{
					Ad0 = gpNum * gpSize * 2;
					Ad1 = Ad0;     // 1st butterfly is different from others - no mults needed
					Ad2 = Ad1 + gpSize;
					Ad3 = Ad1 + gpSize / 2;
					Ad4 = Ad3 + gpSize;
					rt1 = x[base+Ad1];
					x[base+Ad1] = x[base+Ad1] + x[base+Ad2];
					x[base+Ad2] = rt1 - x[base+Ad2];
					rt1 = x[base+Ad3];
					x[base+Ad3] = x[base+Ad3] + x[base+Ad4];
					x[base+Ad4] = rt1 - x[base+Ad4];
					for (bfNum=1; bfNum<numBfs; bfNum++) 
					{
					// subsequent BF's dealt with together
						Ad1 = bfNum + Ad0;
						Ad2 = Ad1 + gpSize;
						Ad3 = gpSize - bfNum + Ad0;
						Ad4 = Ad3 + gpSize;

						CSAd = bfNum * numGps;
						rt1 = x[base+Ad2] * c[CSAd] + x[base+Ad4] * s[CSAd];
						rt2 = x[base+Ad4] * c[CSAd] - x[base+Ad2] * s[CSAd];

						x[base+Ad2] = x[base+Ad1] - rt1;
						x[base+Ad1] = x[base+Ad1] + rt1;
						x[base+Ad4] = x[base+Ad3] + rt2;
						x[base+Ad3] = x[base+Ad3] - rt2;

					} /* end bfNum loop */
				} /* end gpNum loop */
				
				gpSize *= 2;
				numBfs *= 2;
				numGps = numGps / 2;
				
			} /* end for all stages */
		} /* end if Nlog2 > 2 */
	}
}
