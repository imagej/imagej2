package imagej.ui;

/*
 * from: http://www.rgagnon.com/javadetails/java-0661.html
 * 
 * It can be useful to have a short version of a long path only for display purpose. 
 * I propose 2 versions to shorten a given path depending of your need.
 * The first version makes a path shorter according to a limit. If the limit is 4, the returned path will contain 
 * 4 parts (directories) then ellipse and finally the filename.

ex :

C:\1\2\3\4\5\test.txt  == C:\1\2\3\4\...\test.txt
\\server\p1\p2\p3\p4\p5\p6  == \\server\p1\p2\p3\p4\...\p6
http://www.rgagnon.com/p1/p2/p3/p4/p5/pb.html  == http://p1/p2/p3/p4/.../pb.html


 * The second accepts a limit corresponding to the total length of the path. 
 * If the limit is 20, the path beginning will be truncated then ellipse and finally 
 * the filename. The result is similar to the Win32 API PathCompactPathExA

ex :

C:\1\2\3\4\5\test.txt        == C:\1\2\3\...test.txt
\\server\p1\p2\p3\p4\p5\p6   == \\server\p1\p2...p6
http://www.rgagnon.com/p1/p2/p3/p4/p5/pb.html  == http://www...pb.html

 */

public class FileUtils {

  public static final int DEFAULT_SHORTENER_THRESHOLD = 4;
  public static final String SHORTENER_BACKSLASH_REGEX = "\\\\";
  public static final String SHORTENER_SLASH_REGEX = "/";
  public static final String SHORTENER_BACKSLASH = "\\";
  public static final String SHORTENER_SLASH = "/";
  public static final String SHORTENER_ELLIPSE = "...";


  public static String pathShortener(String path) {
    return pathShortener(path, FileUtils.DEFAULT_SHORTENER_THRESHOLD);
  }


  /**
   * Return shorter path based on the limited threshold
   * ex. C:/1/2/test.txt return C:/1/.../test.txt if threshold is 1
   * @param path
   * @param threshold
   * @return
   */

  public static String pathShortener(String path, int threshold) {

    String regex = SHORTENER_BACKSLASH_REGEX;
    String sep = SHORTENER_BACKSLASH;

    if (path.indexOf("/") > 0) {
      regex = SHORTENER_SLASH_REGEX;
      sep = SHORTENER_SLASH;
    }

    String pathtemp[] = path.split(regex);
    // remove empty elements
    int elem = 0;
    {
      String newtemp [] = new String [pathtemp.length];
      int j = 0;
      for (int i=0; i < pathtemp.length; i++) {
        if (!pathtemp[i].equals("")) {
           newtemp [j++] = pathtemp[i];
           elem++;
        }
      }
      pathtemp = newtemp;
    }


    if (elem > threshold) {
      StringBuilder sb = new StringBuilder();
      int index = 0;

      // drive or protocol
      int pos2dots = path.indexOf(":");
      if (pos2dots > 0) {
        // case c:\ c:/ etc.
        sb.append(path.substring(0, pos2dots + 2));
        index++;
        // case http:// ftp:// etc.
        if (path.indexOf(":/") > 0 && pathtemp[0].length() > 2) {
          sb.append(SHORTENER_SLASH);
        }
      }
      else {
        boolean isUNC = path.substring(0,2).equals(SHORTENER_BACKSLASH_REGEX);
        if (isUNC) {
          sb.append(SHORTENER_BACKSLASH).append(SHORTENER_BACKSLASH);
        }
      }

      for (; index <= threshold; index++) {
        sb.append(pathtemp[index]).append(sep);
      }

      if (index == (elem - 1)) {
        sb.append(pathtemp[elem - 1]);
      }
      else {
        sb.append(SHORTENER_ELLIPSE)
          .append(sep)
          .append(pathtemp[elem - 1]);
      }
      return sb.toString();
    }
    return path;
  }

  /**
   * Compact a path into a given number of characters. Similar to the
   * Win32 API PathCompactPathExA
   * @param path
   * @param limit
   * @return
   */
  public static String pathLengthShortener(String path, int limit) {

    if (path.length() <= limit) {
      return path;
    }

    char shortPathArray[] = new char [limit];
    char pathArray [] = path.toCharArray();
    char ellipseArray [] = SHORTENER_ELLIPSE.toCharArray();

    int pathindex = pathArray.length - 1 ;
    int shortpathindex = limit - 1;


    // fill the array from the end
    int i = 0;
    for (; i < limit  ; i++) {
      if (pathArray[pathindex - i] != '/' && pathArray[pathindex - i] != '\\') {
        shortPathArray[shortpathindex - i] = pathArray[pathindex - i] ;
      }
      else {
        break;
      }
    }
    // check how much space is left
    int free = limit - i;

    if (free < SHORTENER_ELLIPSE.length()) {
      // fill the beginning with ellipse
      for(int j = 0; j < ellipseArray.length; j++) {
        shortPathArray[j] = ellipseArray[j] ;
      }
    }
    else {
      // fill the beginning with path and leave room for the ellipse
      int j = 0;
      for(; j + ellipseArray.length < free; j++) {
        shortPathArray[j] = pathArray[j] ;
      }
      // ... add the ellipse
      for(int k = 0; j + k < free;k++) {
        shortPathArray[j + k] = ellipseArray[k] ;
      }
    }
    return new String(shortPathArray);
  }



  public static void main(String args[]) throws Exception {

    String t = "C:\\Documents and Settings\\All Users\\Application Data\\Apple Computer\\iTunes\\SC Info\\SC Info.txt";
    System.out.println(pathShortener(t));
    System.out.println(pathShortener(t, 5));

    System.out.println(pathShortener("C:\\temp"));
    System.out.println(pathShortener("C:\\1\\2\\3\\4\\5\\test.txt"));

    System.out.println(pathShortener("C:/1/2/test.txt"));
    System.out.println(pathShortener("C:/1/2/3/4/5/test.txt"));
    System.out.println(pathShortener("\\\\server\\p1\\p2\\p3\\p4\\p5\\p6"));
    System.out.println(pathShortener("\\\\server\\p1\\p2\\p3"));
    System.out.println(pathShortener("http://www.rgagnon.com/p1/p2/p3/p4/p5/pb.html"));

    System.out.println("-----");

    System.out.println(pathLengthShortener(t,20));
    System.out.println(pathLengthShortener("C:\\temp", 20));
    System.out.println(pathLengthShortener("C:\\1\\2\\3\\4\\5\\test.txt", 20));

    System.out.println(pathLengthShortener("C:/1/2/testfile.txt", 15));
    System.out.println(pathLengthShortener("C:/1/2/3/4/5/test.txt", 15));
    System.out.println(pathLengthShortener("\\\\server\\p1\\p2\\p3\\p4\\p5\\p6", 20));
    System.out.println(pathLengthShortener("http://www.rgagnon.com/p1/p2/p3/p4/p5/pb.html", 20));

    /*
      output :
      C:\Documents and Settings\All Users\Application Data\Apple Computer\iTunes\...\SC Info.txt
      C:\temp
      C:\1\2\3\4\...\test.txt
      C:/1/2/test.txt
      C:/1/2/3/4/.../test.txt
      \\server\p1\p2\p3\p4\...\p6
      \\server\p1\p2\p3
      http://www.rgagnon.com/p1/p2/p3/.../pb.html
      -----
      C:\Doc...SC Info.txt
      C:\temp
      C:\1\2\3\...test.txt
      ...testfile.txt
      C:/1...test.txt
      \\server\p1\p2\...p6
      http://www...pb.html
    */

  }

}