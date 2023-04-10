package com.fsck.k9.custom_encrypt.ecc;

import java.math.BigInteger;

public final class Secp256k1 extends EccCurve {
    
    private static final BigInteger a;
    
    private static final BigInteger b;
    
    private static final BigInteger n;
    
    private static final BigInteger p;
    
    private static final BigInteger x;
    
    private static final BigInteger y;
    public static final Secp256k1 INSTANCE;

    @Override
    
    public BigInteger getA() {
        return a;
    }

    @Override
    
    public BigInteger getB() {
        return b;
    }

    @Override
    
    public BigInteger getN() {
        return n;
    }

    @Override
    
    public BigInteger getP() {
        return p;
    }

    @Override
    
    public BigInteger getX() {
        return x;
    }

    @Override
    
    public BigInteger getY() {
        return y;
    }

    private Secp256k1() {
    }

    static {
        INSTANCE = new Secp256k1();
        a = new BigInteger("00");
        b = new BigInteger("07");
        n = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141", 16);
        p = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F", 16);
        x = new BigInteger("79BE667EF9DCBBAC55A06295CE870B07029BFCDB2DCE28D959F2815B16F81798", 16);
        y = new BigInteger("483ADA7726A3C4655DA4FBFC0E1108A8FD17B448A68554199C47D08FFB10D4B8", 16);
    }
}
