package LanguageModeling;

import Clustering.Document;
import Clustering.DocumentCollection;
import TFIDF.TFIDFCalculator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by mhjang on 3/2/14.
 */
public class LanguageModeling {

    public static void main(String[] args) throws IOException {
        PrintStream console = System.out;
        File file = new File("log.txt");
        FileOutputStream fos = new FileOutputStream(file);
        PrintStream ps = new PrintStream(fos);
        System.setOut(ps);


        getProbabilityBySentence();
    }
    public static void getProbabilityBySentence() throws IOException {
        TFIDFCalculator tfidf = new TFIDFCalculator();
        DocumentCollection dc = tfidf.getDocumentCollection("/Users/mhjang/Documents/teaching_documents/stemmed/", TFIDFCalculator.TRIGRAM, false);
        HashMap<String, Document> docSet = dc.getDocumentSet();
        HashMap<String, Double> globalWordProb = dc.getWordProbablity();

        /****
         * Unigram Language Modeling
         * P(w | D) = |C(w)|/|D|

        for(String docName : docSet.keySet()) {
            System.out.println(docName);
            Document doc = docSet.get(docName);
            HashMap<String, Double> wordProb = doc.getWordProbability();
            LinkedList<ArrayList<String>> corpusByLine = doc.getCorpusByLine();
            for(ArrayList<String> tokensInLine: corpusByLine) {
                double prob = 1.0;
                for(String token : tokensInLine) {
                    prob = prob * wordProb.get(token);
                }
                System.out.print(prob + ":");
                printListToLine(tokensInLine);
            }
        }
        */

        /***
         * Dirichlet smoothing
         */

        int mu = 2000;
        String docName = "MC_Graph_Theory.pdf.txt";
    //    for(String docName : docSet.keySet()) {
            System.out.println(docName);
            Document doc = docSet.get(docName.toLowerCase());
//            HashMap<String, Double> wordProb = doc.getWordProbability();
            HashMap<String, Integer> wordFreq = doc.getTermFrequency();
            int D = doc.getWordCountSum();
            LinkedList<ArrayList<String>> corpusByLine = doc.getCorpusByLine();
            for(ArrayList<String> tokensInLine: corpusByLine) {
                double prob = 1.0;
                for(String token : tokensInLine) {
                    double tokenProb = (wordFreq.get(token) + mu * (globalWordProb.get(token))) / (D + mu);
                    System.out.println(token + ": " + tokenProb);
         //           prob *= tokenProb;
                }
       //         prob = (double)prob / (double)tokensInLine.size();
        //        System.out.print(prob + ":");
        //        printListToLine(tokensInLine);
     //       }
        }
    }

    private static void printListToLine(ArrayList<String> tokensInLine) {
        for(int i=0; i<tokensInLine.size(); i++) {
            System.out.print(tokensInLine.get(i) + " ");
        }
        System.out.println();
    }

}
