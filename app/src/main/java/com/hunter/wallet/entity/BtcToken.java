package com.hunter.wallet.entity;

import com.hunter.wallet.enums.TokenType;

import java.util.Objects;

public class BtcToken extends Token {

    private String blockchain;

    public BtcToken(String name, String symbol, Integer icon, String iconUrl, String blockchain) {
        super(name, symbol, icon, iconUrl);
        this.blockchain = blockchain;
    }

    @Override
    public TokenType getTokenType() {
        return TokenType.btc;
    }

    @Override
    public String getPrimary() {
        return blockchain;
    }

    public String getBlockchain() {
        return blockchain;
    }

    public void setBlockchain(String blockchain) {
        this.blockchain = blockchain;
    }

    @Override
    public String toString() {
        return "BtcToken{" +
                "blockchain='" + blockchain + '\'' +
                ", name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", icon=" + icon +
                ", iconUrl='" + iconUrl + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BtcToken btcToken = (BtcToken) o;
        return Objects.equals(blockchain, btcToken.blockchain);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockchain);
    }
}
