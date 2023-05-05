import jdbm.RecordManager;
import jdbm.RecordManagerFactory;
import jdbm.htree.HTree;
import jdbm.helper.FastIterator;

import java.util.ArrayList;
import java.util.Vector;
import java.io.IOException;
import java.io.Serializable;

class Posting implements Serializable
{
    public int doc;
    public int freq;
    Posting(int doc, int freq)
    {
        this.doc = doc;
        this.freq = freq;
    }
}

public class InvertedIndex
{
    private RecordManager recman;
    private HTree hashtable;
    private ArrayList idMapping = new ArrayList<String>();
    public Integer itemCount = 0;

    InvertedIndex(String recordmanager, String objectname) throws IOException
    {
        recman = RecordManagerFactory.createRecordManager(recordmanager);
        long recid = recman.getNamedObject(objectname);

        if (recid != 0)
            hashtable = HTree.load(recman, recid);
        else
        {
            hashtable = HTree.createInstance(recman);
            recman.setNamedObject( objectname, hashtable.getRecid() );
        }
    }


    protected void finalize() throws IOException
    {
        recman.commit();
        recman.close();
    }

    // Add url/word string + pageObj/wordObj
    public void addEntry(Object value, Object obj) throws IOException
    {
        if(idMapping.contains(value)){
            hashtable.put(idMapping.indexOf(value),obj);
        } else {
            hashtable.put(itemCount, obj);
            itemCount++;
            idMapping.add(value);
        }

    }

    public Object getID(String value) throws IOException{
        if(idMapping.contains(value)){
            return idMapping.indexOf(value);
        }
        return null;
    }

    public ArrayList<Posting> getPosting(String value) throws IOException{
        ArrayList<Posting> p = null;
        if(idMapping.contains(value)){
            Integer idx = idMapping.indexOf(value);
            p = (ArrayList<Posting>) hashtable.get(idx);
        }
        return p;
    }

    public String getURL(Integer id) throws IOException{
        return (String) idMapping.get(id);
    }

    public Page getPage(String value) throws IOException{
        Page p = null;
        if(idMapping.contains(value)){
            Integer idx = idMapping.indexOf(value);
            p = (Page) hashtable.get(idx);
        }
        return p;
    }

    public void delEntry(String word) throws IOException
    {
        // Delete the word and its list from the hashtable
        hashtable.remove(word);

    }
    public void printAll() throws IOException
    {
        // Print all the data in the hashtable
        FastIterator iter = hashtable.keys();
        String key;
        while( (key = (String)iter.next())!=null){
            System.out.println(key + " = "+ hashtable.get(key));
        }
    }
}
