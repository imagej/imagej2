__kernel void rad4but2(
    __global float* x, __global float* c, __global float* s, int gpSize, int numGps, int base, int numBfs )

{

   int index = get_global_id(0);
   int Ad0 = index * gpSize * 2;
   int Ad1 = Ad0;        
   int Ad2 = Ad1 + gpSize;
   int Ad3 = Ad1 + gpSize / 2;
   int Ad4 = Ad3 + gpSize;
   float rt1 = x[base+Ad1];
   x[base+Ad1] = x[base+Ad1] + x[base+Ad2];
   x[base+Ad2] = rt1 - x[base+Ad2];
   rt1 = x[base+Ad3];
   x[base+Ad3] = x[base+Ad3] + x[base+Ad4];
   x[base+Ad4] = rt1 - x[base+Ad4];
   float rt2 = 0;

for (int bfNum=1; bfNum<numBfs; bfNum++) 
{
         
    // subsequent BF's dealt with together
    int Ad1 = bfNum + Ad0;
    int Ad2 = Ad1 + gpSize;
    int Ad3 = gpSize - numBfs + Ad0;
    int Ad4 = Ad3 + gpSize;

    int CSAd = numBfs * numGps;
    rt1 = x[base+Ad2] * c[CSAd] + x[base+Ad4] * s[CSAd];
    rt2 = x[base+Ad4] * c[CSAd] - x[base+Ad2] * s[CSAd];

    x[base+Ad2] = x[base+Ad1] - rt1;
    x[base+Ad1] = x[base+Ad1] + rt1;
    x[base+Ad4] = x[base+Ad3] + rt2;
    x[base+Ad3] = x[base+Ad3] - rt2;
  }
};