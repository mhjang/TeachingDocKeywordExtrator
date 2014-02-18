package parser;

import Clustering.Document;
import TFIDF.StopWordRemover;
import db.DBConnector;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.LinkedHashSet;

/**
 * Created by mhjang on 2/2/14.
 * Written at 8:25 pm
 */
public class Tokenizer {
    public static int UNIGRAM = 1;
    public static int BIGRAM = 2;
    public static int TRIGRAM = 3;


    public Tokenizer() {
    }

    /**
     * takes String and convert it to a document with unigram/bigram/trigrams and term frequency information
     * @param line
     * @param ngram
     * @return
     */
    public Document tokenize(String docName, String line, int ngram) {
        String[] rawwords = line.split("[^a-zA-Z0-9]+");
        // removing stopwords
        StopWordRemover stopRemover = new StopWordRemover();
        String[] words = stopRemover.removeStopWords(rawwords);
        ArrayList<String> wordlist = new ArrayList<String>(Arrays.asList(words));

        LinkedList<String> unigrams = null;
        LinkedList<String> bigrams = null;
        LinkedList<String> trigrams = null;

        if(wordlist.size() >= Tokenizer.TRIGRAM) {
            if(ngram == Tokenizer.UNIGRAM) {
                unigrams = getUnigrams(wordlist);
            }
            else if(ngram == Tokenizer.BIGRAM) {
                unigrams = getUnigrams(wordlist);
                bigrams = generateBigrams(wordlist);
            }
            else {
                unigrams = getUnigrams(wordlist);
                bigrams = generateBigrams(wordlist);
                trigrams = generateTrigrams(wordlist);
            }
        }

        Document doc = new Document(docName, unigrams, bigrams, trigrams);

        LinkedList<String> wordPool = doc.getAllGrams();
        HashMap<String, Integer> docTFMap = new HashMap<String, Integer>();
        for(String word : wordPool) {
           // counting global term frequency
            if(!docTFMap.containsKey(word))
                docTFMap.put(word, 1);
            else
                docTFMap.put(word,  (docTFMap.get(word) + 1));
        }
        doc.setTermFrequency(docTFMap);
        return doc;
     }

    private LinkedList<String> getUnigrams(ArrayList<String> wordlist) {
        LinkedHashSet<String> wordPool = new LinkedHashSet<String>();
        for(String term : wordlist) {
            if(!wordPool.contains(term)) {
                wordPool.add(term);
            }
        }

        LinkedList<String> listToReturn = new LinkedList<String>();
        listToReturn.addAll(wordPool);
        return listToReturn;

    }

    /**
     * generate bigrams
     * @param wordlist
     * @return
     */
    private LinkedList<String> generateBigrams(ArrayList<String> wordlist) {
        //    uncomment if you want to generate *UPTO* bigrams in addition to unigrams (this line includes unigrams)
        //     LinkedList<String> wordPool = new LinkedList<String>(wordlist);
        LinkedHashSet<String> wordPool = new LinkedHashSet<String>();
        LinkedList<String> listToReturn = new LinkedList<String>();

        if(wordlist.size() < 2) {
            System.out.println("There is only one word in the list");
            return listToReturn;
        }

        String bigram = wordlist.get(0) + " " + wordlist.get(1);
        wordPool.add(bigram);
        int length = wordlist.size();
        for(int i=2; i<length; i++) {
            bigram = wordlist.get(i-1) + " " + wordlist.get(i);
            wordPool.add(bigram);
        }

        listToReturn.addAll(wordPool);
        return listToReturn;
    }

    /**
     * generate trigrams
     * @param wordlist
     * @return
     */
    private LinkedList<String> generateTrigrams(ArrayList<String> wordlist) {
        //    uncomment star-marked (*) lines if you want to generate *UPTO* trigrams in addition to unigrams (this line includes unigrams)
        //*     LinkedList<String> wordPool = new LinkedList<String>(wordlist);
        LinkedHashSet<String> wordPool = new LinkedHashSet<String>();
        if(wordlist.size() < 3) {
            System.out.println("There is less than 3 words in the list; can't build trigrams");
            return generateBigrams(wordlist);
        }
        String bigram = wordlist.get(0) + " " + wordlist.get(1);
        //*    wordPool.add(bigram);
        String prevBigram, trigram;
        for(int i=2; i<wordlist.size(); i++) {
            prevBigram = bigram;
            bigram = wordlist.get(i-1) + " " + wordlist.get(i);
            trigram = prevBigram + " " + wordlist.get(i);
            //*     wordPool.add(bigram);
            wordPool.add(trigram);
        }

        LinkedList<String> listToReturn = new LinkedList<String>();
        listToReturn.addAll(wordPool);
        return listToReturn;
    }

    /**
     * haven't decided whether i should put this method here or TFIDFCaluclator
     * @param wordlist
     * @return
     */
    private LinkedList<String> filterWikiAnchor(LinkedList<String> wordlist) {
        DBConnector db = new DBConnector();
        LinkedList<String> filteredList = new LinkedList<String>();
        for(String word: wordlist) {
            ResultSet rs = db.getQueryResult("SELECT * from titles where title ='"+word+"'");
            try {
                if(rs.next()) {
                    filteredList.add(word);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        System.out.println(wordlist.size() - filteredList.size() + " words are dropped with WikiAnchorFiltering.");
        return wordlist;
    }

}
