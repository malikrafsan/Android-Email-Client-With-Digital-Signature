package com.fsck.k9.custom_encrypt.ecc;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

public class EccMain {
    public static final EccMain INSTANCE;
    private EccCurve curve;
    private EccHasher hasher;

    private EccMain() {
        this.curve = Secp256k1.INSTANCE;
        this.hasher = EccSha256.INSTANCE;
    }

    public EccKeyPair generateKeys() {
        return EccGenerator.INSTANCE.newInstance(this.curve);
    }

    public EccSignature sign(BigInteger privateKey, String str) throws Exception {
        return this.sign(privateKey, str.getBytes());
    }

    public EccSignature sign(BigInteger privateKey, byte[] data) throws Exception {
        EccKeyPair keyPair = EccGenerator.INSTANCE.newInstance(privateKey, this.curve);
        return EccSign.INSTANCE.signData(keyPair, data, EccSha256.INSTANCE);
    }

    public boolean validate(String strPublicKey, String str, String strSignature) throws Exception {
        EccSignature signature = new EccSignature(strSignature);
        return this.validate(strPublicKey, str, signature.getR(), signature.getS());
    }

    public boolean validate(String strPublicKey, String str, BigInteger rSignature, BigInteger sSignature)
        throws Exception {
        EccPoint publicKey = new EccPoint(strPublicKey);
        return this.validate(publicKey, str, this.hasher, rSignature, sSignature);
    }

    public boolean validate(BigInteger x, BigInteger y, String str, BigInteger rSignature, BigInteger sSignature)
        throws Exception {
        return this.validate(x,y, str, this.hasher, rSignature, sSignature);
    }

    public boolean validate(BigInteger x, BigInteger y, String str, EccHasher hasher, BigInteger rSignature, BigInteger sSignature)
        throws Exception {
        EccPoint publicKey = new EccPoint(x, y, this.curve);
        return this.validate(publicKey, str, hasher, rSignature, sSignature);
    }

    public boolean validate(EccPoint publicKey, String str, EccHasher hasher, BigInteger rSignature, BigInteger sSignature)
        throws Exception {
        EccSignature signature = new EccSignature(rSignature, sSignature);
        return this.validate(publicKey, str, hasher, signature);
    }

    public boolean validate(EccPoint publicKey, String str, EccHasher hasher, EccSignature signature) throws Exception {
        return this.validate(publicKey, str.getBytes(), hasher, signature);
    }

    public boolean validate(EccPoint publicKey, byte[] bytes, EccHasher hasher, EccSignature signature) throws
        Exception {
        return EccSign.INSTANCE.verifySignature(publicKey, bytes, hasher, signature);
    }

    static {
        INSTANCE = new EccMain();
    }
}
