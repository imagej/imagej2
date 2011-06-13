package imagej.util.diag.inspect;

/*
 * Copyright (C) 2000 Sean Bridges
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */


import java.util.*;


/**
 * A class used as an example of the Inspector.
 * Has a colection of Objects and primitive types,
 * including a circular reference just for fun.
 *
 * @author Sean Bridges
 * @see <a href="http://www.geocities.com/sbridges.geo">more info</a>
 * @version 0.1
 */
public final class ConvolutedClass
{

  public int publicint = 1;
  public static int publicstaticint = 2;
  private int privateint = 3;
  private char privatechar = 'a';
  private float privatefloat = 1.1f;
  private double privatedouble = 2.0;
  private Similiar similar10 = new Similiar(10);
  private Similiar similar20 = new Similiar(20);

  private ConvolutedClass self = this;

  protected String[] someStrings = {"apples", "oranges", "pears"};
  protected int[] someInts = {1,2,3,4,5,4,3,2,1};
  protected String[] nullArray;
  protected int[][] twoDArray = new int[5][5];
  protected Object[] anObjectArray = new Object[5];
  Collection someStuff = new ArrayList(10);
  private Object anObject = new Object();
  private D inheritanceExample = new D();
  private  HashMap hash = new  HashMap();
  private Vector aVector = new Vector();

  public ConvolutedClass()
  {
    someStuff.add(this);
    someStuff.add(someStrings);
    someStuff.add(new Integer(10) );
    someStuff.add(new StringBuffer("the contents"));
    someStuff.add(new Random() );

    for(int i = 0; i < 5; i++)
    {
      for(int j = 0; j < 5; j++)
      {
        twoDArray[i][j] = i * j;
      }
    }

    anObjectArray[0] = anObjectArray;
    anObjectArray[1] = someStrings;
    anObjectArray[2] = this;
    anObjectArray[3] = new Float(2);
    /*
     Note I originially tried stuff.add(stuff), but you get a stack overflow
     in jdk1.3
     To see why try
      ArrayList a = new ArrayList();
      a.add(a);
      a.hashCode();
    */

    hash.put(new Object(), new Integer(5));
    hash.put("a", "e");
    hash.put("b", "f");
    hash.put("c", "g");
    hash.put(null, "?");
    hash.put("?", null);

    aVector.add("First");
    aVector.add(new Integer(2));
  }

}

/**
 * Overrides .equals() to be always equals, and hash code to return 10 always.
 * Shows how the Inspector does not rely on either.
 */
class Similiar
{
  private int myInt;

  Similiar(int anInt)
  {
    myInt = anInt;
  }

  public boolean equals(Object obj)
  {
    return true;
  }

  public int hashCode()
  {
    return 10;
  }

  public String toString()
  {
    return String.valueOf(myInt);
  }

}

class A
{
  public int anAVariable;
}

class B extends A
{
  private int aBVariable;
}

class C extends B
{
  protected boolean aCVariable = false;
}

class D extends C
{
  protected Object aDVariable = new Object();
}


