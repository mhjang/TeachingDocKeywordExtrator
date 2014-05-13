package evaluation;

import Clustering.DocumentCollection;
import NLP.DetectCodeComponent;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by mhjang on 2/7/14.
 * Written at 2:01 pm
 */
public class ClusteringFMeasure {

    HashMap<String, LinkedList<String>> clusters;
    HashMap<String, Integer> clusterLabelMap;
    HashMap<Integer, LinkedList<String>> goldClusters;
    ArrayList<String> topiclist;
    DocumentCollection dc;

    public ClusteringFMeasure(HashMap<String, LinkedList<String>> clusters_, HashMap<String, Integer> clusterLabelMap_, ArrayList<String> topiclist_, String goldDir, DocumentCollection dc) {
        clusters = clusters_;
        clusterLabelMap = clusterLabelMap_;
        goldClusters = readGoldstandard(goldDir);
        this.topiclist = topiclist_;
        this.dc = dc;
    }

    public void getAccuracy() {
        computeAccuracy(topiclist);

    }
    // read goldstandard
    public static HashMap<Integer, LinkedList<String>> readGoldstandard(String goldDir) {
        HashMap<Integer, LinkedList<String>> goldstandard = null;
        Integer clusterID = 0;
        try{
            BufferedReader br = new BufferedReader(new FileReader(goldDir));
            goldstandard = new HashMap<Integer, LinkedList<String>>();
            String line;
            while((line = br.readLine()) != null) {
                String[] elements = line.split(",");
                LinkedList<String> documents = new LinkedList<String>();
                for(int i=0; i<elements.length; i++) {
                    if(elements[i].length() >0) {
                        documents.add(elements[i]);
        //                System.out.print(elements[i] + "\t");
                    }
                }
     //           System.out.println(clusterID);
                goldstandard.put(clusterID++, documents);
            }
        }catch(Exception e) {
           e.printStackTrace();
        }
        return goldstandard;

    }

    /**
     * to see how many lines of codes were affected in each cluster
     */
    public void analyzeCodeRemovedPerCluster(ArrayList<String> topicList) throws FileNotFoundException {
        for (String clusterName : topicList) {
            LinkedList<String> goldCluster = goldClusters.get(clusterLabelMap.get(clusterName));
            int count = 0, otherlines = 0;
            for (String element : goldCluster) {
                BufferedReader br = new BufferedReader(new FileReader(new File("/Users/mhjang/Documents/teaching_documents/extracted/stemmed/"+element)));
                try {
                    String line = br.readLine();
                    while (line != null) {
                        if (line.length() > 0) {
                            line = line.toLowerCase().trim();
                            if (DetectCodeComponent.isCodeLine(line))
                                count++;
                            else
                                otherlines++;
                        }
                        line = br.readLine();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            double ratio = (double)count/(double)(count + otherlines);
            System.out.println(clusterName + ": " + count + "/" + otherlines + ":" + ratio);
        }
    }

    private void computeAccuracy(ArrayList<String> topicList) {
        double avgPrecision = 0.0, avgRecall = 0.0;
        for(String clusterName : topicList) {
            LinkedList<String> goldCluster = goldClusters.get(clusterLabelMap.get(clusterName));
            LinkedList<String> cluster = clusters.get(clusterName);
            /***
             * precision = correctInCluster / |cluster|
             * recall = correctInCluster / |gold cluster|
             */
            int correctInCluster = 0, correctInGoldCluster = 0;
            for (String element : goldCluster) {
                if (cluster.contains(element)) correctInCluster++;
            }
         //   System.out.println(clusterName + " cluster:");
         //   for(String element : cluster) {
          //      dc.getDocument(element).printTerms();
           // }
            double precision = 0.0, recall = 0.0, fMeasure = 0.0;
            if (cluster.size() > 0)
                precision = (double) (correctInCluster) / (double) (cluster.size());
            else
                precision = 0.0;
            if (goldCluster.size() > 0)
                recall = (double) (correctInCluster) / (double) (goldCluster.size());
            else
                recall = -1;
            avgPrecision += precision;
            avgRecall += recall;

            fMeasure = (2 * precision * recall) / (precision + recall);
            System.out.println(clusterName + "\t" + precision + "\t" + recall + "\t" + fMeasure);
        }
        double length = (double)topicList.size()-1;
        avgPrecision /= length;
        avgRecall /= length;
        System.out.println("Avg Precision: " + avgPrecision+ "\t Avg Recall: " + avgRecall + "\t Avg F-measure:" + (2*avgPrecision*avgRecall)/(avgPrecision + avgRecall));
 //      return fMeasure;

      }


}
