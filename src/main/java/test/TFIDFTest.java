package test;

import Clustering.Document;
import Clustering.DocumentCollection;
import TFIDF.TFIDFCalculator;
import junit.framework.TestCase;
import parser.Tokenizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by mhjang on 5/16/14.
 */
public class TFIDFTest extends TestCase {
    DocumentCollection dc;

    public void setUp() {
        TFIDFCalculator tfidf = null;

        try {
            tfidf = new TFIDFCalculator(false);
            tfidf.calulateTFIDF(TFIDFCalculator.LOGTFIDF, "./testdata/", Tokenizer.UNIGRAM, false);
            dc = tfidf.getDocumentCollection();

        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public void testEnds() {
        /***
         * Test for term count within a document
         * Manual check over the test collection
         */
        for (String docName : dc.getDocumentSet().keySet()) {
            Document d = dc.getDocument(docName);
            System.out.println("*************" + docName + "*************");
            LinkedList<Map.Entry<String, Integer>> termMap = d.getTopTermsTF(10);
            for (Map.Entry<String, Integer> entry : termMap) {
                System.out.println(entry.getKey() + " : " + entry.getValue());
            }
        }
        /**
         * Test for global term count within the collection (for raw term frequency)
         */
        HashMap<String, Integer> globaltermMap = dc.getglobalTermCountMap();

        for (String term : globaltermMap.keySet()) {
            int globalCount = 0;
            for (String docName : dc.getDocumentSet().keySet()) {
                Document d = dc.getDocument(docName);
                globalCount += d.getTermFrequency(term);

            }
            assertEquals((int) globaltermMap.get(term), globalCount);

        }
        /**
         * Test for global term count within the collection (for raw term frequency)
         */

        HashMap<String, Integer> globaltermBinaryMap = dc.getBinaryTermFreqInDoc();

        for (String term : globaltermBinaryMap.keySet()) {
            int globalBinaryCount = 0;
            for (String docName : dc.getDocumentSet().keySet()) {
                Document d = dc.getDocument(docName);
                globalBinaryCount += d.getTermFrequency(term)>0?1:0;
            }
            assertEquals((int) globaltermBinaryMap.get(term), globalBinaryCount);

        }


        /**
         * Test for TF-IDF score
         */
        for (String docName : dc.getDocumentSet().keySet()) {
            Document d = dc.getDocument(docName);
            LinkedList<Map.Entry<String, Integer>> tfidfMap = d.getTopTermsTF(10);
            for (Map.Entry<String, Integer> entry : tfidfMap) {
                assertEquals(Math.log(d.getTermFrequency(entry.getKey()) + 1) * Math.log((double)dc.getCollectionSize()/(double)globaltermBinaryMap.get(entry.getKey())), d.getTFIDF(entry.getKey()));
            }
        }
    }
}
