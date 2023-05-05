import org.json.JSONArray;
//import org.json.JSONObject;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

public class Page implements Serializable {
    public String url;
    public String rawPageTitle;
    public Vector<String> pageTitle;
    private Vector<String> pageBody;
    private Vector<String> childLinks;
    private HashSet<String> parentLinks = new HashSet<String>();
    public Date lastModified;
    public int pageSize;
    public boolean isCrawled;
    private HashMap<Integer,Integer> termFrequency = new HashMap<Integer,Integer>();

    Page (String url, String rawPageTitle, Vector<String> pageTitle, Vector<String> childLinks, Vector<String> words, Date _lastModified, int _pageSize){
        this.url = url;
        this.rawPageTitle = rawPageTitle;
        this.pageTitle = pageTitle;
        this.childLinks = childLinks;
        this.pageBody = words;
        this.lastModified = _lastModified;
        this.pageSize = _pageSize;
    }

    public void updatePage(String rawPageTitle, Vector<String> pageTitle, Vector<String> childLinks, Vector<String> words, Date _lastModified, int _pageSize){
        this.rawPageTitle = rawPageTitle;
        this.pageTitle = pageTitle;
        this.childLinks.addAll(childLinks);
        this.pageBody = words;
        this.lastModified = _lastModified;
        this.pageSize = _pageSize;
    }

    public void updateParents(HashSet<String> parentLinks){
        this.parentLinks.addAll(parentLinks);
    }

    public void print(){
        System.out.println("url: " + url);
        System.out.println("pageTitle: " + pageTitle);
//        System.out.println("url: "+url);
        System.out.println("lastModified: "+lastModified);
        System.out.println("pageSize: "+pageSize);
        System.out.println("childLinks: "+childLinks);
        System.out.println("parentLinks: "+parentLinks);
        System.out.println("pageBody: "+pageBody);
        System.out.println("termFrequency: "+termFrequency);
        System.out.println("___________________________________");
    }

    public Vector<String> getPageTitle() {
        return pageTitle;
    }

    public Vector<String> getPageBody() {
        return pageBody;
    }

    public String getUrl() {
        return url;
    }

    public void setTermFrequency(HashMap<Integer, Integer> termFrequency) {
        this.termFrequency = termFrequency;
    }

    public void setPageTitle(Vector<String> pageTitle) {
        this.pageTitle = pageTitle;
    }

    public void setPageBody(Vector<String> pageBody) {
        this.pageBody = pageBody;
    }

    public JSONObject getJSON(InvertedIndex wordsDB) throws IOException {
        System.out.println("termFrequency = " + termFrequency);
        HashMap<Integer,Integer> sortedTermFrequency = sortByValue(termFrequency);
        System.out.println("sortedTermFrequency = " + sortedTermFrequency);
        HashMap<String,Integer> topTermFreq = new  HashMap<String,Integer>();
        JSONArray keywords = new JSONArray();
//        Iterator<Integer> itr = sortedTermFrequency.keySet().iterator();
//        while (itr.hasNext() && count < 5) {
//            topTermFreq.put(wordsDB.getURL(itr), sortedTermFrequency.get(itr.next()))
////            topTermFreq.put(wordsDB.getURL(sortedTermFrequency.get(itr.next())))
//            count++;
//        }

        int count = 0;
        for(Integer key: sortedTermFrequency.keySet()){
            if(count>=5){
                break;
            }
//            obj.put(wordsDB.getURL(key), sortedTermFrequency.get(key));
            JSONObject obj = new JSONObject();
            obj.put("term", wordsDB.getURL(key));
            obj.put("frequency", sortedTermFrequency.get(key));
            keywords.put(obj);
//            obj.add(wordsDB.getURL(key));
//            obj.add(sortedTermFrequency.get(key));
            count++;
        }


        JSONObject json = new JSONObject();
        json.put("Url", url);
        json.put("pageTitle", rawPageTitle);
        json.put("lastModified", lastModified.toString());
//        JSONObject pb = new JSONObject(obj);
//        json.put("keywords", pb);
//        String pb2 = JSONObject.toJSONString(obj);
//        JSONArray keywords = new JSONArray(obj);
        json.put("keywords", keywords);
        HashSet<String> childSet = new HashSet<String>();
        childSet.addAll(childLinks);
        json.put("childLinks", childSet);
        JSONArray pl = new JSONArray(parentLinks);
        json.put("parentLinks", pl);
//        JSONObject posting = new JSONObject(termFrequency);
//        json.put("postingList", posting);
        return json;
    }



    public static HashMap<Integer, Integer> sortByValue(HashMap<Integer, Integer> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<Integer, Integer> > list =
                new LinkedList<Map.Entry<Integer, Integer> >(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<Integer, Integer> >() {
            public int compare(Map.Entry<Integer, Integer> o1,
                               Map.Entry<Integer, Integer> o2)
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<Integer, Integer> temp = new LinkedHashMap<Integer, Integer>();
        for (Map.Entry<Integer, Integer> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }
}
