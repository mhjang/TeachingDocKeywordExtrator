package evaluation;

import Clustering.Clustering;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
    public ClusteringFMeasure(HashMap<String, LinkedList<String>> clusters_, HashMap<String, Integer> clusterLabelMap_, ArrayList<String> topiclist, String goldDir) {
        clusters = clusters_;
        clusterLabelMap = clusterLabelMap_;
        goldClusters = readGoldstandard(goldDir);
        for(String clusterName: topiclist) {
            calcFMeasure(clusterName);
        }

    }

    public static void main(String[] args) {
        ClusteringFMeasure cfm = new ClusteringFMeasure(null, null, null, "/Users/mhjang/Documents/teaching_documents/evaluation/goldstandard_v2.csv");
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

    private double calcFMeasure(String clusterName) {
        LinkedList<String> goldCluster = goldClusters.get(clusterLabelMap.get(clusterName));
        LinkedList<String> cluster = clusters.get(clusterName);
        /***
         * precision = correctInCluster / |cluster|
         * recall = correctInCluster / |gold cluster|
         */
        int correctInCluster = 0, correctInGoldCluster = 0;
        for(String element : goldCluster) {
            if(cluster.contains(element)) correctInCluster++;
        }
        double precision = 0.0, recall = 0.0, fMeasure = 0.0;
        if(cluster.size() > 0)
           precision = (double)(correctInCluster) / (double)(cluster.size());
        else
            precision = 0.0;
        if(goldCluster.size() > 0)
            recall = (double)(correctInCluster) / (double)(goldCluster.size());
        else
            recall = -1;
        fMeasure = (2*precision*recall)/(precision + recall);
        System.out.println(clusterName + "\t" + precision + "\t" + recall + "\t" + fMeasure);
        return fMeasure;
      }


}
