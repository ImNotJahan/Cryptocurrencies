package com.jahanrashidi.crypto.ui;

import com.jahanrashidi.crypto.ui.handlers.*;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

public class HttpInterface {
    public static void start(int port) {
        HttpServer server = null;
        try {
            server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
        } catch (IOException exception) {
            System.out.println("Failed to start HTTP server");
            return;
        }

        assert server != null;

        server.createContext("/", new IndexHandler());
        server.createContext("/chain", new ChainHandler());
        server.createContext("/mineRaw", new MineRawHandler());
        server.createContext("/mine", new MineHandler());
        server.createContext("/mineTransaction", new MineTransactionHandler());
        server.createContext("/balance", new BalanceHandler());
        server.createContext("/peers", new PeerHandler());
        server.createContext("/add_peer", new AddPeerHandler());
        server.createContext("/wallet", new WalletHandler());

        server.start();

        System.out.println("Started HTTP server on port " + port);
    }

    public static Map<String, String> queryToMap(String query){
        Map<String, String> result = new HashMap<String, String>();

        for (String param : query.split("&")) {
            String[] pair = param.split("=");

            if (pair.length > 1) result.put(pair[0], pair[1]);
            else result.put(pair[0], "");
        }

        return result;
    }
}
