package imagedisplay;


import java.awt.*;
import java.awt.color.*;
import java.awt.image.*;

public class ImgInfoDumper {
   
   static StringBuffer sBuffer = new StringBuffer();
   
   public static String dump(BufferedImage image) {
       sBuffer = new StringBuffer();
      dumpAttributes(image);
      dumpColorModel(image.getColorModel());
      dumpRaster(image.getRaster());
      return sBuffer.toString();
   }
   
   public static void dumpAttributes(BufferedImage image) {
      sBuffer.append("BufferedImage AttibutesS:");
      sBuffer.append("\n"); sBuffer.append("    instanceOf "+image.getClass().getName());
      sBuffer.append("\n"); sBuffer.append("    height="+image.getHeight());
      sBuffer.append("\n"); sBuffer.append("    width="+image.getWidth());
      sBuffer.append("\n"); sBuffer.append("    minX="+image.getMinX());
      sBuffer.append("\n"); sBuffer.append("    minY="+image.getMinY());
      sBuffer.append("\n");
      sBuffer.append("    type="+image.getType()+" (");
      sBuffer.append(getImageTypeString(image.getType()));
      sBuffer.append(')');
      sBuffer.append("\n"); sBuffer.append("    isAlphaPremultiplied="+image.isAlphaPremultiplied());
   }
   
   public static String getImageTypeString(int type) {
      switch(type) {
         case BufferedImage.TYPE_CUSTOM:         return "TYPE_CUSTOM";
         case BufferedImage.TYPE_INT_RGB:        return "TYPE_INT_RGB";
         case BufferedImage.TYPE_INT_ARGB:       return "TYPE_INT_ARGB";
         case BufferedImage.TYPE_INT_ARGB_PRE:   return "TYPE_INT_ARGB_PRE";
         case BufferedImage.TYPE_INT_BGR:        return "TYPE_INT_BGR";
         case BufferedImage.TYPE_3BYTE_BGR:      return "TYPE_3BYTE_BGR";
         case BufferedImage.TYPE_4BYTE_ABGR:     return "TYPE_4BYTE_ABGR";
         case BufferedImage.TYPE_4BYTE_ABGR_PRE: return "TYPE_4BYTE_ABGR_PRE";
         case BufferedImage.TYPE_USHORT_565_RGB: return "TYPE_USHORT_565_RGB";
         case BufferedImage.TYPE_USHORT_555_RGB: return "TYPE_USHORT_555_RGB";
         case BufferedImage.TYPE_BYTE_GRAY:      return "TYPE_BYTE_GRAY";
         case BufferedImage.TYPE_USHORT_GRAY:    return "TYPE_USHORT_GRAY";
         case BufferedImage.TYPE_BYTE_BINARY:    return "TYPE_BYTE_BINARY";
         case BufferedImage.TYPE_BYTE_INDEXED:   return "TYPE_BYTE_INDEXED";
         default:                                return "unknown type?";
      }
   }
   
   public static void dumpColorModel(ColorModel colorModel) {
      dumpColorSpace(colorModel.getColorSpace());
      sBuffer.append("\n"); sBuffer.append("COLOR MODEL:");
      sBuffer.append("\n"); sBuffer.append("    instanceOf "+colorModel.getClass().getName());
      sBuffer.append("\n"); sBuffer.append("    hasAlpha="+colorModel.hasAlpha());
      sBuffer.append("\n"); sBuffer.append("    isAlphaPremultiplied="+colorModel.isAlphaPremultiplied());
      sBuffer.append("\n"); sBuffer.append(  "    transparency="+colorModel.getTransparency()+" (");
      sBuffer.append(getTransparencyString(colorModel.getTransparency()));
      sBuffer.append(')');
      sBuffer.append("\n"); sBuffer.append(  "    transferType="+colorModel.getTransferType()+" (");
      sBuffer.append(getTypeString(colorModel.getTransferType()));
      sBuffer.append(')');
      sBuffer.append("\n"); sBuffer.append("    numComponents="+colorModel.getNumComponents());
      sBuffer.append("\n"); sBuffer.append("    numColorComponents="+colorModel.getNumColorComponents());
      sBuffer.append("\n"); sBuffer.append("    pixelSize(bits/pixel)="+colorModel.getPixelSize());
      for(int i=0, ub=colorModel.getNumComponents(); i<ub; ++i){
         sBuffer.append("\n");
         sBuffer.append("    componentSize[" + i + "]="+colorModel.getComponentSize(i));
      }
      if (colorModel instanceof IndexColorModel)
         dumpIndexColorModel((IndexColorModel)colorModel);
      else if (colorModel instanceof PackedColorModel)
         dumpPackedColorModel((PackedColorModel)colorModel);
   }
   
   public static void dumpIndexColorModel(IndexColorModel colorModel) {
      sBuffer.append("\n"); sBuffer.append("    mapSize="+colorModel.getMapSize());
      sBuffer.append("\n"); sBuffer.append("    isValid="+colorModel.isValid());
      sBuffer.append("\n"); sBuffer.append("    transparentPixel="+colorModel.getTransparentPixel());
   }
   
   public static void dumpPackedColorModel(PackedColorModel colorModel) {
      int[] masks = colorModel.getMasks();
      for(int i=0; i<masks.length; ++i) {
         sBuffer.append("\n"); sBuffer.append("    masks[" + i + "]=" + Integer.toHexString(masks[i]));
      }
   }
   
   public static String getTransparencyString(int transparency) {
      switch(transparency) {
         case Transparency.OPAQUE:       return "OPAQUE";
         case Transparency.BITMASK:      return "BITMASK";
         case Transparency.TRANSLUCENT:  return "TRANSLUCENT";
         default:                        return "unknown transparency?";
      }
   }
   
   public static String getTypeString(int type) {
      switch(type) {
         case DataBuffer.TYPE_BYTE:      return "BYTE";
         case DataBuffer.TYPE_USHORT:    return "USHORT";
         case DataBuffer.TYPE_SHORT:     return "SHORT";
         case DataBuffer.TYPE_INT:       return "INT";
         case DataBuffer.TYPE_FLOAT:     return "FLOAT";
         case DataBuffer.TYPE_DOUBLE:    return "DOUBLE";
         default:                        return "unknown type?";
      }
   }
   
   public static void dumpColorSpace(ColorSpace colorSpace) {
      sBuffer.append("\n"); sBuffer.append("COLOR SPACE:");
      sBuffer.append("\n"); sBuffer.append("    instanceOf "+colorSpace.getClass().getName());
      sBuffer.append("\n"); sBuffer.append("    isCS_sRGB="+colorSpace.isCS_sRGB());
        sBuffer.append("\n"); sBuffer.append(  "    type="+colorSpace.getType()+" (");
      sBuffer.append(getColorSpaceTypeString(colorSpace.getType()));
      sBuffer.append(')');
      sBuffer.append("\n"); sBuffer.append("    numComponents="+colorSpace.getNumComponents());
      for(int i=0, ub=colorSpace.getNumComponents(); i<ub; ++i) {
         sBuffer.append("\n");
         sBuffer.append("      name[" + i + "]="+colorSpace.getName(i));
         sBuffer.append(", minValue=" + colorSpace.getMinValue(i));
         sBuffer.append(", maxValue=" + colorSpace.getMaxValue(i));
      }
   }
   
   public static String getColorSpaceTypeString(int type) {
      switch(type) {
         case ColorSpace.TYPE_XYZ:   return "TYPE_XYZ";
         case ColorSpace.TYPE_Lab:   return "TYPE_Lab";
         case ColorSpace.TYPE_Luv:   return "TYPE_Luv";
         case ColorSpace.TYPE_YCbCr: return "TYPE_YCbCr";
         case ColorSpace.TYPE_Yxy:   return "TYPE_Yxy";
         case ColorSpace.TYPE_RGB:   return "TYPE_RGB";
         case ColorSpace.TYPE_GRAY:  return "TYPE_GRAY";
         case ColorSpace.TYPE_HSV:   return "TYPE_HSV";
         case ColorSpace.TYPE_HLS:   return "TYPE_HLS";
         case ColorSpace.TYPE_CMYK:  return "TYPE_CMYK";
         case ColorSpace.TYPE_CMY:   return "TYPE_CMY";
         case ColorSpace.TYPE_2CLR:  return "TYPE_2CLR";
         case ColorSpace.TYPE_3CLR:  return "TYPE_3CLR";
         case ColorSpace.TYPE_4CLR:  return "TYPE_4CLR";
         case ColorSpace.TYPE_5CLR:  return "TYPE_5CLR";
         case ColorSpace.TYPE_6CLR:  return "TYPE_6CLR";
         case ColorSpace.TYPE_7CLR:  return "TYPE_7CLR";
         case ColorSpace.TYPE_8CLR:  return "TYPE_8CLR";
         case ColorSpace.TYPE_9CLR:  return "TYPE_9CLR";
         case ColorSpace.TYPE_ACLR:  return "TYPE_ACLR";
         case ColorSpace.TYPE_BCLR:  return "TYPE_BCLR";
         case ColorSpace.TYPE_CCLR:  return "TYPE_CCLR";
         case ColorSpace.TYPE_DCLR:  return "TYPE_DCLR";
         case ColorSpace.TYPE_ECLR:  return "TYPE_ECLR";
         case ColorSpace.TYPE_FCLR:  return "TYPE_FCLR";
         default:                    return "unknown type?";
      }
   }
   
   public static void dumpRaster(WritableRaster raster) {
      sBuffer.append("\n"); sBuffer.append("RASTER:");
      sBuffer.append("\n"); sBuffer.append("    instanceOf "+raster.getClass().getName());
      sBuffer.append("\n"); sBuffer.append("    height="+raster.getHeight());
      sBuffer.append("\n"); sBuffer.append("    width="+raster.getWidth());
      sBuffer.append("\n"); sBuffer.append("    minX="+raster.getMinX());
      sBuffer.append("\n"); sBuffer.append("    minY="+raster.getMinY());
      sBuffer.append("\n"); sBuffer.append("    sampleModelTranslateX="+raster.getSampleModelTranslateX());
      sBuffer.append("\n"); sBuffer.append("    sampleModelTranslateY="+raster.getSampleModelTranslateY());
      sBuffer.append("\n"); sBuffer.append("    numBands="+raster.getNumBands());
      sBuffer.append("\n"); sBuffer.append("    numDataElements="+raster.getNumDataElements());
      sBuffer.append("\n"); sBuffer.append(  "    transferType="+raster.getTransferType()+" (");
      sBuffer.append(getTypeString(raster.getTransferType()));
      sBuffer.append(')');
      sBuffer.append("\n"); sBuffer.append("    parent is null="+(null==raster.getParent()));
      dumpDataBuffer(raster.getDataBuffer());
      dumpSampleModel(raster.getSampleModel());
   }
   
   public static void dumpDataBuffer(DataBuffer dataBuffer) {
      sBuffer.append("\n"); sBuffer.append("DATA BUFFER:");
      sBuffer.append("\n"); sBuffer.append("    instanceOf "+dataBuffer.getClass().getName());
      sBuffer.append("\n"); sBuffer.append("    dataType="+dataBuffer.getDataType()+" (");
      sBuffer.append(getTypeString(dataBuffer.getDataType()));
      sBuffer.append(')');
      sBuffer.append("\n"); sBuffer.append("    numBanks="+dataBuffer.getNumBanks());
      sBuffer.append("\n"); sBuffer.append("    size="+dataBuffer.getSize());
      for(int i=0, ub=dataBuffer.getNumBanks(); i<ub; ++i) {
         sBuffer.append("\n"); sBuffer.append("    offset["+i+"]="+dataBuffer.getOffsets()[i]);
      }
   }
   
   public static void dumpSampleModel(SampleModel sampleModel) {
      sBuffer.append("\n"); sBuffer.append("SAMPLE MODEL:");
      sBuffer.append("\n"); sBuffer.append("    instanceOf "+sampleModel.getClass().getName());
      sBuffer.append("\n"); sBuffer.append("    height="+sampleModel.getHeight());
      sBuffer.append("\n"); sBuffer.append("    width="+sampleModel.getWidth());
      sBuffer.append("\n"); sBuffer.append("    transferType="+sampleModel.getTransferType()+" (");
      sBuffer.append(getTypeString(sampleModel.getTransferType()));
      sBuffer.append(')');
      sBuffer.append("\n"); sBuffer.append("    dataType="+sampleModel.getDataType()+" (");
      sBuffer.append(getTypeString(sampleModel.getDataType()));
      sBuffer.append(')');
      sBuffer.append("\n"); sBuffer.append("    numBands="+sampleModel.getNumBands());
      sBuffer.append("\n"); sBuffer.append("    numDataElements="+sampleModel.getNumDataElements());
      int[] sampleSize = sampleModel.getSampleSize();
      for(int i=0, ub=sampleSize.length; i<ub; ++i) {
         sBuffer.append("\n"); 
                            sBuffer.append("       sampleSize["+i+"]="+sampleSize[i]);
      }
      if (sampleModel instanceof SinglePixelPackedSampleModel)
         dumpSinglePixelPackedSampleModel((SinglePixelPackedSampleModel)sampleModel);
      else if (sampleModel instanceof MultiPixelPackedSampleModel)
         dumpMultiPixelPackedSampleModel((MultiPixelPackedSampleModel)sampleModel);
      else if (sampleModel instanceof ComponentSampleModel)
         dumpComponentSampleModel((ComponentSampleModel)sampleModel);
   }
   
   public static void dumpSinglePixelPackedSampleModel(SinglePixelPackedSampleModel sampleModel) {
      sBuffer.append("\n"); sBuffer.append("    scanlineStride="+sampleModel.getScanlineStride());
      int[] bitMasks = sampleModel.getBitMasks();
      for(int i=0; i<bitMasks.length; ++i) {
         sBuffer.append("\n"); sBuffer.append("      bitmasks[" + i + "]=" + Integer.toHexString(bitMasks[i]));
      }
   }
   
   public static void dumpMultiPixelPackedSampleModel(MultiPixelPackedSampleModel sampleModel) {
      sBuffer.append("\n"); sBuffer.append("    scanlineStride="+sampleModel.getScanlineStride());
      sBuffer.append("\n"); sBuffer.append("    pixelBitStride="+sampleModel.getPixelBitStride());
   }
   
   public static void dumpComponentSampleModel(ComponentSampleModel sampleModel) {
      sBuffer.append("\n"); sBuffer.append("    scanlineStride="+sampleModel.getScanlineStride());
      sBuffer.append("\n"); sBuffer.append("    pixelStride="+sampleModel.getPixelStride());
      int[] bandOffsets = sampleModel.getBandOffsets();
      for(int i=0; i<bandOffsets.length; ++i) {
         sBuffer.append("\n"); sBuffer.append("      bandOffsets[" + i + "]=" + bandOffsets[i]);
      }
      int[] bankIndices = sampleModel.getBankIndices();
      for(int i=0; i<bankIndices.length; ++i) {
         sBuffer.append("\n"); sBuffer.append("      bankIndices[" + i + "]=" + bankIndices[i]);
      }
   }
}
