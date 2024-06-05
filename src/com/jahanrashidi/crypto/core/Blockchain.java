package com.jahanrashidi.crypto.core;

import com.jahanrashidi.crypto.Main;
import com.jahanrashidi.crypto.core.transactions.Transaction;
import com.jahanrashidi.crypto.core.transactions.UnspentTxOut;
import com.starkbank.ellipticcurve.PublicKey;

import java.util.ArrayList;

public class Blockchain {
    private ArrayList<Block> chain = new ArrayList<Block>();
    private ArrayList<UnspentTxOut> unspentTxOuts = new ArrayList<UnspentTxOut>();
    private final Block genesisBlock;

    // for difficulty calculation
    private final int GENERATION_INTERVAL = 3600; // (ideal) seconds per block find (one hour)
    private final int ADJUSTMENT_INTERVAL = 100; // how many blocks until difficulty readjusted

    public Blockchain(Block genesisBlock){
        this.genesisBlock = genesisBlock;
        chain.add(genesisBlock);
    }

    public Blockchain(ArrayList<Block> chain){
        assert chain.size() > 0;

        this.chain = chain;
        this.genesisBlock = chain.get(0);
    }

    public Blockchain(){
        this(new Block(0, Util.unixTime(), new Transaction[0], new byte[0], 0, 0));
    }

    public int length(){
        return chain.size();
    }

    public Block genesis(){
        return genesisBlock;
    }

    public UnspentTxOut[] unspentTxOuts(){
        return unspentTxOuts.toArray(new UnspentTxOut[0]);
    }

    public Block top(){
        assert length() > 0;
        return chain.get(chain.size() - 1);
    }

    public ArrayList<Block> getChain(){
        return chain;
    }

    public boolean isChainValid(){
        // first block ought to be genesis block
        if(!genesisBlock.equals(chain.get(0))) return false;

        for(int i = 1; i < chain.size(); i++){
            // check if block is valid in comparison to last block
            if(!chain.get(i).isValidBlock(chain.get(i - 1))) return false;
        }

        return true;
    }

    /**
     * When an alternate blockchain arises, the (valid) chain with higher cumulative difficulty is chosen.
     * @param chain Alternative blockchain
     * @return If the blockchain was replaced
     */
    public boolean replaceChain(Blockchain chain){
        // CD - cumulative difficulty
        int currentChainCD = 0;
        for(Block block : this.chain) currentChainCD += Math.pow(2, block.getDifficulty());

        int proposedChainCD = 0;
        for(Block block : chain.getChain()) proposedChainCD += Math.pow(2, block.getDifficulty());

        // chain with greater CD is chosen
        if(proposedChainCD > currentChainCD) {
            this.chain = chain.getChain();

            unspentTxOuts = new ArrayList<UnspentTxOut>();

            for(Block block : chain.chain){
                unspentTxOuts = UnspentTxOut.updateUTxOs(block.data(), unspentTxOuts.toArray(new UnspentTxOut[0]));
            }
            return true;
        }

        return false;
    }

    /**
     * Creates a new block and adds it to the chain
     * @return The block created
     */
    public Block generateRawBlock(Transaction[] data){
        Block block = findBlock(length(), Util.unixTime(), data, top().getHash(), calculateDifficulty());
        chain.add(block);
        unspentTxOuts = UnspentTxOut.updateUTxOs(data, unspentTxOuts());

        return block;
    }

    public Block generateBlock(){
        Transaction coinbase = Transaction.getCoinbaseTransaction(Main.wallet.publicKey(), length());

        return generateRawBlock(new Transaction[]{coinbase});
    }

    public Block generateBlockWithTransaction(PublicKey address, int amount) throws Exception {
        Transaction coinbase = Transaction.getCoinbaseTransaction(Main.wallet.publicKey(), length());
        Transaction transaction = Main.wallet.createTransaction(address, amount, unspentTxOuts.toArray(new UnspentTxOut[0]));

        return generateRawBlock(new Transaction[]{coinbase, transaction});
    }

    /**
     * Finds a block with the correct hash for the given difficulty.
     */
    private Block findBlock(int index, long timestamp, Transaction[] data, byte[] previousHash, int difficulty){
        Block block;
        int nonce = 0;

        // Try to create block with different nonce (arbitrary) value until one matching difficulty made
        do {
            block = new Block(index, timestamp, data, previousHash, difficulty, nonce);
            nonce++;
        } while(!block.validateDifficulty());

        return block;
    }

    /**
     * Calculates what the difficulty for the next block ought to be
     */
    private int calculateDifficulty(){
        final Block lastBlock = top();

        if(lastBlock.getIndex() % ADJUSTMENT_INTERVAL == 0 && lastBlock.getIndex() != 0)
            return calculateAdjustedDifficulty();
        return lastBlock.getDifficulty();
    }

    // recalculates difficulty based on time taken to find last block
    private int calculateAdjustedDifficulty(){
        final Block lastAdjustmentBlock = chain.get(length() - ADJUSTMENT_INTERVAL);
        final int lastDifficulty = lastAdjustmentBlock.getDifficulty();

        // amount of time expected to generate all blocks since last adjustment
        final long expectedTime = GENERATION_INTERVAL * ADJUSTMENT_INTERVAL;
        // amount of time actually taken
        final long takenTime = top().getTime() - lastAdjustmentBlock.getTime();

        // if far too fast, increase difficulty
        if(takenTime < expectedTime / 2)
            return lastDifficulty + 1;
        // if far too slow, decrease difficulty
        else if(takenTime > expectedTime * 2)
            return lastDifficulty - 1;
        // otherwise, keep same
        return lastDifficulty;
    }

    public String toString(){
        StringBuilder builder = new StringBuilder();

        for(Block block : chain){
            builder.append(block);
            builder.append('\n');
            builder.append('\n');
        }

        return builder.toString();
    }

    // for sending to peers
    public String encode(){
        StringBuilder builder = new StringBuilder();

        for(Block block : chain){
            builder.append(block.encode());
            builder.append(';');
        }

        return builder.toString();
    }
}
