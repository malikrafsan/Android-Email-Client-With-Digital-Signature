package com.fsck.k9.custom_encrypt.ecc;

import java.math.BigInteger;

public abstract class EccCurve {
  public abstract BigInteger getP();

  public abstract BigInteger getN();

  public abstract BigInteger getA();

  public abstract BigInteger getB();

  public abstract BigInteger getX();

  public abstract BigInteger getY();

  public final EccPoint getG() {
    return new EccPoint(this.getX(), this.getY(), this);
  }

  public final EccPoint getIdentity() {
    return EccMath.INSTANCE.identity(this.getG());
  }

  public final EccPoint add(EccPoint p1, EccPoint p2) {
    if (p1.getX().equals(this.getP())) {
      return p2;
    }

    if (p2.getX().equals(this.getP())) {
      return p1;
    }

    if (p1.getX().equals(p2.getX())) {
      if (p1.getY().equals(p2.getY())) {
        return EccMath.INSTANCE.doublePoint(p1);
      }

      return EccMath.INSTANCE.identity(p1);
    }

    BigInteger m = EccMath.INSTANCE.divide(
        p1.getY().add(this.getP()).subtract(p2.getY()),
        p1.getX().add(this.getP()).subtract(p2.getX()),
        this.getP());

    return EccMath.INSTANCE.dot(p1, p2, m, this);
  }

  public final EccPoint multiply(EccPoint g, BigInteger n) {
    EccPoint r = this.getIdentity();
    EccPoint q = g;
    BigInteger m = n;

    while (!m.equals(EccConsts.INSTANCE.getZERO())) {
      if (!m.and(EccConsts.INSTANCE.getONE()).equals(EccConsts.INSTANCE.getZERO())) {
        r = this.add(r, q);
      }

      m = m.shiftRight(1);

      if (!m.equals(EccConsts.INSTANCE.getZERO())) {
        q = EccMath.INSTANCE.doublePoint(q);
      }
    }

    return r;
  }

  public final EccPoint times(BigInteger n) {
    return this.multiply(this.getG(), n);
  }
}
