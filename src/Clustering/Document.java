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
    private String docName;
    HashMap<String, Integer> termFrequency;
    public Document(String docName, LinkedList<String> unigrams, LinkedList<String> bigrams, LinkedList<String> trigrams) {
        this.unigrams = unigrams;
        this.bigrams = bigrams;
        this.trigrams = trigrams;
        this.docName = docName;
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

    public String getName() { return this.docName; }

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

    /**
     * for the sake of quality evaluations
     */
    public void printTerms() {
       System.out.println("******************************" + getName() + "*****************************");
       LinkedList<String> allTerms = getAllGrams();
       for(String term : allTerms) {
           System.out.println(term);
       }
    }

    public static int removeInfrequentTerms(Document doc, int ngramType, HashMap<String, Integer> termOccrurenceDic, int threshold) {
        LinkedList<String> terms = null;
        int removedTerms = 0;
        if(ngramType == Document.UNIGRAM)
            terms = new LinkedList<String>(doc.getUnigrams());
        else if(ngramType == Document.BIGRAM)
            terms = new LinkedList<String>(doc.getBigrams());
        else if(ngramType == Document.TRIGRAM)
            terms = new LinkedList<String>(doc.getTrigrams());
        for(String term : terms) {
            if(termOccrurenceDic.containsKey(term)) {
                if(termOccrurenceDic.get(term) < threshold) {
                    doc.removeTerm(ngramType, term);
                    removedTerms++;
                }
            }
            else {
                doc.removeTerm(ngramType, term);
                removedTerms++;

            }
        }
//        doc.printTerms();
        return removedTerms;
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
     * Written in 2/14/2014
     * @param ngramType
     * @param term
     *
     * This is to remove infrequent terms from QueryExpander
     */
    public void removeTerm(int ngramType, String term) {
        if(ngramType == Document.UNIGRAM)
            unigrams.remove(term);
        else if(ngramType == Document.BIGRAM)
            bigrams.remove(term);
        else if(ngramType == Document.TRIGRAM)
            trigrams.remove(term);

    }




    /**
     * return a list of unigrams, bigrams, trigrams combined
     * The list has to be generated on the fly when the method is called because unigrams / bigrams / trigrams can be altered after the document is constructed
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
