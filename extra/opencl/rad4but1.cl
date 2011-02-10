__kernel void rad4but1(
    __global float* data,
   int gpsize, int base )
{
    int x = get_global_id(0);
    int Ad1 = x * 4;
    int Ad2 = Ad1 + 1;
    int Ad3 = Ad1 + gpsize;
    int Ad4 = Ad2 + gpsize;
    float rt1 = data[base+Ad1] + data[base+Ad2];   // a + b
    float rt2 = data[base+Ad1] - data[base+Ad2];   // a - b
    float rt3 = data[base+Ad3] + data[base+Ad4];   // c + d
    float rt4 = data[base+Ad3] - data[base+Ad4];   // c - d
    data[base+Ad1] = rt1 + rt3;      // a + b + (c + d)
    data[base+Ad2] = rt2 + rt4;      // a - b + (c - d)
    data[base+Ad3] = rt1 - rt3;      // a + b - (c + d)
    data[base+Ad4] = rt2 - rt4;      // a - b - (c - d)
};