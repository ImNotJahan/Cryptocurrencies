package com.jahanrashidi.crypto.core.transactions;

import com.jahanrashidi.crypto.core.Util;
import com.starkbank.ellipticcurve.PublicKey;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Transaction {
    private final byte[] id;
    public TxOut[] outputs;
    public TxIn[] inputs;

    // coins earned per mined block
    private static final int COINBASE_AMOUNT = 20;

    public Transaction(TxOut[] outputs, TxIn[] inputs){
        this.outputs = outputs;
        this.inputs = inputs;

        id = calculateId();
    }

    public byte[] id(){
        return id;
    }

    // id is hash of combination of outputs and inputs
    private byte[] calculateId(){
        int size = 0;
        // 88 for address, 4 for amount
        size += outputs.length * (88 + 4);
        // 4 for index, 32 for id
        size += inputs.length * (4 + 32);

        ByteBuffer buff = ByteBuffer.allocate(size);

        for(TxOut output : outputs) {
            buff.putInt(output.amount());
            buff.put(output.address().toDer().getBytes());
        }

        for(TxIn input : inputs){
            buff.putInt(input.txOutIndex());
            buff.put(input.txOutId());
        }

        return Util.hash(buff.array());
    }

    public boolean isValid(UnspentTxOut[] unspentTxOuts){
        if(!Arrays.equals(calculateId(), id)) return false;

        for(TxIn input : inputs)
            if(!input.isValid(this, unspentTxOuts))
                return false;

        int totalTxOutAmount = 0;
        for(TxOut output : outputs) totalTxOutAmount += output.amount();

        int totalTxInAmount = 0;
        for(TxIn input : inputs) totalTxInAmount += input.amount(unspentTxOuts);

        return totalTxOutAmount == totalTxInAmount;
    }

    public boolean isValidCoinbase(int blockIndex){
        if(inputs.length != 1) return false;
        if(inputs[0].txOutIndex() != blockIndex) return false;
        if(outputs.length != 1) return false;
        return outputs[0].amount() == COINBASE_AMOUNT;
    }

    public static Transaction getCoinbaseTransaction(PublicKey address, int blockIndex){
        final TxIn[] txIns = {new TxIn(blockIndex)};
        final TxOut[] txOuts = {new TxOut(address, COINBASE_AMOUNT)};

        return new Transaction(txOuts, txIns);
    }

    public String encode(){
        StringBuilder result = new StringBuilder();

        for(TxIn input : inputs){
            result.append(input.txOutIndex()).append(">");
            result.append(Util.bytesToString(input.txOutId())).append(".");
        }

        result.append('_');

        for(TxOut output : outputs){
            result.append(Util.bytesToString(output.address().toDer().getBytes()))
                            .append('>');
            result.append(output.amount()).append('.');
        }

        return result.toString();
    }

    public String toString(){
        StringBuilder result = new StringBuilder();

        for(TxIn input : inputs){
            result.append(input.txOutIndex());
            result.append('\n');
            result.append(Util.bytesToString(input.txOutId()));
        }

        result.append('\n');
        result.append('\n');

        for(TxOut output : outputs){
            result.append(Util.bytesToString(output.address().toDer().getBytes()));
            result.append('\n');
            result.append(output.amount());
        }

        result.append('\n');

        return encode();
        //return Util.bytesToString(id);
    }
}