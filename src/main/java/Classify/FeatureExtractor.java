package Classify;

import com.clearnlp.dependency.DEPArc;
import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.morphology.MPLibEn;
import com.clearnlp.reader.DEPReader;
import com.clearnlp.util.UTInput;
import de.bwaldvogel.liblinear.Feature;
import de.bwaldvogel.liblinear.FeatureNode;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mhjang on 4/27/14.
 */


public class FeatureExtractor{
    static String noiseTag = "<NOISE>";
    static String noiseCloseTag = "</NOISE>";
    HashSet<DEPNode> traversedNodes;
    static HashMap<String, Integer> featureMap = new HashMap<String, Integer>();
    static HashMap<Integer, String> featureinverseMap = new HashMap<Integer, String>();


    static Pattern numberPattern = Pattern.compile("^[a-zA-Z]*([0-9]+).*");
    static Pattern puctPattern = Pattern.compile("\\p{Punct}");

    static int currentIdx = 4;

    static FeatureNode feature1True = new FeatureNode(1,1);
    static FeatureNode feature1False = new FeatureNode(1,0);

    static FeatureNode feature2True = new FeatureNode(2,1);
    static FeatureNode feature2False = new FeatureNode(2,0);

    static FeatureNode feature3True = new FeatureNode(3,1);
    static FeatureNode feature3False = new FeatureNode(3,0);

    static public void main(String[] args) throws IOException {

   /*    generate features files from all other documents

        ExtractFeature ef = new ExtractFeature();
        ef.generateClassifyFiles();
    */

  /*
   *    from training/test directory


        ExtractFeature ef = new ExtractFeature();
        String trainingDirectory = "/Users/mhjang/Desktop/clearnlp/trainingdata/annotation/";
        String taringParsedDir = "/Users/mhjang/Desktop/clearnlp/trainingdata/parsed/";
        String testDirectory = "/Users/mhjang/Desktop/clearnlp/testdata/annotation/";
        String testParsedDir = "/Users/mhjang/Desktop/clearnlp/testdata/parsed/";

        ef.generateClassifierFile(trainingDirectory, taringParsedDir, "/Users/mhjang/Documents/SVMlight/Source/svm_light/all_small.train");
        ef.generateClassifierFile(testDirectory, testParsedDir, "/Users/mhjang/Documents/SVMlight/Source/svm_light/all_small.test");

   */
   /*
        for five fold cross validation
    */




    }



    /**
     * generate feature files for classification for all documents for extrinsic evaluation
     * from a *.cnlp file, generate a token list file and a feature file
     */
    public void generateClassifyFiles() {

        try {
            File folder = new File("/Users/mhjang/Documents/teaching_documents/extracted/stemmed/parsed");
            for (final File fileEntry: folder.listFiles()) {
                if(fileEntry.getName().contains(".DS_Store")) continue;
                if(!fileEntry.getName().contains(".cnlp")) continue;
                DEPReader reader = new DEPReader(0, 1, 2, 3, 4, 5, 6);
                //	PrintStream fout = UTOutput.createPrintBufferedFileStream(outputFile);
                BufferedWriter bw = new BufferedWriter(new FileWriter(new File("/Users/mhjang/Documents/teaching_documents/extracted/stemmed/parsed/features/"+fileEntry.getName().substring(0, fileEntry.getName().length()-5))));
                BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File("/Users/mhjang/Documents/teaching_documents/extracted/stemmed/parsed/feature_tokens/"+fileEntry.getName().substring(0, fileEntry.getName().length() - 5))));

                // open a corresponding parsed file
                reader.open(UTInput.createBufferedFileReader(fileEntry.getAbsolutePath()));
                DEPTree tree;
                int idx = 0;
                ArrayList<DEPTree> treelist = new ArrayList<DEPTree>();
                while ((tree = reader.next()) != null) {
                    treelist.add(tree);
                }

                for(DEPTree dtree : treelist) {
                    LinkedList<String> tokens = new LinkedList<String>();
                    for(int i=1; i<dtree.size(); i++) {
                        tokens.add(dtree.get(i).form);
                        bw2.append(dtree.get(i).form + " ");
                    }
                    bw2.append("\n");
                    extractFeature(tokens, dtree, bw);
                }
                bw.close();
                bw2.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static LinkedList<LinkedList<String>> readCNLPFile(String parsedFile) {
        DEPReader reader = new DEPReader(0, 1, 2, 3, 4, 5, 6);
        //	PrintStream fout = UTOutput.createPrintBufferedFileStream(outputFile);
        LinkedList<LinkedList<String>> parsedSenList = new LinkedList<LinkedList<String>>();
        try {
            reader.open(UTInput.createBufferedFileReader(parsedFile));
            Set<String> set = new HashSet<String>();
            DEPTree tree;
            int idx = 0;
            ArrayList<DEPTree> treelist = new ArrayList<DEPTree>();
            while ((tree = reader.next()) != null) {
                treelist.add(tree);
            }
            for (DEPTree t : treelist) {
                LinkedList<String> sentence = new LinkedList<String>();
                for (int i = 0; i < t.size(); i++) {
                    DEPNode node = t.get(i);
                    sentence.add(node.form);
                }
                parsedSenList.add(sentence);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parsedSenList;
    }

    public FeatureExtractor(String parsedFile, String annotationFile, String filename) {
        DEPReader reader = new DEPReader(0, 1, 2, 3, 4, 5, 6);
        //	PrintStream fout = UTOutput.createPrintBufferedFileStream(outputFile);
        int noiseCount = 0;
        int tokenCount = 0;
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filename)));
            reader.open(UTInput.createBufferedFileReader(parsedFile));
            Set<String> set = new HashSet<String>();
            DEPTree tree;
            int idx = 0;
            ArrayList<DEPTree> treelist = new ArrayList<DEPTree>();
            while ((tree = reader.next()) != null) {
                treelist.add(tree);
            }
            BufferedReader br = new BufferedReader(new FileReader(new File(annotationFile)));
            String line;
            int treeIdx = 0;
            while((line = br.readLine())!=null) {
                if(!line.isEmpty()) {
                    if (line.contains(noiseTag)) {
                        int startIdx = line.indexOf(noiseTag);
                        int endIdx = line.indexOf(noiseCloseTag);
                        if (startIdx == -1 || endIdx == -1)
                            System.out.println(line);
                        //      System.out.println("NOISE: " + line.substring(startIdx + noiseTag.length(), endIdx));
                        String[] noiseFragments = line.substring(noiseTag.length() + startIdx, endIdx).split(" ");
                        LinkedList<String> tokens = new LinkedList<String>();
                        for (int i = 0; i < noiseFragments.length; i++) {
                            tokens.add(noiseFragments[i]);
                        }
                        noiseCount += extractFeature(tokens, treelist.get(treeIdx), bw);
                        tokenCount += treelist.get(treeIdx).size() - 1;
                        //              ArrayList<ArrayList<String>> subTreeSet = findSubTreeOfNoise(tokens, treelist.get(treeIdx));
                        int subidx = 0;
                        //              System.out.println("# of subtrees: " + subTreeSet.size());

                    }
                    treeIdx++;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(filename + "\t" + noiseCount + "\t" + tokenCount + "\t" + (double)noiseCount/(double)tokenCount);
    }

    public FeatureExtractor() {

    }

    private ArrayList<ArrayList<String>> findSubTreeOfNoise(HashSet<String> noiseFragment, DEPTree tree) {
        int i, size = tree.size();
        tree.resetDependents();
        DEPNode node;
        traversedNodes = new HashSet<DEPNode>();
        ArrayList<ArrayList<String>> subTreeSet = new ArrayList<ArrayList<String>>();
        for (i=0; i<size; i++)
        {
            node = tree.get(i);
            boolean fullTreeFound = false;
        }
        return subTreeSet;
    }
    // feature format: {bag of words} \t {POS tags} \t {dependency relations}




    private int extractFeature(LinkedList<String> noiseFragment, DEPTree tree, BufferedWriter bw) {
        int i, size = tree.size(), npSum = 0;
        tree.resetDependents();
        DEPNode node;
        int noiseCount = 0;
        traversedNodes = new HashSet<DEPNode>();
        LinkedList<String> posTagList = new LinkedList<String>();
        ArrayList<ArrayList<String>> subTreeSet = new ArrayList<ArrayList<String>>();
        for (i=1; i<size; i++)
        {
            node = tree.get(i);
            HashMap<Integer, Integer> nodeFeatureMap = new HashMap<Integer, Integer>();

            boolean isNoise = false;
            if(noiseFragment.contains(node.form)) {
                isNoise = true;
                noiseCount++;
                //     System.out.println(node.form);
            }


            boolean containsNumber = false, isPunctuation= false;
            int charlen = 0;
            Matcher m1 = numberPattern.matcher(node.form);
            if(m1.find())
                nodeFeatureMap.put(1, 1);
            else
                nodeFeatureMap.put(1, 0);

            Matcher m2 = puctPattern.matcher(node.form);
            // whether token is a punctuation
            if(m2.find())
                nodeFeatureMap.put(2, 1);
            else
                nodeFeatureMap.put(2, 0);

            // length of a character
            charlen = node.form.length();
            nodeFeatureMap.put(3, charlen);

            nodeFeatureMap.put(getFeatureIndex(node.form), 1);

            if(i>2) {
                nodeFeatureMap.put(getFeatureIndex(tree.get(i-1).form + " " + node.form), 1);
                //          System.out.println(tree.get(i-1).form + " " + node.form);
            }
            if(i<size-1) {
                nodeFeatureMap.put(getFeatureIndex(node.form + " " + tree.get(i+1).form), 1);

            }

            List<DEPArc> dependents = node.getDependents();
            for (DEPArc arc : dependents) {
                nodeFeatureMap.put(getFeatureIndex(arc.getNode().pos), 1);
                posTagList.add(arc.getNode().pos);
                nodeFeatureMap.put(getFeatureIndex("D_" +arc.getNode().getLabel()), 1);
            }

            for(int j=1; j<size; j++) {
                if(i!= j) {
                    DEPNode anotherNode = tree.get(j);
                    List<DEPArc> aDependents = anotherNode.getDependents();
                    for (DEPArc arc : aDependents) {
                        if(arc.getNode().form == node.form) {
                            nodeFeatureMap.put(getFeatureIndex("H_" + arc.getNode().getLabel()), 1);
                        }
                    }
                }
            }

            // POSTAG bigrams

            for(int j=1; j<posTagList.size(); j++) {
                String bigram = posTagList.get(j-1) + " " + posTagList.get(j);
                nodeFeatureMap.put(getFeatureIndex(bigram), 1);
                //    System.out.println(bigram);
            }

            try {
                if (isNoise) bw.write("1\t");
                else bw.write("-1\t");
                //     bw.write("0 \t");
                SortedSet<Integer> keysets = new TreeSet<Integer>(nodeFeatureMap.keySet());
                for (Integer key : keysets) {
                    bw.write(key + ":" + nodeFeatureMap.get(key) + " ");
                }
                bw.write("\n");
            }catch(Exception e) {
                e.printStackTrace();
            }

        }
        return noiseCount;

    }

    int getFeatureIndex(String word) {
        if(featureMap.containsKey(word))
            return featureMap.get(word);
        else {
            featureMap.put(word, currentIdx);
            featureinverseMap.put(currentIdx, word);
            currentIdx++;
        }
        return featureMap.get(word);
    }


    boolean includeNP(DEPNode node)
    {
        for (DEPNode sub : node.getSubNodeSet())
        {
            if (sub != node && MPLibEn.isNoun(sub.pos))
                return true;
        }

        return false;
    }

    Map.Entry<ArrayList<ArrayList<String>>, Boolean> printSubTree2(DEPNode root, DEPTree tree, HashSet<String> noiseCorpus, ArrayList<ArrayList<String>> subtreeSet, boolean fullTreeFound) {
        //    List<DEPNode> subnodelist = root.getDependentNodeList();
        if(traversedNodes.contains(root)) return new AbstractMap.SimpleEntry<ArrayList<ArrayList<String>>, Boolean>(subtreeSet, fullTreeFound);
        traversedNodes.add(root);


        // search leftwards; sorted by decreasing id
        int leftDependentSize = root.getLeftDependents().size();
        for(int i = leftDependentSize-1; i>=0; i--) {
            DEPNode depnode = root.getLeftDependents().get(i);
            int startIdx = depnode.id;
            int endIdx = root.id;
            //     System.out.println(sub.getLabel());
            ArrayList<String> subTreeCorpus = new ArrayList<String>();
            for(int j=startIdx; j<=endIdx; j++) {
                //          System.out.print(tree.get(i).form + ", ");
                subTreeCorpus.add(tree.get(j).pos);
            }

            // spotted a tree that covers all of the fragment!
            if(subTreeCorpus.containsAll(noiseCorpus)) {
                // now we don't need all the partial sub-trees once we identify the subtree of exact match
                subtreeSet.clear();
                subtreeSet.add(subTreeCorpus);
                fullTreeFound = true;
                // but there could be a smaller sub-tree that also covers the fragment
                if(subTreeCorpus.size() > noiseCorpus.size())
                    return printSubTree2(depnode, tree, noiseCorpus, subtreeSet, fullTreeFound);
                //  fullTreeFound = true;
                //     System.out.println("Full Cover Tree:" +  sub.getLabel());
                //     for(int i=0; i<subTreeCorpus.size(); i++) {
                //          System.out.print(tree.get(i).form + ", ");
                //          System.out.print(subTreeCorpus.get(i) +", ");
                //     }
            }
            else {
                // what i've found was the smallest sub-tree that covers the noisy fragment
                if(fullTreeFound) return new AbstractMap.SimpleEntry<ArrayList<ArrayList<String>>, Boolean>(subtreeSet, fullTreeFound);

                for(String element : noiseCorpus) {
                    if(subTreeCorpus.contains(element)) {
                        subtreeSet.add(subTreeCorpus);
                        break;
                    }
                }
            }

            //        System.out.println();

        }


        // search leftwards; sorted by decreasing id
        for(int i = 0; i<root.getRightDependents().size(); i++) {
            DEPNode depnode = root.getRightDependents().get(i);
            int startIdx = root.id;
            int endIdx = depnode.id;
            //     System.out.println(sub.getLabel());
            ArrayList<String> subTreeCorpus = new ArrayList<String>();
            for(int j=startIdx; j<=endIdx; j++) {
                //          System.out.print(tree.get(i).form + ", ");
                subTreeCorpus.add(tree.get(j).pos);
            }

            // spotted a tree that covers all of the fragment!
            if(subTreeCorpus.containsAll(noiseCorpus)) {
                // now we don't need all the partial sub-trees once we identify the subtree of exact match
                subtreeSet.clear();
                subtreeSet.add(subTreeCorpus);
                fullTreeFound = true;
                if(subTreeCorpus.size() > noiseCorpus.size())
                    return printSubTree2(depnode, tree, noiseCorpus, subtreeSet, fullTreeFound);

                //  fullTreeFound = true;
                //     System.out.println("Full Cover Tree:" +  sub.getLabel());
                //     for(int i=0; i<subTreeCorpus.size(); i++) {
                //          System.out.print(tree.get(i).form + ", ");
                //          System.out.print(subTreeCorpus.get(i) +", ");
                //     }
                return new AbstractMap.SimpleEntry<ArrayList<ArrayList<String>>, Boolean>(subtreeSet, fullTreeFound);
            }
            else {
                if(fullTreeFound) return new AbstractMap.SimpleEntry<ArrayList<ArrayList<String>>, Boolean>(subtreeSet, fullTreeFound);

                for(String element : noiseCorpus) {
                    if(subTreeCorpus.contains(element)) {
                        subtreeSet.add(subTreeCorpus);
                        break;
                    }
                }
            }

            //        System.out.println();

        }


        return new AbstractMap.SimpleEntry<ArrayList<ArrayList<String>>, Boolean>(subtreeSet, fullTreeFound);


    }
/*
    public void classify() {
        ColumnDataClassifier cdc = new ColumnDataClassifier("/Users/mhjang/Documents/workspace/ClearNLP/examples/iris2007.prop");
        SVMLightClassifierFactory<String, String> factory = new SVMLightClassifierFactory<String, String>("/Users/mhjang/Documents/SVMlight/Source/svm_light/svm_learn", "/Users/mhjang/Documents/SVMlight/Source/svm_light/svm_multiclass_learn");

        GeneralDataset<String, String> gd = (GeneralDataset<String, String>)cdc.readTrainingExamples("/Users/mhjang/Documents/workspace/ClearNLP/examples/iris.train");
        SVMLightClassifier classifier = factory.trainClassifier(gd);

        Pair<GeneralDataset<String, String>, List<String[]>> datasetPairs  = cdc.readTestExamples("/Users/mhjang/Documents/workspace/ClearNLP/examples/iris.test");
        GeneralDataset testGD = datasetPairs.first();

        for (int i = 0; i < testGD.size(); i++) {
            System.out.println(classifier.classOf(testGD.getDatum(i)));


        }

/*        ColumnDataClassifier cdc = new ColumnDataClassifier("/Users/mhjang/Documents/workspace/ClearNLP/examples/noiseclassifier.prop");
        GeneralDataset gd = cdc.readTrainingExamples("training.txt");
        Classify classifier = cdc.makeClassifier(gd);
        Pair<GeneralDataset<String, String>, List<String[]>> datasetPairs = cdc.readTestExamples("test.txt");
        GeneralDataset testGD = datasetPairs.first();
        List<String[]> second = datasetPairs.second();
        testGD.summaryStatistics();
        for (int i = 0; i < testGD.size(); i++) {
            System.out.println(classifier.classOf(testGD.getDatum(i)) + " : " + second.get(i)[0]);


        }
    */
    // }

    public void generateClassifier(ArrayList<String> data, String output) throws IOException {
        //     read all annotated files from the directory
        //     String directory = "/Users/mhjang/Desktop/clearnlp/trainingdata/annotation/";
        String parsedDir = "/Users/mhjang/Desktop/clearnlp/all/parsed/";
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(output)));
        LinkedList<String> positiveFeatures = new LinkedList<String>();
        LinkedList<String> negativeFeatures = new LinkedList<String>();
        System.out.println(data);
        try {
            for(String filename: data) {
                int noiseCount = 0;
                int tokenCount = 0;
                DEPReader reader = new DEPReader(0, 1, 2, 3, 4, 5, 6);
                //	PrintStream fout = UTOutput.createPrintBufferedFileStream(outputFile);

                if(filename.contains(".DS_Store")) continue;
                // open a corresponding parsed file
                reader.open(UTInput.createBufferedFileReader(parsedDir + filename + ".cnlp"));
                DEPTree tree;
                int idx = 0;
                ArrayList<DEPTree> treelist = new ArrayList<DEPTree>();
                while ((tree = reader.next()) != null) {
                    treelist.add(tree);
                }

                // then open an annotation file
                int treeIdx = 0;
                String line;
                //          System.out.println("Tree size: " + treelist.size());
                File file = new File("/Users/mhjang/Desktop/clearnlp/all/annotation/" + filename);
                BufferedReader br = new BufferedReader(new FileReader(file));
                //          System.out.println(fileEntry);
                int nonEmptyLine = 0;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        LinkedList<String> tokens = new LinkedList<String>();
                        int startIdx = 0;
                        // for a given annotation file, read all <NOISE> </NOISE> string fragment
                        if (line.contains(noiseTag)) {
                            startIdx = (line.indexOf(noiseTag)+noiseTag.length()<line.length())?line.indexOf(noiseTag)+startIdx:0;
                            int endIdx = line.indexOf(noiseCloseTag);
                            if(endIdx == -1) endIdx = line.length();
                            //   System.out.println(line);
                            String[] noiseFragments = line.substring(startIdx, endIdx).split(" ");
                            for (int i = 0; i < noiseFragments.length; i++) {
                                tokens.add(noiseFragments[i]);
                            }
                            // for the given string fragment, read the corresponding parsing tree
                        }
                        //        nonEmptyLine++;
                        noiseCount += extractFeature(tokens, treelist.get(treeIdx), bw);
                        tokenCount += treelist.get(treeIdx).size() - 1;
                        treeIdx++;


                    }
                }
                //    System.out.println("nonEmpty: " + nonEmptyLine);
                //    System.out.println("alllines: " + treeIdx);

                System.out.println(filename + "\t" + noiseCount + "\t" + tokenCount + "\t" + (double)noiseCount/(double)tokenCount);

            }
            bw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    /**
     * 5/1/ 12:08PM
     */
    public void generateClassifierFile(String directory, String parsedDir, String filename) {
        //     read all annotated files from the directory
        //     String directory = "/Users/mhjang/Desktop/clearnlp/trainingdata/annotation/";
        //     String parsedDir = "/Users/mhjang/Desktop/clearnlp/trainingdata/parsed/";

        LinkedList<String> positiveFeatures = new LinkedList<String>();
        LinkedList<String> negativeFeatures = new LinkedList<String>();
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filename)));
            File folder = new File(directory);
            for (final File fileEntry: folder.listFiles()) {
                if(fileEntry.getName().contains(".DS_Store")) continue;
                DEPReader reader = new DEPReader(0, 1, 2, 3, 4, 5, 6);
                //	PrintStream fout = UTOutput.createPrintBufferedFileStream(outputFile);

                // open a corresponding parsed file
                reader.open(UTInput.createBufferedFileReader(parsedDir + fileEntry.getName() + ".cnlp"));
                DEPTree tree;
                int idx = 0;
                ArrayList<DEPTree> treelist = new ArrayList<DEPTree>();
                while ((tree = reader.next()) != null) {
                    treelist.add(tree);
                }

                // then open an annotation file
                int treeIdx = 0;
                String line;
                System.out.println("Tree size: " + treelist.size());
                BufferedReader br = new BufferedReader(new FileReader(fileEntry));
                System.out.println(fileEntry);
                int nonEmptyLine = 0;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        LinkedList<String> tokens = new LinkedList<String>();
                        int startIdx = 0;
                        // for a given annotation file, read all <NOISE> </NOISE> string fragment
                        if (line.contains(noiseTag)) {
                            startIdx = (line.indexOf(noiseTag)+noiseTag.length()<line.length())?line.indexOf(noiseTag)+startIdx:0;
                            int endIdx = line.indexOf(noiseCloseTag);
                            if(endIdx == -1) endIdx = line.length();
                            //   System.out.println(line);
                            String[] noiseFragments = line.substring(startIdx, endIdx).split(" ");
                            for (int i = 0; i < noiseFragments.length; i++) {
                                tokens.add(noiseFragments[i]);
                            }
                            // for the given string fragment, read the corresponding parsing tree
                        }
                        //        nonEmptyLine++;
                        extractFeature(tokens, treelist.get(treeIdx), bw);
                        treeIdx++;


                    }
                }
                //    System.out.println("nonEmpty: " + nonEmptyLine);
                //    System.out.println("alllines: " + treeIdx);


            }
            bw.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}


