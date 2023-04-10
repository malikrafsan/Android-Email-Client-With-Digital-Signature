package com.fsck.k9.custom_encrypt.ecc;

import java.math.BigInteger;

public final class EccConsts {

  private static final BigInteger ZERO;

  private static final BigInteger ONE;

  private static final BigInteger TWO;

  private static final BigInteger THREE;

  public static final EccConsts INSTANCE;

  public BigInteger getZERO() { return ZERO; }

  public BigInteger getONE() { return ONE; }

  public BigInteger getTWO() { return TWO; }

  public BigInteger getTHREE() { return THREE; }

  private EccConsts() {}

  static {
    INSTANCE = new EccConsts();
    ZERO = BigInteger.ZERO;
    ONE = BigInteger.valueOf(1L);
    TWO = BigInteger.valueOf(2L);
    THREE = BigInteger.valueOf(3L);
  }
}
