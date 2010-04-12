__kernel void rowtemp2(
    __global float* data,
    __global float* temp, int col, int w )
{
    int index = get_global_id(0);
    data[ col + w*index ] = temp[index];    
};