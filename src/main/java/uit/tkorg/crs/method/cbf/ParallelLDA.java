package uit.tkorg.crs.method.cbf;

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
import uit.tkorg.crs.utility.TextFileUtility;

/**
 *
 * @author tin
 */
public class ParallelLDA {
    private static StringBuffer buffInputParallelLDA = new StringBuffer();
    private static StringBuffer buffAuthorIDAndDocMapping = new StringBuffer();
    public static HashMap<Integer, Integer> _AuthorInstanceHM = new HashMap<>();
    public static HashMap<Integer, Integer> _InstanceAuthorHM = new HashMap<>();
    public static HashMap<Integer, String> _InstancePublicationHM = new HashMap<>();
    private static HashMap<Integer, HashMap<Integer, Float>> _KLDivergenceHM;

    public HashMap<Integer, HashMap<Integer, Float>> process(String inputFile, HashMap<Integer, String> listAuthorID) {
        System.out.println("START TRAINING LDA");
        try {
            loadInstancePublication(inputFile);
            
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

            // Use two parallel samplers, which each look at one half the corpus and combine
            //  statistics after every iteration.
            model.setNumThreads(22);

            // Run the model for 50 iterations and stop (this is for testing only, 
            //  for real applications, use 1000 to 2000 iterations)
            model.setNumIterations(1000);
            model.estimate();

            System.out.println("***************************************************************************");
            String pathFile = (new File(inputFile)).getParent();
            model.printDocumentTopics(new File(pathFile + "\\" + "DocumentTopics.txt"));
            model.printTopicWordWeights(new File(pathFile + "\\" + "TopicWords.txt"));
            model.printTopWords(new File(pathFile + "\\" + "TopWords.txt"), 11, true);
            System.out.println("***************************************************************************");

            _KLDivergenceHM = new HashMap<>();
            System.out.println("NUMBER OF INSTANCES:" + instances.size());
            loadMappingInstanceIDAuthorID(pathFile + "\\CRS-AuthorIDAndInstance.txt");
            
            for (int inputAuthorID : listAuthorID.keySet()) {
                System.out.println("CURRENT INSTANCE IS:" + inputAuthorID);
                int instanceID = getInstanceFromAuthorID(inputAuthorID);
                double[] topicDistInputAuthor = model.getTopicProbabilities(instanceID);
                
                for (int otherInstanceID = 0; otherInstanceID < instances.size(); otherInstanceID++) {
                    if (instanceID != otherInstanceID) {
                        double[] topicDistOtherAuthor = model.getTopicProbabilities(otherInstanceID);
                        double klDivergence = Maths.klDivergence(topicDistInputAuthor, topicDistOtherAuthor);
                        //double klDivergence = Maths.jensenShannonDivergence(topicDistInputAuthor, topicDistOtherAuthor);
                                    
                        HashMap<Integer, Float> listKLDivergence = _KLDivergenceHM.get(inputAuthorID);
                        if (listKLDivergence == null) {
                            listKLDivergence = new HashMap<>();
                        }
                        
                        int otherAuthorID = getAuthorIDFromInstanceID(otherInstanceID);
                        listKLDivergence.put(otherAuthorID, (float) klDivergence);
                        _KLDivergenceHM.put(inputAuthorID, listKLDivergence);
                    }
                }
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return _KLDivergenceHM;
    }

    public void formatInputForParallelLDA(String rootPath) {
        File mainFolder = new File(rootPath); 
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
                        buffInputParallelLDA.append(TextFileUtility.readTextFile(fList[j].getAbsolutePath()));
                        buffAuthorIDAndDocMapping.append(
                                fileName.substring(fileName.lastIndexOf("_")+1, fileName.lastIndexOf(".")) 
                                + "\t" + instanceID + "\n");
                        instanceID++;
                    }
                    buffInputParallelLDA.append("\n");
                }
            }
        }

        TextFileUtility.writeTextFile(rootPath + "\\CRS-InputParallelLDA.txt", buffInputParallelLDA.toString());
        TextFileUtility.writeTextFile(rootPath + "\\CRS-AuthorIDAndInstance.txt", buffAuthorIDAndDocMapping.toString());
    }
    
    private int getInstanceFromAuthorID(int authorID) {
        return _AuthorInstanceHM.get(authorID);
    }
    
    private int getAuthorIDFromInstanceID(int instanceID) {
        return _InstanceAuthorHM.get(instanceID);
    }
    
    public String getPublicationFromAuthorID(int authorID) {
        int instanceID = getInstanceFromAuthorID(authorID);
        return (_InstancePublicationHM.get(instanceID));
    }
    
    private void loadMappingInstanceIDAuthorID(String mapFile) {
        try {
            FileInputStream fis = new FileInputStream(mapFile);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine(); // skip the header line
            String line = null;
            String[] tokens;
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split("\t");
                if (tokens.length != 2) {
                    continue;
                }

                int authorID = Integer.parseInt(tokens[0]);
                int instanceID = Integer.parseInt(tokens[1]);
                _AuthorInstanceHM.put(authorID, instanceID);
                _InstanceAuthorHM.put(instanceID, authorID);
            }
            bufferReader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    
        private void loadInstancePublication(String inputFile) {
        try {
            FileInputStream fis = new FileInputStream(inputFile);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            String line = null;
            String[] tokens;
            int instanceID = 0;
            while ((line = bufferReader.readLine()) != null) {
//                tokens = line.split("X\t");
//                if (tokens.length != 2) {
//                    continue;
//                }
//                String publications = tokens[1];
                //line = StringUtils.substringAfter(line,"X");
                _InstancePublicationHM.put(instanceID, line);
                instanceID++;
            }
            bufferReader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
