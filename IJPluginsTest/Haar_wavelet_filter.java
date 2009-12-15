/*
 * Haar_wavelet_filter.java
 *
 * Created on 20/01/2004 Copyright (C) 2005 IBMP
 * ImageJ plugin
 * Version  : 1.0
 * Authors  : Olivier Marchal & Jérôme Mutterer
 *            written for the IBMP-CNRS Strasbourg(France)
 * Email    : jerome.mutterer at ibmp-ulp.u-strasbg.fr
 *            olmarchal at wanadoo.fr
 *
 * Description : The purpose of this plugin is to remove the noise present
 * in the original image by thresholding the wavelet coefficients.
 * This plugin performs the 2D Haar wavelet transform on every size of image
 * and on an simulated image of the same size with a gaussian noise of std_dev=1.
 * We assume that the standard deviation at each scale is the product between
 * the standard deviation of the noise in the original image and the standard
 * deviation of the same scale in the simulated image. We also assume that the
 * noise lies in the three first scales. We added an adaptative median filter
 * in order to remove "hot" or "cold" pixels (e.g. poissonian noise in CCD).
 *
 * Limitations : Blocky artifacts due to the wavelet shape may appear
 * after the reconstruction.
 *
 * The code for the 1D Haar wavelet transform was taken from Ian Kaplan,
 * Bear Products International. The noise removal algorithm was found in
 * Multiscale Analysis Methods in Astronomy and Engineering by Jean Luc Starck
 * and Fionn Murtagh.
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */


import ij.*;
import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;
import ij.gui.*;
import java.lang.Math.*;
import java.util.Vector;
import java.awt.*;


public class Haar_wavelet_filter implements PlugInFilter {

  ImagePlus imp;
  private double haar_value;  // final Haar step value
  private Vector coef;        // contains Haar coefficients
  private Vector coef_s;      // contains simulated Haar coefficients
  private byte[] save;        // save of the original image
  private double[] line;
  private double[] col;
  private double[] line_s;
  private double[] col_s;
  private double k1,k2,k3;    // thresholding coefficients
  private double pdev;
  private double pix;
  private double pix_d[];
  private double pix_med2[];
  private double pix_s[];
  private double res[];
  private double stdev;
  private boolean poisson;
  private boolean noise;
  private boolean save_coff;
  private double matrix[][];
  private double fish[];
  private String result;



  public int setup(String arg, ImagePlus imp){

    imp = WindowManager.getCurrentImage();
    return DOES_8G+NO_CHANGES;
  }


  public void run(ImageProcessor ip) {


    int width1 = ip.getWidth();
    int height1= ip.getHeight();

// check if the size of the image is not squared sized. If not, the image is expanded
  if(width1 != height1){
  if(width1 != 32  || width1 != 64 || width1 != 128 || width1 != 256 || width1 != 512 || width1 != 1024 || width1 != 2048 || width1 != 4096
      || height1 != 32 || height1 != 64 || height1 != 128 || height1 != 256 || height1 != 512 || height1 != 1024 || height1 != 2048 || height1 != 4096){

 ip=resize_image(width1,height1,ip);

  }//if
}//if


   k1=3;
   k2=3;
   k3=3;
   pdev=1.6;

  GenericDialog gd = new GenericDialog("Haar Wavelet filter");
  gd.addMessage("Coefficients for the different scales");
  gd.addNumericField("k1",k1,1);
  gd.addNumericField("k2",k2,1);
  gd.addNumericField("k3",k3,1);
  gd.addCheckbox("Non gaussian noise removal",false);
  gd.addNumericField("std dev",pdev,1);
  gd.addCheckbox("Noise display",true);
  gd.addCheckbox("Coefficients display ",false);
  gd.showDialog();

  if(gd.wasCanceled()){
    IJ.error("Plugin canceled");
    return;
  }

  k1=gd.getNextNumber();
  k2=gd.getNextNumber();
  k3=gd.getNextNumber();
  poisson = gd.getNextBoolean();
  pdev=gd.getNextNumber();
  noise=gd.getNextBoolean();
  save_coff=gd.getNextBoolean();

  if(gd.invalidNumber()){
    IJ.error("Invalid field");
    return;
  }


 int width = ip.getWidth();
 int height= ip.getHeight();


// final image
ImagePlus denoised = NewImage.createByteImage("Denoised image",width,height,1,NewImage.FILL_BLACK);
ImageProcessor den_ip = denoised.getProcessor();
den_ip.copyBits(ip,0,0,Blitter.COPY);
byte[] pixels =(byte[])den_ip.getPixels();

//save original image
byte[] save= new byte[width*height];
for(int h=0;h<pixels.length;h++) save[h]=pixels[h];

// All pixels have values between 0 and 255
double[] pix_d= new double[width*height];
for(int i=0;i<pixels.length;i++){
  pix =  0xff  & pixels[i] ;
  pix_d[i] = pix;
}


// noise estimation of the input image
stdev=noise_estimate( ip, width, height, pix_d);

// simulation of an image with mean=0 and std_dev=1
ImagePlus simul = NewImage.createByteImage("Simulation",width,height,1,NewImage.FILL_BLACK);
ImageProcessor simul_ip = simul.getProcessor();
simul_ip.noise(1.67) ;
byte[] pixels2 =(byte[])simul_ip.getPixels();

double[] pix_s= new double[width*height];
for(int i=0;i<pixels2.length;i++){
  pix = 0xff & pixels2[i];
  pix_s[i] = pix;
}



double line[]= new double[width];
double col[] = new double[height];
double line_s[]= new double[width];
double col_s[] = new double[height];

if(save_coff==true) disp_coeff(width, height, width1, height1, pix_d);

for(int l=0;l<height;l++){         // loop for every row

for(int k=width*l; k < width*l + width; k++ ) {line[k-l*width]=pix_d[k];}
for(int k=width*l; k < width*l + width; k++ ) {line_s[k-l*width]=pix_s[k];}

  Haar_calc(line_s);
  coef_s=coef;
  Haar_calc(line);
  Denoise(coef, coef_s, k1, k2, k3, stdev);
  Haar_inverse(l);

  for(int f=0;f<width;f++){
    if(line[f]<0) line[f]=0;
    if(line[f]>255) line[f]=255;
}

   for(int f=0;f<width;f++){                      //matrix update
    pix_d[f+l*width]=(double)line[f];
    pixels[f+l*width]=(byte)( pix_d[f+l*width]);
  }

}//for row



for(int l=0;l<width;l++){        // loop for every column

for(int k=0; k < height; k++ ){col[k]=pix_d[k*height+l];}
for(int k=0; k < height; k++ ){col_s[k]=pix_s[k*height+l];}

  Haar_calc(col_s);
  coef_s=coef;
  Haar_calc(col);
  Denoise(coef, coef_s, k1, k2, k3, stdev);
  Haar_inverse(l);

  for(int f=0;f<height;f++){
   if(col[f]<0) col[f]=0;
   if(col[f]>255) col[f]=255;
 }


  for(int f=0;f<height;f++){                   // matrix update
     pix_d[l+f*width]=col[f];
     pixels[l+f*width]= (byte)pix_d[l+f*width];
  }


 }//for col



if(poisson==true){
     double matrix[][]=new double[width][height];

     for(int i=0;i<width;i++){
       for(int j=0;j<height;j++){
          matrix[i][j]=pix_d[i+j*width];
        }
      }

  filter_poisson(matrix,width,height,pdev);

     for(int i=0;i<width;i++){
       for(int j=0;j<height;j++){
          pix_d[i+j*width]=matrix[i][j];
        }
      }

   for(int f=0;f<height*width;f++)                   // matrix update
      pixels[f]= (byte)pix_d[f];

}//if poisson


// display the denoised image
if(width1 != width || height1 != height) {

  ImagePlus denoised_resized = NewImage.createByteImage("Denoised image",width1,height1,1,NewImage.FILL_BLACK);
  ImageProcessor denre_ip = denoised_resized.getProcessor();
  denre_ip.copyBits(den_ip,0,0,Blitter.COPY);
  byte[] pixels4 =(byte[])denre_ip.getPixels();

  denoised_resized.show();
  denoised_resized.updateAndDraw();

}

else{
  denoised.show();
  denoised.updateAndDraw();
}



//display removed noise
if(noise==true){

  ImagePlus noise = NewImage.createByteImage("Removed noise",width,height,1,NewImage.FILL_BLACK);
  ImageProcessor n_ip = noise.getProcessor();
  byte[] rem_noise =(byte[])n_ip.getPixels();
  double[] rem_noise_d= new double[width*height];
    for(int i=0;i<rem_noise.length;i++){
      pix =  0xff  & rem_noise[i] ;
      rem_noise_d[i] = pix;
    }

  double[] save_d= new double[width*height];
    for(int i=0;i<save.length;i++){
      pix =  0xff  & save[i] ;
      save_d[i] = pix;
    }

  for(int d=0;d<width*height;d++) rem_noise_d[d]=save_d[d]-pix_d[d];

  for(int f=0;f<width*height;f++){
    if(rem_noise_d[f]<0) rem_noise_d[f]=0;
    if(rem_noise_d[f]>255) rem_noise_d[f]=255;
  }

  for(int d=0;d<width*height;d++) rem_noise[d]=(byte)rem_noise_d[d];

if(width1 != width || height1 != height) {

  ImagePlus noise_resized = NewImage.createByteImage("Removed noise",width1,height1,1,NewImage.FILL_BLACK);
  ImageProcessor nre_ip = noise_resized.getProcessor();
  nre_ip.copyBits(n_ip,0,0,Blitter.COPY);

  byte[] pixels5 =(byte[])nre_ip.getPixels();

  noise_resized.show();
  noise_resized.updateAndDraw();
  IJ.run("3-3-2 RGB");

}

else{
  noise.show();
  noise.updateAndDraw();
  IJ.run("3-3-2 RGB");
}

}//display noise



} //run



// the reconstruction works on 2**n values
public static int power2( byte n )
{
 int rslt = 0x1 << (n & 0x1f);
 return rslt;
}//power2



// the reconstruction works on 2**n values
public static byte log2( int val )
{
  byte log;
  for (log = 0; val > 0; log++, val = val >> 1)
    ;
  log--;
  return log;
  }//log


// 1D Haar wavelet transform
public void Haar_calc( double[] values )
  {
    if (values != null) {
      line=values;
      coef=new Vector();
      haar_value = Haar_1d( values );
      Reverse_coef();
    }
  } // Haar_calc



// calculate the Haar 1D transform of an array
private double Haar_1d( double[] values )
  {
    double last_val;

    double[] a = new double[ values.length/2 ];  // Haar step values
    double[] c = new double[ values.length/2 ];  // Haar wave values

    for (int i = 0, j = 0; i < values.length; i += 2, j++) {
      a[j] = (values[i] + values[i+1])/2;
      c[j] = (values[i] - values[i+1])/2;
    }

    coef.addElement( c );

    if (a.length == 1)
      last_val = a[0];
    else
      last_val = Haar_1d(a);

    return last_val;
  } // Haar_1d



// reverse the order of the coefficient arrays so that the reconstruction begins from
// the smallest.
private void Reverse_coef() {
    int size = coef.size();
    Object tmp;

    for (int i = 0, j = size-1; i < j; i++, j--) {
      tmp = coef.elementAt(i);
      coef.setElementAt(coef.elementAt(j), i);
      coef.setElementAt(tmp, j);
    }
  }//reverse



// performs the denoising from the coefficients values. If a coefficient is superior to a
// given value, then it is set to 0.
public void Denoise(Vector coff, Vector coff_s, double k1, double k2, double k3, double dev){

  int size = coff.size();
  int size_s=coff_s.size();
  double dev_s[]=new double[5];

  double c_tmp1[] = (double[])coff.elementAt( size-1 );
  double c_tmp2[] = (double[])coff.elementAt( size-2 );
  double c_tmp3[] = (double[])coff.elementAt( size-3 );
  double c_tmp4[] = (double[])coff.elementAt( size-4 );

  double cs_tmp1[] = (double[])coff_s.elementAt( size-1 );
  double cs_tmp2[] = (double[])coff_s.elementAt( size-2 );
  double cs_tmp3[] = (double[])coff_s.elementAt( size-3 );
  double cs_tmp4[] = (double[])coff_s.elementAt( size-4 );

 dev_s[0]=stdev_calc(cs_tmp1);
 dev_s[1]=stdev_calc(cs_tmp2);
 dev_s[2]=stdev_calc(cs_tmp3);
 dev_s[3]=stdev_calc(cs_tmp4);


for(int k=0;k<c_tmp1.length;k++){
  if(Math.abs(c_tmp1[k])<=(dev*k1*dev_s[0])) c_tmp1[k]=0;
}

for(int k=0;k<c_tmp2.length;k++){
  if(Math.abs(c_tmp2[k])<(dev*k2*dev_s[1])) c_tmp2[k]=0;
}

for(int k=0;k<c_tmp3.length;k++){
  if(Math.abs(c_tmp3[k])<(dev*k3*dev_s[2])) c_tmp3[k]=0;
}

for(int k=0;k<c_tmp4.length;k++){
  if(Math.abs(c_tmp4[k])<(dev*k3*dev_s[3])) c_tmp4[k]=0;
}

coef.setElementAt(c_tmp1, size-1);
coef.setElementAt(c_tmp2, size-2);
coef.setElementAt(c_tmp3, size-3);
coef.setElementAt(c_tmp4, size-4);


}//denoise



// performs the inverse Haar transform
public void Haar_inverse(int v) {
    if (line != null && coef != null && coef.size() > 0) {
      int len = line.length;

      line[0] = haar_value;

      if (len > 0) {

        byte log = log2( len );
        len = power2( log );

        int vec_ix = 0;
        int last_p = 0;
        byte p_adj = 1;

        for (byte l = (byte)(log-1); l >= 0; l--) {

          int p = power2( l );
          double c[] = (double[])coef.elementAt( vec_ix );

          int coef_ix = 0;
          for (int j = 0; j < len; j++) {
            int a_1 = p * (2 * j);
            int a_2 = p * ((2 * j) + 1);

            if (a_2 < len) {
              double tmp = line[a_1];
              line[ a_1 ] = tmp + c[coef_ix];
              line[ a_2 ] = tmp - c[coef_ix];
              coef_ix++;
            }
            else {
              break;
            }
          } // for j
          last_p = p;
          p_adj++;
          vec_ix++;
        } // for l
      }

    }
  } // inverse


//estimation of the noise in the input image
public double noise_estimate(ImageProcessor ip,int width,int height, double[] pix_d){

 double stdev;

  ImagePlus med = NewImage.createByteImage("med image",width,height,1,NewImage.FILL_BLACK);
  ImageProcessor med_ip = med.getProcessor();
  med_ip.copyBits(ip,0,0,Blitter.COPY);
  byte[] pix_med =(byte[])med_ip.getPixels();

   med_ip.medianFilter();
   double[] pix_med2= new double[width*height];
    for(int i=0;i<pix_med.length;i++){
       pix =  0xff  & pix_med[i] ;
       pix_med2[i] = pix;
     }

   stdev=0;
   double[] res= new double[width*height];
     for(int i=0;i<width*height;i++){
           res[i]=pix_d[i]-pix_med2[i];
           if(res[i]<0) res[i]=0;
           if(res[i]>255) res[i]=255;
     }

   stdev=stdev_calc(res);
 return stdev;

 }


// filter that acts like a filter median when the value of central pixel
// is superior or inferior to the mean +/- dev_std of the 3x3 neighborhood
public void filter_poisson(double mat[][],int width, int height, double devp){

int pos;
double dev;
double mean;
double[][] tmp_mat = new double[width+2][height+2]; //temporary matrix for edge effets
double[] fish = new double[9];
double[] fish_save = new double[9];
double[] tmp =new double[8];

  for(int i=0;i<width+2;i++)
    for(int j=0;j<height+2;j++)
      tmp_mat[i][j]=0;             // fill the tmp matrix with zeros

  for(int i=0;i<width;i++)
    for(int j=0;j<height;j++)
      tmp_mat[i+1][j+1]=mat[i][j];  // copy the matrix in the center of the tmp matrix

  for(int i=1;i<=width;i++){          //scan of the matrix
    for(int j=1;j<=height;j++){

      pos=0;
      for(int x=i-1;x<=i+1;x++){
        for(int y=j-1;y<=j+1;y++){    // copy of the 3x3 neighborhood
           fish[pos]=tmp_mat[x][y];
            pos++;
	}
      }

  for(int z=1;z<fish.length;z++)  fish_save[z]=fish[z];

  sort_array(fish_save,9);

  mean=0;
  dev=0;
  for(int k=0;k<tmp.length;k++) {tmp[k]=fish[k]; mean+=tmp[k];}
  mean=mean/tmp.length;
  dev=stdev_calc(tmp);
  if(fish[4]>mean+devp*dev || fish[4]<mean-devp*dev) tmp_mat[i][j]=fish_save[4];

    }
}

for(int i=0;i<width;i++)
  for(int j=0;j<height;j++)
    mat[i][j]=tmp_mat[i+1][j+1];  // copy of the tmp matrix in the final matrix

}//poisson filter



// save the horizontal wavelet coefficients
public void save_coeff(double[] scale1, double[] scale2, double[] scale3, Vector coff, int l, int wid){

   int size = coff.size();
   double[] tmp1 = (double[])coef.elementAt( size -1 );
   double[] tmp2 = (double[])coef.elementAt( size -2 );
   double[] tmp3 = (double[])coef.elementAt( size -3 );

   for(int i=0;i<tmp1.length;i++)  scale1[i+l*wid/2]=tmp1[i];

   for(int i=0;i<tmp2.length;i++)  scale2[i+l*wid/4]=tmp2[i];

   for(int i=0;i<tmp3.length;i++)  scale3[i+l*wid/8]=tmp3[i];

  }// save h



// save the vertical wavelet coefficients
public void save_coeff_v(double[] scale1v, double[] scale2v, double[] scale3v, Vector coff, int l, int wid){

   int size = coff.size();
   double[] tmp1 = (double[])coef.elementAt( size -1 );
   double[] tmp2 = (double[])coef.elementAt( size -2 );
   double[] tmp3 = (double[])coef.elementAt( size -3 );

   for(int i=0;i<tmp1.length;i++)  scale1v[l+i*wid]=tmp1[i];

   for(int i=0;i<tmp2.length;i++)  scale2v[l+i*wid]=tmp2[i];

   for(int i=0;i<tmp3.length;i++)  scale3v[l+i*wid]=tmp3[i];

  }//save v



// calculate the standard deviation of an array
  public double stdev_calc(double[] tab){

    double mean_t=0;
    double sigma_t=0;
    double stdev_t=0;

    for(int m=0;m<tab.length;m++) {mean_t+=tab[m];}
    mean_t=mean_t/tab.length;

    for(int n=0;n<tab.length;n++) {sigma_t+=(tab[n]-mean_t)*(tab[n]-mean_t);}
    sigma_t=sigma_t/tab.length;
    stdev_t=Math.sqrt(sigma_t);

   return stdev_t;

}//stdev_calc



// sort an array of doubles
public void sort_array(double array[], int size){

int i,j;
double  tmp1,tmp2;

 for(i=0;i<=size-2;i++){
   for(j=i+1;j<=size-1;j++){
      if (array[i]>=array[j]){
       tmp1=array[i];
       tmp2=array[j];
       array[j]=tmp1;
       array[i]=tmp2;
     }
  }
}

}//sort


//resize the input image if necessary
public ImageProcessor resize_image(int width1, int height1,ImageProcessor ip){


  if( width1>4096 && height1 >4096) {IJ.error("Invalid size");}

  else if(width1<32 && height1 <32) {
    ImagePlus tmp = NewImage.createByteImage("tmp",32,32,1,NewImage.FILL_BLACK);
    ImageProcessor tmp_ip = tmp.getProcessor();
    tmp_ip.copyBits(ip,0,0,Blitter.COPY);
    tmp_ip.copyBits(ip,width1+1,0,Blitter.COPY);
    tmp_ip.copyBits(ip,0,height1+1,Blitter.COPY);
    tmp_ip.copyBits(ip,width1+1,height1+1,Blitter.COPY);
    ip=tmp_ip;
  }

  else if(width1<64 && height1 <64) {
    ImagePlus tmp = NewImage.createByteImage("tmp",64,64,1,NewImage.FILL_BLACK);
    ImageProcessor tmp_ip = tmp.getProcessor();
    tmp_ip.copyBits(ip,0,0,Blitter.COPY);
    tmp_ip.copyBits(ip,width1+1,0,Blitter.COPY);
    tmp_ip.copyBits(ip,0,height1+1,Blitter.COPY);
    tmp_ip.copyBits(ip,width1+1,height1+1,Blitter.COPY);
    ip=tmp_ip;
  }

  else if(width1<128 && height1 <128){
    ImagePlus tmp = NewImage.createByteImage("tmp",128,128,1,NewImage.FILL_BLACK);
    ImageProcessor tmp_ip = tmp.getProcessor();
    tmp_ip.copyBits(ip,0,0,Blitter.COPY);
    tmp_ip.copyBits(ip,width1+1,0,Blitter.COPY);
    tmp_ip.copyBits(ip,0,height1+1,Blitter.COPY);
    tmp_ip.copyBits(ip,width1+1,height1+1,Blitter.COPY);
    ip=tmp_ip;
      }

  else if(width1<256 && height1 <256) {
    ImagePlus tmp = NewImage.createByteImage("tmp",256,256,1,NewImage.FILL_BLACK);
    ImageProcessor tmp_ip = tmp.getProcessor();
    tmp_ip.copyBits(ip,0,0,Blitter.COPY);
    tmp_ip.copyBits(ip,width1+1,0,Blitter.COPY);
    tmp_ip.copyBits(ip,0,height1+1,Blitter.COPY);
    tmp_ip.copyBits(ip,width1+1,height1+1,Blitter.COPY);
    ip=tmp_ip;
  }

  else if(width1<512 && height1 <512) {
    ImagePlus tmp = NewImage.createByteImage("tmp",512,512,1,NewImage.FILL_BLACK);
    ImageProcessor tmp_ip = tmp.getProcessor();
    tmp_ip.copyBits(ip,0,0,Blitter.COPY);
    tmp_ip.copyBits(ip,width1+1,0,Blitter.COPY);
    tmp_ip.copyBits(ip,0,height1+1,Blitter.COPY);
    tmp_ip.copyBits(ip,width1+1,height1+1,Blitter.COPY);
    ip=tmp_ip;
  }

  else if(width1<1024 && height1 <1024){
    ImagePlus tmp = NewImage.createByteImage("tmp",1024,1024,1,NewImage.FILL_BLACK);
    ImageProcessor tmp_ip = tmp.getProcessor();
    tmp_ip.copyBits(ip,0,0,Blitter.COPY);
    tmp_ip.copyBits(ip,width1+1,0,Blitter.COPY);
    tmp_ip.copyBits(ip,0,height1+1,Blitter.COPY);
    tmp_ip.copyBits(ip,width1+1,height1+1,Blitter.COPY);
    ip=tmp_ip;
  }

  else if(width1<2048 && height1 <2048) {
    ImagePlus tmp = NewImage.createByteImage("tmp",2048,2048,1,NewImage.FILL_BLACK);
    ImageProcessor tmp_ip = tmp.getProcessor();
    tmp_ip.copyBits(ip,0,0,Blitter.COPY);
    tmp_ip.copyBits(ip,width1+1,0,Blitter.COPY);
    tmp_ip.copyBits(ip,0,height1+1,Blitter.COPY);
    tmp_ip.copyBits(ip,width1+1,height1+1,Blitter.COPY);
    ip=tmp_ip;
  }

  else if (width1<4096 && height1 <4096) {
    ImagePlus tmp = NewImage.createByteImage("tmp",4096,4096,1,NewImage.FILL_BLACK);
    ImageProcessor tmp_ip = tmp.getProcessor();
    tmp_ip.copyBits(ip,0,0,Blitter.COPY);
    tmp_ip.copyBits(ip,width1+1,0,Blitter.COPY);
    tmp_ip.copyBits(ip,0,height1+1,Blitter.COPY);
    tmp_ip.copyBits(ip,width1+1,height1+1,Blitter.COPY);
    ip=tmp_ip;
  }


  return ip;

}

public void disp_coeff(int width, int height, int width1, int height1,double[] pix_d){


double line[]= new double[width];
double col[] = new double[height];

//horizontal scale1 coefficients
   ImagePlus scale1 = NewImage.createByteImage("Horizontal scale 1",width/2,height,1,NewImage.FILL_BLACK);
   ImageProcessor scale1_ip = scale1.getProcessor();
   byte[] pix_scale1 =(byte[])scale1_ip.getPixels();
   double[] scale1_save= new double[width*height/2];
   for(int i=0;i<pix_scale1.length;i++){
     pix = 0xff & pix_scale1[i];
     scale1_save[i] = pix;
   }


//horizontal scale2 coefficients
   ImagePlus scale2 = NewImage.createByteImage("Horizontal scale 2",width/4,height,1,NewImage.FILL_BLACK);
   ImageProcessor scale2_ip = scale2.getProcessor();
   byte[] pix_scale2 =(byte[])scale2_ip.getPixels();
   double[] scale2_save= new double[width*height/4];
   for(int i=0;i<pix_scale2.length;i++){
     pix = 0xff & pix_scale2[i];
     scale2_save[i] = pix;
   }


//horizontal scale3 coefficients
   ImagePlus scale3 = NewImage.createByteImage("Horizontal scale 3",width/8,height,1,NewImage.FILL_BLACK);
   ImageProcessor scale3_ip = scale3.getProcessor();
   byte[] pix_scale3 =(byte[])scale3_ip.getPixels();
   double[] scale3_save= new double[width*height/8];
   for(int i=0;i<pix_scale3.length;i++){
     pix = 0xff & pix_scale3[i];
     scale3_save[i] = pix;
   }


//vertical scale1 coefficients
   ImagePlus scale1v = NewImage.createByteImage("Vertical scale 1",width,height/2,1,NewImage.FILL_BLACK);
   ImageProcessor scale1v_ip = scale1v.getProcessor();
   byte[] pix_scale1v =(byte[])scale1v_ip.getPixels();
   double[] scale1v_save= new double[width*height/2];
   for(int i=0;i<pix_scale1v.length;i++){
     pix = 0xff & pix_scale1v[i];
     scale1v_save[i] = pix;
   }


//vertical scale2 coefficients
   ImagePlus scale2v = NewImage.createByteImage("Vertical scale 2",width,height/4,1,NewImage.FILL_BLACK);
   ImageProcessor scale2v_ip = scale2v.getProcessor();
   byte[] pix_scale2v =(byte[])scale2v_ip.getPixels();
   double[] scale2v_save= new double[width*height/4];
   for(int i=0;i<pix_scale2v.length;i++){
     pix = 0xff & pix_scale2v[i];
     scale2v_save[i] = pix;
   }


//vertical scale3 coefficients
   ImagePlus scale3v = NewImage.createByteImage("Vertical scale 3",width,height/8,1,NewImage.FILL_BLACK);
   ImageProcessor scale3v_ip = scale3v.getProcessor();
   byte[] pix_scale3v =(byte[])scale3v_ip.getPixels();
   double[] scale3v_save= new double[width*height/8];
   for(int i=0;i<pix_scale3v.length;i++){
     pix = 0xff & pix_scale3v[i];
     scale3v_save[i] = pix;
   }


for(int l=0;l<height;l++){         // loop for every row

   for(int k=width*l; k < width*l + width; k++ ) {line[k-l*width]=pix_d[k];}
   Haar_calc(line);
   save_coeff(scale1_save, scale2_save, scale3_save,coef,l,width);

}//for row


//update horizontal scale 1
for(int f=0;f<scale1_save.length;f++){
   if(scale1_save[f]<0) scale1_save[f]=0;
   if(scale1_save[f]>255) scale1_save[f]=255;}
for(int f=0;f<height*width/2;f++){
     pix_scale1[f]= (byte)scale1_save[f];
  }

//update horizontal scale 2
for(int f=0;f<scale2_save.length;f++){
   if(scale2_save[f]<0) scale2_save[f]=0;
   if(scale2_save[f]>255) scale2_save[f]=255;}
for(int f=0;f<height*width/4;f++){
     pix_scale2[f]= (byte)scale2_save[f];
  }

//update horizontal scale 3
for(int f=0;f<scale3_save.length;f++){
   if(scale3_save[f]<0) scale3_save[f]=0;
   if(scale3_save[f]>255) scale3_save[f]=255;}
for(int f=0;f<height*width/8;f++){
     pix_scale3[f]= (byte)scale3_save[f];
  }

for(int l=0;l<width;l++){        // loop for every column

  for(int k=0; k < height; k++ ){col[k]=pix_d[k*height+l];}
  Haar_calc(col);
  save_coeff_v(scale1v_save, scale2v_save, scale3v_save,coef,l,width);

   }//for col


   //update vertical scale 1
  for(int f=0;f<scale1v_save.length;f++){
     if(scale1v_save[f]<0) scale1v_save[f]=0;
     if(scale1v_save[f]>255) scale1v_save[f]=255;}
  for(int f=0;f<height*width/2;f++){
       pix_scale1v[f]= (byte)scale1v_save[f];
    }

    //update vertical scale 2
  for(int f=0;f<scale2v_save.length;f++){
     if(scale2v_save[f]<0) scale2v_save[f]=0;
     if(scale2v_save[f]>255) scale2v_save[f]=255;}
  for(int f=0;f<height*width/4;f++){
       pix_scale2v[f]= (byte)scale2v_save[f];
    }

    //update vertical scale 3
  for(int f=0;f<scale3v_save.length;f++){
     if(scale3v_save[f]<0) scale3v_save[f]=0;
     if(scale3v_save[f]>255) scale3v_save[f]=255;}
  for(int f=0;f<height*width/8;f++){
       pix_scale3v[f]= (byte)scale3v_save[f];
  }


  // display horizontal wavelet coefficients

 if(width1 != width || height1 != height) {

  ImagePlus scale1_re = NewImage.createByteImage("Horizontal scale 1",width1/2,height1,1,NewImage.FILL_BLACK);
  ImageProcessor scale1_re_ip = scale1_re.getProcessor();
  scale1_re_ip.copyBits(scale1_ip,0,0,Blitter.COPY);
  scale1_re.show();
  scale1_re.updateAndDraw();
  IJ.run("3-3-2 RGB");

  ImagePlus scale2_re = NewImage.createByteImage("Horizontal scale 2",width1/4,height1,1,NewImage.FILL_BLACK);
  ImageProcessor scale2_re_ip = scale2_re.getProcessor();
  scale2_re_ip.copyBits(scale2_ip,0,0,Blitter.COPY);
  scale2_re.show();
  scale2_re.updateAndDraw();
  IJ.run("3-3-2 RGB");

  ImagePlus scale3_re = NewImage.createByteImage("Horizontal scale 3",width1/8,height1,1,NewImage.FILL_BLACK);
  ImageProcessor scale3_re_ip = scale3_re.getProcessor();
  scale3_re_ip.copyBits(scale3_ip,0,0,Blitter.COPY);
  scale3_re.show();
  scale3_re.updateAndDraw();
  IJ.run("3-3-2 RGB");

 }

  else{
    scale1.show();
    scale1.updateAndDraw();
    IJ.run("3-3-2 RGB");
    scale2.show();
    scale2.updateAndDraw();
    IJ.run("3-3-2 RGB");
    scale3.show();
    scale3.updateAndDraw();
    IJ.run("3-3-2 RGB");
  }



// display vertical wavelet coefficients


 if(width1 != width || height1 != height) {

  ImagePlus scale1v_re = NewImage.createByteImage("Vetical scale 1",width1,height1/2,1,NewImage.FILL_BLACK);
  ImageProcessor scale1v_re_ip = scale1v_re.getProcessor();
  scale1v_re_ip.copyBits(scale1v_ip,0,0,Blitter.COPY);
  scale1v_re.show();
  scale1v_re.updateAndDraw();
  IJ.run("3-3-2 RGB");

  ImagePlus scale2v_re = NewImage.createByteImage("Vertical scale 2",width1,height1/4,1,NewImage.FILL_BLACK);
  ImageProcessor scale2v_re_ip = scale2v_re.getProcessor();
  scale2v_re_ip.copyBits(scale2v_ip,0,0,Blitter.COPY);
  scale2v_re.show();
  scale2v_re.updateAndDraw();
  IJ.run("3-3-2 RGB");

  ImagePlus scale3v_re = NewImage.createByteImage("Vertical scale 3",width1,height1/8,1,NewImage.FILL_BLACK);
  ImageProcessor scale3v_re_ip = scale3v_re.getProcessor();
  scale3v_re_ip.copyBits(scale3v_ip,0,0,Blitter.COPY);
  scale3v_re.show();
  scale3v_re.updateAndDraw();
  IJ.run("3-3-2 RGB");

 }

  else{
    scale1v.show();
    scale1v.updateAndDraw();
    IJ.run("3-3-2 RGB");
    scale2v.show();
    scale2v.updateAndDraw();
    IJ.run("3-3-2 RGB");
    scale3v.show();
    scale3v.updateAndDraw();
    IJ.run("3-3-2 RGB");
   }




}




  }//class







