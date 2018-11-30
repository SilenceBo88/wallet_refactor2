package com.hunter.wallet.entity;

import org.web3j.crypto.Keys;

import java.io.Serializable;
import java.util.Arrays;

public class WalletInfo implements Serializable {

    private int id;
    private String name;
    private byte[] pubkey;
    private boolean hasLock;
    private int failTimes;
    private boolean hasMne;


    @Override
    public String toString() {
        return "WalletInfo{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", pubkey=" + Arrays.toString(pubkey) +
                ", hasLock=" + hasLock +
                ", failTimes=" + failTimes +
                ", hasMne=" + hasMne +
                '}';
    }

    public WalletInfo() {
    }

    public WalletInfo(int id, String name, byte[] pubkey, boolean hasLock, int failTimes, boolean hasMne) {
        this.id = id;
        this.name = name;
        this.pubkey = pubkey;
        this.hasLock = hasLock;
        this.failTimes = failTimes;
        this.hasMne = hasMne;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getPubkey() {
        return pubkey;
    }

    public void setPubkey(byte[] pubkey) {
        this.pubkey = pubkey;
    }

    public boolean isHasLock() {
        return hasLock;
    }

    public void setHasLock(boolean hasLock) {
        this.hasLock = hasLock;
    }

    public int getFailTimes() {
        return failTimes;
    }

    public void setFailTimes(int failTimes) {
        this.failTimes = failTimes;
    }

    public boolean isHasMne() {
        return hasMne;
    }

    public void setHasMne(boolean hasMne) {
        this.hasMne = hasMne;
    }

    public byte[] getAddr() {
        return Keys.getAddress(pubkey);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WalletInfo that = (WalletInfo) o;
        return Arrays.equals(pubkey, that.pubkey);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(pubkey);
    }
}
