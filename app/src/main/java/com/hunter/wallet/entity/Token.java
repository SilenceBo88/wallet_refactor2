package com.hunter.wallet.entity;

import com.hunter.wallet.enums.TokenType;

import java.io.Serializable;

public abstract class Token implements Serializable {

    protected String name;
    protected String symbol;
    protected Integer icon;
    protected String iconUrl;

    public Token(String name, String symbol, Integer icon, String iconUrl) {
        this.name = name;
        this.symbol = symbol;
        this.icon = icon;
        this.iconUrl = iconUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getIcon() {
        return icon;
    }

    public void setIcon(Integer icon) {
        this.icon = icon;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    /**
     * 代币类型
     *
     * @return
     */
    public abstract TokenType getTokenType();

    /**
     * 相对唯一标识
     *
     * @return
     */
    public abstract String getPrimary();

    /**
     * 绝对唯一标识
     * @return
     */
    public String getUid() {
        return getTokenType() + getPrimary();
    }
}
