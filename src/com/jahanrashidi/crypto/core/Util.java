package com.jahanrashidi.crypto.core;

import com.jahanrashidi.crypto.core.transactions.Transaction;
import com.jahanrashidi.crypto.core.transactions.UnspentTxOut;
import com.starkbank.ellipticcurve.Curve;
import com.starkbank.ellipticcurve.Point;
import com.starkbank.ellipticcurve.PublicKey;
import com.starkbank.ellipticcurve.utils.ByteString;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class Util {
    // https://stackoverflow.com/a/5531479
    /**
     * Hashes byte array in SHA256 (for block hashes).
     */
    public static byte[] hash(byte[] data){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(data);
        } catch(NoSuchAlgorithmException exception){
            System.out.println("SHA-256 is not a valid algorithm.");
            return new byte[0];
        }
    }

    // https://stackoverflow.com/a/11529308
    public static String bytesToBinary( byte[] bytes ) {
        StringBuilder sb = new StringBuilder(bytes.length * Byte.SIZE);
        for( int i = 0; i < Byte.SIZE * bytes.length; i++ )
            sb.append((bytes[i / Byte.SIZE] << i % Byte.SIZE & 0x80) == 0 ? '0' : '1');
        return sb.toString();
    }

    // Assumes string hash is given in hex
    public static byte[] stringHashToByte(String hash){
        if(hash.equals("")) return new byte[0];

        byte[] byteHash = new byte[hash.length() / 2];

        // 2 char per byte
        // https://stackoverflow.com/a/19119453
        for (int i = 0; i < hash.length(); i += 2) {
            byteHash[i / 2] = (byte) ((Character.digit(hash.charAt(i), 16) << 4)
                    + Character.digit(hash.charAt(i+1), 16));
        }

        return byteHash;
    }

    /**
     * Converts byte array to hexadecimal representation as a string
     */
    public static String bytesToString(byte[] bytes){
        StringBuilder byteString = new StringBuilder();
        for(byte aByte : bytes) byteString.append(String.format("%02x", aByte));

        return byteString.toString();
    }

    public static long unixTime(){
        return System.currentTimeMillis() / 1000L;
    }

    /**
     * Converts encoded chain (str from peer) to blockchain object
     * @param chain Encoded blockchain
     * @return Decoded blockchain
     */
    public static Blockchain parseChain(String chain){
        ArrayList<Block> tempChain = new ArrayList<Block>();

        String[] blocks = chain.split(";");

        for (String s : blocks) {
            String[] block = s.split(",");
            Transaction[] data = new Transaction[0];

            if(!block[2].isEmpty()) {
                String[] encodedTransactions = block[2].split(":");
                data = new Transaction[encodedTransactions.length];

                for (int i = 0; i < data.length; i++)
                    data[i] = Transaction.decode(encodedTransactions[i]);
            }

            tempChain.add(
                    new Block(
                            Integer.parseInt(block[0]),
                            Long.parseLong(block[1]),
                            data,
                            stringHashToByte(block[3]),
                            stringHashToByte(block[4]),
                            Integer.parseInt(block[5]),
                            Integer.parseInt(block[6])));
        }

        return new Blockchain(tempChain);
    }

    public static UnspentTxOut findUnspentTxOut(byte[] txOutId, int txOutIndex, UnspentTxOut[] allUnspentTx){
        for(UnspentTxOut unspentTx : allUnspentTx){
            if(Arrays.equals(txOutId, unspentTx.txOutId()) && txOutIndex == unspentTx.txOutIndex())
                return unspentTx;
        }

        return null;
    }

    public static PublicKey stringToPublicKey(String key){
        return PublicKey.fromDer(new ByteString(stringHashToByte(key)));
    }

    // various methods for testing the equality of ECDSA objects

    public static boolean publicKeysEqual(PublicKey key1, PublicKey key2){
        // compare points
        if(!pointsEqual(key1.point, key2.point)) return false;

        // compare curves
        return curvesEqual(key1.curve, key2.curve);
    }

    public static boolean pointsEqual(Point p1, Point p2){
        //noinspection SuspiciousNameCombination
        return p1.x.equals(p2.x) && p1.y.equals(p2.y) && p1.z.equals(p2.z);
    }

    public static boolean curvesEqual(Curve c1, Curve c2){
        if(!c1.A.equals(c2.A)) return false;
        if(!c1.B.equals(c2.B)) return false;
        if(!c1.P.equals(c2.P)) return false;
        if(!c1.N.equals(c2.N)) return false;
        if(!pointsEqual(c1.G, c2.G)) return false;
        if(!c1.name.equals(c2.name)) return false;
        return Arrays.equals(c1.oid, c2.oid);
    }
}
