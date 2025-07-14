package com.jahanrashidi.crypto.p2p;

import com.jahanrashidi.crypto.Main;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server extends Thread{
    public final ArrayList<Socket> clients = new ArrayList<Socket>();
    private final int port;

    public Server(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        ServerSocket server = null;
        Socket client = null;

        try{
            server = new ServerSocket(port);
            System.out.println("Started P2P server on port " + port);

            while(true) {
                client = server.accept();
                System.out.println("Client connected");

                clients.add(client);

                Peer thread = new Peer(client);
                Client.connections.add(thread);
                thread.start();

                broadcast(Main.chain.encode(), client);
            }
        } catch(IOException exception){
            System.out.println("P2P server failed: " + exception.getMessage());
        }

        assert server != null;
        assert client != null;
    }

    public void broadcast(String message) throws IOException{
        for(Socket client : clients){
            broadcast(message, client);
        }
    }

    public void broadcast(String message, Socket client) throws IOException{
        OutputStream output = client.getOutputStream();
        PrintWriter writer = new PrintWriter(output, true);
        writer.println(message);
    }
}
