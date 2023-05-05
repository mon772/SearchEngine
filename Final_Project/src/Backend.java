//package com.stackoverflow.q3732109;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class Backend {

    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
        server.createContext("/crawl", new crawlHandler());
        server.createContext("/search", new searchHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class crawlHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange query) throws IOException {
            String rawParams = query.getRequestURI().getQuery();
            String[] params = rawParams.split("[ &=]");
            for(String x : params){
                System.out.println("x = " + x);
            }
            String response = "Crawl Success!";
//            System.out.println("params = " + params);
            try {
                Index.crawl(params[1], Integer.parseInt(params[3]));
            } catch (Exception e){
                response = "Crawl Failed, Try again";
            }
            query.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            query.getResponseHeaders().set("Access-Control-Allow-Methods", "*");
            query.getResponseHeaders().set("Access-Control-Allow-Headers", "*");
            query.getResponseHeaders().set("Access-Control-Allow-Credentials", "false");
            query.sendResponseHeaders(200, response.length());
            OutputStream os = query.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

    static class searchHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange query) throws IOException {
            String rawParams = query.getRequestURI().getQuery();
            String[] params = rawParams.split("=");
            for(String x : params){
                System.out.println("x = " + x);
            }
            String response = "Search Success!";
//            System.out.println("params = " + params);
            try {
                response = Index.search(params[1]).toString();
            } catch (Exception e){
                response = "Search Failed, Try again";
            }
            query.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            query.getResponseHeaders().set("Access-Control-Allow-Methods", "*");
            query.getResponseHeaders().set("Access-Control-Allow-Headers", "*");
            query.getResponseHeaders().set("Access-Control-Allow-Credentials", "false");
            query.sendResponseHeaders(200, response.length());
            OutputStream os = query.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }

}
