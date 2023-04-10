package com.fsck.k9.custom_encrypt.ecc;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class EccSha256 implements EccHasher {
    public static final EccSha256 INSTANCE;

    public byte[] hash(byte[] data) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256").digest(data);
    }

    private EccSha256() {
    }

    static {
        INSTANCE = new EccSha256();
    }
}
