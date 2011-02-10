__kernel void bitrev2(
    __global float* data,
    __global float* temp, int base )
{
    int x = get_global_id(0);
    data[base+x] = temp[x];
};