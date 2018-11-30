package com.hunter.wallet.entity;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

public class EthTransfer implements Serializable {

    public static final int STATUS_UNCOMMIT = 0;
    public static final int STATUS_ERROR = -1;
    public static final int STATUS_SUCCESS = 1;

    private byte[] from;
    private byte[] to;
    private BigInteger value;
    private BigInteger gas;
    private BigInteger gasUsed;
    private BigInteger gasPrice;
    private Date time;
    private String hash;
    private int status;
    private String blockNumber;

    public byte[] getFrom() {
        return from;
    }

    public void setFrom(byte[] from) {
        this.from = from;
    }

    public byte[] getTo() {
        return to;
    }

    public void setTo(byte[] to) {
        this.to = to;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public BigInteger getGas() {
        return gas;
    }

    public void setGas(BigInteger gas) {
        this.gas = gas;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }


    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getBlockNumber() {
        return blockNumber;
    }

    public void setBlockNumber(String blockNumber) {
        this.blockNumber = blockNumber;
    }

    public BigInteger getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(BigInteger gasUsed) {
        this.gasUsed = gasUsed;
    }

    @Override
    public String toString() {
        return "EthTransfer{" +
                "from=" + Arrays.toString(from) +
                ", to=" + Arrays.toString(to) +
                ", value=" + value +
                ", gas=" + gas +
                ", gasPrice=" + gasPrice +
                ", time=" + time +
                ", hash='" + hash + '\'' +
                ", status=" + status +
                ", blockNumber='" + blockNumber + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EthTransfer ethTransfer = (EthTransfer) o;
        return Objects.equals(hash, ethTransfer.hash);
    }

    @Override
    public int hashCode() {

        return Objects.hash(hash);
    }
}
