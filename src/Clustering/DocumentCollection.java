package Clustering;

import java.util.HashMap;

/**
 * Created by mhjang on 2/18/14.
 */
public class DocumentCollection {

    private HashMap<String, Document> documentSet = null;
    private HashMap<String, Integer> globalTermCountMap = null;
    private HashMap<String, Integer> binaryTermFreqInDoc = null;

    public DocumentCollection(HashMap<String, Document> documentSet, HashMap<String, Integer> globalTermCountMap, HashMap<String, Integer> binaryTermFreqInDoc) {
        this.documentSet = documentSet;
        this.globalTermCountMap = globalTermCountMap;
        this.binaryTermFreqInDoc = binaryTermFreqInDoc;
    }
    public HashMap<String, Document> getDocumentSet()
    {
        return documentSet;
    }

    public HashMap<String, Integer> getglobalTermCountMap()
    {
        return globalTermCountMap;
    }
    public HashMap<String, Integer> getBinaryTermFreqInDoc()
    {
        return binaryTermFreqInDoc;
    }

}