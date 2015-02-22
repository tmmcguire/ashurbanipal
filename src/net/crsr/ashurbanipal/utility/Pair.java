package net.crsr.ashurbanipal.utility;

public class Pair<L,R> {

  public final L l;
  public final R r;

  public Pair(L l, R r) {
    this.l = l;
    this.r = r;
  }

  public static <L1,R1> Pair<L1,R1> pair(L1 l, R1 r) {
    return new Pair<>(l, r);
  }
}
