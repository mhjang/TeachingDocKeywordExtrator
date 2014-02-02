package test;

import Similarity.CosineSimilarity;
import junit.framework.TestCase;

import java.util.LinkedList;

/**
 * Created by mhjang on 2/1/14.
 */
public class ConsineSimilarityTest extends TestCase {
    double sim;
    public void setUp() throws Exception {
        LinkedList<String> l1 = new LinkedList<String>();
        LinkedList<String> l2 = new LinkedList<String>();

        /**
         *  V1: {Red: 1, Green: 0, Pink: 1}
         *  V2: {Red: 0, Green: 1, Pink: 1}
         *  1 / 2 = 0.5
         *
         */
        l1.add("Red");
        l1.add("Pink");

        l2.add("Green");
        l2.add("Pink");

        sim = CosineSimilarity.BinaryCosineSimilarity(l1, l2);



    }

    public void testEnds() {
        assertEquals(sim - 0.5 < 0.0001, true);
    }
}
