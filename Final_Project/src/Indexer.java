import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Indexer {
    private Porter porter;
    private HashSet<String> stopWords;

    public Indexer(String stopWordsPath) {
        super();
        porter = new Porter();
        stopWords = new HashSet<String>();
        stopStem(stopWordsPath);
    }

    public boolean isStopWord(String str) {
        return stopWords.contains(str);
    }

    public String stem(String str) {
        return porter.stripAffixes(str);
    }

    public void stopStem(String str) {

        // use BufferedReader to extract the stopwords in stopwords.txt (path passed as parameter str)
        // add them to HashSet<String> stopWords
        try {
            BufferedReader br = new BufferedReader(new FileReader(str));
            String line = br.readLine();
            while (line != null) {
                stopWords.add(line);
                line = br.readLine();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void start(Page page, InvertedIndex wordsDB, InvertedIndex pagesDB) throws IOException {
        Vector<String> words = page.getPageBody();
        HashMap<String, Integer> stopStemmedWords = new HashMap<String, Integer>();
        HashMap<Integer, Integer> idStemmedWords = new HashMap<Integer, Integer>();
        for (String word : words) {
            if (word.length() == 0) {
                continue;
            }
            if (!this.isStopWord(word)) { // Make second vector to set as (Make setPageBody Func)
                // System.out.println("Before Stem Word = " + word);
                String stemmedWord = this.stem(word);
                if (stemmedWord.equals("")) {
                    continue;
                }
                if (stopStemmedWords.get(stemmedWord) == null) {
                    stopStemmedWords.put(stemmedWord, 1);
                } else {
                    Integer freq = stopStemmedWords.get(stemmedWord);
                    stopStemmedWords.put(stemmedWord, freq + 1);
                }
            }
        }
        System.out.println("page.pageTitle = " + page.pageTitle);
        System.out.println("stopStemmedWords = " + stopStemmedWords);
        for (String word : stopStemmedWords.keySet()) {
            if (wordsDB.getID(word) == null) {
                ArrayList<Posting> postingList = new ArrayList<>();
                postingList.add(new Posting((int)pagesDB.getID(page.getUrl()), stopStemmedWords.get(word)));
                wordsDB.addEntry(word, postingList);
            } else {
                ArrayList<Posting> postingList = wordsDB.getPosting(word);
                postingList.add(new Posting((int)pagesDB.getID(page.getUrl()), stopStemmedWords.get(word)));
                wordsDB.addEntry(word, postingList);
            }
            idStemmedWords.put((Integer) wordsDB.getID(word),stopStemmedWords.get(word));
        }

        // Stopstem title
        Vector<String> title = page.getPageTitle();
        Vector<String> stopStemmedTitle = new Vector<String>();
        for (String titleWord : title) {
            if (titleWord.length() == 0) {
                continue;
            }
            if (!this.isStopWord(titleWord)) {
                String stemmedWord = this.stem(titleWord);
                if (stemmedWord.equals("")) {
                    continue;
                }
                stopStemmedTitle.add(stemmedWord);
            }
        }

        // Stopstem body
        Vector<String> pb = page.getPageBody();
        Vector<String> stopPB = new Vector<String>();
        for (String bodyWord : pb) {
            if (bodyWord.length() == 0) {
                continue;
            }
            if (!this.isStopWord(bodyWord)) {
                String stemmedWord = this.stem(bodyWord);
                if (stemmedWord.equals("")) {
                    continue;
                }
                stopPB.add(stemmedWord);
            }
        }
        page.setPageTitle(stopStemmedTitle);
        page.setPageBody(stopPB);
        page.setTermFrequency(idStemmedWords);
    }

    public Vector<String> stopStemString(String words){
        String[] splitWords = words.split(" ");
        Vector<String> stopStemmedWords = new Vector<String>();
        for (String word : splitWords) {
            if (word.length() == 0) {
                continue;
            }
            if (!this.isStopWord(word)) { // Make second vector to set as (Make setPageBody Func)
                String stemmedWord = this.stem(word);
                if (stemmedWord.equals("")) {
                    continue;
                }
                stopStemmedWords.add(stemmedWord);
            }
        }
        return stopStemmedWords;
    }
}
