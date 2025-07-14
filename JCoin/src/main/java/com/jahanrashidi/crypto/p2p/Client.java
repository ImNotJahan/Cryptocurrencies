package com.jahanrashidi.crypto.p2p;

import com.jahanrashidi.crypto.Main;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class Client {
    public static ArrayList<String> peers = new ArrayList<String>();
    public static ArrayList<Peer> connections = new ArrayList<Peer>();

    public static void connectToPeers() throws IOException {
        for(Peer connection : connections) connection.interrupt();
        connections.clear();

        for(String peer : peers) {
            System.out.println(peer);
            String[] tokens = peer.split(":");
            Socket socket = new Socket(tokens[0], Integer.parseInt(tokens[1]));
            Peer thread = new Peer(socket);

            connections.add(thread);
            thread.start();

            Main.server.clients.add(socket);

            System.out.println("Connected to " + peer);
        }
    }
}
