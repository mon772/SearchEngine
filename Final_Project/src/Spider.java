import java.io.InputStream;
import java.util.*;
import java.time.LocalDateTime;
import org.htmlparser.beans.StringBean;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.AndFilter;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.beans.LinkBean;
import java.net.URL;
import java.net.URLConnection;
import jdbm.RecordManager;
import jdbm.RecordManagerFactory;


public class Spider
{
    private String baseUrl;
    private Queue<String> toVisit = new LinkedList<> ();
    private Vector<String> visited = new Vector<String>();
    private HashMap<String, Page> urlStore = new HashMap<String,Page>();
    private HashSet<Page> pages = new HashSet<Page>();
    private InvertedIndex urlDB;
    private InvertedIndex pagesDB;
    private int searchLimit;
    public Spider(String url, int _searchLimit){
        baseUrl = url;
        toVisit.add(baseUrl);
        searchLimit = _searchLimit;
    }

    public boolean canFetch(String _url){
        // Check if page can be visited (i.e. not Password Protected)
        try {
            InputStream urlStream = new URL(_url).openStream();
        } catch (Exception e) {
            // Skip if cannot be visited
            System.out.printf("CANNOT VISIT");
            return false;
        }
        return true;
    }

    public Vector<String> extractWords(String _url) throws ParserException{
        // extract words in _url and return them

        Vector<String> words = new Vector<String>();
        StringBean sb;
        sb = new StringBean ();
        sb.setURL(_url);
        String extractedStrings = sb.getStrings ();
        if(extractedStrings==null) {
            return words;
        }
        StringTokenizer st = new StringTokenizer(extractedStrings);
        while(st.hasMoreTokens()){
            words.add(st.nextToken());
        }
        return words;

    }
    public Vector<String> extractChildLinks(String _url) throws ParserException{
        // extract links in url and return them
        LinkBean lb = new LinkBean();
        lb.setURL(_url);
        URL[] URL_array = lb.getLinks();
        Vector<String> links = new Vector<String>();
        for (URL u: URL_array) {
            links.add(u.toString());
        }
        return links;
    }

    public Date extractLastModified(String _url) throws Exception {
        URL urlToExtract = new URL(_url);
        URLConnection conn = urlToExtract.openConnection();
        Date date = new Date();
        if(conn.getHeaderField("Last-Modified")!=null){
            date = new Date(conn.getHeaderField("Last-Modified"));
        }
        else if(conn.getHeaderField("Date")!=null){
            date = new Date(conn.getHeaderField("Date"));
        }
        // System.out.println(date);
        return date;
    }

    public int extractPageSize(String _url) throws Exception {
        URL urlToExtract = new URL(_url);
        URLConnection conn = urlToExtract.openConnection();
        int pageSize = 0;
        if(conn.getHeaderField("Content-Length")!=null){
            pageSize = Integer.parseInt(conn.getHeaderField("Content-Length"));
        } else {
//			Vector<String> content = extractWords(_url);
//			int sumString = 0;
//			for (String s : content){
//				sumString+=s.length();
//			}
//			pageSize = sumString;
            InputStream urlStream = new URL(_url).openStream();
            Scanner sc = new Scanner(urlStream);
            String responseBody = sc.useDelimiter("\\Z").next();
            pageSize = responseBody.length();
        }
        // System.out.println(pageSize);
        return pageSize;
    }

    public String extractRawPageTitle(String _url) throws Exception{
        InputStream urlStream = new URL(_url).openStream();
        Scanner sc = new Scanner(urlStream);
        String pageTitle = "No Title";
        if(sc.hasNext()){
            String responseBody = sc.useDelimiter("\\A").next();
            pageTitle = responseBody.substring(responseBody.indexOf("<title>") + 7, responseBody.indexOf("</title>"));
            if (pageTitle.equals("")){
                pageTitle = "No Title";
            }
        }
        return pageTitle;
    }

    public Vector<String> extractPageTitle(String _url) throws Exception{
        InputStream urlStream = new URL(_url).openStream();
        Scanner sc = new Scanner(urlStream);
        String pageTitle = "No Title";
        if(sc.hasNext()){
            String responseBody = sc.useDelimiter("\\A").next();
            pageTitle = responseBody.substring(responseBody.indexOf("<title>") + 7, responseBody.indexOf("</title>"));
            if (pageTitle.equals("")){
                pageTitle = "No Title";
            }
        }

        Vector<String> words = new Vector<String>(List.of(pageTitle.split(" ")));

        return words;
    }

    public void crawl (InvertedIndex urlDB){ // Add recordManagers here for crawling
        HashMap<String, HashSet<String>> parentList = new HashMap<String, HashSet<String>>();
        try{
            // Create basePage
            Page basePage = new Page(this.baseUrl, extractRawPageTitle(this.baseUrl), extractPageTitle(this.baseUrl),extractChildLinks(this.baseUrl),extractWords(this.baseUrl),extractLastModified(this.baseUrl),extractPageSize(this.baseUrl));

            Vector<String> baseLinks = this.extractChildLinks(this.baseUrl);

            for (String s : baseLinks){
                if(s.equals(baseUrl)){
                    continue;
                }
                if(parentList.get(s)==null){
                    HashSet<String> hs = new HashSet<String>();
                    parentList.put(s,hs);
                }
                parentList.get(s).add(baseUrl);
                this.toVisit.add(s);
            }
            urlDB.addEntry(this.baseUrl, basePage);
//            basePage.print();
            String head = this.baseUrl;
            while(!this.toVisit.isEmpty() && this.toVisit.peek()!= null) {
                // Retrieve head of queue
                head = this.toVisit.poll();
                if(!canFetch(head)){
                    continue;
                }
                // Get page from DB
                Page currentPage = urlDB.getPage(head);
                Vector<String> childLinks = null;
                if(currentPage==null){
                    // Fill all page details
                    childLinks = this.extractChildLinks(head);
                    currentPage = new Page(head, extractRawPageTitle(head), extractPageTitle(head),childLinks,extractWords(head),extractLastModified(head),extractPageSize(head));
                } else if (extractLastModified(head).compareTo(urlDB.getPage(head).lastModified)<=0) {
                    // Update page details
                    childLinks = this.extractChildLinks(head);
                    currentPage.updatePage(extractRawPageTitle(head), extractPageTitle(head),childLinks,extractWords(head),extractLastModified(head),extractPageSize(head));
                } else {
                    continue;
                }
//                currentPage.print();
                urlDB.addEntry(head,currentPage);
//                System.out.println("ADD: "+ head);
//                System.out.println(currentPage);
                toVisit.addAll(childLinks);
                // Update parentList
                for (String s : childLinks){
                    if(s.equals(head)){
                        continue;
                    }
                    if(parentList.get(s)==null){
                        HashSet<String> hs = new HashSet<String>();
                        parentList.put(s,hs);
                    }
                    parentList.get(s).add(head);
                }
                if(urlDB.itemCount>=searchLimit){
                    break;
                }
            }
            // Update parents at the end
            for ( String key : parentList.keySet()){
//                System.out.println("key = " + key);
                HashSet<String> parents = parentList.get(key);
//                System.out.println("parents = " + parents);
                Page p  = urlDB.getPage(key);
                if(p!=null){
//                    System.out.println("UPDATE: "+ key);
                    p.updateParents(parents);
                } else {
//                    System.out.println("NULL: "+ key);
                }
            }


        }
        catch (Exception e){
            e.printStackTrace ();
        }
    }
}


