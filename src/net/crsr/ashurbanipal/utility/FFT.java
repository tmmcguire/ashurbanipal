package net.crsr.ashurbanipal.utility;

import java.util.ArrayList;
import java.util.List;

import org.jtransforms.fft.DoubleFFT_1D;
import org.jtransforms.utils.CommonUtils;

public class FFT {

  /**
   * Convert an array of doubles in the format produced by {@link DoubleFFT_1D#realForward(double[])} into a list of Complex<Double>.
   * 
   * The format of the input array should be:
   * <pre>
   *  a[2*k]   = Re[k], 0<=k<n/2
   *  a[2*k+1] = Im[k], 0<k<n/2
   *  a[1]     = Re[n/2]
   * </pre>
   * 
   * @param values Array of doubles, in the format produced by {@link DoubleFFT_1D#realForward(double[])}.
   * @return List of Complex<Double>.
   */
  private static List<Complex<Double>> toComplexList(double[] values) {
    if (values.length % 2 != 0) {
      throw new IllegalArgumentException("length of values must be even");
    }
    final int boundary = values.length / 2;
    final List<Complex<Double>> result = new ArrayList<>(boundary);
    result.add( new Complex<>(values[0], 0.0) );
    for (int k = 1; k < boundary; ++k) {
      result.add( new Complex<>(values[2*k], values[2*k + 1]) );
    }
    result.add( new Complex<>(values[1], 0.0) );
    return result;
  }

  /**
   * Convert a list of Complex<Double> into an array of doubles in the format produced by {@link DoubleFFT_1D#realForward(double[])}.
   * 
   * The format of the output array should be:
   * <pre>
   *  a[2*k]   = Re[k], 0<=k<n/2
   *  a[2*k+1] = Im[k], 0<k<n/2
   *  a[1]     = Re[n/2]
   * </pre>
   * 
   * @param values List of Complex<Double>.
   * @return Array of doubles, in the format produced by {@link DoubleFFT_1D#realForward(double[])}.
   */
  private static void storeComplexList(List<Complex<Double>> data, double[] ary) {
    final int n = data.size() - 1;
    if (ary.length < n * 2) {
      throw new IllegalArgumentException("output array too small: " + (n * 2) + " elements required");
    }
    ary[0] = data.get(0).real;
    ary[1] = data.get(n).real;
    for(int k = 1; k < n; ++k) {
      ary[2*k] = data.get(k).real;
      ary[2*k + 1] = data.get(k).imag;
    }
  }
  
  /**
   * Compute the FFT of data padded with data.size() zeros and expanded to the next power of two.
   * 
   * @param data Input data.
   * @return List of complex numbers representing the FFT of data.
   */
  public static List<Complex<Double>> fft(List<Double> data) {
    final int n = CommonUtils.nextPow2( data.size() * 2 );
    final double[] ary = new double[n];
    int i = 0;
    for (Double d : data) {
      ary[i++] = d;
    }
    new DoubleFFT_1D(ary.length).realForward(ary);
    return toComplexList(ary);

  }

  public static List<Double> inverseFFT(List<Complex<Double>> data, int length) {
    if (length < data.size()) { throw new IllegalArgumentException("length must be >= data.size()"); }
    final int size = CommonUtils.nextPow2( length * 2 * 2 );
    final double[] ary = new double[size]; // Assumption: d[data.size() * 2...] == 0
    storeComplexList(data, ary);
    new DoubleFFT_1D(size).realInverse(ary, true);
    final List<Double> results = new ArrayList<>(length);
    for (int i = 0; i < length; ++i) {
      results.add(ary[i]);
    }
    return results;
  }
}
