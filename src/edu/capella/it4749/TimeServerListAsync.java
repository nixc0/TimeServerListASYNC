/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.capella.it4749;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static String timeServerAddress = "https://tf.nist.gov/tf-cgi/servers.cgi";

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
        
        // initialize our ArrayList of Strings that uses the Future interface
        Future<ArrayList<String>> result = null;
        
        // try assign result to what is returned by the GetTimeServerListAsync() method
        try{
            logger.log(Level.INFO, "Retrieving list of servers..");
            result = GetTimeServerListAsync();
            
            // while waiting for the GetTimeServerListAsync() method is executing
            // sleep for 100 milliseconds
            while(!result.isDone()){
                Thread.sleep(100);
                System.out.println("waiting...");
            }
            
            ArrayList<String> servers = null;
            try{
                logger.log(Level.INFO, "Getting the list of time servers from future object");
                servers = result.get();
            }
            catch (InterruptedException ex){
                logger.log(Level.SEVERE, "Interrupted by: " + ex.getMessage());
                
            }
           
            // for each String s in servers, print String s
            for(String s : servers){
                System.out.println(s);
            }
        }
        
        // log the name and message of an exception that is caught
        catch(Exception ex){
            logger.log(Level.SEVERE, "Error in asynchronous call: " + ex.getClass().getName()
             + "-" + ex.getMessage());
            
        }
        
        
    }
    
    public static Future<ArrayList<String>> GetTimeServerListAsync() {
        CompletableFuture<ArrayList<String>> serverList = new CompletableFuture<>();
        
        // Create a new anonymous Thread
        new Thread( () -> {
            URL timeServerListURL = null;
            HttpURLConnection connection = null;
            String html;
            
            ArrayList<String> ip = new ArrayList<>();
            
            try {
                timeServerListURL = new URL(timeServerAddress);
            } 
            catch(MalformedURLException ex){
                logger.log(Level.WARNING, "Error in URL: (0)", ex.getMessage());
            }
            // Check if the URL is not null
            if(timeServerListURL != null){
                try{
                    // connection (HttpURLConnection), previously null, now
                    // assigned what is returned from the openConnection() method
                    connection = (HttpURLConnection) timeServerListURL.openConnection();
                    // an integer to record the response code, 200 is HTTP_OK
                    int response = connection.getResponseCode();
                    logger.log(Level.INFO, "HTTP response code: " + response);
                    // If response code is 200 (what we want)
                    if(response == HttpURLConnection.HTTP_OK){
                        // using StringBuilder to simplify garbage collection
                        StringBuilder htmlString = new StringBuilder();
                        // try with resources to make sure that if the connection
                        // fails that it is terminated cleanly
                        try(Scanner htmlReader = new Scanner(connection.getInputStream())){
                            // As long as there is another line of HTML, Scan it
                            while(htmlReader.hasNextLine()){
                                htmlString.append(htmlReader.nextLine());
                            }
                        }
                        // #beginscreenscraping
                        // Look for the HTML tag that signifies the begining of the table
                        String startTag = "<TABLE BORDER=6";
                        // Look for the HTML tag that signifies the end of the table
                        String stopTag = "</table>";
                        // Get the index value of the beginning of the table
                        int startTable = htmlString.indexOf(startTag);
                        // Get the index value of the first instance of the stopTag
                        // (which is the </table> tag) after the startTag
                        int stopTable = htmlString.indexOf(stopTag, startTable);
                        String tableHTML = htmlString.substring(startTable, stopTable + stopTag.length());
                        // Regular expression for digit+ period digit+ period digit+
                        // period digit+ in a group (which is why they are surrounded by () )
                        
                        
                        Pattern serverURLPattern = Pattern.compile("(\\d+\\.\\d+\\.\\d+\\.\\d+)");
                        Matcher m = serverURLPattern.matcher(tableHTML);
                        
                        while(m.find()){
                            ip.add(m.group(1));
                        }
                        
                        //INFO level logger to let us know how many IP addresses to expect
                        logger.log(Level.INFO, ip.size() + " server IP addresses found.");
                        
                        //serverList is our CompletableFuture ArrayList of strings 
                        //This returns the ArrayList ip wrapped object that implements
                        //the Future interface
                        serverList.complete(ip);
                    }
                }
                catch(IOException ex){
                    logger.log(Level.SEVERE,"Error Connecting: " + ex.getMessage());
                }
            }
            
            else {
                logger.log(Level.SEVERE, "URL object cannot be null.");
            }
            
        // Now that the thread is set up, now tell the thread to start    
        }).start();
        
        // After the thread is done running, return that serverList
        return serverList;
    }
}
