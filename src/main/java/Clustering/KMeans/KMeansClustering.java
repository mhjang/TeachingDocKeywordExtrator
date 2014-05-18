package Clustering.KMeans;

import Clustering.*;
import Similarity.CosineSimilarity;

import java.io.IOException;
import java.util.*;

/**
 * Created by mhjang on 5/13/14.
 * @author Myung-ha Jang (mhjang@cs.umass.edu)
 */
public class KMeansClustering extends Clustering{
    ArrayList<String> topiclist;
    DocumentCollection dc;
    public KMeansClustering(ArrayList<String> topiclist, DocumentCollection dc) {
        super(dc);
        this.topiclist = topiclist;
        this.dc = dc;
    }

    /**
     * initialize centroid with the given topic label vectors
     * @return
     * @throws IOException
     */
    public CentroidDocument[] initCentroid() throws IOException {
        AbstractMap.SimpleEntry<HashMap<String, LinkedList<String>>, HashMap<String, Document>> entry = (AbstractMap.SimpleEntry) convertTopicToDocument(topiclist);
        CentroidDocument[] centroids = new CentroidDocument[(entry.getValue().size())];
        HashMap<String, Document> topicDocMap = entry.getValue();
        int i=0;
        for(Document d : topicDocMap.values()) {
            CentroidDocument cd = new CentroidDocument(d);
            centroids[i++] = cd;
        }
        return centroids;
    }
    public void clusterRun(int maxIteration, double rssThreshold) throws IOException {
        CentroidDocument[] centroids = initCentroid();
        double curRSS = 0.0, prevRSS = 0.0;
        int[] aa = new int[5];
        LinkedList<LinkedList<Document>> clusterAssignments = null;
        Collection<Document> collection =  dc.getDocumentSet().values();
        for (int k = 0; k < maxIteration; k++) {
            System.out.println("************* Iteration " + k + " *************");
            clusterAssignments = new LinkedList<LinkedList<Document>>();
            //initialize the cluster assignments
            for(int i = 0; i<centroids.length; i++) {
                clusterAssignments.add(new LinkedList<Document>());
            }
            // assign a document to each cluster that has the highest cosine similarity
            for (Document d : collection) {
                double maxScore = 0.0;
                int bestClusterIdx = -1;
                for (int i = 0; i < centroids.length; i++) {
                    double score = CosineSimilarity.TFIDFCosineSimilarity(d, centroids[i]);
                    if (score > maxScore) {
                        maxScore = score;
                        bestClusterIdx = i;
                    }
       //            System.out.println(score);
                }
                if(bestClusterIdx > 0) clusterAssignments.get(bestClusterIdx).add(d);
            }
            // update the centroid vector values by the means of the assigned documents
            for (int i = 0; i < centroids.length; i++) {
                CentroidDocument centroid = centroids[i];
                System.out.println("cluster: " + clusterAssignments.get(i).size());

                centroid.updateNewCentroid(clusterAssignments.get(i), CentroidDocument.TFIDFVECTOR);
                centroids[i] = centroid;
                System.out.println("centroid size after update: " + centroid.getTFIDFMap().size());
            }
            /**
             * compute RSS for termination condition
             */
            prevRSS = curRSS;
            curRSS = 0.0;
            for (int i = 0; i < centroids.length; i++) {
                curRSS += computeRSS(clusterAssignments.get(i));
            }
            System.out.println("RSS : " + curRSS);
            if (Math.abs(curRSS / prevRSS) < rssThreshold) break;
        for(int i=0; i< centroids.length; i++) {
                System.out.print(topiclist.get(i) + ": \t");
                LinkedList<Document> cluster = clusterAssignments.get(i);
                for (Document d : cluster) {
                    System.out.print(d.getName() + "\t");
                }
                System.out.println();
            }
        }
        /**
         * print the clusters
         */

    }


    public double computeRSS(LinkedList<Document> cluster) {
        HashSet<String> keys = new HashSet<String>();
        for(Document d: cluster) {
            keys.addAll(d.getAllGrams());
        }
        int vecLen = keys.size();
        String[] labelList = new String[vecLen];
        labelList = keys.toArray(labelList);

        // vectorization
        LinkedList<Double[]> documentVectors = new LinkedList<Double[]>();
        for(Document d: cluster) {
            Double[] v = new Double[vecLen];
            for(int i=0; i<vecLen; i++) {
                String label = labelList[i];
                v[i] = d.getTFIDF(label);
       //         System.out.print(v[i] + "\t");
            }
            documentVectors.add(v);
      //      System.out.println();
        }
        // computing a centroid
        Double[] centroidVector = new Double[vecLen];
        int clusterSize = documentVectors.size();
        for(int i=0; i<vecLen; i++) {
            centroidVector[i] = 0.0;
           for(Double[] vector : documentVectors) {
                centroidVector[i] += vector[i];
            }
            centroidVector[i] = centroidVector[i] / (double)clusterSize;

        }
        double rssAtK = 0.0;
        for(Double[] vector: documentVectors)
        {
            rssAtK +=rss(centroidVector, vector);
        }
        return rssAtK;
    }

    public double rss(Double[] centroid, Double[] vector) {
        double rssSum = 0.0;
        for(int i=0; i<centroid.length; i++) {
             double diff = Math.abs(centroid[i] - vector[i]);
             rssSum += (diff * diff);

       }
  //      System.out.println(rssSum);
        return rssSum;

    }
}
