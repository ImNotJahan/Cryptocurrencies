package com.jahanrashidi.crypto.p2p;

import com.jahanrashidi.crypto.Main;
import com.jahanrashidi.crypto.core.Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Peer extends Thread{
    private final Socket socket;

    public Peer(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try{
            while(true) {
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String message = reader.readLine();
                Main.chain.replaceChain(Util.parseChain(message));
            }
        } catch(IOException exception){
            System.out.println("Failed in reading peer node: " + exception.getMessage());
        }

    }

    @Override
    public void interrupt() {
        super.interrupt();
        try {
            socket.close();
        } catch(IOException exception){
            System.out.println("Failed to close peer socket: " + exception.getMessage());
        }
    }
}
