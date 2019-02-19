/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.capella.it4749;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author curtis
 * 
 * Make an asyc call to HTTP server for list of NIST time servers.
 * Server will return list as part of an HTML page.
 * 
 * URL: https://tf.nist.got/tf-cgi/servers.cgi
 * 
 * add -Dhttps.protocols=TLSv1.2 to run options in project setup
 */
public class TimeServerListAsync {
    private static Logger logger = Logger.getLogger(TimeServerListAsync.class.getName());

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            FileHandler fileHandler = new FileHandler("TimeServerList.log");
            SimpleFormatter simpleFormatter = new SimpleFormatter();
            fileHandler.setFormatter(simpleFormatter);
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
            Logger.getLogger("").getHandlers()[0].setLevel(Level.WARNING);
            
        }
        catch(IOException ex){
            System.err.println("Error opening log file.");
            System.exit(1);
        }
        
    }
    
    public static Future<ArrayList<String>> GetTimeServerListAsync() {
        CompletableFuture<ArrayList<String>> serverList = new CompletableFuture<>();
        
        
        
        return serverList;
    }
}
