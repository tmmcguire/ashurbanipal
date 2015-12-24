package net.crsr.ashurbanipal.utility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jtransforms.utils.CommonUtils;

public class CollectionsUtilities {

  /**
   * Return the contents of the collection as an array of raw doubles, expanded in size.
   * @param c A collection of n Doubles.
   * @param mult Append zeros to the collection until it has the next power of 2 above (c.length() * mult) entries.
   * @return An array of length >= (|c|*mult) of doubles, padded with zeros.
   */
  public static <T extends Collection<Double>> double[] asArray(T c, int mult) {
    final int cSize = CommonUtils.nextPow2( Math.max(c.size() * mult, 1) );
    final double[] ary = new double[cSize];
    int i = 0;
    for (Double d : c) {
      ary[i++] = d;
    }
    for (; i < ary.length; ++i) {
      ary[i] = 0.0;
    }
    return ary;
  }
  
  /**
   * Convert an array of raw doubles to a List<Double>.
   * 
   * @param ary An array of doubles.
   * @return A list containing the exact elements of ary.
   */
  public static List<Double> asList(double[] ary) {
    final List<Double> result = new ArrayList<>(ary.length);
    for (double d : ary) {
      result.add(d);
    }
    return result;
  }

}
