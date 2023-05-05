import java.io.IOException;
import java.util.*;

public class SearchEngine {
    public static HashMap<Integer,Double> searchQuery(Vector<String> query, InvertedIndex wordsDB, InvertedIndex pagesDB) throws IOException {
        HashMap<Integer,Double> queryScores = new HashMap<Integer,Double>();
        HashMap<Integer,Double> totScores = new HashMap<Integer,Double>();
        try{
            // Score query
            for (String word: query) {
                if(wordsDB.getID(word)==null) {
                    continue;
                }
                Integer wordID = (Integer) wordsDB.getID(word);
                if(queryScores.containsKey(wordID)){
                    queryScores.put(wordID, queryScores.get(wordID)+1);
                } else {
                    queryScores.put(wordID, 1.0);
                }
            }
            System.out.println("queryScores = " + queryScores);


            System.out.println("query = " + query);
            for (String word: query){
                if(wordsDB.getID(word)==null) {
                    continue;
                }
                // Get posting list
                ArrayList<Posting> postingList= wordsDB.getPosting(word);

                // Get document frequency
                double df = postingList.size();

                // Get max tf
                double max_tf = 0;
                for(Posting pos : postingList) {
                    max_tf = Math.max(max_tf, pos.freq);
                }

                // Get tf and calculate scores
                for(Posting pos : postingList){
                    System.out.print(pos.doc+ " "+ pos.freq+ ", ");
                    double tf = pos.freq;
                    double idf = Math.log((double)pagesDB.itemCount / df) / Math.log(2);
                    double weight = (tf/max_tf) * idf;
                    double titleBonus = 0.0;
                    Page page = pagesDB.getPage(pagesDB.getURL(pos.doc));
                    Vector<String> pageTitle = page.getPageTitle();
                    System.out.println("pageTitle = " + pageTitle);
                    int bonusCounter = 0;
                    for(String titleWord: pageTitle){
//                        System.out.println("titleWord = " + titleWord);
//                        System.out.println("word = " + word);
                        if(titleWord.equals(word)){
                            bonusCounter++;
                            System.out.println("BONUS ADDED");
                        }
                    }
                    weight+=bonusCounter*0.2;
                    double dotWeight = 0.0;
                    if(wordsDB.getID(word)!=null){
                        dotWeight = weight * queryScores.get(wordsDB.getID(word));
                    }
                    if(totScores.get(pos.doc)==null){
                        totScores.put(pos.doc,dotWeight);
                    } else {
                        double currentWeight = totScores.get(pos.doc);
                        totScores.put(pos.doc, currentWeight + dotWeight);
                    }
                }
                System.out.println();

                // Print totScores Hashmap
//                for(Integer key: totScores.keySet()){
//                    System.out.println("key = " + pagesDB.getURL(key));
//                    System.out.println("totScores = " + totScores.get(key));
//                }
            }

            // Cosine Similarity
            for(Integer pageID: totScores.keySet()){
//                System.out.println("key = " + pagesDB.getURL(pageID));
//                System.out.println("totScores = " + totScores.get(pageID));
    //            HashMap<Integer, Double> pageTotScores = totScores.get(pageID);

                Page page = pagesDB.getPage(pagesDB.getURL(pageID));

                Double documentLength = Math.sqrt(page.pageSize);
                Double queryLength = Math.sqrt(query.size());

                Double cosSim = totScores.get(pageID) / (documentLength * queryLength);

                totScores.put(pageID, cosSim);

            }
            // Print totScores Hashmap
//            for(Integer key: totScores.keySet()){
//                System.out.println("key = " + pagesDB.getURL(key));
//                System.out.println("cosSim = " + totScores.get(key));
//            }
            totScores = sortByValue(totScores);
            System.out.println("SORTED totScores = " + totScores);
        } catch (Exception e){
            e.printStackTrace ();
        }
        return totScores;
    }

    public static HashMap<Integer,Double> searchPhrase(Vector<String> query, InvertedIndex wordsDB, InvertedIndex pagesDB) throws IOException {
        HashMap<Integer,Double> totScores = new HashMap<Integer,Double>();
        HashMap<Integer,Integer> phraseCount = new HashMap<Integer,Integer>();
        try{
            System.out.println("query = " + query);
            String word = query.get(0);
            System.out.println("word = " + word);
            if(wordsDB.getID(word)==null) {
                return null;
            }
            // Get posting list
            ArrayList<Posting> postingList= wordsDB.getPosting(word);
            System.out.println("postingList.size() = " + postingList.size());
            // Get document frequency
            for(Posting pos : postingList) {
                Page page = pagesDB.getPage(pagesDB.getURL(pos.doc));
                System.out.println("pagesDB.getURL(pos.doc) = " + pagesDB.getURL(pos.doc));
                Vector<String> pageBody = page.getPageBody();
                System.out.println("pageBody = " + pageBody);
                int index = 0;
                int phraseSize = 0;
                int phraseExists = 0;
//                System.out.println("pageBody.contains(word,index) = " + pageBody.contains(word));
//                System.out.println("pageBody.indexOf(word,index) = " + pageBody.indexOf(word));
                while (pageBody.indexOf(word,index)!=-1 && pageBody.size() - query.size() >= index) {
//                    System.out.println("pageBody.indexOf(word,index) = " + pageBody.indexOf(word,index));
                    index = pageBody.indexOf(word,index);
                    for(int i = index; i< index + query.size(); i++) {
//                        System.out.println("pageBody = " + pageBody.get(i));
//                        System.out.println("query.get(i-index) = " + query.get(i-index));
                        if(pageBody.get(i).equals(query.get(i-index))) {
                            phraseSize++;
                        }
                    }
                    if(phraseSize== query.size()){
                        phraseExists++;
                    }
                    index++;
                    phraseSize=0;
                }
                if(phraseExists>0){
                    System.out.println("phraseExists = " + phraseExists);
                    phraseCount.put(pos.doc, phraseExists);
                }
            }
            System.out.println("phraseCount = " + phraseCount);
            double df = phraseCount.size();

            // Calculate Scores
            double max_tf = Collections.max(phraseCount.values());
            System.out.println("max_tf = " + max_tf);

            for(Integer pageID: phraseCount.keySet()){
                double tf = phraseCount.get(pageID);
                double idf = Math.log((double)pagesDB.itemCount / df) / Math.log(2);
                double weight = (tf/max_tf) * idf;
                totScores.put(pageID,weight);
            }
            // Print totScores Hashmap
//            for(Integer key: totScores.keySet()){
//                System.out.println("key = " + pagesDB.getURL(key));
//                System.out.println("weight = " + totScores.get(key));
//            }

            // Cosine Similarity
            for(Integer pageID: totScores.keySet()){
//                System.out.println("key = " + pagesDB.getURL(pageID));
//                System.out.println("totScores = " + totScores.get(pageID));
                //            HashMap<Integer, Double> pageTotScores = totScores.get(pageID);

                Page page = pagesDB.getPage(pagesDB.getURL(pageID));

                Double documentLength = Math.sqrt(page.pageSize);
                Double queryLength = Math.sqrt(query.size());

                Double cosSim = totScores.get(pageID) / (documentLength * queryLength);

                totScores.put(pageID, cosSim);

            }
            // Print totScores Hashmap
//            for(Integer key: totScores.keySet()){
//                System.out.println("key = " + pagesDB.getURL(key));
//                System.out.println("cosSim = " + totScores.get(key));
//            }
            totScores = sortByValue(totScores);
            System.out.println("SORTED totScores = " + totScores);

        } catch (Exception e){
            e.printStackTrace ();
        }


        return totScores;
    }


    public static HashMap<Integer, Double> sortByValue(HashMap<Integer, Double> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<Integer, Double> > list =
                new LinkedList<Map.Entry<Integer, Double> >(hm.entrySet());

        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<Integer, Double> >() {
            public int compare(Map.Entry<Integer, Double> o1,
                               Map.Entry<Integer, Double> o2)
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // put data from sorted list to hashmap
        HashMap<Integer, Double> temp = new LinkedHashMap<Integer, Double>();
        for (Map.Entry<Integer, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }


}