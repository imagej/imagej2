__kernel void bitrev1(
    __global float* data,
    __global float* temp,
    int base, int Nlog2)
{
    int x = get_global_id(0);
    int  l = 0;
    for (int i=0; i<= Nlog2; i++)
    {
      if ((x & (1<<i)) !=0)
      {
        l  |= (1<<(Nlog2-i-1));
      }
    }
    l = (l & 0x0000ffff) + base;
    
    temp[x] = data[l];
 
};