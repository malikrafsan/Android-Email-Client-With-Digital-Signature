package com.fsck.k9.keccak;


import com.fsck.k9.custom_encrypt.ecc.EccHasher;


public final class KeccakHash implements EccHasher {
    public static final KeccakHash INSTANCE;
    @Override
    public byte[] hash(byte[] data) throws Exception {
        return Keccak.INSTANCE.digest(data);
    }

    private KeccakHash() {
    }

    static {
        INSTANCE = new KeccakHash();
    }
}
