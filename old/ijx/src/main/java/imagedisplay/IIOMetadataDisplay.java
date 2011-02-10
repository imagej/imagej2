package imagedisplay;

import org.w3c.dom.Node;
import javax.imageio.metadata.IIOMetadata;
import org.w3c.dom.NamedNodeMap;


public class IIOMetadataDisplay
{
   //public IIOMetadataDisplay () {
   //}


   public static void displayIIOMetadata (IIOMetadata meta) {
      displayMetadata(meta.getAsTree("javax_imageio_1.0"), 0);
   }
 public static void displayIIOMetadataNative (IIOMetadata meta) {
      displayMetadata(meta.getAsTree(meta.getNativeMetadataFormatName()), 0);
   }

   static void indent (int level) {
      for (int i = 0; i < level; i++) {
         System.out.print("  ");
      }
   }


   static void displayMetadata (Node node, int level) {
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
