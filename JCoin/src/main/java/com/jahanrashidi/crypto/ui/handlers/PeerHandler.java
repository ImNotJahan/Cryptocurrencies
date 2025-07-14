package com.jahanrashidi.crypto.ui.handlers;

import com.jahanrashidi.crypto.p2p.Client;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class PeerHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        OutputStream stream = exchange.getResponseBody();

        StringBuilder response = new StringBuilder();
        for(String peer : Client.peers){
            response.append(peer);
            response.append('\n');
        }

        exchange.sendResponseHeaders(200, response.length());

        stream.write(response.toString().getBytes());
        stream.flush();
        stream.close();
    }
}
