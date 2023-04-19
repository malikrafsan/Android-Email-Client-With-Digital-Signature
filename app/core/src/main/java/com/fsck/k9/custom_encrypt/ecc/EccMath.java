package com.fsck.k9.custom_encrypt.ecc;

import java.math.*;

public final class EccMath {
  public static final EccMath INSTANCE;

  public BigInteger multiply(BigInteger a, BigInteger b,
                                   BigInteger prime) {
    BigInteger temp = a.multiply(b);
    return temp.remainder(prime);
  }

  public BigInteger divide(BigInteger num, BigInteger dom,
                                 BigInteger prime) {
    BigInteger inverseDen = dom.modInverse(prime);
    BigInteger remain = num.remainder(prime);
    return this.multiply(remain, inverseDen, prime);
  }

  public BigInteger tangent(EccPoint point, EccCurve curve) {
    BigInteger temp1 = point.getX();
    BigInteger temp2 = point.getX();
    BigInteger multiplied = temp1.multiply(temp2);
    temp1 = multiplied;
    temp2 = EccConsts.INSTANCE.getTHREE();
    multiplied = temp1.multiply(temp2);

    temp1 = multiplied;
    temp2 = curve.getA();
    multiplied = temp1.add(temp2);

    temp1 = point.getY();
    temp2 = EccConsts.INSTANCE.getTWO();
    BigInteger multiplied2 = temp1.multiply(temp2);

    return this.divide(multiplied, multiplied2, curve.getP());
  }

  public EccPoint identity(EccPoint point) {
    return new EccPoint(point.getCurve().getP(), EccConsts.INSTANCE.getZERO(),
                        point.getCurve());
  }

  public EccPoint dot(EccPoint p1, EccPoint p2, BigInteger m,
                            EccCurve curve) {
    var v1 = m.multiply(p1.getX()).
                        remainder(curve.getP());
    var v2 = (p1.getY().
                        add(curve.getP()).
                        subtract(v1));
    var v = v2.remainder(curve.getP());

    var x1 = m.multiply(m);
    var x2 = x1.add(curve.getP()).
                subtract(p1.getX()).
                add(curve.getP()).
                subtract(p2.getX());
    var x = x2.remainder(curve.getP());

    var y1 = m.multiply(x).remainder(curve.getP());
    var y2 = curve.getP().
                        subtract(y1).
                        add(curve.getP()).
                        subtract(v);
    var y = y2.remainder(curve.getP());

    return new EccPoint(x, y, curve);
  }

  public EccPoint doublePoint(EccPoint point) {
    if (point.getX().equals(point.getCurve().getP())) {
      return point;
    }

    return dot(point, point, tangent(point, point.getCurve()), point.getCurve());
  }

  private EccMath() {}

  static {
    INSTANCE = new EccMath();
  }
}
