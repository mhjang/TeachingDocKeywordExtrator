package parser;

import org.lemurproject.galago.core.parse.stem.KrovetzStemmer;

import java.io.*;

/**
 * Created by mhjang on 2/2/14.
 * Just a few different interface methods for using KrovetzStemmer
 */
public class Stemmer {

    public Stemmer() {

    }

    public static void main(String[] args) {
        Stemmer stemmer = new Stemmer();
        stemmer.stemAllFilesOutput("./wikiexpansion_resource");
    }

    public void stemAllFilesOutput(String path) {
        File fileEntry = new File(path);
        File[] listOfFiles = fileEntry.listFiles();
        for(File file : listOfFiles) {
            if(file.isFile() && file.getName().endsWith(".html")) {
                stemOneFileOutput(file.getPath());
            }
        }
    }

    public String stemOneFileToString(String path) {
        StringBuilder sb = new StringBuilder();
        try {
            File fileEntry = new File(path);
            LineNumberReader reader = new LineNumberReader(new FileReader(fileEntry));
            String line;
            KrovetzStemmer stemmer = new KrovetzStemmer();
            line = reader.readLine();
            while (line != null) {
                line = line.trim();
                String [] tokens = line.split(" ,");
                for( int i=0 ; i<tokens.length ; i++ ) {
                        String stem = stemmer.stem(tokens[i]);
                        System.out.println(tokens[i] + " " + stem);
                        sb.append(stem + " ");
                }
                sb.append("\n");
                line = reader.readLine();
            }

            }catch(Exception e) {
                 e.printStackTrace();
            }
        return sb.toString();

     }

    public void stemOneFileOutput(String path) {
        try {
            File fileEntry = new File(path);
            LineNumberReader reader = new LineNumberReader(new FileReader(fileEntry));
            String pathPreFix = path.substring(0, path.lastIndexOf('/'));
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(pathPreFix + "/stemmed/"+fileEntry.getName())));
            String line;
            KrovetzStemmer stemmer = new KrovetzStemmer();
            try {
                line = reader.readLine();
                while (line != null) {
                    line = line.trim();
                    String [] tokens = line.split(" ,");
                    for( int i=0 ; i<tokens.length ; i++ ) {
                        String stem = stemmer.stem(tokens[i]);
                        System.out.println(tokens[i] + " " + stem);
                        bw.write(stem + " ");
                    }
                    bw.write("\n");
                    bw.flush();
                    line = reader.readLine();
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
            bw.close();
        }catch(Exception e) {
            e.printStackTrace();
     }
    }
}

