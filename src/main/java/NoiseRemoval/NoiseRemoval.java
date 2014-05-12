package NoiseRemoval;
;

import java.io.*;
import java.util.ArrayList;

/**
 * Created by mhjang on 5/10/14.
 */
public class NoiseRemoval {
    public static void main(String[] args) {
        NoisyRemoval nrm = new NoisyRemoval();
        nrm.decodeClassification();
    }

    public void decodeClassification() {
        try {
            File folder = new File("/Users/mhjang/Documents/SVMlight/Source/svm_light/result/");
            String docDir = "/Users/mhjang/Documents/teaching_documents/extracted/stemmed/parsed/feature_tokens/";
            String outputDir = "/Users/mhjang/Documents/teaching_documents/extracted/stemmed/parsed/noise_removed/";
            for (final File fileEntry : folder.listFiles()) {
                int notNoisyTokens = 0, noisyTokens = 0;
                if(fileEntry.getName().contains(".DS_Store")) continue;
                System.out.println(fileEntry.getName());
                BufferedReader br = new BufferedReader(new FileReader(fileEntry));
                String line;
                ArrayList<Boolean> noisyLabels = new ArrayList<Boolean>();
                while((line = br.readLine()) != null) {
                    double score = Double.parseDouble(line);
                    if(score > 0)
                        noisyLabels.add(Boolean.TRUE);
                    else
                        noisyLabels.add(Boolean.FALSE);
                }
                BufferedReader br2 = new BufferedReader(new FileReader(docDir + fileEntry.getName()));
                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(outputDir + fileEntry.getName()));

                    while ((line = br2.readLine()) != null) {
                        String[] tokens = line.split(" ");
                        for (int i = 0; i < tokens.length; i++) {
                            if (!noisyLabels.get(i)) {
                                bw.write(tokens[i] + " ");
                                notNoisyTokens++;
                            }
                            else {
                                System.out.print(tokens[i] + " ");
                                noisyTokens++;
                            }
                        }
                        bw.write("\n");
                        //       System.out.println();
                    }
                    bw.close();
                    System.out.println((double)(noisyTokens) / (double)(notNoisyTokens + noisyTokens));
                    //        removedTokens = 0;

                }catch(FileNotFoundException fe) {
                    fe.printStackTrace();
                }


            }
        }catch (Exception e) {
            e.printStackTrace();
        }

    }



}
