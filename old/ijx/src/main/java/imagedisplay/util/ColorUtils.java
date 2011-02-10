package imagedisplay.util;

import java.awt.*;
import java.util.*;


// Includes:
// HLStoRGB utilities
// 140 colors defined for X Window listed in O'Reilly html pocket reference 87p

public class ColorUtils
{
   int vals[] = new int[6];
   final int RED = 0;
   final int GRN = 1;
   final int BLU = 2;
   final int H = 3;
   final int L = 4;
   final int S = 5;

   float hue = 0.0f;
   float sat = 0.5f;
   float value = 0.5f;

   private ColorUtils () {}


   public Color HLStoRGB (float h, float l, float s) {
      vals[H] = (int) (h * 255);
      vals[L] = (int) (l * 255);
      vals[S] = (int) (s * 255);
      toRGB(vals);
      return new Color(vals[RED], vals[GRN], vals[BLU]);
   }


// From Computer Graphics Principles and Practice, Foley, van Dam,
// Feiner, Hughes
   public double value (double n1, double n2, double hue) {
      if (hue < 0) {
         hue += 360;
      }
      if (hue > 360) {
         hue -= 360;
      }
      if (hue < 60) {
         return n1 + (n2 - n1) * hue / 60.;
      } else if (hue < 180) {
         return n2;
      } else if (hue < 240) {
         return n1 + (n2 - n1) * (240 - hue) / 60.;
      } else {
         return n1;
      }
   }


   public void toRGB (int vals[]) {
      double h, l, s;
      double r, g, b;
      h = vals[H] / 255. * 360;
      l = vals[L] / 255.;
      s = vals[S] / 255.;
      double m1, m2;
      if (l < .5) {
         m2 = l * (1 + s);
      } else {
         m2 = l + s - l * s;
      }
      m1 = 2 * l - m2;
      if (s == 0) {
         r = l;
         g = l;
         b = l;
      } else {
         r = value(m1, m2, h + 120);
         g = value(m1, m2, h);
         b = value(m1, m2, h - 120);
      }
      vals[RED] = (int) (r * 255);
      vals[GRN] = (int) (g * 255);
      vals[BLU] = (int) (b * 255);
   }


   public void fromRGB (int vals[]) {
      double r, g, b;
      double h, l, s;
      r = vals[RED] / 255.;
      g = vals[GRN] / 255.;
      b = vals[BLU] / 255.;
      double min = min(min(r, g), b);
      double max = max(max(r, g), b);
      l = (max + min) / 2;
      if (max == min) {
         s = 0;
         h = 0;
      } else {
         if (l <= 0.5) {
            s = (max - min) / (max + min);
         } else {
            s = (max - min) / (2 - max - min);
         }
         double delta = max - min;
         if (r == max) {
            h = (g - b) / delta;
         } else if (g == max) {
            h = 2 + (b - r) / delta;
         } else {
            h = 4 + (r - g) / delta;
         }
         h /= 6.;
         if (h < 0) {
            h += 1;
         }
      }
      vals[H] = (int) (h * 255);
      vals[L] = (int) (l * 255);
      vals[S] = (int) (s * 255);
   }


//--------------------------------------------
   public double abs (double v) {
      if (v > 0) {
         return v;
      } else {
         return -v;
      }
   }


   public double max (double a, double b) {
      if (a > b) {
         return a;
      } else {
         return b;
      }
   }


   public double min (double a, double b) {
      if (a < b) {
         return a;
      } else {
         return b;
      }
   }


   public double clip (double v) {
      // Clip to 0..1
      return min(1, max(0, v));
   }


//--------------------------------------------
   public final static Color aliceblue = new Color(240, 248, 255);
   public final static Color antiquewhite = new Color(250, 235, 215);
   public final static Color aqua = new Color(0, 255, 255);
   public final static Color aquamarine = new Color(127, 255, 212);
   public final static Color azure = new Color(240, 255, 255);
   public final static Color beige = new Color(245, 245, 220);
   public final static Color bisque = new Color(255, 228, 196);
   public final static Color black = new Color(0, 0, 0);
   public final static Color blanchedalmond = new Color(255, 255, 205);
   public final static Color blue = new Color(0, 0, 255);
   public final static Color blueviolet = new Color(138, 43, 226);
   public final static Color brown = new Color(165, 42, 42);
   public final static Color burlywood = new Color(222, 184, 135);
   public final static Color cadetblue = new Color(95, 158, 160);
   public final static Color chartreuse = new Color(127, 255, 0);
   public final static Color chocolate = new Color(210, 105, 30);
   public final static Color coral = new Color(255, 127, 80);
   public final static Color cornflowerblue = new Color(100, 149, 237);
   public final static Color cornsilk = new Color(255, 248, 220);
   public final static Color crimson = new Color(220, 20, 60);
   public final static Color cyan = new Color(0, 255, 255);
   public final static Color darkblue = new Color(0, 0, 139);
   public final static Color darkcyan = new Color(0, 139, 139);
   public final static Color darkgoldenrod = new Color(184, 134, 11);
   public final static Color darkgray = new Color(169, 169, 169);
   public final static Color darkgreen = new Color(0, 100, 0);
   public final static Color darkkhaki = new Color(189, 183, 107);
   public final static Color darkmagenta = new Color(139, 0, 139);
   public final static Color darkolivegreen = new Color(85, 107, 47);
   public final static Color darkorange = new Color(255, 140, 0);
   public final static Color darkorchid = new Color(153, 50, 204);
   public final static Color darkred = new Color(139, 0, 0);
   public final static Color darksalmon = new Color(233, 150, 122);
   public final static Color darkseagreen = new Color(143, 188, 143);
   public final static Color darkslateblue = new Color(72, 61, 139);
   public final static Color darkslategray = new Color(47, 79, 79);
   public final static Color darkturquoise = new Color(0, 206, 209);
   public final static Color darkviolet = new Color(148, 0, 211);
   public final static Color deeppink = new Color(255, 20, 147);
   public final static Color deepskyblue = new Color(0, 191, 255);
   public final static Color dimgray = new Color(105, 105, 105);
   public final static Color dodgerblue = new Color(30, 144, 255);
   public final static Color firebrick = new Color(178, 34, 34);
   public final static Color floralwhite = new Color(255, 250, 240);
   public final static Color forestgreen = new Color(34, 139, 34);
   public final static Color fuchsia = new Color(255, 0, 255);
   public final static Color gainsboro = new Color(220, 220, 220);
   public final static Color ghostwhite = new Color(248, 248, 255);
   public final static Color gold = new Color(255, 215, 0);
   public final static Color goldenrod = new Color(218, 165, 32);
   public final static Color gray = new Color(128, 128, 128);
   public final static Color green = new Color(0, 128, 0);
   public final static Color greenyellow = new Color(173, 255, 47);
   public final static Color honeydew = new Color(240, 255, 240);
   public final static Color hotpink = new Color(255, 105, 180);
   public final static Color indianred = new Color(205, 92, 92);
   public final static Color indigo = new Color(75, 0, 130);
   public final static Color ivory = new Color(255, 240, 240);
   public final static Color khaki = new Color(240, 230, 140);
   public final static Color lavender = new Color(230, 230, 250);
   public final static Color lavenderblush = new Color(255, 240, 245);
   public final static Color lawngreen = new Color(124, 252, 0);
   public final static Color lemonchiffon = new Color(255, 250, 205);
   public final static Color lightblue = new Color(173, 216, 230);
   public final static Color lightcoral = new Color(240, 128, 128);
   public final static Color lightcyan = new Color(224, 255, 255);
   public final static Color lightgoldenrodyellow = new Color(250, 250, 210);
   public final static Color lightgreen = new Color(144, 238, 144);
   public final static Color lightgrey = new Color(211, 211, 211);
   public final static Color lightpink = new Color(255, 182, 193);
   public final static Color lightsalmon = new Color(255, 160, 122);
   public final static Color lightseagreen = new Color(32, 178, 170);
   public final static Color lightskyblue = new Color(135, 206, 250);
   public final static Color lightslategray = new Color(119, 136, 153);
   public final static Color lightsteelblue = new Color(176, 196, 222);
   public final static Color lightyellow = new Color(255, 255, 224);
   public final static Color lime = new Color(0, 255, 0);
   public final static Color limegreen = new Color(50, 205, 50);
   public final static Color linen = new Color(250, 240, 230);
   public final static Color magenta = new Color(255, 0, 255);
   public final static Color maroon = new Color(128, 0, 0);
   public final static Color mediumaquamarine = new Color(102, 205, 170);
   public final static Color mediumblue = new Color(0, 0, 205);
   public final static Color mediumorchid = new Color(186, 85, 211);
   public final static Color mediumpurple = new Color(147, 112, 219);
   public final static Color mediumseagreen = new Color(60, 179, 113);
   public final static Color mediumslateblue = new Color(123, 104, 238);
   public final static Color mediumspringgreen = new Color(0, 250, 154);
   public final static Color mediumturquoise = new Color(72, 209, 204);
   public final static Color mediumvioletred = new Color(199, 21, 133);
   public final static Color midnightblue = new Color(25, 25, 112);
   public final static Color mintcream = new Color(245, 255, 250);
   public final static Color mistyrose = new Color(255, 228, 225);
   public final static Color mocassin = new Color(255, 228, 181);
   public final static Color navajowhite = new Color(255, 222, 173);
   public final static Color navy = new Color(0, 0, 128);
   public final static Color oldlace = new Color(253, 245, 230);
   public final static Color olive = new Color(128, 128, 0);
   public final static Color olivedrab = new Color(107, 142, 35);
   public final static Color orange = new Color(255, 165, 0);
   public final static Color orangered = new Color(255, 69, 0);
   public final static Color orchid = new Color(218, 112, 214);
   public final static Color palegoldenrod = new Color(238, 232, 170);
   public final static Color palegreen = new Color(152, 251, 152);
   public final static Color paleturquoise = new Color(175, 238, 238);
   public final static Color palevioletred = new Color(219, 112, 147);
   public final static Color papayawhip = new Color(255, 239, 213);
   public final static Color peachpuff = new Color(255, 218, 185);
   public final static Color peru = new Color(205, 133, 63);
   public final static Color pink = new Color(255, 192, 203);
   public final static Color plum = new Color(221, 160, 221);
   public final static Color powderblue = new Color(176, 224, 230);
   public final static Color purple = new Color(128, 0, 128);
   public final static Color red = new Color(255, 0, 0);
   public final static Color rosybrown = new Color(188, 143, 143);
   public final static Color royalblue = new Color(65, 105, 225);
   public final static Color saddlebrown = new Color(139, 69, 19);
   public final static Color salmon = new Color(250, 128, 114);
   public final static Color sandybrown = new Color(244, 164, 96);
   public final static Color seagreen = new Color(46, 139, 87);
   public final static Color seashell = new Color(255, 245, 238);
   public final static Color sienna = new Color(160, 82, 45);
   public final static Color silver = new Color(192, 192, 192);
   public final static Color skyblue = new Color(135, 206, 235);
   public final static Color slateblue = new Color(106, 90, 205);
   public final static Color slategray = new Color(112, 128, 144);
   public final static Color snow = new Color(255, 250, 250);
   public final static Color springgreen = new Color(0, 255, 127);
   public final static Color steelblue = new Color(70, 138, 180);
   public final static Color tan = new Color(210, 180, 140);
   public final static Color teal = new Color(0, 128, 128);
   public final static Color thistle = new Color(216, 191, 216);
   public final static Color tomato = new Color(253, 99, 71);
   public final static Color turquoise = new Color(64, 224, 208);
   public final static Color violet = new Color(238, 130, 238);
   public final static Color wheat = new Color(245, 222, 179);
   public final static Color white = new Color(255, 255, 255);
   public final static Color whitesmoke = new Color(245, 245, 245);
   public final static Color yellow = new Color(255, 255, 0);
   public final static Color yellowgreen = new Color(154, 205, 50);

   private static HashMap _colors;

   private static Object[][] data = {
                                    {"aliceblue", aliceblue}, {"antiquewhite",
                                    antiquewhite}, {"aqua", aqua},
                                    {"aquamarine", aquamarine}, {"azure",
                                    azure}, {"beige", beige}, {"bisque", bisque},
                                    {"black", black}, {"blanchedalmond",
                                    blanchedalmond}, {"blue", blue},
                                    {"blueviolet",
                                    blueviolet}, {"brown", brown}, {"burlywood",
                                    burlywood}, {"cadetblue",
                                    cadetblue}, {"chartreuse", chartreuse},
                                    {"chocolate", chocolate}, {"coral", coral},
                                    {"cornflowerblue", cornflowerblue},
                                    {"cornsilk",
                                    cornsilk}, {"crimson", crimson}, {"cyan",
                                    cyan}, {"darkblue", darkblue}, {"darkcyan",
                                    darkcyan}, {"darkgoldenrod", darkgoldenrod},
                                    {"darkgray",
                                    darkgray}, {"darkgreen", darkgreen},
                                    {"darkkhaki", darkkhaki}, {"darkmagenta",
                                    darkmagenta}, {"darkolivegreen",
                                    darkolivegreen}, {"darkorange", darkorange},
                                    {"darkorchid", darkorchid}, {"darkred",
                                    darkred}, {"darksalmon", darksalmon},
                                    {"darkseagreen", darkseagreen},
                                    {"darkslateblue", darkslateblue},
                                    {"darkslategray", darkslategray},
                                    {"darkturquoise", darkturquoise},
                                    {"darkviolet", darkviolet}, {"deeppink",
                                    deeppink}, {"deepskyblue", deepskyblue},
                                    {"dimgray", dimgray}, {"dodgerblue",
                                    dodgerblue}, {"firebrick", firebrick},
                                    {"floralwhite",
                                    floralwhite}, {"forestgreen", forestgreen},
                                    {"fuchsia", fuchsia}, {"gainsboro",
                                    gainsboro}, {"ghostwhite", ghostwhite},
                                    {"gold", gold}, {"goldenrod", goldenrod},
                                    {"gray", gray}, {"green", green},
                                    {"greenyellow", greenyellow}, {"honeydew",
                                    honeydew}, {"hotpink", hotpink},
                                    {"indianred", indianred}, {"indigo", indigo},
                                    {"ivory", ivory}, {"khaki",
                                    khaki}, {"lavender", lavender},
                                    {"lavenderblush", lavenderblush},
                                    {"lawngreen", lawngreen}, {"lemonchiffon",
                                    lemonchiffon}, {"lightblue",
                                    lightblue}, {"lightcoral", lightcoral},
                                    {"lightcyan", lightcyan},
                                    {"lightgoldenrodyellow",
                                    lightgoldenrodyellow}, {"lightgreen",
                                    lightgreen}, {"lightgrey", lightgrey},
                                    {"lightpink", lightpink}, {"lightsalmon",
                                    lightsalmon}, {"lightseagreen",
                                    lightseagreen}, {"lightskyblue",
                                    lightskyblue}, {"lightslategray",
                                    lightslategray}, {"lightsteelblue",
                                    lightsteelblue}, {"lightyellow",
                                    lightyellow}, {"lime", lime}, {"limegreen",
                                    limegreen}, {"linen", linen}, {"magenta",
                                    magenta}, {"maroon", maroon},
                                    {"mediumaquamarine", mediumaquamarine},
                                    {"mediumblue",
                                    mediumblue}, {"mediumorchid", mediumorchid},
                                    {"mediumpurple",
                                    mediumpurple}, {"mediumseagreen",
                                    mediumseagreen}, {"mediumslateblue",
                                    mediumslateblue}, {"mediumspringgreen",
                                    mediumspringgreen}, {"mediumturquoise",
                                    mediumturquoise}, {"mediumvioletred",
                                    mediumvioletred}, {"midnightblue",
                                    midnightblue}, {"mintcream", mintcream},
                                    {"mistyrose",
                                    mistyrose}, {"moccasin", mocassin},
                                    {"navajowhite", navajowhite}, {"navy",
                                    navy}, {"oldlace", oldlace}, {"olive",
                                    olive}, {"olivedrab", olivedrab}, {"orange",
                                    orange}, {"orangered", orangered},
                                    {"orchid", orchid}, {"palegoldenrod",
                                    palegoldenrod}, {"palegreen", palegreen},
                                    {"paleturquoise", paleturquoise},
                                    {"palevioletred", palevioletred},
                                    {"papayawhip", papayawhip}, {"peachpuff",
                                    peachpuff}, {"peru", peru}, {"pink", pink},
                                    {"plum", plum}, {"powderblue", powderblue},
                                    {"purple",
                                    purple}, {"red", red}, {"rosybrown",
                                    rosybrown}, {"royalblue", royalblue},
                                    {"saddlebrown", saddlebrown}, {"salmon",
                                    salmon}, {"sandybrown",
                                    sandybrown}, {"seagreen", seagreen},
                                    {"seashell", seashell}, {"sienna",
                                    sienna}, {"silver", silver}, {"skyblue",
                                    skyblue}, {"slateblue",
                                    slateblue}, {"slategray", slategray},
                                    {"snow", snow}, {"springgreen",
                                    springgreen}, {"steelblue", steelblue},
                                    {"tan", tan}, {"teal", teal}, {"thistle",
                                    thistle}, {"tomato", tomato}, {"turquoise",
                                    turquoise}, {"violet", violet}, {"wheat",
                                    wheat}, {"white", white}, {"whitesmoke",
                                    whitesmoke}, {"yellow", yellow},
                                    {"yellowgreen", yellowgreen},
   };

   public static HashMap getColors () {
      return _colors;
   }


   public static Color findColor (String key) {
      return (Color) _colors.get(key);
   }


   static {
      _colors = new HashMap();
      for (int i = 0; i < data.length; i++) {
         Object row[] = data[i];
         _colors.put(row[0], row[1]);
      }
   }


   /**
      Generates a series of colors such that the
      distribution of the colors is (fairly) evenly spaced
      throughout the color spectrum. This is especially
      useful for generating unique color codes to be used
      in a legend or on a graph.

      @param numColors the number of colors to generate
      @return an array of Color objects representing the
      colors in the table
    */
   private Color[] createColorCodeTable (int numColors) {
      Color[] table = new Color[numColors];

      if (numColors == 1) {
         // Special case for only one color
         table[0] = Color.red;
      } else {
         float hueMax = (float) 0.85;
         float sat = (float) 0.8;

         for (int i = 0; i < numColors; i++) {
            float hue = hueMax * i / (numColors - 1);

            // Here we interleave light colors and dark colors
            // to get a wider distribution of colors.
            if (i % 2 == 0) {
               table[i] = Color.getHSBColor(hue, sat, (float) 0.9);
            } else {
               table[i] = Color.getHSBColor(hue, sat, (float) 0.7);
            }
         }
      }

      return table;
   }


//
//---------------------------------------------------------------------------
   public static void main (String[] args) {
      Iterator i = _colors.values().iterator();
      while (i.hasNext()) {
         Color c = (Color) i.next();
         System.out.println(c);
      }

   }

}
