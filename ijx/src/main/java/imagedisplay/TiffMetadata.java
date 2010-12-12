/*
 * TiffMetadataStruct.java
 *
 * Created on February 17, 2006, 1:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package imagedisplay;
/*
 * TiffMetadataStruct.java
 *
 * Created on February 16, 2006, 10:24 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import com.sun.media.imageio.plugins.tiff.TIFFTagSet;

/**
 *
 * @author GBH
 */
public class TiffMetadata {
   //TIFFTagSet
   //TIFFTag.

   //BaselineTIFFTagSet.TAG_IMAGE_DESCRIPTION
   /**
    * The DOM element ID (tag) for a TIFF Image File Directory
    */
   public static final String TIFF_IFD_TAG = "TIFFIFD";
   /**
    * The DOM element ID (tag) for a TIFF Field
    */
   public static final String TIFF_FIELD_TAG = "TIFFField";
   /**
    * The DOM element ID (tag) for a set of TIFF Double values
    */
   public static final String TIFF_DOUBLES_TAG = "TIFFDoubles";
   /**
    * The DOM element ID (tag) for a single TIFF double. The
    * value is stored in an attribute named "value"
    */
   public static final String TIFF_DOUBLE_TAG = "TIFFDouble";
   /**
    * The DOM element ID (tag) for a set of TIFF Short values
    */
   public static final String TIFF_SHORTS_TAG = "TIFFShorts";
   /**
    * The DOM element ID (tag) for a single TIFF Short value. The
    * value is stored in an attribute named "value"
    */
   public static final String TIFF_SHORT_TAG = "TIFFShort";
   /**
    * The DOM element ID (tag) for a set of TIFF Ascii values
    */
   public static final String TIFF_ASCIIS_TAG = "TIFFAsciis";
   /**
    * The DOM element ID (tag) for a single TIFF Ascii value
    */
   public static final String TIFF_ASCII_TAG = "TIFFAscii";
   
   /**
    * The DOM attribute name for a TIFF Field Tag (number)
    */
   public static final String NUMBER_ATTR = "number";
   /**
    * The DOM attribute name for a TIFF Entry value (whether Short,
    * Double, or Ascii)
    */
   public static final String VALUE_ATTR = "value";
   
   /**
    * The root of the metadata DOM tree
    */
   private IIOMetadataNode myRootNode;
   private IIOMetadata imageMetadata;
   /**
    * The constructor builds a metadata adapter for the image metadata
    * root IIOMetadataNode.
    *
    * @param imageNode The image metadata
    */
   
   public TiffMetadata(IIOMetadata imageMetadata) {
      this.imageMetadata = imageMetadata;
      String formatName = imageMetadata.getNativeMetadataFormatName();
      myRootNode = (IIOMetadataNode)imageMetadata.getAsTree(formatName);
   }
   

   public String getImageDescription() {
         //BaselineTIFFTagSet.TAG_IMAGE_DESCRIPTION
      return "";
   }
   
   
   public int getNumericTag(int tag) {
      IIOMetadataNode keyDir = getTiffField(tag);
      if(keyDir == null) {
         throw new UnsupportedOperationException("directory does not exist");}
      
      IIOMetadataNode tiffShortsNode = (IIOMetadataNode)keyDir.getFirstChild();
      NodeList keys = tiffShortsNode.getElementsByTagName(TIFF_SHORT_TAG);
      Node n = keys.item(0);
      int value = getIntValueAttribute(n);
      return value;
   }
   /**
    * Gets the value attribute of the given Node.
    *
    * @param node A Node containing a value attribute, for example the
    * node &ltTIFFShort value=&quot123&quot&gt
    * @return A String containing the text from the value attribute.
    * In the above example, the string would be 123
    */
   
   protected String getValueAttribute(Node node) {
      return node.getAttributes().getNamedItem(VALUE_ATTR).getNodeValue();
   }
//   public String getValueAttribute(Node node) {
//          NamedNodeMap attributes = node.getAttributes();
//        int numAttributes = attributes.getLength();
//        for(int i = 0; i < numAttributes; i++) {
//            Node attribute = attributes.item(i);
//            if(i > 0) {
//                System.out.println("");
//                indent(0, node.getNodeName().length()+1);
//            }
//            System.out.print(" "+attribute.getNodeName()+"=");
//            System.out.print("\""+attribute.getNodeValue()+"\"");
//
   
   /**
    * Gets the value attribute's contents and parses it as an int
    */
   public int getIntValueAttribute(Node node) {
      return Integer.parseInt(getValueAttribute(node));
   }
   
   /**
    * Gets a TIFFField node with the given tag number. This is done by
    * searching for a TIFFField with attribute number whose value is
    * the specified tag value.
    */
   public IIOMetadataNode getTiffField(int tag) {
      IIOMetadataNode result = null;
      IIOMetadataNode tiffDirectory = getTiffDirectory();
      NodeList children = tiffDirectory.getElementsByTagName(TIFF_FIELD_TAG);
      // embed the exit condition in the for loop
      for (int i = 0; i < children.getLength() && result == null; i++) {
         // search through all the TIFF fields to find the one with the
         // given tag value
         Node child = children.item(i);
         Node number = child.getAttributes().getNamedItem(NUMBER_ATTR);
         if (number != null) {
            int num = Integer.parseInt(number.getNodeValue());
            if (num == tag) {
               result = (IIOMetadataNode) child;
            }
         }
      }
      return result;
   }
   
   /**
    * Gets the TIFFIFD (image file directory) node.
    */
   public IIOMetadataNode getTiffDirectory() {
      // there should only be one, and it should be the only node
      // in the metadata, so just get it.
      return (IIOMetadataNode) myRootNode.getFirstChild();
   }
   
   /**
    * Gets an array of int values stored in a TIFFShorts element that
    * contains a sequence of TIFFShort values.
    *
    * @param tiffField An IIOMetadataNode pointing to a TIFFField
    * element that contains a TIFFShorts element.
    */
   public int[] getTiffShorts(IIOMetadataNode tiffField) {
      IIOMetadataNode shortsElement = (IIOMetadataNode) tiffField.getFirstChild();
      NodeList shorts = shortsElement.getElementsByTagName(TIFF_SHORT_TAG);
      int[] result = new int[shorts.getLength()];
      for (int i = 0; i < shorts.getLength(); i++) {
         Node node = shorts.item(i);
         result[i] = getIntValueAttribute(node);
      }
      return result;
   }
   
   /**
    * Gets a single TIFFShort value at the given index.
    *
    * @param tiffField An IIOMetadataNode pointing to a TIFFField
    * element that contains a TIFFShorts element.
    * @param index The 0-based index of the desired short value
    */
   public int getTiffShort(IIOMetadataNode tiffField, int index) {
      IIOMetadataNode shortsElement = (IIOMetadataNode) tiffField.getFirstChild();
      NodeList shorts = shortsElement.getElementsByTagName(TIFF_SHORT_TAG);
      Node node = shorts.item(index);
      int result = getIntValueAttribute(node);
      return result;
   }
   
   /**
    * Gets an array of double values from a TIFFDoubles TIFFField.
    *
    * @param tiffField An IIOMetadataNode pointing to a TIFFField
    * element that contains a TIFFDoubles element.
    */
   public double[] getTiffDoubles(IIOMetadataNode tiffField) {
      IIOMetadataNode doublesElement = (IIOMetadataNode) tiffField.getFirstChild();
      NodeList doubles = doublesElement.getElementsByTagName(TIFF_DOUBLE_TAG);
      double[] result = new double[doubles.getLength()];
      for (int i = 0; i < doubles.getLength(); i++) {
         Node node = doubles.item(i);
         result[i] = Double.parseDouble(getValueAttribute(node));
      }
      return result;
   }
   
   /**
    * Gets a single double value at the specified index from a sequence
    * of TIFFDoubles
    *
    * @param tiffField An IIOMetadataNode pointing to a TIFFField
    * element that contains a TIFFDoubles element.
    */
   public double getTiffDouble(IIOMetadataNode tiffField, int index) {
      IIOMetadataNode doublesElement = (IIOMetadataNode) tiffField.getFirstChild();
      NodeList doubles = doublesElement.getElementsByTagName(TIFF_DOUBLE_TAG);
      Node node = doubles.item(index);
      double result = Double.parseDouble(getValueAttribute(node));
      return result;
   }
   
   /**
    * Gets a portion of a TIFFAscii string with the specified start
    * character and length;
    *
    * @param tiffField An IIOMetadataNode pointing to a TIFFField
    * element that contains a TIFFAsciis element. This element should
    * contain a single TiffAscii element.
    * @return A substring of the value contained in the TIFFAscii node,
    * with the final '|' character removed.
    */
   public String getTiffAscii(IIOMetadataNode tiffField, int start, int length) {
      IIOMetadataNode asciisElement = (IIOMetadataNode) tiffField.getFirstChild();
      NodeList asciis = asciisElement.getElementsByTagName(TIFF_ASCII_TAG);
      // there should be only one, so get the first
      Node node = asciis.item(0);
      // GeoTIFF specification places a vertical bar '|' in place of \0
      // null delimiters so drop off the vertical bar for Java Strings
      String result = getValueAttribute(node).substring(start, length - 1);
      return result;
   }
   
    public String getTIFF_FIELD_TAG() {
      return TIFF_FIELD_TAG;
   }
    
    
   //=======================================================================
   // Display
   
   public static void displayIIOMetadata(IIOMetadata meta) {
      displayMetadata(meta.getAsTree("javax_imageio_1.0"), 0);
   }
   public static void displayIIOMetadataNative(IIOMetadata meta) {
      displayMetadata(meta.getAsTree(meta.getNativeMetadataFormatName()), 0);
   }
   
   static void indent(int level) {
      for (int i = 0; i < level; i++) {
         System.out.print("  ");
      }
   }
   
   static void displayMetadata(Node node, int level) {
      indent(level); // emit open tag
      System.out.print("<" + node.getNodeName());
      NamedNodeMap map = node.getAttributes();
      if (map != null) { // print attribute values
         int length = map.getLength();
         for (int i = 0; i < length; i++) {
            Node attr = map.item(i);
            System.out.print(" " + attr.getNodeName() + "=\"" +
                  attr.getNodeValue() + "\"");
         }
      }
      Node child = node.getFirstChild();
      if (child != null) {
         System.out.println(">"); // close current tag
         while (child != null) { // emit child tags recursively
            displayMetadata(child, level + 1);
            child = child.getNextSibling();
         }
         indent(level); // emit close tag
         System.out.println("</" + node.getNodeName() + ">");
      } else {
         System.out.println("/>");
      }
   }

}
