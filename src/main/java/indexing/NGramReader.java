package indexing;


import indexing.simple.DiskMapReader;
import org.lemurproject.galago.core.btree.simple.DiskMapSortedBuilder;
import org.lemurproject.galago.tupleflow.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
import java.util.StringTokenizer;


/**
 * Created by mhjang on 3/2/14.
 */
public class NGramReader {
    public static void main(String[] args) throws IOException {
       // BufferedReader br = new BufferedReader(new FileReader(new File("/Users/mhjang/3gms/extracted/3gm-0000")));
    //    HashMap<byte[], byte[]> data = new HashMap<byte[], byte[]>();
 /*
        DiskMapSortedBuilder dmb = new DiskMapSortedBuilder("ngram_index/3gms");
        String fileName = "/Users/mhjang/3gms/extracted/3gm-00";
        for(int i=0; i<=97; i++) {
            String fileIndex = "";
            if(i < 10) {
                fileIndex = "0" + Integer.toString(i);
            }
            else {
                fileIndex = Integer.toString(i);
            }
            BufferedReader br = new BufferedReader(new FileReader(new File(fileName + fileIndex)));

            String line = null;

            while((line = br.readLine()) != null) {
                try{
                    StringTokenizer st = new StringTokenizer(line, "\t");
               //     System.out.println(line);
                    String word = st.nextToken();
                    String freq = st.nextToken();
             //       Integer occurrence = Integer.parseInt(freq);
                    byte[] ngram = Utility.fromString(word);
                    byte[] frequency = Utility.fromString(freq);
                    dmb.put(ngram, frequency);
          //      data.put(ngram, frequency);
                }catch(NumberFormatException e) {
                    System.out.println(line);
                    e.printStackTrace();

                }
            }
        }
        dmb.close();
*/
   /*     byte[] foo = Utility.fromString("foo");
        byte[] bar = Utility.fromString("bar");
        byte[] baz = Utility.fromString("baz");
        byte[] hmm = Utility.fromString("hmm");

        HashMap<byte[], byte[]> data = new HashMap<byte[], byte[]>();
        data.put(foo, bar);
        data.put(bar, baz);
        data.put(baz, hmm);
        data.put(hmm, foo);
        */
        File file = new File("ngram_index/3gms");

        /**
         * Build an on-disk map using galago
         */
        DiskMapReader mapReader = new DiskMapReader(file.getAbsolutePath());

    /*
     * pull keys
     */
        byte[] data = mapReader.get(Utility.fromString("natural language processing"));
        if(data != null) {
            String count = Utility.toString(data);
            System.out.println("count: "+ count);
        }

        // java using object equality is dumb
        // assertTrue(memKeys.contains(key)) will fail because it does pointer comparisons...

    }
}
