package com.jahanrashidi.crypto.core.transactions;

import com.starkbank.ellipticcurve.PublicKey;

/**
 * Transaction output
 */
public class TxOut {
    private final PublicKey address;
    private final int amount;

    public TxOut(PublicKey address, int amount){
        this.address = address;
        this.amount = amount;
    }

    public PublicKey address(){
        return address;
    }

    public int amount(){
        return amount;
    }
}
