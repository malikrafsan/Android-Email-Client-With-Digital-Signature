package com.fsck.k9.custom_encrypt.ecc;

import java.math.*;

public final class EccPoint {
  private static final EccCurve defaultCurve = Secp256k1.INSTANCE;
  private final BigInteger x;
  private final BigInteger y;
  private final EccCurve curve;

  public EccPoint plus(EccPoint other) {
    return this.curve.add(this, other);
  }

  public EccPoint times(BigInteger n) {
    return this.curve.multiply(this, n);
  }

  public boolean equals(EccPoint other) {
    return x.equals(other.getX()) && y.equals(other.getY());
  }

  public int hashCode() { return this.x.hashCode() + this.y.hashCode(); }

  public BigInteger getX() { return this.x; }

  public BigInteger getY() { return this.y; }

  public EccCurve getCurve() { return this.curve; }

  public EccPoint(BigInteger x, BigInteger y, EccCurve curve) {
    this.x = x;
    this.y = y;
    this.curve = curve;
  }

  public EccPoint(String str) {
    String[] split = str.split(";");
    this.x = new BigInteger(split[0]);
    this.y = new BigInteger(split[1]);
    this.curve = EccPoint.defaultCurve;
  }

  public String toString() {
    return x.toString() + ";" + y.toString();
  }
}
