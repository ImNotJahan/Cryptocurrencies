package com.jahanrashidi.crypto.ui.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class AddPeerHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        OutputStream stream = exchange.getResponseBody();

        String response = "add peeeeeeeeeeeeers";

        exchange.sendResponseHeaders(200, response.length());

        stream.write(response.toString().getBytes());
        stream.flush();
        stream.close();
    }
}
