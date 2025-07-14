package com.jahanrashidi.crypto;

import com.jahanrashidi.crypto.core.Block;
import com.jahanrashidi.crypto.core.Blockchain;
import com.jahanrashidi.crypto.core.Wallet;
import com.jahanrashidi.crypto.core.transactions.Transaction;
import com.jahanrashidi.crypto.p2p.Client;
import com.jahanrashidi.crypto.p2p.Server;
import com.jahanrashidi.crypto.ui.HttpInterface;

import java.io.IOException;

public class Main {
    public static Blockchain chain;
    public static Server server;
    public static Wallet wallet;

    public static void main(String[] args) {

        int httpPort = args.length > 0 ? Integer.parseInt(args[0]) : 8003;
        int serverPort = args.length > 1 ? Integer.parseInt(args[1]) : 8004;

        Block genesisBlock = new Block(0, 1717278174, new Transaction[0], new byte[0], 0, 0);
        chain = new Blockchain(genesisBlock);

        if(IO.fileExists(FileType.Blockchain)) chain.replaceChain(IO.readBlockchain());

        HttpInterface.start(httpPort);

        // server runs in separate thread
        server = new Server(serverPort);
        server.start();

        if(args.length < 3){//args.length > 2) {
            Client.peers.add("108.220.106.111:8002");

            try {
                Client.connectToPeers();
            } catch (IOException exception) {
                System.out.println("Failed to connect to peers");
            }
        }

        if(IO.fileExists(FileType.Wallet)) wallet = IO.readWallet();
        else{
            wallet = new Wallet();
            IO.writeWallet();
        }

        Thread writeBlockchain = new Thread(IO::writeBlockchain);

        Runtime.getRuntime().addShutdownHook(writeBlockchain);
    }
}
