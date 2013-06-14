/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.method;

import cc.mallet.util.*;
import cc.mallet.types.*;
import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.*;
import cc.mallet.topics.*;
import java.util.*;
import java.util.regex.*;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import uit.tkorg.utility.TextFileProcessor;

/**
 *
 * @author tin
 */
public class ParallelLDA {
    private static StringBuffer buffInputParallelLDA = new StringBuffer();
    private static StringBuffer buffAuthorIDAndDocMapping = new StringBuffer();
    private static HashMap<Integer, HashMap<Integer, Float>> _KLDivergenceHM;

    public HashMap<Integer, HashMap<Integer, Float>> process(String inputFile) {
        try {
            // Begin by importing documents from text to feature sequences
            ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

            // Pipes: lowercase, tokenize, remove stopwords, map to features
            pipeList.add(new CharSequenceLowercase());
            pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
            pipeList.add(new TokenSequenceRemoveStopwords(new File("stoplists/en.txt"), "UTF-8", false, false, false));
            pipeList.add(new TokenSequence2FeatureSequence());

            InstanceList instances = new InstanceList(new SerialPipes(pipeList));

            Reader fileReader = new InputStreamReader(new FileInputStream(new File(inputFile)), "UTF-8");
            instances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
                    3, 2, 1)); // data, label, name fields

            // Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
            //  Note that the first parameter is passed as the sum over topics, while
            //  the second is the parameter for a single dimension of the Dirichlet prior.
            int numTopics = 100;
            ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);

            model.addInstances(instances);
            model.wordsPerTopic = 20;

            // Use two parallel samplers, which each look at one half the corpus and combine
            //  statistics after every iteration.
            model.setNumThreads(11);

            // Run the model for 50 iterations and stop (this is for testing only, 
            //  for real applications, use 1000 to 2000 iterations)
            model.setNumIterations(1000);
            model.estimate();

            String pathFile = (new File(inputFile)).getPath();
            model.printDocumentTopics(new File(pathFile + "\\" + "DocumentTopics.txt"));
            model.printTopicWordWeights(new File(pathFile + "\\" + "TopicWords.txt"));
            model.printTopWords(new File(pathFile + "\\" + "TopWords.txt"), 11, true);

            System.out.println("***************************************************************************");
            // The data alphabet maps word IDs to strings
            Alphabet dataAlphabet = instances.getDataAlphabet();

            FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
            LabelSequence topics = model.getData().get(0).topicSequence;

            Formatter out = new Formatter(new StringBuilder(), Locale.US);
            for (int position = 0; position < tokens.getLength(); position++) {
                out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
            }
            System.out.println(out);
            System.out.println("***************************************************************************");

            _KLDivergenceHM = new HashMap<>();
            System.out.println("NUMBER OF INSTANCES:" + instances.size());
            for (int authorID1 = 0; authorID1 < instances.size(); authorID1++) {
                System.out.println("CURRENT INSTANCE IS:" + authorID1);

                double[] topicDistributionAuthor1 = model.getTopicProbabilities(authorID1);

                for (int authorID2 = 0; authorID2 < instances.size(); authorID2++) {
                    if (authorID1 != authorID2) {
                        System.out.println("KL DIVERGENCE OF CURRENT INSTANCE WITH THE INSTANCE NUMBER:" + authorID2);
                        double[] topicDistributionAuthor2 = model.getTopicProbabilities(authorID2);
                        float klDivergence = (float) Maths.klDivergence(topicDistributionAuthor1, topicDistributionAuthor2);

                        HashMap<Integer, Float> listKLDivergence = _KLDivergenceHM.get(authorID1);
                        if (listKLDivergence == null) {
                            listKLDivergence = new HashMap<>();
                        }
                        listKLDivergence.put(authorID2, klDivergence);
                        _KLDivergenceHM.put(authorID1, listKLDivergence);
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return _KLDivergenceHM;
    }

    public void formatInputForParallelLDA(String rootPath) {
        File mainFolder = new File(rootPath); // C:\CRS-Experiment\OutStem
        File[] subFolderList = mainFolder.listFiles();
        int instanceID = 0;
        buffAuthorIDAndDocMapping.append("AuthorID" + "\t" + "InstanceID" + "\n");
        for (int i = 0; i < subFolderList.length; i++) {
            if (subFolderList[i].isDirectory()) {
                System.out.println("Processing Folder: " + subFolderList[i].getName());
                File[] fList = subFolderList[i].listFiles();
                for (int j = 0; j < fList.length; j++) {
                    if (fList[j].isFile()) {
                        String fileName = fList[j].getName();
                        buffInputParallelLDA.append(fileName + "\t" + "X" + "\t");
                        buffInputParallelLDA.append(TextFileProcessor.readTextFile(fList[j].getAbsolutePath()));
                        buffAuthorIDAndDocMapping.append(fileName.substring(fileName.lastIndexOf("_")+1) + "\t" + instanceID + "\n");
                        instanceID++;
                    }
                    buffInputParallelLDA.append("\n");
                }
            }
        }

        TextFileProcessor.writeTextFile(rootPath + "\\CRS-InputParallelLDA.txt", buffInputParallelLDA.toString());
        TextFileProcessor.writeTextFile(rootPath + "\\CRS-AuthorIDAndInstance.txt", buffAuthorIDAndDocMapping.toString());
    }
}
