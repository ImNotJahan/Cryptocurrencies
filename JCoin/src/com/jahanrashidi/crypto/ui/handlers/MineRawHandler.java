package com.jahanrashidi.crypto.ui.handlers;

import com.jahanrashidi.crypto.Main;
import com.jahanrashidi.crypto.core.transactions.Transaction;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class MineRawHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        OutputStream stream = exchange.getResponseBody();
        String response = Main.chain.generateRawBlock(new Transaction[0]).toString();

        exchange.sendResponseHeaders(200, response.length());

        stream.write(response.getBytes());
        stream.flush();
        stream.close();

        Main.server.broadcast(Main.chain.encode());
    }
}
