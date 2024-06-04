package com.jahanrashidi.crypto.core.transactions;

import com.jahanrashidi.crypto.core.Util;
import com.starkbank.ellipticcurve.Ecdsa;
import com.starkbank.ellipticcurve.PrivateKey;
import com.starkbank.ellipticcurve.PublicKey;
import com.starkbank.ellipticcurve.Signature;

import java.rmi.NoSuchObjectException;
import java.security.InvalidKeyException;

/**
 * Transaction input
 */
public class TxIn {
    private byte[] txOutId = new byte[0];
    private final int txOutIndex;
    private Signature signature; // proves user has ownership of txOut

    public TxIn(byte[] txOutId, Transaction transaction, PrivateKey privateKey, UnspentTxOut[] unspentTx, int txOutIndex) throws NoSuchObjectException, InvalidKeyException {
        this.txOutId =  txOutId;
        this.txOutIndex = txOutIndex;
        this.signature = sign(transaction, privateKey, unspentTx);
    }

    public TxIn(byte[] txOutId, int txOutIndex){
        this.txOutId = txOutId;
        this.txOutIndex = txOutIndex;
    }

    public TxIn(int txOutIndex){
        this.txOutIndex = txOutIndex;
    }

    public int txOutIndex(){
        return txOutIndex;
    }

    public byte[] txOutId(){
        return txOutId;
    }

    public int amount(UnspentTxOut[] unspentTxOuts){
        UnspentTxOut referencedUTxO = Util.findUnspentTxOut(txOutId, txOutIndex, unspentTxOuts);

        if(referencedUTxO == null) return 0;
        return referencedUTxO.amount();
    }

    public boolean isValid(Transaction transaction, UnspentTxOut[] unspentTxOuts){
        // does the referenced TxOut exist?
        UnspentTxOut referencedUTxO = Util.findUnspentTxOut(txOutId, txOutIndex, unspentTxOuts);
        if(referencedUTxO == null) return false;

        // is the signature correct?
        PublicKey address = referencedUTxO.address();
        return Ecdsa.verify(Util.bytesToString(transaction.id()), signature, address);
    }

    /**
     * Signs transaction if doing so is valid
     * @param transaction Transaction to be signed
     * @param privateKey Private key of output owner
     * @param unspentTx All currently unspent transactions
     * @return Transaction signature
     */
    public Signature sign(Transaction transaction, PrivateKey privateKey, UnspentTxOut[] unspentTx) throws NoSuchObjectException, InvalidKeyException {
        final byte[] dataToSign = transaction.id();
        final UnspentTxOut referencedOutput = Util.findUnspentTxOut(txOutId, txOutIndex, unspentTx);

        if(referencedOutput == null) throw new NoSuchObjectException("Cannot find referenced unspent transaction output.");
        if(!Util.publicKeysEqual(privateKey.publicKey(), referencedOutput.address()))
            throw new InvalidKeyException("Private key does not match output ownership");

        return Ecdsa.sign(Util.bytesToString(transaction.id()), privateKey);
    }
}
