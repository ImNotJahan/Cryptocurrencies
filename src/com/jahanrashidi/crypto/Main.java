package com.jahanrashidi.crypto;

import com.jahanrashidi.crypto.core.Block;
import com.jahanrashidi.crypto.core.Blockchain;
import com.jahanrashidi.crypto.core.Util;
import com.jahanrashidi.crypto.core.Wallet;
import com.jahanrashidi.crypto.core.transactions.Transaction;
import com.jahanrashidi.crypto.p2p.Client;
import com.jahanrashidi.crypto.p2p.Server;
import com.jahanrashidi.crypto.ui.HttpInterface;
import com.starkbank.ellipticcurve.PrivateKey;

import java.io.IOException;

public class Main {
    public static Blockchain chain;
    public static Server server;
    public static Wallet wallet;

    public static void main(String[] args) {

        int httpPort = args.length > 0 ? Integer.parseInt(args[0]) : 8001;
        int serverPort = args.length > 1 ? Integer.parseInt(args[1]) : 8002;

        Block genesisBlock = new Block(0, 1717278174, new Transaction[0], new byte[0], 0, 0);
        chain = new Blockchain(genesisBlock);

        HttpInterface.start(httpPort);

        // server runs in separate thread
        server = new Server(serverPort);
        server.start();

        if(args.length > 2) {
            Client.peers.add("localhost:" + args[2]);

            try {
                Client.connectToPeers();
            } catch (IOException exception) {
                System.out.println("Failed to connect to peers");
            }
        }

        wallet = new Wallet();
    }
}
