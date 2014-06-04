package Classify;

import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.morphology.MPLibEn;
import com.clearnlp.reader.DEPReader;
import com.clearnlp.util.UTInput;
import de.bwaldvogel.liblinear.*;
import simle.io.myungha.SimpleFileReader;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by mhjang on 4/27/14.
 */


public class ExtractFeature {



    static String initiatedTag = null;
    static String[] tags = {TagConstant.codeTag, TagConstant.tableTag, TagConstant.equTag, TagConstant.miscTag};
    static String[] closetags = {TagConstant.codeCloseTag, TagConstant.tableCloseTag, TagConstant.equCloseTag, TagConstant.miscCloseTag};

    HashSet<DEPNode> traversedNodes;
    static HashMap<String, Integer> featureMap = new HashMap<String, Integer>();
    static HashMap<Integer, String> featureinverseMap = new HashMap<Integer, String>();


    static Pattern numberPattern = Pattern.compile("^[a-zA-Z]*([0-9]+).*");
    static Pattern puctPattern = Pattern.compile("\\p{Punct}");


    static FeatureNode feature1True = new FeatureNode(1, 1);
    static FeatureNode feature1False = new FeatureNode(1, 0);

    static FeatureNode feature2True = new FeatureNode(2, 1);
    static FeatureNode feature2False = new FeatureNode(2, 0);

    static class Component {
        public int begin, intermediate, end;
        public Component(int begin, int intermidiate, int end) {
            this.begin = begin;
            this.intermediate = intermediate;
            this.end = end;
        }
    }

    Component table = new Component(TagConstant.BEGINTABLE, TagConstant.INTTABLE, TagConstant.ENDTABLE);
    Component code = new Component(TagConstant.BEGINCODE, TagConstant.INTCODE, TagConstant.ENDCODE);
    Component equation = new Component(TagConstant.BEGINEQU, TagConstant.INTEQU, TagConstant.ENDEQU);
    Component misc = new Component(TagConstant.BEGINMISC, TagConstant.INTMISC, TagConstant.ENDMISC);

    LinkedList<Feature[]> allFeatures = new LinkedList<Feature[]>();

    double[] answers = new double[1000000];
    int featureIdx = 0;
    int averageNoiseTokenLength = 0, averageNotNoiseTokenLength = 0;

    static int featureNodeNum = 4;

    static class FeatureComparator<Feature> implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            de.bwaldvogel.liblinear.Feature f1 = (de.bwaldvogel.liblinear.Feature) o1;
            de.bwaldvogel.liblinear.Feature f2 = (de.bwaldvogel.liblinear.Feature) o2;

            return (f1.getIndex() - f2.getIndex());
        }
    }

    ;

    FeatureComparator fc = new FeatureComparator();


    static public void main(String[] args) throws IOException {


   /*
        for five fold cross validation
    */

        ExtractFeature ef = new ExtractFeature();

        String allAnnotationDir = "/Users/mhjang/Desktop/clearnlp/all/annotation";
        String allParsingDir = "/Users/mhjang/Desktop/clearnlp/all/parsed";

        final File folder = new File(allAnnotationDir);
        ArrayList<String> filenames = new ArrayList<String>();

        int crossfold = 5;
        // for five fold
        for (final File fileEntry : folder.listFiles()) {
            if (!fileEntry.getName().contains(".DS_Store")) {
                filenames.add(fileEntry.getName());
            }
            System.out.println(fileEntry.getName());
            // for five fold
        }
        int j, testIdx;

        LinkedList<Feature[]> allFeatures = ef.generateClassifyFeatures(filenames);
        /** Generate a problem with trainng set features */
        Feature[][] allFeaturesArray = new Feature[allFeatures.size()][];

        for (int i = 0; i < allFeatures.size(); i++) {
            allFeaturesArray[i] = allFeatures.get(i);
        }

        Problem problem = new Problem();
        problem.x = allFeaturesArray;
        problem.n = featureNodeNum - 1;
        problem.y = Arrays.copyOfRange(ef.answers, 0, allFeatures.size());
        problem.l = allFeatures.size();

        double numOfNoises = 0.0;
        for(int i = 0; i < ef.answers.length; i++) {
            numOfNoises += ef.answers[i];
        }

        System.out.println("average length of noises : " + (double)ef.averageNoiseTokenLength / (double)numOfNoises);
        System.out.println("average length of X noises : " + (double)ef.averageNotNoiseTokenLength / ((double)ef.allFeatures.size()- numOfNoises));
        System.out.println("# of Noises: " + numOfNoises);
        System.out.println("# of X Noises: " + ((double)ef.allFeatures.size() - numOfNoises));

        SolverType solver = SolverType.L2R_LR; // -s 0
        double C = 1.0;    // cost of constraints violation
        double eps = 0.01; // stopping criteria


        Parameter param = new Parameter(SolverType.L2R_LR, 10, 0.01);
        int nr_fold = 5;
        double[] target = new double[problem.l];
        Linear.crossValidation(problem, param, nr_fold, target);


        double error = 0.0;
        for (int i = 0; i < target.length; i++) {
            error += Math.abs(problem.y[i] - target[i]);
        }
        System.out.println("error : " + error / (double) target.length);


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


    int getFeatureIndex(String word) {
        if (featureMap.containsKey(word))
            return featureMap.get(word);
        else {
            featureMap.put(word, featureNodeNum);
            featureinverseMap.put(featureNodeNum, word);
            featureNodeNum++;
        }
        return featureMap.get(word);
    }

    private String findMatchingEndTag(Tag beginTag) {
        if(beginTag == Tag.tableTag) return EndTag.tableCloseTag.toString();
        else if(beginTag == Tag.codeTag) return EndTag.codeCloseTag.toString();
        else if(beginTag == Tag.equTag) return EndTag.equCloseTag.toString();
        else return EndTag.miscCloseTag.toString();
    }

    public void generateClassifyFeatures(ArrayList<String> data) throws IOException {
        //     read all annotated files from the directory
        //     String directory = "/Users/mhjang/Desktop/clearnlp/trainingdata/annotation/";
        String parsedDir = "/Users/mhjang/Desktop/clearnlp/all/parsed/";
        try {
            for (String filename : data) {
                DEPReader reader = new DEPReader(0, 1, 2, 3, 4, 5, 6);

                if (filename.contains(".DS_Store")) continue;
                // open a corresponding parsed file
                reader.open(UTInput.createBufferedFileReader(parsedDir + filename + ".cnlp"));
                DEPTree tree;
                ArrayList<DEPTree> treelist = new ArrayList<DEPTree>();
                while ((tree = reader.next()) != null) {
                    treelist.add(tree);
                }

                // then open an annotation file
                int treeIdx = 0;
                String line;
                SimpleFileReader freader = new SimpleFileReader("/Users/mhjang/Desktop/clearnlp/all/annotation/" + filename);
                while(freader.hasMoreLines()) {
                    line = freader.readLine();
                    if (!line.trim().isEmpty()) {
                        LinkedList<String> tokens = new LinkedList<String>();
                        int startIdx = 0;
                        for(Tag tag : Tag.values()) {
                            if(line.contains(tag.toString())) {
                             //   initiatedTag = tag.toString();
                            }
                        }
                        if(initiatedTag != null) {
                            String tag = initiatedTag.toString();
                            String endTag = findMatchingEndTag(initiatedTag);
                            startIdx = (line.indexOf(tag) + tag.length() < line.length()) ? line.indexOf(tag) + startIdx : 0;
                            int endIdx = line.indexOf(endTag);
                            // this line doesn't contain the end tag
                            if (endIdx == -1) endIdx = line.length();
                            else {
                                initiatedTag = null;
                            }
                            String[] componentFrag = line.substring(startIdx, endIdx).split(" ");
                            for (int i = 0; i < componentFrag.length; i++) {
                                tokens.add(componentFrag[i]);
                            }
                            extractFeatureFromTree(tokens, treelist.get(treeIdx), initiatedTag);
                       }
                   }
              }
           }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }



    /**
     * 6/1/2014
     * extract features for each token in a parsing tree
     * @param noiseFragment
     * @param tree
     * @return
     */
    private void extractFeatureFromTree(LinkedList<String> noiseFragment, DEPTree tree, Tag tagType) {
        int i, size = tree.size(), npSum = 0;
        tree.resetDependents();
        DEPNode node;
        int noiseCount = 0;

        /** select the component tag **/
        Component component;
        if(tagType == null) {
            component = null;
        }
        else if(tagType == Tag.tableTag) {
            component = table;
        }
        else if(tagType == Tag.codeTag) {
            component = code;
        }
        else {
            component = misc;
        }
        traversedNodes = new HashSet<DEPNode>();
        for (i = 1; i < size; i++) {
            LinkedList<Feature> features = new LinkedList<Feature>();
            node = tree.get(i);
            Matcher m1 = numberPattern.matcher(node.form);
           /* if (m1.find())
                features.add(feature1True);
            else
                features.add(feature1False);


            Matcher m2 = puctPattern.matcher(node.form);
            // whether token is a punctuation
           /* if (m2.find())
                features.add(feature2True);
            else
                features.add(feature2False);
*/
              int charlen = node.form.length();
              features.add(new FeatureNode(1, charlen));

         /*   List<DEPArc> dependents = node.getDependents();
            for (DEPArc arc : dependents) {
                int fi = getFeatureIndex(arc.getNode().getLabel());
                FeatureNode fn = new FeatureNode(fi, 1);
                if(!features.contains(fn))
                    features.add(fn);
            }
    */
            Collections.sort(features, fc);
     /*       System.out.println(node.form);
            for(Feature f : features) {
                System.out.println(f.getIndex() + ": " + f.getValue());
            }
       */
            Feature[] featureArray;
            featureArray = features.toArray(new Feature[features.size()]);
            allFeatures.add(featureArray);
            if(component == null)
                answers[featureIdx] = TEXT;
            else {
                if(noiseFragment.get(0) == node.form)
                    answers[featureIdx] = component.begin;
                else if(noiseFragment.get(noiseFragment.size()-1) == node.form)
                    answers[featureIdx] = component.end;
                else
                    answers[featureIdx] = component.intermediate;
            }
            featureIdx++;

        }
    }

}

