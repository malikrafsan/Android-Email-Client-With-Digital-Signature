package com.fsck.k9.custom_encrypt.ecc;

import java.math.BigInteger;

public final class EccSignature {
    
    private final BigInteger r;
    
    private final BigInteger s;

    
    public BigInteger getR() {
        return this.r;
    }

    
    public BigInteger getS() {
        return this.s;
    }

    public EccSignature( BigInteger r,  BigInteger s) {
        this.r = r;
        this.s = s;
    }

    public EccSignature(String str) {
        String[] split = str.split(";");
        this.r = new BigInteger(split[0]);
        this.s = new BigInteger(split[1]);
    }

    @Override
    public String toString() {
        return r.toString() + ";" + s.toString();
    }
}
