package com.hunter.wallet.entity;

import com.hunter.wallet.enums.TokenType;

import org.web3j.utils.Numeric;

import java.io.Serializable;
import java.util.Arrays;

public class EthToken extends Token implements Serializable {

    public static final int TOKEN_TYPE_ETH = 0;
    public static final int TOKEN_TYPE_DEFAULT = 1;
    public static final int TOKEN_TYPE_PRE = 2;
    public static final int TOKEN_TYPE_IMPORT = 3;

    private byte[] contract;
    private int decimals;
    private int type;

    public EthToken(String name, String symbol, Integer icon, String iconUrl, byte[] contract, int decimals, int type) {
        super(name, symbol, icon, iconUrl);
        this.contract = contract;
        this.decimals = decimals;
        this.type = type;
    }

    public byte[] getContract() {
        return contract;
    }

    public void setContract(byte[] contract) {
        this.contract = contract;
    }

    @Override
    public TokenType getTokenType() {
        return TokenType.eth;
    }

    @Override
    public String getPrimary() {
        return Numeric.toHexString(getContract());
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EthToken ethToken = (EthToken) o;
        return Arrays.equals(contract, ethToken.contract);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(contract);
    }
}
