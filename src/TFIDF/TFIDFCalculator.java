package TFIDF;

import Clustering.Document;
import db.DBConnector;
import parser.Tokenizer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by mhjang on 1/21/14.
 */
public class TFIDFCalculator {
    public static int UNIGRAM = 1;
    public static int BIGRAM = 2;
    public static int TRIGRAM = 3;

    public static int BINARYTFIDF = 0;
    public static int LOGTFIDF = 1;
    public static int AUGMENTEDTFIDF = 2;

    public HashSet<String> stopwords;
    public HashMap<String, Document> documentSet;
    public Tokenizer tokenizer;
    // test
    // contains a parsed text per each document in the given directory
    private HashMap<String, String> documentTextMap = new HashMap<String, String>();
    public TFIDFCalculator() throws IOException {
        stopwords = new HashSet<String>();
        BufferedReader br = new BufferedReader(new FileReader("stopwords.txt"));
        String line = br.readLine();
        int docLength = 0;
        while(line != null) {
            String word = line.replaceAll("[^A-Za-z0-9]", "");
            stopwords.add(word.toLowerCase().trim());
            line = br.readLine();
        }
        tokenizer = new Tokenizer();
    }

    /***
     *
     * @return a map of document set with tokenized n-grams and term frequency
     */
    public HashMap<String, Document> getDocumentSet(String dir, int ngram, boolean wikifiltering) {
        if(documentSet == null) {
            HashMap<String, LinkedList<String>> documentMap = documentize(dir, ngram, wikifiltering);
            calculateTFIDF(documentMap, TFIDFCalculator.LOGTFIDF);
        }
        return documentSet;
    }

    /**
     * reads files from the given directory and generate a Document object
     * returns a map <filename, a list of tokenized words>
     * @param dir
     */
    public HashMap<String, LinkedList<String>> documentize(String dir, int ngram, boolean wikifiltering) {
        HashMap<String, String> documentTextMap= readFiles(dir);
        HashMap<String, LinkedList<String>> documentTokensMap = new HashMap<String, LinkedList<String>>();
        for(String docName : documentTextMap.keySet()) {
            Document doc = tokenizer.tokenize(documentTextMap.get(docName), Tokenizer.TRIGRAM);
            LinkedList<String> wordPool = doc.getAllGrams();
            if(documentSet == null)
                documentSet = new HashMap<String, Document>();
            documentSet.put(docName, doc);
            if(wikifiltering)
                  documentTokensMap.put(docName, filterWikiAnchor(wordPool));
             else
              documentTokensMap.put(docName, wordPool);
        }
        return documentTokensMap;
    }

    /**
     * takes a list of words and drops if a word is not matched with any of wikipedia title.
     * Return the list of remaining words
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


    //public HashMap<String, LinkedList<String>> generateInvertedIndex(HashMap<String, LinkedList<String>> documentTokensMap) {}

    /**
     *
     * @param documentTokensMap
     * @param TFIDFOption  = {BINARYTFIDF, LOGTFIDF, AUGMENTEDTFIDF}
     */
    public void calculateTFIDF(HashMap<String, LinkedList<String>> documentTokensMap, int TFIDFOption) {
       // the number of documents that the term appears
       HashMap<String, Integer> binaryTermFreqInDoc = new HashMap<String, Integer>();
       // stores term frequency over the collection
       HashMap<String, Integer> globalMap = new HashMap<String, Integer>();
       // count term frequencies
       HashMap<String, HashMap<String, Integer>> docTermFreqMap = new HashMap<String, HashMap<String, Integer>>();
       for(String docName : documentTokensMap.keySet()) {
            LinkedList<String> wordPool = documentTokensMap.get(docName);
            // stores term frequency within the document
            HashMap<String, Integer> docTFMap = new HashMap<String, Integer>();
            for(String word : wordPool) {
                // counting global term frequency
                if(!globalMap.containsKey(word))
                    globalMap.put(word, 1);
                else
                {
                    int count = globalMap.get(word);
                    globalMap.put(word, count+1);
                }
                // counting term frequency within the document
                if(!docTFMap.containsKey(word)) {
                    docTFMap.put(word, 1);
                    // if the word appears in the collection for the first time, initiate counting
                    if(!binaryTermFreqInDoc.containsKey(word)) {
                        binaryTermFreqInDoc.put(word, 1);
                    }
                    else
                        binaryTermFreqInDoc.put(word, (binaryTermFreqInDoc.get(word) + 1));
                }
                else {
                    docTFMap.put(word,  (docTFMap.get(word) + 1));
                }
            }
           Document d = documentSet.get(docName);
           d.setTermFrequency(docTFMap);
        }
        for(String docName : docTermFreqMap.keySet()) {
            LinkedList<TermTFIDF> rankedTerms = null;
            boolean printResult = true;
            rankedTerms = getTFIDFScore(TFIDFOption, globalMap, docTermFreqMap.get(docName), binaryTermFreqInDoc, printResult);
        }
    }


    private LinkedList<TermTFIDF> getTFIDFScore(int TFType, HashMap<String, Integer> globalMap, HashMap<String, Integer> termFreqMap, HashMap<String, Integer> binaryTermFreqInDoc, boolean printTerms) {
        int totalDocs = globalMap.keySet().size();
        LinkedList<TermTFIDF> scoredTerms = new LinkedList<TermTFIDF>();
        LinkedList<String> terms = new LinkedList<String>();
        for(String term : termFreqMap.keySet()) {
            double tf = 0.0;
            if(TFType == TFIDFCalculator.BINARYTFIDF) {
                 if(termFreqMap.containsKey(term))
                      tf = 1.0;
             }
            else if (TFType == TFIDFCalculator.LOGTFIDF)
                tf = Math.log(termFreqMap.get(term) + 1);
            else if (TFType == TFIDFCalculator.AUGMENTEDTFIDF) {
                int maxFrequency = 0;
                LinkedList<String> termKeySet = new LinkedList<String>(termFreqMap.keySet());
                for(String t : termKeySet) {
                    if(termFreqMap.get(t) > maxFrequency)
                        maxFrequency = termFreqMap.get(t);
                }
                tf = 0.5 + 0.5 * ((double)(termFreqMap.get(term)) / (double)(maxFrequency));
            }

            double idf = Math.log(globalMap.keySet().size() / binaryTermFreqInDoc.get(term));
            double tfidf = tf * idf;
            scoredTerms.add(new TermTFIDF(term, tfidf));
        }

        Collections.sort(scoredTerms, new Comparator() {
            @Override
            public int compare(Object o1, Object o2) {
                TermTFIDF t1 = (TermTFIDF) (o1);
                TermTFIDF t2 = (TermTFIDF) (o2);

                return t1.score.compareTo(t2.score);
            }
        });
        if(printTerms) {
            for(TermTFIDF st : scoredTerms) {
                System.out.println(st.term + " : " + st.score);
             }
        }
        return scoredTerms;
    }

    /**
     * reads files in the given directory and return a parsed text per each document that's ready to be tokenized
     * @param dir
     * @return
     */
    private HashMap<String, String> readFiles(String dir) {
        HashMap<String, String> documentTextMap = new HashMap<String, String>();
        File folder = new File(dir);
        for (final File fileEntry : folder.listFiles()) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(fileEntry));
                StringBuilder sb = new StringBuilder();
                String line = br.readLine();
                int charsCount = 0;
                int wordsCount = 0;
                while (line != null) {
                    sb.append(line);
                    sb.append('\n');
                    line = br.readLine();
                }
                String text = sb.toString();
                documentTextMap.put(fileEntry.getName().toLowerCase(), text);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return documentTextMap;
    }




    class TermTFIDF {
        String term;
        Double score;
        public TermTFIDF(String term_, Double score_) {
            term = term_;
            score = score_;
        }

    }


}
