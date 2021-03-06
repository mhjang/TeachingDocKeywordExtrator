package TermScoring;

import Clustering.Document;
import Clustering.DocumentCollection;
import QueryExpansion.QueryExpander;
import TermScoring.TFIDF.TFIDFCalculator;
import evaluation.ClusteringFMeasure;
import indexing.NGramReader;
import parser.Tokenizer;

import java.io.*;
import java.text.DecimalFormat;
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

    static int lineIndex = 0;
    static DecimalFormat numberFormat = new DecimalFormat(("#.000"));
    static NGramReader ngReader;

    /**
     *
     * @param args: -i [input filepath] -c [the number of columns] -o [output filename]
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        String dir = null;
        int columnNum = 5;
        String outFileName = "ouptut.html";
        for(int i =0; i<args.length; i++) {
            if(args[i].equals("-i")) {
               dir = args[++i];
            }
            else if(args[i].equals("-c")) {
                columnNum = Integer.parseInt(args[++i]);
            }
            else if(args[i].equals("-o")) {
                outFileName = args[++i];
            }

        }

        System.out.println(dir + "\t" + columnNum + "\t" + outFileName);
        PrintStream console = System.out;
        File file = new File(outFileName);
        FileOutputStream fos = new FileOutputStream(file);
        PrintStream ps = new PrintStream(fos);
        System.setOut(ps);
        attachInstruction();
        String[] colors = {"yellow", "Coral", "orange", "DarkSalmon", "DarkTurquoise", "GreenYellow", "lime", "teal", "Pink", "Salmon", "SlateBlue", "Skyblue", "RoyalBlue", "Violet", "Tomato"};
        ClusteringFMeasure cfm = new ClusteringFMeasure();
        HashMap<Integer, LinkedList<String>> goldSet = cfm.readGoldstandard("./annotation/goldstandard_v2.csv");
        TFIDFCalculator tfidf = new TFIDFCalculator(true);
        tfidf.calulateTFIDF(TFIDFCalculator.LOGTFIDF,"/Users/mhjang/Documents/teaching_documents/extracted/stemmed/parsed/noise_removed", Tokenizer.UNIGRAM, false);
        DocumentCollection dc = tfidf.getDocumentCollection();

        ngReader = new NGramReader();

    //    LanguageModeling lm = new LanguageModeling(dc, 10, 0.7, 0.2);
     //   lm.run();

      //  dc.printDocumentList();
        /** topic name -> topic index -> gold documents **/
        Integer clusterLabelIndex = 0;
        HashMap<String, Integer> clusterLabelMap = new HashMap<String, Integer>();
        // reading a topic file
        BufferedReader br = new BufferedReader(new FileReader(new File("./topics_resource/topics_v2_stemmed")));
        ArrayList<String> topiclist = new ArrayList<String>();
        String line = null;
       System.out.println("<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.10.2/jquery.min.js\"></script>");
        System.out.println("<table border=\"0\" width=\"1051\">");


        while((line= br.readLine()) != null) {
            topiclist.add(line);
            clusterLabelMap.put(line, clusterLabelIndex++);
          //  System.out.println(line + "," + clusterLabelIndex);
        }
        /** topic name -> wikipedia article */
        QueryExpander qe = new QueryExpander(dc);
        HashMap<String, Document> wikiDocMap = qe.getWikiDocuments("./wikiexpansion_resource/ver2/html");

        int colorIdx = 0;
        for(String topicName: topiclist) {
            Document wikiDoc = wikiDocMap.get(topicName);
            LinkedList<Map.Entry<String, Integer>> topRankedTermsWiki = wikiDoc.getTopTermsTF(10);
            generateHTMLTableWiki(topicName, topRankedTermsWiki, colors[colorIdx]);
            // print wikiDoc.top20TFTerms
      //      System.out.println(topicName);
      //      System.out.println(clusterLabelMap.get(topicName));
            LinkedList<String> relevantDocSet = goldSet.get(clusterLabelMap.get(topicName));
        //    System.out.println("relevant doc set " + relevantDocSet.size());
            for(String docName : relevantDocSet) {
       //         System.out.println("document name:" + docName);
                Document relevantDoc = dc.getDocument(docName);
                if(relevantDoc != null) {
               //     LinkedList<String> topRankedTerms = relevantDoc.getTopTermsTF2(10);
                    LinkedList<Map.Entry<String, Double>> topRankedTerms = relevantDoc.getTopTermsTFIDF(20);
                    generateHTMLTable(relevantDoc.getName(), topRankedTerms, colors[colorIdx]);
               //     generateHTMLTableFirstTopK(relevantDoc, topRankedTerms, colors[colorIdx]);
                }
            }
            colorIdx++;
            if(colorIdx == colors.length) colorIdx = 0;
        }
        System.out.println("</table>");
        attachScript();


    }

    /**
     * For wiki table. It's the same as the other method, but takes a Integer-valued elements (since it's TF),
     * and generates a different colored table.
     * @param tableName
     * @param elements
     */
    public static void generateHTMLTableWiki(String tableName, LinkedList<Map.Entry<String, Integer>> elements, String tableColor) {
        if(lineIndex % 5 == 0) {
            System.out.println("</tr>");
            System.out.println("<tr>");
        }
        System.out.println("<td>");
        System.out.println("<table border=\"0\" cellpadding=\"2\" cellspacing=\"0\" style=\"width:250px\" text-align: center;\">");
        System.out.println("<tr> <td style=\"background-color: "+tableColor +"; id=\""+ tableName + "\"><b>" + tableName + "</b></td></tr>");
            for(Map.Entry<String, Integer> ele : elements) {
            System.out.println("<tr> <td style=\"background-color: "+tableColor+"; id=\""+ele.getKey()+"\">" + ele.getKey() + "("+ ele.getValue() +") </td></tr>");
        }
        System.out.println("</table>");
        System.out.println("</td>");
        lineIndex++;
    }



    public static void attachInstruction() {
        System.out.println("<h2> A Table of Terms extracted from Wikipedia Articles & Relevant Documents </h2>\n" +
                "\n" +
                "<li><h4> Fully-colored table: terms extracted from a relevant wikipedia article: Term (term frequency)  <h4></li>\n" +
                "<li><h4> The following tables with the same color heading contain the top TF-IDF terms from the relevant documents labeled in the gold standard: Term (TF/IDF score). Note that terms are filtered using Wikipedia title dataset. </h4></li>\n" +
                "<li><h4> If you click the term, the similar terms over the documents will be highlighted </h4></li>\n" +
                "<li><h4> To navigate full TF/IDF term score from each document in the collection (257) <a href=\"./tfidf.txt\"> Click HERE </a></h4>  </li>\n");
    }
    public static void generateHTMLTable(String tableName, LinkedList<Map.Entry<String, Double>> elements, String tableColor) {
        if(lineIndex % 5 == 0) {
            System.out.println("</tr>");
            System.out.println("<tr>");
        }
        System.out.println("<td>");
        System.out.println("<table border=\"0\" cellpadding=\"2\" cellspacing=\"0\" style=\"width:250px\" text-align: center;\">");
        System.out.println("<tr> <td style=\"background-color: "+tableColor +" id=\""+ tableName + "\"><b>" + tableName + "</b></td></tr>");
        for(Map.Entry<String, Double> ele : elements) {
            String[] tokens = ele.getKey().split(" ");

            System.out.println("<tr> <td class=\""+tokens[0]+"\">" + ele.getKey() + "("+ numberFormat.format(ele.getValue()) +") </td></tr>");
        }
        System.out.println("</table>");
        System.out.println("</td>");
        lineIndex++;
    }

    public static void generateHTMLTableFirstTopK(Document doc, LinkedList<String> elements, String tableColor) {
        if(lineIndex % 5 == 0) {
            System.out.println("</tr>");
            System.out.println("<tr>");
        }
        String tableName = doc.getName();
        HashMap<String, Integer> tfMap = doc.getTermFrequencyMap();
        System.out.println("<td>");
        System.out.println("<table border=\"0\" cellpadding=\"2\" cellspacing=\"0\" style=\"width:250px\" text-align: center;\">");
        System.out.println("<tr> <td style=\"background-color: "+tableColor +" class=\""+ tableName + "\"><b>" + tableName + "</b></td></tr>");
        for(String ele : elements) {
            String[] tokens = ele.split(" ");
            System.out.println("<tr> <td class=\""+tokens[0]+"\">" + ele +" ("+tfMap.get(ele) + "/ " + ngReader.lookUpTerm(ele)+") </td></tr>");
        }
        System.out.println("</table>");
        System.out.println("</td>");
        lineIndex++;
    }

    /**
     * Attach JQUery Script that allows highlights the terms over the documents
     */
    public static void attachScript() {
        System.out.println("<script>\n" +
                "$(document).ready(function(){\n" +
                "var idx = 0;" +
                "var colour = ['yellow', 'Coral', 'orange', 'DarkSalmon', 'DarkTurquoise', 'GreenYellow', 'lime', 'teal', 'Pink', 'Salmon', 'SlateBlue', 'Skyblue', 'RoyalBlue', 'Violet', 'Tomato'];\n" +
                "   $(\"td\").click(function() {\n" +
                "        var rand = Math.floor(Math.random() * colour.length);\n" +
                "        var classId = $(this).attr(\"class\");" +
                "        $(\".\"+classId).css(\"background-color\", colour[rand]);\n" +
                "        });\n" +
                "});\n" +
                "</script>");


    }
//        qe.expandTopicQueriesWithFrequentTerms(clusterFeatureMap, "./wikiexpansion_resource/ver2/html", dc.getglobalTermCountMap(), termFilterThreshold);

    }


