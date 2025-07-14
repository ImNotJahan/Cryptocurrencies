package com.jahanrashidi.crypto.core.transactions;

import com.jahanrashidi.crypto.core.Util;
import com.starkbank.ellipticcurve.PublicKey;

import java.util.ArrayList;

public class UnspentTxOut {
    private final byte[] txOutId;
    private final int txOutIndex;
    private final PublicKey address;
    private final int amount;

    public UnspentTxOut(byte[] txOutId, int txOutIndex, PublicKey address, int amount){
        this.txOutId = txOutId;
        this.txOutIndex = txOutIndex;
        this.address = address;
        this.amount = amount;
    }

    public byte[] txOutId(){
        return txOutId;
    }

    public int txOutIndex(){
        return txOutIndex;
    }

    public PublicKey address(){
        return address;
    }

    public int amount(){
        return amount;
    }

    // uTxO - unspent transaction output
    public static ArrayList<UnspentTxOut> updateUTxOs(Transaction[] newTransactions, UnspentTxOut[] unspentTxOuts){
        ArrayList<UnspentTxOut> resultingUTxO = new ArrayList<UnspentTxOut>();

        // add new transactions as uTxOs
        for(Transaction transaction : newTransactions)
            for(int i = 0; i < transaction.outputs.length; i++)
                resultingUTxO.add(new UnspentTxOut(transaction.id(), i, transaction.outputs[i].address(), transaction.outputs[i].amount()));

        ArrayList<UnspentTxOut> consumedTxO = new ArrayList<UnspentTxOut>();

        for(Transaction transaction : newTransactions)
            for(TxIn input : transaction.inputs)
                consumedTxO.add(new UnspentTxOut(input.txOutId(), input.txOutIndex(), null, 0));

        // add in old uTxOs which were not consumed yet
        for(UnspentTxOut uTxO : unspentTxOuts)
            if(Util.findUnspentTxOut(uTxO.txOutId(), uTxO.txOutIndex(), consumedTxO.toArray(new UnspentTxOut[0])) == null)
                resultingUTxO.add(uTxO);

        return resultingUTxO;
    }

    public String toString(){
        return Util.bytesToString(address.toDer().getBytes());
    }
}
