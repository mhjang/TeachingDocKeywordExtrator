package Classify;

import com.clearnlp.dependency.DEPNode;
import com.clearnlp.dependency.DEPTree;
import com.clearnlp.morphology.MPLibEn;
import com.clearnlp.reader.DEPReader;
import com.clearnlp.util.UTInput;
import componentDetection.DetectCodeComponent;
import componentDetection.DetectEquation;
import componentDetection.DetectTable;
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
            this.intermediate = intermidiate;
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



      /*  System.out.println("average length of noises : " + (double)ef.averageNoiseTokenLength / (double)numOfNoises);
        System.out.println("average length of X noises : " + (double)ef.averageNotNoiseTokenLength / ((double)ef.allFeatures.size()- numOfNoises));
        System.out.println("# of Noises: " + numOfNoises);
        System.out.println("# of X Noises: " + ((double)ef.allFeatures.size() - numOfNoises));
        */

        SolverType solver = SolverType.L2R_LR; // -s 0
        double C = 1.0;    // cost of constraints violation
        double eps = 0.01; // stopping criteria


        Parameter param = new Parameter(SolverType.L2R_LR, 10, 0.01);
        int nr_fold = 5;
        double[] target = new double[problem.l];
        Linear.crossValidation(problem, param, nr_fold, target);


        int error = 0;
        HashMap<Integer, Integer> classLabelCount = new HashMap<Integer, Integer>();
        for (int i = 0; i < target.length; i++) {
            System.out.println("predicted label: " + target[i] + " answer: " + problem.y[i]);
            if((int)problem.y[i] != (int)target[i]) error++;
            if(classLabelCount.containsKey((int)problem.y[i])) {
                int c = classLabelCount.get((int)problem.y[i]);
                classLabelCount.put((int) problem.y[i], c + 1);
            }
            else
                classLabelCount.put((int) problem.y[i], 1);

        }
        for(int i=0; i<13; i++) {
            System.out.println(TagConstant.getTagLabel(i) + ":" + classLabelCount.get(i));
        }
        System.out.println(error);
        System.out.println(target.length);
        System.out.println("error : " + (double)error / (double) target.length);


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

    private String findMatchingEndTag(String beginTag) {
        if(beginTag == TagConstant.tableTag) return TagConstant.tableCloseTag;
        else if(beginTag == TagConstant.codeTag) return TagConstant.codeCloseTag;
        else if(beginTag == TagConstant.equTag) return TagConstant.equCloseTag;
        else return TagConstant.miscCloseTag;
    }

    public LinkedList<Feature[]> generateClassifyFeatures(ArrayList<String> data) throws IOException {
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
                System.out.println("Tree size: " + treelist.size());
                // then open an annotation file
                int treeIdx = 0;
                String line;
                SimpleFileReader freader = new SimpleFileReader("/Users/mhjang/Desktop/clearnlp/all/annotation/" + filename);
                System.out.println("opening" + filename);

                boolean isTagBeginLine = false, tagClosed = false;
                String endTag = null;
                while(freader.hasMoreLines()) {
                    line = freader.readLine();
                    /**
                     * beginToken: begin of the component over the lines. It is set only if the begin tag is present in the current line; Otherwise set to -1
                       endToken: end of the component over the lines. It is set only if the end tag is present in the current line; Otherwise set to -1
                       componentBegin: begin of the component in this line.
                       componentEnd: begin of the component in this line.
                    */
                    int beginToken = -1, endToken = -1;
                    int componentBegin= -1, componentEnd = -1;
                    /**
                     * treeIdxSkip: a flag that determines whether or not to skip current line
                     * clearNLP skipped an empty line. To find the matching tree, an empty line in the annotation should also be skipped.
                     *
                     */
                    boolean treeIdxSkip = false;
                    line = line.trim();

                    if (!line.isEmpty()) {
                        LinkedList<String> tokens = new LinkedList<String>();
                        int startIdx = 0;
                     // If currently no tag was opened
                        if (initiatedTag == null) {
                            for (String tag : tags) {
                                if (line.contains(tag)) {
                                    initiatedTag = tag;
                                    isTagBeginLine = true;
                                    break;
                                }
                            }
                        }
                        if (initiatedTag != null) {
                            /**
                             * If initiated tag is JUST SET, that means we have a begin tag in this line.
                               (1) To set this location to beginToken, first find the character offset of this begin tag to find the token location
                               (2) Find the matching end tag
                             */
                            if (isTagBeginLine) {
                                endTag = findMatchingEndTag(initiatedTag);
                                startIdx = (line.indexOf(initiatedTag) + initiatedTag.length() + 1 < line.length()) ? line.indexOf(initiatedTag) + initiatedTag.length() + 1 : 0;
                              // because of the tag itself, minus one
                                beginToken = componentBegin = StringTokenizerIndx.findTokenNthfromIndex(line, startIdx) - 1; // location - (startTag)
                              // If a line only contains a tag, in the original file that clearNLP parsed on, that line was empty.
                                if (line.replace(initiatedTag, "").trim().length() == 0) treeIdxSkip = true;
                            } else {
                              // the component is being continued from previous lines
                                componentBegin = 0;
                            }
                            int endIdx;
                            if(line.contains(endTag)) {
                                if (line.replace(endTag, "").trim().length() == 0) treeIdxSkip = true;
                                endIdx = line.indexOf(endTag);
                                tagClosed = true;
                            }
                            else {
                                endIdx = line.length();
                            }
                            // If there is a begin tag in the line, subtract one from the found index
                            if (isTagBeginLine) {
                                componentEnd = StringTokenizerIndx.findTokenNthfromIndex(line, endIdx) - 1;
                                isTagBeginLine = false;
                            } else
                                componentEnd = StringTokenizerIndx.findTokenNthfromIndex(line, endIdx);

                            if (tagClosed) endToken = componentEnd;
                        }
                        if (treeIdxSkip) continue;
           //             printTree(treeIdx, treelist.get(treeIdx));
           //             System.out.println(treeIdx + ":" + line);
                        extractFeatureFromTree(componentBegin, componentEnd, treelist.get(treeIdx), initiatedTag, beginToken, endToken);


                        if (tagClosed) {
                            initiatedTag = null;
                            tagClosed = false;
                        }

                        treeIdx++;
                    }

                    }
              }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return allFeatures;
    }

    private void printTree(int n, DEPTree tree) {
        int size = tree.size();
        DEPNode node;
        System.out.print(n + ": ");
        for (int i = 1; i < size; i++) {
            node = tree.get(i);
            System.out.print(node.form + " ");
        }
        System.out.println();
    }

    /**
     * 6/1/2014
     * extract features for each token in a parsing tree
     * @param tree
     * @param componentFragBegin, componentFragEnd: a fragment idx that should be at least tagged as <COMPONENT-I> in this line
     * @param beginTokenIdx: begin of the component, not the line. If this line doesn't contain the begin token, it is set to -1
     *        endTokenIdx: end of the component, not the line.
     *                            * @return
     */
    private void extractFeatureFromTree(int componentFragBegin, int componentFragEnd, DEPTree tree, String tagType, int beginTokenIdx, int endTokenIdx) {
        int i, size = tree.size(), npSum = 0;
        tree.resetDependents();
        DEPNode node;
        int noiseCount = 0;

        /** select the component tag **/
        Component component;
        if(tagType == null) {
            component = null;
        }
        else if(tagType == TagConstant.tableTag) {
            component = table;
        }
        else if(tagType == TagConstant.codeTag) {
            component = code;
        }
        else if(tagType == TagConstant.equTag) {
            component = equation;
        }
        else {
            component = misc;
        }
        traversedNodes = new HashSet<DEPNode>();
        String[] tokens = new String[size-1];
        for (i = 1; i < size; i++) {
            node = tree.get(i);
            tokens[i-1] = node.form;
            LinkedList<Feature> features = new LinkedList<Feature>();
/*            Matcher m1 = numberPattern.matcher(node.form);
            if (m1.find())
                features.add(feature1True);
            else
                features.add(feature1False);


           // Matcher m2 = puctPattern.matcher(node.form);
            // whether token is a punctuation
           /* if (m2.find())
                features.add(feature2True);
            else
                features.add(feature2False);
*/
      //        int charlen = node.form.length();
      //        features.add(new FeatureNode(1, charlen));

         /*   List<DEPArc> dependents = node.getDependents();
            for (DEPArc arc : dependents) {
                int fi = getFeatureIndex(arc.getNode().getLabel());
                FeatureNode fn = new FeatureNode(fi, 1);
                if(!features.contains(fn))
                    features.add(fn);
            }
    */
    //        Collections.sort(features, fc);
     /*       System.out.println(node.form);
            for(Feature f : features) {
                System.out.println(f.getIndex() + ": " + f.getValue());
            }
       */
            if(component != null) {
                if (beginTokenIdx == i - 1) answers[featureIdx] = component.begin;
                else if (beginTokenIdx + 1 == i - 1 && componentFragEnd > i - 1)
                    answers[featureIdx] = component.intermediate;
                else if (endTokenIdx -1 == i - 1) answers[featureIdx] = component.end;
                else if (componentFragBegin <= i - 1 && componentFragEnd > i - 1)
                    answers[featureIdx] = component.intermediate;
                else
                    answers[featureIdx] = TagConstant.TEXT;
            }
            else {
                answers[featureIdx] = TagConstant.TEXT;
            }
            if(answers[featureIdx] != TagConstant.TEXT)
                System.out.println(node.form + "\t" + TagConstant.getTagLabel((int) answers[featureIdx]));
            featureIdx++;
            if(featureIdx > 0)
                features.add(new FeatureNode(1, answers[featureIdx-1]));
            else
                features.add(new FeatureNode(1, 0));

            Feature[] featureArray;
            featureArray = features.toArray(new Feature[features.size()]);
            allFeatures.add(featureArray);


        }




    }

}

