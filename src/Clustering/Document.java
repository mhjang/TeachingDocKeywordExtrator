package Clustering;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Created by mhjang on 1/31/14.
 */
public class Document {
    private LinkedList<String> unigrams;
    private LinkedList<String> bigrams;
    private LinkedList<String> trigrams;
    public static int UNIGRAM = 0;
    public static int BIGRAM = 1;
    public static int TRIGRAM = 2;

    HashMap<String, Integer> termFrequency;
    public Document(LinkedList<String> unigrams, LinkedList<String> bigrams, LinkedList<String> trigrams) {
        this.unigrams = unigrams;
        this.bigrams = bigrams;
        this.trigrams = trigrams;
    }

    public Document(LinkedList<String> unigrams) {
        this.unigrams = unigrams;
    }

    public void setUnigrams(LinkedList<String> grams) {
        this.unigrams = grams;
    }

    public void setBigrams(LinkedList<String> grams) {
        this.bigrams = grams;
    }

    public void setTrigrams(LinkedList<String> grams) {
        this.trigrams = grams;
    }

    public LinkedList<String> getUnigrams() {
        return this.unigrams;
    }

    public LinkedList<String> getBigrams() {
        return this.bigrams;
    }

    public LinkedList<String> getTrigrams() {
        return this.trigrams;
    }

    public void setTermFrequency(HashMap<String, Integer> termFrequency) {
        this.termFrequency = termFrequency;

    }

    public boolean hasBigrams() {
        if(bigrams == null) return false;
        return true;
    }

    public boolean hasTrigrams() {
        if(trigrams == null) return false;
        return true;
    }

    public HashMap<String, Integer> getTermFrequency() {
        return termFrequency;
    }
    public void mergeDocument(Document d) {
        mergeTerms(d.getUnigrams(), Document.UNIGRAM);
        if(d.hasBigrams())
            mergeTerms(d.getBigrams(), Document.BIGRAM);
        if(d.hasTrigrams())
            mergeTerms(d.getTrigrams(), Document.TRIGRAM);
    }

    private void mergeTerms(LinkedList<String> anotherGrams, int type) {
        for(String w : anotherGrams) {
            if(termFrequency.containsKey(w))
                termFrequency.put(w, (termFrequency.get(w)+1));
            else {
                termFrequency.put(w, 1);
                if(type == Document.UNIGRAM)
                   unigrams.add(w);
                else if(type == Document.BIGRAM) {
                    if(bigrams == null) bigrams = new LinkedList<String>();
                    bigrams.add(w);
                }
                else if(type == Document.TRIGRAM) {
                    if(trigrams == null) trigrams = new LinkedList<String>();
                    trigrams.add(w);
                }
                else {
                    System.out.println("ERROR: UNKNOWN N-GRAM TYPE");
                    return;
                }
            }
        }
    }
    /**
     * return a list of unigrams, bigrams, trigrams combined
     * @return
     */
    public LinkedList<String> getAllGrams() {
        LinkedList<String> l = new LinkedList<String>();
        if(unigrams != null)
            l.addAll(getUnigrams());
        if(bigrams != null)
           l.addAll(getBigrams());
        if(trigrams != null)
            l.addAll(getTrigrams());
        return l;
    }


}
