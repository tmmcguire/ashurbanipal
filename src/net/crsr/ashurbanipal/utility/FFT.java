package net.crsr.ashurbanipal.utility;

import java.util.ArrayList;
import java.util.List;

import org.jtransforms.fft.DoubleFFT_1D;

public class FFT {

  /**
   * Convert an array of doubles in the format produced by {@link DoubleFFT_1D#realForward(double[])} into a list of Complex<Double>.
   * 
   * The format of the input array should be, for an even-length array:
   * <pre>
   *  a[2*k]   = Re[k], 0<=k<n/2
   *  a[2*k+1] = Im[k], 0<k<n/2
   *  a[1]     = Re[n/2]
   * </pre>
   * 
   * The format of the input array should be, for an odd-length array:
   * <pre>
   * a[2*k]   = Re[k], 0<=k<(n+1)/2 
   * a[2*k+1] = Im[k], 0<k<(n-1)/2
   * a[1]     = Im[(n-1)/2]
   * </pre>
   * 
   * @param ary Array of doubles, in the format produced by {@link DoubleFFT_1D#realForward(double[])}.
   * @return List of Complex<Double>.
   */
  private static List<Complex<Double>> toComplexList(double[] ary) {
    final List<Complex<Double>> result = new ArrayList<>();
    result.add( new Complex<>(ary[0], 0.0) );
    if (ary.length % 2 == 1) {
      // [a,e,b,c,d] -> (a,0) (b,c) (d,e)
      final int boundary = (ary.length - 1) / 2;
      for (int k = 1; k < boundary; ++k) {
        result.add( new Complex<>(ary[2*k], ary[2*k + 1]) );
      }
      result.add( new Complex<>(ary[2*boundary], ary[1]) );
    } else {
      // [a,b,c,d,e,f] -> (a,0) (b,c) (d,e) (f,0)
      final int boundary = ary.length / 2;
      for (int k = 1; k < boundary; ++k) {
        result.add( new Complex<>(ary[2*k], ary[2*k + 1]) );
      }
      result.add( new Complex<>(ary[1], 0.0) );
    }
    return result;
  }

  /**
   * Convert a list of Complex<Double> into an array of doubles in the format produced by {@link DoubleFFT_1D#realForward(double[])}.
   * 
   * The format of the output array should be, for an even-length array:
   * <pre>
   *  a[2*k]   = Re[k], 0<=k<n/2
   *  a[2*k+1] = Im[k], 0<k<n/2
   *  a[1]     = Re[n/2]
   * </pre>
   * 
   * The format of the output array should be, for an odd-length array:
   * <pre>
   * a[2*k]   = Re[k], 0<=k<(n+1)/2 
   * a[2*k+1] = Im[k], 0<k<(n-1)/2
   * a[1]     = Im[(n-1)/2]
   * </pre>
   * 
   * @param values List of Complex<Double>.
   * @return Array of doubles, in the format produced by {@link DoubleFFT_1D#realForward(double[])}.
   */
  private static double[] fromComplexList(List<Complex<Double>> data) {
    final int n = data.size();
    double[] ary = null;
    if (data.get(n - 1).imag == 0.0) {
      // (a,0) (b,c) (d,e) (f,0) -> [a,f,b,c,d,e]
      ary = new double[2*n - 2];
      ary[0] = data.get(0).real;
      ary[1] = data.get(n - 1).real;
      for (int k = 1; k < n - 1; ++k) {
        ary[2*k] = data.get(k).real;
        ary[2*k + 1] = data.get(k).imag;
      }
    } else {
      // (a,0) (b,c) (d,e) (f,g) -> [a,g,b,c,d,e,f]
      ary = new double[2*n - 1];
      ary[0] = data.get(0).real;
      ary[1] = data.get(n - 1).imag;
      for (int k = 1; k < n - 1; ++k) {
        ary[2*k] = data.get(k).real;
        ary[2*k + 1] = data.get(k).imag;
      }
      ary[2*n - 2] = data.get(n - 1).real;
    }
    return ary;
  }
  
  private static List<Complex<Double>> setSamples(List<Complex<Double>> frequencies, int samples) {
    if (samples < frequencies.size()) {
      final List<Complex<Double>> result = new ArrayList<>(samples);
      result.addAll(frequencies.subList(0, samples));
      return result;
    } else if (samples > frequencies.size()) {
      final List<Complex<Double>> result = new ArrayList<>(samples);
      result.addAll(frequencies);
      for (int i = frequencies.size(); i < samples; ++i) {
        result.add( new Complex<>(0.0,0.0) );
      }
      return result;
    } else {
      return frequencies;
    }
  }
  
  private static List<Complex<Double>> clipFrequencies(List<Complex<Double>> frequencies, int nFrequencies) {
    final List<Complex<Double>> result = new ArrayList<>(frequencies.size());
    int i = 0;
    for (Complex<Double> frequency : frequencies) {
      if (i < nFrequencies) {
        result.add(frequency);
      } else {
        result.add( new Complex<>(0.0,0.0) );
      }
      i += 1;
    }
    return result;
  }
  
  /**
   * Compute the FFT of data padded with data.size() zeros and expanded to the next power of two.
   * 
   * @param data Input data.
   * @return List of complex numbers representing the FFT of data.
   */
  public static List<Complex<Double>> fft(List<Double> data) {
    final double[] ary = new double[data.size() * 2];
    int i = 0;
    for (Double d : data) {
      ary[i++] = d;
    }
    new DoubleFFT_1D(ary.length).realForward(ary);
    final List<Complex<Double>> results = toComplexList(ary);
    return results.subList(0, results.size() / 2);
  }

  public static List<Double> inverseFFT(List<Complex<Double>> data, int samples, int nFrequencies) {
    double[] ary = fromComplexList(setSamples(clipFrequencies(data, nFrequencies * 2), samples));
    new DoubleFFT_1D(ary.length).realInverse(ary, true);
    final List<Double> results = new ArrayList<>();
    for (int i = 0; i < ary.length / 2; ++i) {
      results.add(ary[i]);
    }
    return results;
  }
}
