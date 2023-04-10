package com.fsck.k9.custom_encrypt.ecc;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

public final class EccSign {
    
    public static final EccSign INSTANCE;

    private BigInteger getRandomK(BigInteger n) {
        BigInteger randomValue = new BigInteger(256, (Random)(new SecureRandom()));
        return randomValue.compareTo(n) < 0 && randomValue.compareTo(BigInteger.ONE) > 0 ? randomValue : this.getRandomK(n);
    }
    
    public EccSignature signData( EccKeyPair keyPair,  byte[] data,  EccHasher hasher) throws Exception {
        var hash = new BigInteger(1, hasher.hash(data));
        var g = keyPair.getPublicKey().getCurve().getG();
        var n = keyPair.getPublicKey().getCurve().getN();

        var k = this.getRandomK(n).remainder(n);
        var p1 = g.times(k);
        var r = p1.getX();

        if (r.equals(EccConsts.INSTANCE.getZERO())) {
            this.signData(keyPair, data, hasher);
        }

        var sTimes = hash.add(keyPair.getPrivateKey().multiply(r).remainder(n));
        var s = k.modInverse(n).multiply(sTimes).remainder(n);

        if (s.equals(EccConsts.INSTANCE.getZERO())) {
            this.signData(keyPair, data, hasher);
        }

        return new EccSignature(r, s);
    }

    public boolean verifySignature( EccPoint publicKey,  byte[] data,  EccHasher hasher,  EccSignature signature)
        throws Exception {
        BigInteger hash = new BigInteger(1, hasher.hash(data));
        EccPoint g = publicKey.getCurve().getG();
        BigInteger n = publicKey.getCurve().getN();
        BigInteger r = signature.getR();
        BigInteger s = signature.getS();

        if (r.compareTo(EccConsts.INSTANCE.getONE()) < 0 ||
            r.compareTo(n.subtract(EccConsts.INSTANCE.getONE())) > 0) {
            return false;
        }

        if (s.compareTo(EccConsts.INSTANCE.getONE()) < 0 ||
            s.compareTo(n.subtract(EccConsts.INSTANCE.getONE())) > 0) {
            return false;
        }

        BigInteger c = s.modInverse(n);
        BigInteger u1 = hash.multiply(c).remainder(n);
        BigInteger u2 = r.multiply(c).remainder(n);
        EccPoint xy = g.times(u1).plus(publicKey.times(u2));
        BigInteger v = xy.getX().remainder(n);

        return v.equals(r);
    }

    private EccSign() {
    }

    static {
        INSTANCE = new EccSign();
    }
}
