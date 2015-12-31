package net.crsr.ashurbanipal.utility;

import java.util.ArrayList;
import java.util.List;

import org.jtransforms.fft.DoubleFFT_1D;

public class FFT {
  
  /**
   * Compute the FFT of data padded with data.size() zeros and expanded to the next power of two.
   * 
   * @param data Input data.
   * @return List of complex numbers representing the FFT of data.
   */
  public static List<Complex> fft(List<Double> data) {
    final int dataSize = data.size();
    final double[] ary = new double[dataSize * 2];
    for (int i = 0; i < ary.length; ++i) { ary[i] = (i < dataSize) ? data.get(i) : 0.0; }
    new DoubleFFT_1D(ary.length).realForward(ary);
    final List<Complex> results = toComplexList(ary);
    return results.subList(0, results.size() / 2);
  }

  public static List<Double> inverseFFT(List<Complex> data, int samples, int nFrequencies) {
    final List<Complex> clipped = clipFrequencies(data, nFrequencies * 2);
    final List<Complex> sized = setSamples(clipped, samples);
    double[] ary = fromComplexList(sized);
    new DoubleFFT_1D(ary.length).realInverse(ary, true);
    final List<Double> results = new ArrayList<>();
    for (int i = 0; i < ary.length / 2; ++i) {
      results.add(ary[i]);
    }
    return results;
  }
  
  public static double cosineSimilarity(List<Complex> left, List<Complex> right) {
    final List<Double> leftI = FFT.inverseFFT(left, left.size(), left.size());
    final List<Double> rightI = FFT.inverseFFT(right, right.size(), right.size());
    return dotProduct(leftI, rightI) / (magnitude(leftI) * magnitude(rightI));
  }
  
  public static double dotProduct(List<Double> left, List<Double> right) {
    final int len = Math.min(left.size(), right.size());
    double result = 0.0;
    for (int i = 0; i < len; ++i) {
      result += left.get(i) * right.get(i);
    }
    return result;
  }

  public static double magnitude(final List<Double> leftI) {
    return Math.sqrt(dotProduct(leftI, leftI));
  }
  
  public static List<Double> correlate(List<Complex> left, List<Complex> right) {
    final List<Complex> x = new ArrayList<>();
    for (int i = 0; i < Math.min(left.size(), right.size()); ++i) {
      x.add( left.get(i).multiply( right.get(i).conjugate() ) );
    }
    final List<Double> inverseFFT = inverseFFT(x, x.size(), x.size());
    return inverseFFT;
  }
  
  public static List<Double> normalize(List<Double> l) {
    double max = Double.NEGATIVE_INFINITY;
    double min = Double.POSITIVE_INFINITY;
    for (Double d : l) {
      if (d > max) { max = d; }
      if (d < min) { min = d; }
    }
    final double range = max - min;
    final List<Double> results = new ArrayList<>(l.size());
    for (Double d : l) {
      results.add(((2 * (d - min)) / range) - 1);
    }
    return results;
  }
  
  public static Complex avg(List<Complex> values) {
    Complex result = new Complex(0.0,0.0);
    for (Complex value : values) {
      result = result.add(value);
    }
    return result.div( new Complex(values.size(), 0.0) );
  }

  /**
   * Convert an array of doubles in the format produced by {@link DoubleFFT_1D#realForward(double[])} into a list of Complex.
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
   * @return List of Complex.
   */
  private static List<Complex> toComplexList(double[] ary) {
    final List<Complex> result = new ArrayList<>();
    result.add( new Complex(ary[0], 0.0) );
    if (ary.length % 2 == 1) {
      // [a,e,b,c,d] -> (a,0) (b,c) (d,e)
      final int boundary = (ary.length - 1) / 2;
      for (int k = 1; k < boundary; ++k) {
        result.add( new Complex(ary[2*k], ary[2*k + 1]) );
      }
      result.add( new Complex(ary[2*boundary], ary[1]) );
    } else {
      // [a,b,c,d,e,f] -> (a,0) (b,c) (d,e) (f,0)
      final int boundary = ary.length / 2;
      for (int k = 1; k < boundary; ++k) {
        result.add( new Complex(ary[2*k], ary[2*k + 1]) );
      }
      result.add( new Complex(ary[1], 0.0) );
    }
    return result;
  }

  /**
   * Convert a list of Complex into an array of doubles in the format produced by {@link DoubleFFT_1D#realForward(double[])}.
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
   * @param values List of Complex.
   * @return Array of doubles, in the format produced by {@link DoubleFFT_1D#realForward(double[])}.
   */
  private static double[] fromComplexList(List<Complex> data) {
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
  
  private static List<Complex> setSamples(List<Complex> frequencies, int samples) {
    final List<Complex> result = new ArrayList<>(samples);
    if (samples <= frequencies.size()) {
      result.addAll(frequencies.subList(0, samples));
    } else if (samples > frequencies.size()) {
      result.addAll(frequencies);
      for (int i = frequencies.size(); i < samples; ++i) {
        result.add( new Complex(0.0,0.0) );
      }
    }
    return result;
  }
  
  private static List<Complex> clipFrequencies(List<Complex> frequencies, int nFrequencies) {
    final List<Complex> result = new ArrayList<>(frequencies.size());
    for (int i = 0; i < frequencies.size(); ++i) {
      result.add( (i < nFrequencies) ? frequencies.get(i) : new Complex(0.0, 0.0) );
    }
    return result;
  }
}
