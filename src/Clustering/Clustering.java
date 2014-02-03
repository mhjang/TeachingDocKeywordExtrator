package Clustering;

import Similarity.CosineSimilarity;
import TFIDF.StopWordRemover;
import TFIDF.TFIDFCalculator;
import org.lemurproject.galago.core.parse.stem.KrovetzStemmer;
import parser.Tokenizer;
import parser.WikiParser;

import java.io.*;
import java.util.*;

/**
 * Created by mhjang on 1/28/14.
 * Tuesday 10:30pm
 */
public class Clustering {
    private String DUMMY = "dummy";
    public static void main(String[] args) throws IOException {
        Clustering clustering = new Clustering();
        TFIDFCalculator tfidf = new TFIDFCalculator();
        HashMap<String, Document> documentMap = tfidf.getDocumentSet("/Users/mhjang/Documents/teaching_documents/stemmed_coderm/", TFIDFCalculator.UNIGRAM, false);
     //   String[] topics = {"list and array representation", "graph traverse", "sorting algorithm"};
    //    clustering.stemming("./topics");
        BufferedReader br = new BufferedReader(new FileReader(new File("./topics_stemmed")));
        ArrayList<String> topiclist = new ArrayList<String>();
        String line = null;
        while((line= br.readLine()) != null) {
            topiclist.add(line);
        }
        clustering.naiveAssignmentFirstRandomAssign(documentMap, topiclist);
     //   clustering.naiveAssignmentLazyUpdate(documentMap, topiclist);
     }



    /**
     *
     * @param topicDocumentMap: a set of documents whose words are initial topic words
     * @param resourceDir: the directory that contains Wikipedia files
     *                   I manually downloaded a few matching Wikipedia articles.
     * Written 2/2/14 8:21pm
     */
    private HashMap<String, Document> expandTopicQueries(HashMap<String, Document> topicDocumentMap, String resourceDir) {
        File directory = new File(resourceDir);
        File[] listOfFiles = directory.listFiles();
        WikiParser wikiparser = new WikiParser();
        Tokenizer tokenizer = new Tokenizer();
        for(File file: listOfFiles) {
            String parsedText = wikiparser.parse(file.getPath());
            // For simplicity, I saved all wikipedi articles with the same topic labels
            // It ends with ".txt". I just need a name of the file, which is also the topic label
            String topicName = file.getName().substring(0, file.getName().length()-5);

            Document wikiDoc = tokenizer.tokenize(parsedText, Tokenizer.TRIGRAM);
            String matchingTopic = getMatchingTopicLabel(topicName.toLowerCase(), topicDocumentMap);
            Document topicDoc = topicDocumentMap.get(matchingTopic);
       //     System.out.println("Topic Name: " + topicName);
            topicDoc.mergeDocument(wikiDoc);
            topicDocumentMap.put(matchingTopic, topicDoc);
        }
        return topicDocumentMap;
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
    /**
     *
     * @param documentMap
     * @param topics
     * @throws IOException
     */
    public void naiveAssignmentFirstRandomAssign(HashMap<String, Document> documentMap, ArrayList<String> topics) throws IOException {

        AbstractMap.SimpleEntry <HashMap<String, LinkedList<String>>, HashMap<String, Document>> entry = (AbstractMap.SimpleEntry) convertTopicToDocument(documentMap, topics);
        HashMap<String, LinkedList<String>> clusters = entry.getKey();
        HashMap<String, Document> clusterFeatureMap = entry.getValue();

   //     clusterFeatureMap = expandTopicQueries(clusterFeatureMap, "./wikiexpansion_resource/stemmed/");
        /****
         * clustering
         */
        for(String docID : documentMap.keySet()) {
            Document document = documentMap.get(docID);
            double sim = 0.0, maxSim = 0.0;
            String bestTopic = null;
            for(String clusterTopic : clusterFeatureMap.keySet()) {
                Document clusterDoc = clusterFeatureMap.get(clusterTopic);
                sim = CosineSimilarity.CosineSimilarity(document, clusterDoc);
                if(sim > maxSim) {
                    maxSim = sim;
                    bestTopic = clusterTopic;
                }
            }
            if(bestTopic == null) {
                // similarity to all existing cluster labels was 0
                bestTopic = DUMMY;
            }
            LinkedList<String> clusterList = clusters.get(bestTopic);
            clusterList.add(docID);
            clusters.put(docID, clusterList);

            // updating cluster features
            if(bestTopic != DUMMY) {
                Document clusterDoc = clusterFeatureMap.get(bestTopic);
                clusterDoc.mergeDocument(document);
            }
        }

        /***
         * print the clustering result
         */

        printCluster(clusters, topics);

    }


    /**
     * Written in in 2/2 3:23 pm
     * assign a set of documents to the cluster whose similarity score to is the maximum at one phase, and update the cluster features.
     * Then come back to the "dummy" clusters, and deal with the unassigned documents until it converges or there is no document to be assigned.
     * @param documentMap
     * @param topics
     * @throws IOException
     */
    public void naiveAssignmentLazyUpdate(HashMap<String, Document> documentMap, ArrayList<String> topics) throws IOException {
        AbstractMap.SimpleEntry <HashMap<String, LinkedList<String>>, HashMap<String, Document>> entry = (AbstractMap.SimpleEntry) convertTopicToDocument(documentMap, topics);
        HashMap<String, LinkedList<String>> clusters = entry.getKey();
        HashMap<String, Document> clusterFeatureMap = entry.getValue();
//        clusterFeatureMap = expandTopicQueries(clusterFeatureMap, "./wikiexpansion_resource/stemmed/");

        /****
         * clustering
         */
        LinkedList<String> dummyCluster = clusters.get(DUMMY);
        dummyCluster.addAll(documentMap.keySet());
        boolean isConverged = false, finished = false;
        while(!isConverged || !finished) {
            // since we can't modify the list while enumerating, save the list of items we're moving, and delete them after the loop
            LinkedList<String> documentMoved = new LinkedList<String>();
            HashMap<String, LinkedList<String>> clusterUpdate = initializeTopicCluster(topics);
            isConverged = true;
            for(String docID : dummyCluster) {
                Document document = documentMap.get(docID);
                double sim = 0.0, maxSim = 0.0;
                String bestTopic = null;
                for(String clusterTopic : clusterFeatureMap.keySet()) {
                    if(clusterTopic == DUMMY) continue;
                    Document clusterDoc = clusterFeatureMap.get(clusterTopic);
                    sim = CosineSimilarity.CosineSimilarity(document, clusterDoc);
                    if(sim > maxSim) {
                        maxSim = sim;
                        bestTopic = clusterTopic;
                    }
                }
                if(bestTopic != null) {
//                    System.out.println(bestTopic);
                    LinkedList<String> clusterList =  clusterUpdate.get(bestTopic);
                    clusterList.add(docID);
                     clusterUpdate.put(bestTopic, clusterList);
                    isConverged = false;
                }
            }
            int numOfDocMoved = 0;
            int numOfDummyDocs = dummyCluster.size();
            // cluster feature update
            for(String clusterTopic :  clusterUpdate.keySet()) {
                LinkedList<String> clusterList = clusters.get(clusterTopic);
                LinkedList<String> clusterListToAttach =  clusterUpdate.get(clusterTopic);
                // removing the documents from the dummy cluster that were just assigned to clusters
                dummyCluster.removeAll(clusterListToAttach);
                numOfDocMoved += clusterListToAttach.size();
                // updating the cluster (1) by adding the documents and (2) updating the features
                clusterList.addAll(clusterListToAttach);
                Document clusterDoc = clusterFeatureMap.get(clusterTopic);
                for(String docID : clusterListToAttach)
                    clusterDoc.mergeDocument(documentMap.get(docID));
                clusterFeatureMap.put(clusterTopic, clusterDoc);
            }
            clusters.put(DUMMY, dummyCluster);
            if(dummyCluster.isEmpty()) finished = true;
            System.out.println("# of dummy Docs: " + numOfDummyDocs +", # of documents moved: " + numOfDocMoved);
        }
        printCluster(clusters, topics);
   }


    public void naiveAssignmentSortByPurity(HashMap<String, Document> documentMap, ArrayList<String> topics) throws IOException {

        AbstractMap.SimpleEntry <HashMap<String, LinkedList<String>>, HashMap<String, Document>> entry = (AbstractMap.SimpleEntry) convertTopicToDocument(documentMap, topics);
        HashMap<String, LinkedList<String>> clusters = entry.getKey();
        HashMap<String, Document> clusterFeatureMap = entry.getValue();

        /****
         * clustering
         */
        // for each topic find the best document at each phase

        /***
         * print the clustering result
         */
        printCluster(clusters, topics);

    }


    private void printCluster(HashMap<String, LinkedList<String>> clusters, ArrayList<String> topics) {
        for(String topic : topics) {
            LinkedList<String> cluster = clusters.get(topic);
            System.out.println(topic + ":" + cluster.toString());
        }
        System.out.println(clusters.get(DUMMY).toString());
    }


    /**
     * 2014/2/2 3:05 pm
     * Made a separate method for this. Let's try not to duplicate any code component..
     * it'll get crazy when you try to modify the same thing over the multiple duplicate code components!
     * @param documentMap
     * @param topics
     * @return
     * @throws IOException
     */
    private Map.Entry<HashMap<String, LinkedList<String>>, HashMap<String, Document>> convertTopicToDocument(HashMap<String, Document> documentMap, ArrayList<String> topics) throws IOException {
        // initializing the cluster dictionary
        HashMap<String, LinkedList<String>> clusters = initializeTopicCluster(topics);
        /***
         *  constructing tokens for topic queries
         *  data structure and list --> "data" "structure" "and" "list"
         */
        HashMap<String, Document> clusterFeatureMap = new HashMap<String, Document>();
        StopWordRemover swr = new StopWordRemover();
        for(String topic: topics) {
            String[] tokens = topic.split("[:, ]");
            tokens = swr.removeStopWords(tokens);
            // using hashset to remove duplicate words
            // constructing the initial set of feature words and their frequency of the cluster
            LinkedList<String> topicTokens = new LinkedList<String>();
            HashMap<String, Integer> topicTokensFreq = new HashMap<String, Integer>();
            for(String token : tokens) {
                if(token.trim().length() > 0) {
                    // if this term had already appeared before, just add the frequency count
                    if(topicTokensFreq.containsKey(token))
                        topicTokensFreq.put(token, topicTokensFreq.get(token) + 1);
                        // if this term appears for the first time, initiate the count and add it to the word list
                    else {
                        topicTokensFreq.put(token, 1);
                        topicTokens.add(token);
                    }
                    System.out.println("token: " + token);
                }
            }
            Document topicDoc = new Document(topicTokens);
            topicDoc.setTermFrequency(topicTokensFreq);
            clusterFeatureMap.put(topic, topicDoc);
        }
        // I know this is an abuse of "Entry" for using a tuple.. but it works!
        return new AbstractMap.SimpleEntry<HashMap<String, LinkedList<String>>, HashMap<String, Document>>(clusters, clusterFeatureMap);
    }

    private HashMap<String, LinkedList<String>> initializeTopicCluster(ArrayList<String> topics) {
        HashMap<String, LinkedList<String>> clusters = new HashMap<String, LinkedList<String>>();
        for(String t : topics) {
            clusters.put(t, new LinkedList<String>());
        }
        clusters.put(DUMMY, new LinkedList<String>());
        return clusters;
    }
}
