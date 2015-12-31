package net.crsr.ashurbanipal.utility;

import static java.lang.Math.*;

public class Complex {
  public final double real;
  public final double imag;
  
  public Complex(double real, double imag) {
    this.real = real;
    this.imag = imag;
  }
  
  public double magnitude() {
    return Math.sqrt((real * real) + (imag * imag));
  }
  
  public Complex conjugate() {
    return new Complex(this.real, -this.imag);
  }
  
  public Complex multiply(Complex other) {
    double real = (this.real * other.real) - (this.imag * other.imag);
    double imag = (this.real * other.imag) + (this.imag * other.real);
    return new Complex(real, imag);
  }
  
  public Complex add(Complex other) {
    return new Complex(this.real + other.real, this.imag + other.imag);
  }
  
  public Complex sub(Complex other) {
    return new Complex(this.real - other.real, this.imag - other.imag);
  }
  
  public Complex div(Complex other) {
    final Complex otherC = other.conjugate();
    final Complex num = this.multiply(otherC);
    final Complex den = other.multiply(otherC);
    return new Complex(num.real / den.real, num.imag / den.real);
  }
  
  public double distance(Complex other) {
    final double realDifference = this.real - other.real;
    final double imagDifference = this.imag - other.imag;
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
