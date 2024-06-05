package com.jahanrashidi.crypto.core;

import com.jahanrashidi.crypto.core.transactions.Transaction;
import com.jahanrashidi.crypto.core.transactions.TxIn;
import com.jahanrashidi.crypto.core.transactions.TxOut;
import com.jahanrashidi.crypto.core.transactions.UnspentTxOut;
import com.starkbank.ellipticcurve.PrivateKey;
import com.starkbank.ellipticcurve.PublicKey;

import java.util.ArrayList;

public class Wallet {
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    // for creating a new wallet
    public Wallet(){
        privateKey = new PrivateKey();
        publicKey = privateKey.publicKey();
    }

    // for loading an existing wallet
    public Wallet(PrivateKey privateKey){
        this.privateKey = privateKey;
        publicKey = privateKey.publicKey();
    }

    public PrivateKey privateKey() {
        return privateKey;
    }

    public PublicKey publicKey(){
        return publicKey;
    }

    public int balance(UnspentTxOut[] unspentTxOuts){
        int total = 0;

        for(UnspentTxOut uTxO : findOwnedUTxOs(unspentTxOuts))
            total += uTxO.amount();

        return total;
    }

    // finds unspent transaction outputs owned by the wallet to convert into inputs for sending coins
    public InputsForTx findTxOutsForAmount(int amount, UnspentTxOut[] ownedUTxOs) throws Exception {
        int total = 0;
        ArrayList<UnspentTxOut> TxOsToSpend = new ArrayList<UnspentTxOut>();

        for(UnspentTxOut uTxO : ownedUTxOs){
            TxOsToSpend.add(uTxO);
            total += uTxO.amount();

            if(total >= amount){
                return new InputsForTx(TxOsToSpend.toArray(new UnspentTxOut[0]), total - amount);
            }
        }

        throw new Exception("Not enough coins for transaction.");
    }

    public UnspentTxOut[] findOwnedUTxOs(UnspentTxOut[] unspentTxOuts){
        ArrayList<UnspentTxOut> ownedUTxOs = new ArrayList<UnspentTxOut>();

        for(UnspentTxOut unspentTxOut : unspentTxOuts)
            if(Util.publicKeysEqual(unspentTxOut.address(), publicKey))
                ownedUTxOs.add(unspentTxOut);

        return ownedUTxOs.toArray(new UnspentTxOut[0]);
    }

    public Transaction createTransaction(PublicKey receiver, int amount, UnspentTxOut[] unspentTxOuts) throws Exception {
        UnspentTxOut[] ownedUTxOs = findOwnedUTxOs(unspentTxOuts);
        InputsForTx inputsForTx = findTxOutsForAmount(amount, ownedUTxOs);

        TxIn[] unsignedTxIns = new TxIn[inputsForTx.inputs().length];

        for(int i = 0; i < unsignedTxIns.length; i++){
            UnspentTxOut unspentTxOut = inputsForTx.inputs()[i];
            TxIn txIn = new TxIn(unspentTxOut.txOutId(), unspentTxOut.txOutIndex());

            unsignedTxIns[i] = txIn;
        }

        TxOut[] txOuts = createTxOuts(receiver, amount, inputsForTx.remainder());

        // transaction id is calculated without TxIn signatures, so can put in unsignedTxIns
        Transaction transaction = new Transaction(txOuts, unsignedTxIns);

        // TxIn signature requires transaction id, which is why we wait until now to sign them
        for(TxIn txIn : unsignedTxIns)
            txIn.sign(transaction, privateKey, unspentTxOuts);

        return transaction;
    }

    private TxOut[] createTxOuts(PublicKey receiver, int amount, int remainder){
        if(remainder == 0) return new TxOut[]{new TxOut(receiver, amount)};

        TxOut[] txOuts = new TxOut[2];
        txOuts[0] = new TxOut(receiver, amount);
        txOuts[1] = new TxOut(publicKey, remainder);

        return txOuts;
    }

    public String toString(){
        String encoding = "";
        encoding += "Private key: ";
        encoding += Util.privateKeyToString(privateKey);
        encoding += "\n";
        encoding += "Public key: ";
        encoding += Util.publicKeyToString(publicKey);

        return encoding;
    }
}

class InputsForTx {
    private final UnspentTxOut[] inputs;
    private final int remainder;

    public InputsForTx(UnspentTxOut[] inputs, int remainder){
        this.inputs = inputs;
        this.remainder = remainder;
    }

    public UnspentTxOut[] inputs() {
        return inputs;
    }

    public int remainder(){
        return remainder;
    }
}