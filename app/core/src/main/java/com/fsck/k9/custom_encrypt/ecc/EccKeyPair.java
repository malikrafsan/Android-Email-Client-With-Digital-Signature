package com.fsck.k9.custom_encrypt.ecc;

import java.math.BigInteger;

public final class EccKeyPair {
    private final EccPoint publicKey;
    private final BigInteger privateKey;

    public EccPoint getPublicKey() {
        return this.publicKey;
    }

    public BigInteger getPrivateKey() {
        return this.privateKey;
    }

    public EccKeyPair( EccPoint publicKey,  BigInteger privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }
}

