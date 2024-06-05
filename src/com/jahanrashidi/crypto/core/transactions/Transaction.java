package com.jahanrashidi.crypto.core.transactions;

import com.jahanrashidi.crypto.core.Util;
import com.starkbank.ellipticcurve.PublicKey;
import com.starkbank.ellipticcurve.Signature;

import java.math.BigInteger;
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

    public Transaction(TxOut[] outputs, TxIn[] inputs, byte[] id){
        this.outputs = outputs;
        this.inputs = inputs;
        this.id = id;
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
            // tx output id > tx output index > signature r value > signature s value .
            result.append(Util.bytesToString(input.txOutId())).append(">");
            result.append(input.txOutIndex()).append(">");

            if(input.signature() != null) {
                result.append(input.signature().r).append(">");
                result.append(input.signature().s).append(".");
            }
        }

        result.append('_');

        for(TxOut output : outputs){
            if(output == null) continue;

            // output address > amount .
            result.append(Util.bytesToString(output.address().toDer().getBytes()))
                            .append('>');
            result.append(output.amount()).append('.');
        }

        result.append('_').append(Util.bytesToString(id));

        return result.toString();
    }

    // I really need to switch over to json
    public static Transaction decode(String encoded){
        String[] parts = encoded.split("_");

        String[] encodedInputs = parts[0].split("\\.");
        String[] encodedOutputs = parts[1].split("\\.");

        TxIn[] inputs = new TxIn[encodedInputs.length];
        for(int i = 0; i < inputs.length; i++){
            String[] inputParts = encodedInputs[i].split(">");
            TxIn input;

            if(inputParts.length == 4)
                input = new TxIn(Util.stringHashToByte(inputParts[0]), Integer.parseInt(inputParts[1]),
                        new Signature(new BigInteger(inputParts[2]), new BigInteger(inputParts[3])));
            else
                input = new TxIn(Util.stringHashToByte(inputParts[0]), Integer.parseInt(inputParts[1]));
            inputs[i] = input;
        }

        TxOut[] outputs = new TxOut[encodedOutputs.length];
        for(int i = 0; i < outputs.length; i++){
            String[] outputParts = encodedOutputs[i].split(">");
            TxOut output = new TxOut(Util.stringToPublicKey(outputParts[0]), Integer.parseInt(outputParts[1]));

            outputs[i] = output;
        }

        return new Transaction(outputs, inputs, Util.stringHashToByte(parts[2]));
    }

    public String toString(){
        return encode();
    }
}