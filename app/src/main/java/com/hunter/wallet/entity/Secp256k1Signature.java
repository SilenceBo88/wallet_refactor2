package com.hunter.wallet.entity;

public class Secp256k1Signature {
    private byte[] r;
    private byte[] s;

    public Secp256k1Signature(byte[] r, byte[] s) {
        this.r = r;
        this.s = s;
    }

    public byte[] getR() {
        return r;
    }

    public void setR(byte[] r) {
        this.r = r;
    }

    public byte[] getS() {
        return s;
    }

    public void setS(byte[] s) {
        this.s = s;
    }
}
