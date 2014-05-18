package test;

import parser.StopWordRemover;
import junit.framework.TestCase;

import java.util.LinkedList;

/**
 * Created by mhjang on 2/1/14.
 * Written in 6:11pm
 */
public class StopWordRemoverTest extends TestCase {
    LinkedList<String> l1;
    public void setUp() throws Exception {
        l1 = new LinkedList<String>();
        l1.add("and");
        l1.add("hello");
        l1.add("but");
        l1.add("world");
        StopWordRemover swr = new StopWordRemover();
        l1 = swr.removeStopwords(l1);

    }

    public void testEnds() {
        assertEquals(l1.size(), 1);
    }
}
