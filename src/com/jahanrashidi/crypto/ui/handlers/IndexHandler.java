package com.jahanrashidi.crypto.ui.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class IndexHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        OutputStream stream = exchange.getResponseBody();
        StringBuilder response = new StringBuilder();

        response.append("<p><a href=\"/chain\">View blockchain</a></p>");
        response.append("<p><a href=\"/mine\">Mine coin</a></p>");
        response.append("<p><a href=\"/peers\">View peers</a></p>");
        response.append("<p><a href=\"/mineRaw\">Mine raw</a></p>");
        response.append("<p><a href=\"/balance\">View balance</a></p>");
        response.append("<p><a href=\"/wallet\">View wallet</a></p>");

        exchange.sendResponseHeaders(200, response.length());

        stream.write(response.toString().getBytes());
        stream.flush();
        stream.close();
    }
}
