__kernel void rowtemp1(
    __global float* data,
    __global float* temp, int col, int w )
{
    int index = get_global_id(0);
    temp[index] = data[ col + w*index ];    
};