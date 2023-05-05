import jdbm.RecordManager;
import jdbm.RecordManagerFactory;

import javax.net.ssl.*;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;
import org.json.JSONArray;
import org.json.simple.JSONObject;

public class Index {
    static InvertedIndex urlDB;
    static InvertedIndex wordsDB;

    public Index() throws IOException {
    }

    public static void main(String[] args) {
        crawl("https://cse.hkust.edu.hk/", 20);
        search("This exhibition showcases");
        /*
        try {
            disableCertificateValidation();
            InvertedIndex urlDB = new InvertedIndex("UrlDatabase","urlHt");

            // Fetch pages using dfs
            Spider spider = new Spider("https://cse.hkust.edu.hk/", 300);
            spider.crawl(urlDB);
//            for (int i = 0; i < urlDB.itemCount; i++) {
//                urlDB.getPage(urlDB.getURL(i)).print();
//            }
            System.out.println("urlDB.itemCount = " + urlDB.itemCount);

            InvertedIndex wordsDB = new InvertedIndex("wordDatabase","wordHt");
            // Index words from pages
            Indexer indexer = new Indexer("src/stopwords.txt");
            for (int i = 0; i < urlDB.itemCount; i++) {
                Page p = urlDB.getPage(urlDB.getURL(i));
//                p.print();
                indexer.start(p, wordsDB, urlDB);
                System.out.println("p.getUrl() = " + p.getUrl());
            }



            // Process Query
//            Vector<String> query = indexer.stopStemString("HKUST This exhibition showcases a collection of maps and aviation-related artifacts from the Walter R. Kent Collection, including posters, airplane models and other interesting items. ");
            Vector<String> query = indexer.stopStemString("robo lawyer");
            System.out.println("_query = " + query);

            //Submit Query
            HashMap<Integer,Double> sortedResults = SearchEngine.searchQuery(query, wordsDB, urlDB);
//            HashMap<Integer,Double> sortedResults = SearchEngine.searchPhrase(query, wordsDB, urlDB);

            for(Integer pageID: sortedResults.keySet()){
                System.out.println(urlDB.getURL(pageID) + "     SCORE: "+sortedResults.get(pageID));
//                urlDB.getPage(urlDB.getURL(pageID)).print();
            }

//            for (int i = 0; i < urlDB.itemCount; i++) {
//                urlDB.getPage(urlDB.getURL(i)).print();
//            }


        } catch (Exception e) {
            System.out.println(e);
        }
        */

    }

    public static void disableCertificateValidation() throws Exception{
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }
        };

        // Install the all-trusting trust manager
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        // Create all-trusting host name verifier
        HostnameVerifier allHostsValid = new HostnameVerifier() {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };

        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
    }

    public static void crawl(String baseURL, Integer searchLimit){
        try{
            disableCertificateValidation();
             urlDB = new InvertedIndex("UrlDatabase","urlHt");

            // Fetch pages using dfs
            Spider spider = new Spider(baseURL, searchLimit);
            spider.crawl(urlDB);
//            for (int i = 0; i < urlDB.itemCount; i++) {
//                urlDB.getPage(urlDB.getURL(i)).print();
//            }
            System.out.println("urlDB.itemCount = " + urlDB.itemCount);

            wordsDB = new InvertedIndex("wordDatabase","wordHt");
            // Index words from pages
            Indexer indexer = new Indexer("src/stopwords.txt");
            for (int i = 0; i < urlDB.itemCount; i++) {
                Page p = urlDB.getPage(urlDB.getURL(i));
//                p.print();
                indexer.start(p, wordsDB, urlDB);
                System.out.println("p.getUrl() = " + p.getUrl());
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static JSONArray search(String _query){
        JSONArray jsonArray = new JSONArray();
        try {
            Indexer indexer = new Indexer("src/stopwords.txt");
            // Process Query
            //            Vector<String> query = indexer.stopStemString("HKUST This exhibition showcases a collection of maps and aviation-related artifacts from the Walter R. Kent Collection, including posters, airplane models and other interesting items. ");
            Vector<String> query = indexer.stopStemString(_query);
            System.out.println("_query = " + query);

            //Submit Query
            HashMap<Integer, Double> sortedResults = new HashMap<>();

            if(_query.startsWith("\"" ) && _query.endsWith("\"")){
                sortedResults = SearchEngine.searchPhrase(query, wordsDB, urlDB);
            } else {
                sortedResults = SearchEngine.searchQuery(query, wordsDB, urlDB);
            }
            //            HashMap<Integer,Double> sortedResults = SearchEngine.searchPhrase(query, wordsDB, urlDB);
            int counter = 0;
            for (Integer pageID : sortedResults.keySet()) {
                if (counter >=50){
                    continue;
                }
                System.out.println(urlDB.getURL(pageID) + "     SCORE: " + sortedResults.get(pageID));
                JSONObject pageObj = urlDB.getPage(urlDB.getURL(pageID)).getJSON(wordsDB);
                pageObj.put("score", sortedResults.get(pageID));
//                pageObj.put("score", sortedResults.get(pageID));
//                pageObj.put("pageData", urlDB.getPage(urlDB.getURL(pageID)).getJSON(wordsDB));
                //                urlDB.getPage(urlDB.getURL(pageID)).print();
                jsonArray.put(pageObj);
                counter++;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("jsonArray = " + jsonArray);
        return jsonArray;
    }
}
