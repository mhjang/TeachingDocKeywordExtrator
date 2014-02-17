package QueryExpansion;

import Clustering.Document;
import parser.Stemmer;
import parser.Tokenizer;
import parser.WikiParser;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by mhjang on 2/16/14.
 * Written by 9:13 pm
 */
public class QueryExpander {
    public QueryExpander() {

    }

    /**
     *
     * @param topicDocumentMap: a set of documents whose words are initial topic words
     * @param resourceDir: the directory that contains Wikipedia files
     *                   I manually downloaded a few matching Wikipedia articles.
     * Written 2/2/14 8:21pm
     */
    public HashMap<String, Document> expandTopicQueries(HashMap<String, Document> topicDocumentMap, String resourceDir) {
        File directory = new File(resourceDir);
        File[] listOfFiles = directory.listFiles();
        WikiParser wikiparser = new WikiParser();
        Tokenizer tokenizer = new Tokenizer();
        Stemmer stemmer = new Stemmer();
        for(File file: listOfFiles) {
            String parsedText = wikiparser.parse(file.getPath());
            parsedText = stemmer.stemString(parsedText);
            //   System.out.println("parsed " + file.getName());
            // For simplicity, I saved all wikipedi articles with the same topic labels
            // It ends with ".txt". I just need a name of the file, which is also the topic label
            String topicName = file.getName().substring(0, file.getName().length()-5);

            Document wikiDoc = tokenizer.tokenize(file.getName(), parsedText, Tokenizer.TRIGRAM);
            String matchingTopic = getMatchingTopicLabel(topicName.toLowerCase(), topicDocumentMap);
            Document topicDoc = topicDocumentMap.get(matchingTopic);
            //     System.out.println("Topic Name: " + topicName);
            topicDoc.mergeDocument(wikiDoc);
            topicDocumentMap.put(matchingTopic, topicDoc);
        }
        return topicDocumentMap;
    }

    /**
     * expand the topic queries with frequent terms whose occurence in the collection is bigger than a given threshold
     * @param topicDocumentMap
     * @param resourceDir
     * @return
     */
    public HashMap<String, Document> expandTopicQueriesWithFrequentTerms(HashMap<String, Document> topicDocumentMap, String resourceDir, HashMap<String, Integer> termOccrurenceDic, int threshold) {
        File directory = new File(resourceDir);
        File[] listOfFiles = directory.listFiles();
        WikiParser wikiparser = new WikiParser();
        Tokenizer tokenizer = new Tokenizer();
        Stemmer stemmer = new Stemmer();
        int numOfExpandedTerms = 0;
        for(File file: listOfFiles) {
            String parsedText = wikiparser.parse(file.getPath());
            parsedText = stemmer.stemString(parsedText);
            String topicName = file.getName().substring(0, file.getName().length()-5);
            Document wikiDoc = tokenizer.tokenize(file.getName(), parsedText, Tokenizer.TRIGRAM);
            /****
             * filtering out the infrequent terms
             *
             * I could've modified the tokenizer in a way that throws away the terms that are not in the freequent term pool,
             * but I just thought it would be better if "Tokenizer" does tokenizing, and this sort of post-processing for query expansion
             * is done in the "Query Expander" for the sake of component modularization. (2/16/2014)
             *
             */
            removeInfrequentTerms(wikiDoc, Document.UNIGRAM, termOccrurenceDic, threshold);
            removeInfrequentTerms(wikiDoc, Document.BIGRAM, termOccrurenceDic, threshold);
            removeInfrequentTerms(wikiDoc, Document.TRIGRAM, termOccrurenceDic, threshold);

            numOfExpandedTerms += wikiDoc.getAllGrams().size();

            String matchingTopic = getMatchingTopicLabel(topicName.toLowerCase(), topicDocumentMap);
            Document topicDoc = topicDocumentMap.get(matchingTopic);
            //     System.out.println("Topic Name: " + topicName);
            topicDoc.mergeDocument(wikiDoc);
            topicDocumentMap.put(matchingTopic, topicDoc);
        }
        System.out.println("# of average expanded terms: " + (double)(numOfExpandedTerms)/(double)(listOfFiles.length));
        return topicDocumentMap;


    }

    private void removeInfrequentTerms(Document doc, int ngramType, HashMap<String, Integer> termOccrurenceDic, int threshold) {
        LinkedList<String> terms = null;
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
                }
            }
            else {
                doc.removeTerm(ngramType, term);

            }
        }
        doc.printTerms();

    }


    /**
     * The name of the Wikipedia article is like "analysis of algorithm" whereas the topic key is "analysis of algorithm, and bubble sort..."
     * So instead of retrieving the matching document by hashing, we have to see if the key "contains" the given query
     * The usage of this method is very limited; it works under the assumption that the wikipedia article's filename is always the part of the topic key string.
     * I hate generating such ad-hoc methods like this, but at this point making it work is more important.
     *
     * Written in 2/2/14 8:55 pm
     * @param articleName
     * @param topicDocumentMap
     * @return
     */
    private String getMatchingTopicLabel(String articleName, HashMap<String, Document> topicDocumentMap) {
        for(String label : topicDocumentMap.keySet()) {
            if(label.contains(articleName))
                return label;
        }
        return null;
    }
}


