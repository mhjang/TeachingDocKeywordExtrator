package LanguageModeling;

import Clustering.Document;
import Clustering.DocumentCollection;
import TFIDF.TFIDFCalculator;
import indexing.NGramReader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mhjang on 3/2/14.
 */
public class LanguageModeling {
    NGramReader ng;

    public LanguageModeling() throws IOException {
        ng = new NGramReader();
    }
    public static void main(String[] args) throws IOException {
        PrintStream console = System.out;
        File file = new File("log.txt");
        FileOutputStream fos = new FileOutputStream(file);
        PrintStream ps = new PrintStream(fos);
        System.setOut(ps);

        LanguageModeling lm = new LanguageModeling();
        lm.getProbabilityBySentence();
    }



    public double getCollectionProbability(String term) {
        BigInteger collectionCount = new BigInteger("1024908267229");
        Integer count = ng.lookUpTerm(term);
        double collectionProb = (count.doubleValue()) / (collectionCount.doubleValue());
   //     System.out.println(count + " : " + collectionProb);
        return collectionProb;
    }

    public void getProbabilityBySentence() throws IOException {
        TFIDFCalculator tfidf = new TFIDFCalculator();
        DocumentCollection dc = tfidf.getDocumentCollection("/Users/mhjang/Documents/teaching_documents/stemmed/", TFIDFCalculator.TRIGRAM, false);
        HashMap<String, Document> docSet = dc.getDocumentSet();
  //      HashMap<String, Double> globalWordProb = dc.getWordProbablity();

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
        String docName = "4.04.binarytrees.ppt.txt";
    //    for(String docName : docSet.keySet()) {
            System.out.println(docName);
            Document doc = docSet.get(docName.toLowerCase());

            HashMap<String, Integer> wordFreq = doc.getTermFrequency();
            int D = doc.getWordCountSum();
            LinkedList<String> corpus = doc.getCorpus();
            int len = corpus.size();
            int k = 7;

            System.out.println("==================Unigram=======================");
        /**
         * 5 window unigram language modeling
        */
            for(int i=0; i<len; i+=k) {
                int endIdx = i+k;
                if(endIdx > len)
                    endIdx = len;
                List<String> sublist = new LinkedList();
                sublist =  corpus.subList(i, endIdx);
                double logprob = 0.0;
                int sublen = sublist.size();
                for(int j = 0; j<sublen; j++) {
                    String token = sublist.get(j);
                    double tokenProb = (wordFreq.get(token) + mu * (getCollectionProbability(token))) / (D + mu);
                    logprob += Math.log(tokenProb);
                }
                System.out.println(sublist + "\t" + logprob);
            }
        System.out.println("==================Bigram=======================");

        /**
         * 5 window bigram language modeling
            */
            for(int i=0; i<len; i+=k) {
                int endIdx = i+k;
                if(endIdx > len)
                    endIdx = len;
                List<String> sublist = new LinkedList();
                sublist =  corpus.subList(i, endIdx);
                double logprob = 0.0;
                int sublen = sublist.size();
                for(int j = 0; j<sublen-1; j++) {
                    String token1 = sublist.get(j);
                    String token2 = sublist.get(j+1);
                    String bigram = token1 + " " + token2;
                    int tokenCount = ng.lookUpTerm(token1);
                    double prob_wi_given_w_i_1 = (double)ng.lookUpTerm(bigram) / (double)(tokenCount);
                    if(Double.isNaN(prob_wi_given_w_i_1))
                        prob_wi_given_w_i_1= 0.0;

                    //     System.out.println(token1 + ", " + token2);
                    double tokenProb = (wordFreq.get(bigram) + mu * (prob_wi_given_w_i_1)) / (tokenCount + mu);
                    logprob += Math.log(tokenProb);
                }
                System.out.println(sublist + "\t" + logprob);
            }

         System.out.println("==================Trigram=======================");

        /**
         *  5 window bigram language modeling
         */
            for(int i=0; i<len; i+=k) {
                int endIdx = i+k;
                if(endIdx > len)
                    endIdx = len;
                List<String> sublist = new LinkedList();
                sublist =  corpus.subList(i, endIdx);
                double logprob = 0.0;
                int sublen = sublist.size();
                for(int j = 0; j<sublen-2; j++) {
                    String token1 = sublist.get(j);
                    String token2 = sublist.get(j+1);
                    String token3 = sublist.get(j+2);
                    String bigram = token1 + " " + token2;
                    String trigram = token1 + " " + token2 + " " + token3;
                    int tokenCount = ng.lookUpTerm(bigram);
                    double prob_wi_given_w_i_1_w_i_2 = (double)ng.lookUpTerm(trigram) / (double)(tokenCount);
                    if(Double.isNaN(prob_wi_given_w_i_1_w_i_2))
                        prob_wi_given_w_i_1_w_i_2= 0.0;
                    //     System.out.println(token1 + ", " + token2);
                    double tokenProb = (wordFreq.get(trigram) + mu * (prob_wi_given_w_i_1_w_i_2)) / (tokenCount + mu);
                    logprob += Math.log(tokenProb);
                }
                System.out.println(sublist + "\t" + logprob);
            }

        /*
            LinkedList<ArrayList<String>> corpusByLine = doc.getCorpusByLine();
            for(ArrayList<String> tokensInLine: corpusByLine) {
                double logprob = 0.0;
                for(String token : tokensInLine) {
                    double tokenProb = (wordFreq.get(token) + mu * (getCollectionProbability(token))) / (D + mu);
                    logprob += Math.log(tokenProb);
                }
                System.out.println(tokensInLine + ": " + (logprob)/(tokensInLine.size()));
             }
            */
       }


    private static void printListToLine(ArrayList<String> tokensInLine) {
        for(int i=0; i<tokensInLine.size(); i++) {
            System.out.print(tokensInLine.get(i) + " ");
        }
        System.out.println();
    }

}
