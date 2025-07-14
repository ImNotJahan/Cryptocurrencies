package com.jahanrashidi.crypto.ui.handlers;

import com.jahanrashidi.crypto.Main;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class ChainHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        OutputStream stream = exchange.getResponseBody();
        String response = Main.chain.toString();

        exchange.sendResponseHeaders(200, response.length());

        stream.write(response.getBytes());
        stream.flush();
        stream.close();
    }
}
