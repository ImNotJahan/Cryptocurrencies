package com.jahanrashidi.crypto;

import com.jahanrashidi.crypto.core.Blockchain;
import com.jahanrashidi.crypto.core.Util;
import com.jahanrashidi.crypto.core.Wallet;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Scanner;

/** @noinspection ResultOfMethodCallIgnored*/
public class IO {
    public static boolean fileExists(FileType fileType){
        File file = new File("");

        if(fileType == FileType.Wallet)
            file = new File("wallet.dat");
        else if(fileType == FileType.Blockchain)
            file = new File("blockchain.dat");

        return file.isFile();
    }

    public static Wallet readWallet(){
        String path = "wallet.dat";
        File file = new File(path);

        try {
            Scanner scanner = new Scanner(file);
            String keyString = scanner.nextLine();
            scanner.close();

            return new Wallet(Util.stringToPrivateKey(keyString));
        } catch(IOException exception){
            System.out.println(exception.getMessage());
        }

        return new Wallet();
    }

    public static Blockchain readBlockchain(){
        String path = "blockchain.dat";
        File file = new File(path);

        try {
            Scanner scanner = new Scanner(file);
            String blockchain = scanner.nextLine();
            scanner.close();

            return Util.parseChain(blockchain);
        } catch(IOException exception){
            System.out.println(exception.getMessage());
        }

        return new Blockchain();
    }

    public static void writeWallet(){
        String path = "wallet.dat";
        File file = new File(path);

        try {
            file.createNewFile();

            Writer fileWriter = new FileWriter(path, false);
            fileWriter.write(Util.privateKeyToString(Main.wallet.privateKey()));
            fileWriter.close();
        } catch(IOException exception){
            System.out.println(exception.getMessage());
        }
    }

    public static void writeBlockchain(){
        String path = "blockchain.dat";
        File file = new File(path);

        try {
            file.createNewFile();

            Writer fileWriter = new FileWriter(path, false);
            fileWriter.write(Main.chain.encode());
            fileWriter.close();
        } catch(IOException exception){
            System.out.println(exception.getMessage());
        }
    }

    private static String documents(){
        return FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
    }
}

enum FileType {
    Blockchain,
    Wallet
}
