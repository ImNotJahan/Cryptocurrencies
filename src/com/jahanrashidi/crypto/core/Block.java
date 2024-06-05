package com.jahanrashidi.crypto.core;

import com.jahanrashidi.crypto.core.transactions.Transaction;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class Block {
    private final int index;
    private final long timestamp;
    private final Transaction[] data;
    private final byte[] hash;
    private final byte[] previousHash;
    private final int difficulty;
    private final int nonce; // arbitrary value to make hash match difficulty

    /**
     * Creates block with predetermined hash
     * @param index Block's height in chain
     * @param timestamp Time of block creation
     * @param data Transaction data included in the block
     * @param hash SHA256 hash of block content
     * @param previousHash Hash of last block in chain
     */
    public Block(int index, long timestamp, Transaction[] data, byte[] hash, byte[] previousHash, int difficulty, int nonce){
        this.index = index;
        this.timestamp = timestamp;
        this.data = data;
        this.hash = hash;
        this.previousHash = previousHash;
        this.difficulty = difficulty;
        this.nonce = nonce;
    }

    /**
     * Creates block and auto generates hash
     * @param index Block's height in chain
     * @param timestamp Time of block creation
     * @param data Transaction data included in the block
     * @param previousHash Hash of last block in chain
     */
    public Block(int index, long timestamp, Transaction[] data, byte[] previousHash, int difficulty, int nonce){
        this.index = index;
        this.timestamp = timestamp;
        this.data = data;
        this.previousHash = previousHash;
        this.difficulty = difficulty;
        this.nonce = nonce;

        byte[] sum = combineBytes();
        this.hash = Util.hash(sum);
    }

    public byte[] getHash(){
        return hash;
    }

    public int getIndex(){
        return index;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public long getTime() {
        return timestamp;
    }

    public boolean isValidBlock(Block lastBlock){
        // is valid in relation to previous block?
        if(lastBlock.getIndex() + 1 != index) return false;
        if(!Arrays.equals(lastBlock.getHash(), previousHash)) return false;
        // is hash valid?
        return validateHash();
    }

    /**
     * @return If the block content matches the hash value and hash matches difficulty
     */
    public boolean validateHash(){
        byte[] sum = combineBytes();
        // check if block content matches hash
        if(!Arrays.equals(this.hash, Util.hash(sum))) return false;

        // check if hash matches difficulty
        return validateDifficulty();
    }

    /**
     * @return If the block hash matches difficulty
     */
    public boolean validateDifficulty(){
        // convert hash (as bytes) to binary
        String hash = Util.bytesToBinary(this.hash);

        // to be of valid difficulty, the (binary) hash must start with x amount of ones, x being difficulty
        for(int i = 0; i < difficulty; i++)
            if(hash.charAt(i) != '1') return false;
        return true;
    }

    // https://stackoverflow.com/a/5683621

    /**
     * Combines the bytes of some block properties for hashing.
     */
    private byte[] combineBytes(){
        StringBuilder dataString = new StringBuilder();
        for(Transaction transaction : data) dataString.append(transaction);

        byte[] dataBytes = dataString.toString().getBytes();

        // 12 comes from 4 for each int (index, difficulty, nonce) and 8 for timestamp
        ByteBuffer buff = ByteBuffer.allocate(20 + dataBytes.length + previousHash.length);
        buff.putInt(index);
        buff.putLong(timestamp);
        buff.put(dataBytes);
        buff.put(previousHash);
        buff.putInt(difficulty);
        buff.putInt(nonce);

        return buff.array();
    }

    public boolean equals(Block block){
        return block.toString().equals(toString());
    }

    public String toString(){
        String encoding = "";
        encoding += "Block " + index + "\n";
        encoding += "Hash: " + Util.bytesToString(hash) + "\n";
        encoding += "From: " + Util.bytesToString(previousHash) + "\n";
        encoding += "Made: " + timestamp + "\n";
        encoding += "Data: " + Arrays.toString(data);

        return encoding;
    }

    public Transaction[] data(){
        return data;
    }

    /**
     * Encodes block for sending to peers
     */
    public String encode(){
        StringBuilder encoding = new StringBuilder();
        encoding.append(index).append(",");
        encoding.append(timestamp).append(",");

        for(Transaction tx : data) encoding.append(tx.encode()).append(":");
        encoding.append(",");

        encoding.append(Util.bytesToString(hash)).append(",");
        encoding.append(Util.bytesToString(previousHash)).append(",");
        encoding.append(difficulty).append(",");
        encoding.append(nonce);

        return encoding.toString();
    }
}
