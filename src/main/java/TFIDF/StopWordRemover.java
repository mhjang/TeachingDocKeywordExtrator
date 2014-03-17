package TFIDF;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by mhjang on 2/1/14.
 */
public class StopWordRemover extends TestCase {
    public HashSet<String> stopwords;

    public StopWordRemover() {
        try{
            BufferedReader br = new BufferedReader(new FileReader("stopwords.txt"));
            stopwords = new HashSet<String>();
            String line = br.readLine();
            int docLength = 0;
            while(line != null) {
                String word = line.replaceAll("[^A-Za-z0-9]", "");
                stopwords.add(word.toLowerCase().trim());
                line = br.readLine();
            }
        }catch(Exception e) {
            e.printStackTrace();
        }
    }

    public LinkedList<String> removeStopwords(LinkedList<String> list) {
        int len = list.size();
        LinkedList<String> stopRemovedList = new LinkedList<String>();
        for(int i=0; i<len; i++) {
            String word = list.get(i).toLowerCase();
            if(!stopwords.contains(word))
                stopRemovedList.add(word);
        }
        list = stopRemovedList;

        return stopRemovedList;
    }

    /**
     * Filters out the stop words AND drops a word that only consists of numbers. ("3 4 5")
     * @param list
     * @return
     */
    public String[] removeStopWords(String[] list) {
        ArrayList<String> l = new ArrayList<String>();
        for(int i=0; i<list.length; i++) {
            String word = list[i].toLowerCase();
            if(!stopwords.contains(word) && !word.matches("[ 0-9]*"))
            {
                l.add(word);
            }
        }
        String[] newlist = new String[l.size()];
        newlist = l.toArray(newlist);
        return newlist;
    }


}
