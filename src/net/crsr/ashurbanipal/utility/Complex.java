package net.crsr.ashurbanipal.utility;

import static java.lang.Math.*;

public class Complex<T extends Number> {
  public final T real;
  public final T imag;
  
  public Complex(T real, T imag) {
    this.real = real;
    this.imag = imag;
  }
  
  public double distance(Complex<T> other) {
    final double realDifference = this.real.doubleValue() - other.real.doubleValue();
    final double imagDifference = this.imag.doubleValue() - other.imag.doubleValue();
    return sqrt(pow(realDifference, 2) + pow(imagDifference, 2));
  }
  
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder()
    .append('(')
    .append(real)
    .append(" + ")
    .append(imag)
    .append("i)");
    return sb.toString();
  }
  
}
