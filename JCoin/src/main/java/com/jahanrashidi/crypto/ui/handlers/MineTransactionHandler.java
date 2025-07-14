package com.jahanrashidi.crypto.ui.handlers;

import com.jahanrashidi.crypto.Main;
import com.jahanrashidi.crypto.core.Util;
import com.jahanrashidi.crypto.ui.HttpInterface;
import com.starkbank.ellipticcurve.PublicKey;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

public class MineTransactionHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Map<String, String> query = HttpInterface.queryToMap(exchange.getRequestURI().getQuery());

        PublicKey address = Util.stringToPublicKey(query.get("address"));
        int amount = Integer.parseInt(query.get("amount"));

        OutputStream stream = exchange.getResponseBody();
        String response;

        try{
            response = Main.chain.generateBlockWithTransaction(address, amount).toString();
        } catch(Exception e){
            response = e.getMessage();
        }

        exchange.sendResponseHeaders(200, response.length());

        stream.write(response.getBytes());
        stream.flush();
        stream.close();

        Main.server.broadcast(Main.chain.encode());
    }
}
