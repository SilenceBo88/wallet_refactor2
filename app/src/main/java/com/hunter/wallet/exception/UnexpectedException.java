package com.hunter.wallet.exception;

public class UnexpectedException extends Exception {

    public UnexpectedException(int errorCode) {
        super(String.format("Error code : %08X", errorCode));
    }
}
