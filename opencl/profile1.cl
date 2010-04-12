__kernel void profile1(
    __global float* x, __global float* c )
{

   int index = get_global_id(0);

   //add two arrays
   x[index] = x[index] + c[index];
};