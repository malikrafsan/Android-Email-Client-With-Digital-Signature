package com.fsck.k9.custom_encrypt.ecc;

import java.math.BigInteger;
import java.security.SecureRandom;

public final class EccGenerator {
    public static final EccGenerator INSTANCE;

    public  EccKeyPair newInstance( EccCurve curve) {
        BigInteger bigInteger = this.getRandom32BytePrivateKey();
        BigInteger bigInteger2 = curve.getP();
        BigInteger privateKey = bigInteger.remainder(bigInteger2);
        EccPoint publicKey = curve.getG().times(privateKey);
        return new EccKeyPair(publicKey, privateKey);
    }

    public  EccKeyPair newInstance( BigInteger privateKey,  EccCurve curve) {
        EccPoint publicKey = curve.getG().times(privateKey);
        return new EccKeyPair(publicKey, privateKey);
    }

    private BigInteger getRandom32BytePrivateKey() {
        byte[] privateKeyBytes = new byte[32];
        new SecureRandom().nextBytes(privateKeyBytes);
        return new BigInteger(privateKeyBytes).abs();
    }

    private EccGenerator() {
    }

    static {
        INSTANCE = new EccGenerator();
    }
}
