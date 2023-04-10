package com.fsck.k9.custom_encrypt.ecc;

import java.security.NoSuchAlgorithmException;

public interface EccHasher {
    byte[] hash(byte[] data) throws Exception;
}
