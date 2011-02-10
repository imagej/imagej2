__kernel void sobel(
    __global char* input,
    __global char* output,
    int width, int height)
{
    int x = get_global_id(0);
    int y = get_global_id(1);
    int p[9];
    int offset = y * width + x;
if( x < 1 || y < 1 || x > width - 2 || y > height - 2 )
{
  output[offset] = 0; //TODO implement image edges
}
  else
{
    p[0] = input[offset - width - 1] & 0xff;
    p[1] = input[offset - width] & 0xff;
    p[2] = input[offset - width + 1] & 0xff;
    p[3] = input[offset - 1] & 0xff;
    p[4] = input[offset] & 0xff;
    p[5] = input[offset + 1] & 0xff;
    p[6] = input[offset + width - 1] & 0xff;
    p[7] = input[offset + width] & 0xff;
    p[8] = input[offset + width + 1] & 0xff;

    int sum1 = p[0] + 2*p[1] + p[2] - p[6] - 2*p[7] - p[8];
    int sum2 = p[0] + 2*p[3] + p[6] - p[2] - 2*p[5] - p[8];
    float sum3 = sum1*sum1 + sum2*sum2;
   
    int sum = sqrt( sum3 );
    if (sum > 255) sum = 255;
    output[offset] = (char) sum;
 }
};