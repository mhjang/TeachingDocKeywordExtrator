package TFIDF;

import Clustering.Document;
import Clustering.DocumentCollection;
import QueryExpansion.QueryExpander;
import evaluation.ClusteringFMeasure;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Created by mhjang on 2/19/14 2:45pm
 *
 * This class is for laying out and comparing the top K TF-IDF terms from the gold standard documents in the cluster
 * and top TF terms from the corresponding Wikipedia articles.
 * Just for eyeballing purpose.
 *
 */
public class TermExtractor {
    public static void main(String[] args) throws IOException {

        PrintStream console = System.out;
        File file = new File("table.html");
        FileOutputStream fos = new FileOutputStream(file);
        PrintStream ps = new PrintStream(fos);
        System.setOut(ps);

        HashMap<Integer, LinkedList<String>> goldSet = ClusteringFMeasure.readGoldstandard("./annotation/goldstandard_v2.csv");
        TFIDFCalculator tfidf = new TFIDFCalculator();
        DocumentCollection dc = tfidf.getDocumentCollection("/Users/mhjang/Documents/teaching_documents/stemmed/", TFIDFCalculator.TRIGRAM, true);
      //  dc.printDocumentList();
        /** topic name -> topic index -> gold documents **/
        Integer clusterLabelIndex = 0;
        HashMap<String, Integer> clusterLabelMap = new HashMap<String, Integer>();
        // reading a topic file
        BufferedReader br = new BufferedReader(new FileReader(new File("./topics_resource/topics_v2_stemmed")));
        ArrayList<String> topiclist = new ArrayList<String>();
        String line = null;
        while((line= br.readLine()) != null) {
            topiclist.add(line);
            clusterLabelMap.put(line, clusterLabelIndex++);
          //  System.out.println(line + "," + clusterLabelIndex);
        }
        /** topic name -> wikipedia article */
      //  QueryExpander qe = new QueryExpander(dc);
     //   HashMap<String, Document> wikiDocMap = qe.getWikiDocuments("./wikiexpansion_resource/ver2/html");

        for(String topicName: topiclist) {
//            Document wikiDoc = wikiDocMap.get(topicName);
            // print wikiDoc.top20TFTerms
      //      System.out.println(topicName);
      //      System.out.println(clusterLabelMap.get(topicName));
            LinkedList<String> relevantDocSet = goldSet.get(clusterLabelMap.get(topicName));
        //    System.out.println("relevant doc set " + relevantDocSet.size());
            for(String docName : relevantDocSet) {
       //         System.out.println("document name:" + docName);
                Document relevantDoc = dc.getDocument(docName);
                if(relevantDoc != null) {
                    LinkedList<Map.Entry<String, Double>> topRankedTerms = relevantDoc.getTopTermsTFIDF(10);
                    generateHTMLTable(docName, topRankedTerms);
                }
            }
        }



    }

    public static void generateHTMLTable(String tableName, LinkedList<Map.Entry<String, Double>> elements) {
        System.out.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" style=\"width:150px\" text-align: center;\">");
        System.out.println("<tr> <td style=\"background-color: orange; id=\""+ tableName + "\">" + tableName + "</td></tr>");
        for(Map.Entry<String, Double> ele : elements) {
            System.out.println("<tr> <td id=\""+ele.getKey()+"\">" + ele.getKey() + "("+ ele.getValue() +") </td></tr>");
        }
        System.out.println("</table>");

    }


//        qe.expandTopicQueriesWithFrequentTerms(clusterFeatureMap, "./wikiexpansion_resource/ver2/html", dc.getglobalTermCountMap(), termFilterThreshold);

    }


