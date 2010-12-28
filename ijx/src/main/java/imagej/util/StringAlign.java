
package imagej.util;


/*
 * Copyright (c) Ian F. Darwin, http://www.darwinsys.com/, 1996-2002.
 * All rights reserved. Software written by Ian F. Darwin and others.
 * $Id: LICENSE,v 1.8 2004/02/09 03:33:38 ian Exp $
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS''
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Java, the Duke mascot, and all variants of Sun's Java "steaming coffee
 * cup" logo are trademarks of Sun Microsystems. Sun's, and James Gosling's,
 * pioneering role in inventing and promulgating (and standardizing) the Java
 * language and environment is gratefully acknowledged.
 *
 * The pioneering role of Dennis Ritchie and Bjarne Stroustrup, of AT&T, for
 * inventing predecessor languages C and C++ is also gratefully acknowledged.
 */
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;


/** Bare-minimum String formatter (string aligner).
 * XXX When 1.5 is common, change from ints to enum for alignment.
 */
public class StringAlign extends Format {
  /* Constant for left justification. */
  public static final int JUST_LEFT = 'l';
  /* Constant for centering. */
  public static final int JUST_CENTRE = 'c';
  /* Centering Constant, for those who spell "centre" the American way. */
  public static final int JUST_CENTER = JUST_CENTRE;
  /** Constant for right-justified Strings. */
  public static final int JUST_RIGHT = 'r';

  /** Current justification */
  private int just;
  /** Current max length */
  private int maxChars;

    /** Construct a StringAlign formatter; length and alignment are
     * passed to the Constructor instead of each format() call as the
     * expected common use is in repetitive formatting e.g., page numbers.
     * @param nChars - the length of the output
     * @param just - one of JUST_LEFT, JUST_CENTRE or JUST_RIGHT
     */
  public StringAlign(int maxChars, int just) {
    switch(just) {
    case JUST_LEFT:
    case JUST_CENTRE:
    case JUST_RIGHT:
      this.just = just;
      break;
    default:
      throw new IllegalArgumentException("invalid justification arg.");
    }
    if (maxChars < 0) {
      throw new IllegalArgumentException("maxChars must be positive.");
    }
    this.maxChars = maxChars;
  }

  /** Format a String.
     * @param input _ the string to be aligned.
     * @parm where - the StringBuffer to append it to.
     * @param ignore - a FieldPosition (may be null, not used but
     * specified by the general contract of Format).
     */
  public StringBuffer format(
    Object obj, StringBuffer where, FieldPosition ignore)  {

    String s = (String)obj;
    String wanted = s.substring(0, Math.min(s.length(), maxChars));

    // Get the spaces in the right place.
    switch (just) {
      case JUST_RIGHT:
        pad(where, maxChars - wanted.length());
        where.append(wanted);
        break;
      case JUST_CENTRE:
        int toAdd = maxChars - wanted.length();
        pad(where, toAdd/2);
        where.append(wanted);
        pad(where, toAdd - toAdd/2);
        break;
      case JUST_LEFT:
        where.append(wanted);
        pad(where, maxChars - wanted.length());
        break;
      }
    return where;
  }

  protected final void pad(StringBuffer to, int howMany) {
    for (int i=0; i<howMany; i++)
      to.append(' ');
  }

  /** Convenience Routine */
  String format(String s) {
    return format(s, new StringBuffer(), null).toString();
  }

  /** ParseObject is required, but not useful here. */
  public Object parseObject (String source, ParsePosition pos)  {
    return source;
  }

  /** Demonstrate and test StringAlign class */
  public static void main(String[] argv) {
    String[] mesg = {"JavaFun", "JavaFun!" };
    for (int i=0; i<mesg.length; i++) {
      System.out.println("Input String \"" + mesg[i] + "\"");
      dump(StringAlign.JUST_LEFT, 5,
        new StringAlign(5, StringAlign.JUST_LEFT).format(mesg[i]));
      dump(StringAlign.JUST_LEFT, 10,
        new StringAlign(10, StringAlign.JUST_LEFT).format(mesg[i]));
      dump(StringAlign.JUST_CENTER, 5,
        new StringAlign(5, StringAlign.JUST_CENTER).format(mesg[i]));
      dump(StringAlign.JUST_CENTER, 10,
        new StringAlign(10, StringAlign.JUST_CENTER).format(mesg[i]));
      dump(StringAlign.JUST_RIGHT, 5,
        new StringAlign(5, StringAlign.JUST_RIGHT).format(mesg[i]));
      dump(StringAlign.JUST_RIGHT, 10,
        new StringAlign(10, StringAlign.JUST_RIGHT).format(mesg[i]));
    }
  }

  private static void dump(int format, int len, String s) {
    System.out.print((char)format + "[" + len + "]");
    System.out.print(" ==> \"");
    System.out.print(s);
    System.out.print('"');
    System.out.println();
  }
}


