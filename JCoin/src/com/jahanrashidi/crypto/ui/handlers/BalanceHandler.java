package com.jahanrashidi.crypto.ui.handlers;

import com.jahanrashidi.crypto.Main;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class BalanceHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        OutputStream stream = exchange.getResponseBody();
        String response = "J" + Main.wallet.balance(Main.chain.unspentTxOuts());

        exchange.sendResponseHeaders(200, response.length());

        stream.write(response.getBytes());
        stream.flush();
        stream.close();

        Main.server.broadcast(Main.chain.encode());
    }
}
