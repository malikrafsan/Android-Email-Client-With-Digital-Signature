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
    BigInteger var3 = point.getX();
    BigInteger var4 = point.getX();
    BigInteger var10001 = var3.multiply(var4);
    var3 = var10001;
    var4 = EccConsts.INSTANCE.getTHREE();
    var10001 = var3.multiply(var4);

    var3 = var10001;
    var4 = curve.getA();
    var10001 = var3.add(var4);

    var3 = point.getY();
    var4 = EccConsts.INSTANCE.getTWO();
    BigInteger var10002 = var3.multiply(var4);

    return this.divide(var10001, var10002, curve.getP());
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
