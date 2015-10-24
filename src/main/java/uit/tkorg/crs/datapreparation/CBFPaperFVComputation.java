/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.datapreparation;

import uit.tkorg.utility.common.MahoutFile;
import ir.vsr.HashMapVector;
import java.io.File;
import java.io.IOException;
import java.sql.Blob;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileIterable;
import org.apache.mahout.vectorizer.DictionaryVectorizer;
import org.apache.mahout.vectorizer.DocumentProcessor;
import org.apache.mahout.vectorizer.common.PartialVectorMerger;
import org.apache.mahout.vectorizer.tfidf.TFIDFConverter;
import uit.tkorg.crs.model.Paper;
import uit.tkorg.utility.common.DatabaseTool;
import uit.tkorg.utility.common.TextFileUtility;
import uit.tkorg.utility.common.WeightingUtility;
import uit.tkorg.utility.common.TextVectorizationByMahoutTerminalUtility;

/**
 *
 * @author THNghiep 
 * This class handles logic to compute feature vector of all papers.
 * Method: 
 * - Compute papers' full vector: its content itself or combining its refs and cits by linear, cosine, rpy.
 */
public class CBFPaperFVComputation {
    private static Configuration configuration;
    private static FileSystem fileSystem;
    private static Path documentsSequencePath;
    private static Path tokenizedDocumentsPath;
    private static Path tfidfPath;
    private static Path termFrequencyVectorsPath;
    
    // Prevent instantiation.
    private CBFPaperFVComputation() {}

    /**
     * vector hóa abstract các bài báo từ năm year trở về trước và ghi ra sequence file 
     * dữ liệu lấy trực tiếp từ database 
     * @param year
     * @throws Exception
     */
    public static void vectorzie(int year, String outputFolder ) throws Exception {
        configuration = new Configuration();
        fileSystem = FileSystem.get(configuration);
        
        documentsSequencePath = new Path(outputFolder, "sequence");
        tokenizedDocumentsPath = new Path(outputFolder,
                DocumentProcessor.TOKENIZED_DOCUMENT_OUTPUT_FOLDER);
        tfidfPath = new Path(outputFolder + "tfidf");
        termFrequencyVectorsPath = new Path(outputFolder
                + DictionaryVectorizer.DOCUMENT_VECTOR_OUTPUT_FOLDER);
//        tester.createTestDocuments();
        abstractToSequence(year);
        calculateTfIdf(outputFolder);
//        System.out.println("\n Step 1: Word count ");
//        printSequenceFile(new Path(outputFolder
//                + "wordcount/part-r-00000"));
//
//        System.out.println("\n Step 2: Word dictionary ");
//        printSequenceFile(new Path(outputFolder,
//                "dictionary.file-0"));
//
//        System.out.println("\n Step 3: Term Frequency Vectors ");
//        printSequenceFile(new Path(outputFolder
//                + "tf-vectors/part-r-00000"));
//
//        System.out.println("\n Step 4: Document Frequency ");
//        printSequenceFile(new Path(outputFolder
//                + "tfidf/df-count/part-r-00000"));
//
//        System.out.println("\n Step 5: TFIDF ");
//        printSequenceFile(new Path(outputFolder
//                + "tfidf/tfidf-vectors/part-r-00000"));
    }
    
    /**
     *
     * @param inputFolder
     * @param outputFolder
     * @throws Exception
     */
    public static void vectorzie(String inputFolder, String outputFolder ) throws Exception {
        configuration = new Configuration();
        fileSystem = FileSystem.get(configuration);
        
        documentsSequencePath = new Path(outputFolder, "sequence");
        tokenizedDocumentsPath = new Path(outputFolder,
                DocumentProcessor.TOKENIZED_DOCUMENT_OUTPUT_FOLDER);
        tfidfPath = new Path(outputFolder + "tfidf");
        termFrequencyVectorsPath = new Path(outputFolder
                + DictionaryVectorizer.DOCUMENT_VECTOR_OUTPUT_FOLDER);
//        tester.createTestDocuments();
        abstractToSequence(inputFolder);
        calculateTfIdf(outputFolder);
        
//        System.out.println("\n Step 1: Word count ");
//        printSequenceFile(new Path(outputFolder
//                + "wordcount/part-r-00000"));
//
//        System.out.println("\n Step 2: Word dictionary ");
//        printSequenceFile(new Path(outputFolder,
//                "dictionary.file-0"));
//
//        System.out.println("\n Step 3: Term Frequency Vectors ");
//        printSequenceFile(new Path(outputFolder
//                + "tf-vectors/part-r-00000"));
//
//        System.out.println("\n Step 4: Document Frequency ");
//        printSequenceFile(new Path(outputFolder
//                + "tfidf/df-count/part-r-00000"));
//
//        System.out.println("\n Step 5: TFIDF ");
//        printSequenceFile(new Path(outputFolder
//                + "tfidf/tfidf-vectors/part-r-00000"));
    }
    
    /**
     *
     * @param path
     */
    public static void printSequenceFile(Path path) {
        SequenceFileIterable<Writable, Writable> iterable = new SequenceFileIterable<Writable, Writable>(
                path, configuration);
        for (Pair<Writable, Writable> pair : iterable) {
            System.out
                    .format("%10s -> %s\n", pair.getFirst(), pair.getSecond());
        }
    }
    
    /**
     *chuyen abstract tat ca cac bai bao sang sequence file
     * @param inputFolder la thu muc chua cac bai bao
     */
    private static void abstractToSequence(String inputFolder) {
        try {
            final SequenceFile.Writer writer = new SequenceFile.Writer(fileSystem,
                    configuration, documentsSequencePath, Text.class, Text.class);
            List<String> fileList = TextFileUtility.getPathFile(new File(inputFolder));
            for (int i = 0; i < fileList.size(); i++){
                File f = new File(fileList.get(i));
                String fileName = f.getName(); 
                Text idPaper = new Text(fileName.substring(0, fileName.lastIndexOf('.')));
                String str = TextFileUtility.readTextFile(fileList.get(i));
                Text content = new Text(str);
                writer.append(idPaper, content);
            }
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(CBFPaperFVComputation.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(CBFPaperFVComputation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     *chuyen abstract tat ca cac bai bao lay tu database tu nam hien tai tro ve
     * truoc sang sequence file
     * @param year: nam hien tai
     */
    private static void abstractToSequence(int year) throws Exception {
        final SequenceFile.Writer writer = new SequenceFile.Writer(fileSystem,
                configuration, documentsSequencePath, Text.class, Text.class);
        
        DatabaseTool db = new DatabaseTool();
        db.connect();
        ResultSet rs = db.getPapersByYear(year);//Lay abstract cac bai bao tu database 
        //ghi idPaper và abstract ra sequence file 
        while (rs.next()){
            int id = rs.getInt(1);
            Text idPaper = new Text(id + "");
            String content = "";
            Blob blob = rs.getBlob(2);
            if (blob != null){
                content = new String(blob.getBytes(1,(int)blob.length()));
                Text paperAbstract = new Text(content);
                writer.append(idPaper, paperAbstract);
            }
        }  
        db.disconnect();
        writer.close();
    }
    
    private static void calculateTfIdf(String outputFolder) throws ClassNotFoundException, IOException,
            InterruptedException {

        // Tokenize the documents using Apache Lucene StandardAnalyzer
        DocumentProcessor.tokenizeDocuments(documentsSequencePath,
                StandardAnalyzer.class, tokenizedDocumentsPath, configuration);

        DictionaryVectorizer.createTermFrequencyVectors(tokenizedDocumentsPath,
                new Path(outputFolder),
                DictionaryVectorizer.DOCUMENT_VECTOR_OUTPUT_FOLDER,
                configuration, 1, 1, 0.0f, PartialVectorMerger.NO_NORMALIZING,
                true, 1, 100, false, false);

        Pair<Long[], List<Path>> documentFrequencies = TFIDFConverter
                .calculateDF(termFrequencyVectorsPath, tfidfPath,
                        configuration, 100);

        TFIDFConverter.processTfIdf(termFrequencyVectorsPath, tfidfPath,
                configuration, documentFrequencies, 1, 100,
                PartialVectorMerger.NO_NORMALIZING, false, false, false, 1);
    }
    
    
    public static void readTFIDFFromMahoutFile(HashMap<String, Paper> papers, String vectorDir) throws Exception {
        // Step 1: Read vectors of all papers store in
        // - HashMap<Integer, String> dictMap: Dictionary of the whole collection.
        // - HashMap<String, HashMapVector> vectorizedDocuments: <PaperID, Vector TF*IDF of PaperID>
        System.out.println("Begin reading vector...");
        long startTime = System.nanoTime();
//        HashMap<Integer, String> dictMap = MahoutFile.readMahoutDictionaryFiles(vectorDir);
        HashMap<String, HashMapVector> vectorizedPapers = MahoutFile.readMahoutVectorFiles(papers,vectorDir);
        long estimatedTime = System.nanoTime() - startTime;
        System.out.println("Reading vector elapsed time: " + estimatedTime / 1000000000 + " seconds");
        System.out.println("End reading vector.");

        // Step 2: put TFIDF vectors of all paper (vectorizedDocuments)
        // into HashMap<String, Paper> papers (model)
        System.out.println("Begin setting tf-idf to papers...");
        startTime = System.nanoTime();
        CBFPaperFVComputation.setTFIDFVectorForAllPapers(papers, vectorizedPapers);
        estimatedTime = System.nanoTime() - startTime;
        System.out.println("Setting tf-idf to papers elapsed time: " + estimatedTime / 1000000000 + " seconds");
        System.out.println("End setting tf-idf to papers.");
    }
    
    public static void clearPaperAbstract(HashMap<String, Paper> papers) throws Exception {
        for (String paperId : papers.keySet()) {
            papers.get(paperId).setPaperAbstract(null);
        }
    }

    public static void clearTFIDF(HashMap<String, Paper> papers) throws Exception {
        for (String paperId : papers.keySet()) {
            papers.get(paperId).setTfidfVector(new HashMapVector());
        }
    }

    public static void clearFV(HashMap<String, Paper> papers) throws Exception {
        for (String paperId : papers.keySet()) {
            papers.get(paperId).setFeatureVector(new HashMapVector());
        }
    }

    public static void setTFIDFVectorForAllPapers(HashMap<String, Paper> papers, HashMap<String, HashMapVector> vectorizedDocuments) throws Exception {
        for (String paperId : papers.keySet()) {
            HashMapVector v = vectorizedDocuments.get(paperId);
            if (v != null)
                papers.get(paperId).setTfidfVector(v);
//            else
//                System.out.println("Missing tfidf vector of " + paperId);
        }
    }

    public static HashMap<String, Paper> extractPapers(HashMap<String, Paper> papers, HashSet<String> paperIds) throws Exception {
        HashMap<String, Paper> returnPapers = new HashMap<>();
        
        for (String paperId : paperIds) {
            if (papers.containsKey(paperId)) {
                returnPapers.put(paperId, papers.get(paperId));
            }
        }
        
        return returnPapers;
    }
  
    /**
     * This method computes and set value for all papers' full feature vector
     * (after combining citation and reference papers).
     * 
     * @param paperIds: restrict paper ids to compute FV. Null means no restriction.
     * @param combiningScheme   0: itself content, 1: itself content + content of references; 
     *                          2: itself content + content of citations; 3: itself content + content of references + content of citations.
     * @param weightingScheme   0: linear; 1: cosine; 2: rpy
     * @param pruning : Threshold of similarity between the main paper and combining papers.
     */
    public static void computeFeatureVectorForAllPapers(HashMap<String, Paper> papers, HashSet<String> paperIds, 
            int combiningScheme, int weightingScheme,
            double pruning) throws Exception {
        
        if (paperIds == null) {
            paperIds = (HashSet) papers.keySet();
        }
        
        // Current paper.
        int count = 0;
        System.out.println("Number of papers to compute FV: " + paperIds.size());

        for (String paperId : paperIds) {
            // Print current paper number.
            if (count % 1000 == 0) {
                System.out.println("Computing FV for paper No. " + (count + 1));
            }
            count++;

            if (papers.containsKey(paperId)) {
                computePaperFV(papers, paperId, combiningScheme, weightingScheme, pruning);
            }
        }
    }

    /**
     * This method compute final feature vector by combining citation and
     * reference.
     *
     * @param paperId
     * @return list represents feature vector.
     */
    public static void computePaperFV(HashMap<String, Paper> papers, String paperId, 
            int combiningScheme, int weightingScheme,
            double pruning) throws Exception {

        papers.get(paperId).setFeatureVector(new HashMapVector()); // Re-initiate feature vector
        papers.get(paperId).getFeatureVector().add(papers.get(paperId).getTfidfVector());// add tfidf to zero vector, not assign
        
        // weighting scheme
        if (weightingScheme == 0) {
            if (combiningScheme == 1) {
                sumFVLinear(papers, paperId, papers.get(paperId).getReferenceList(), pruning);
            } else if (combiningScheme == 2) {
                sumFVLinear(papers, paperId, papers.get(paperId).getCitationList(), pruning);
            } else if (combiningScheme == 3) {
                sumFVLinear(papers, paperId, papers.get(paperId).getReferenceList(), pruning);
                sumFVLinear(papers, paperId, papers.get(paperId).getCitationList(), pruning);
            }
        } else if (weightingScheme == 1) {
            if (combiningScheme == 1) {
                sumFVCosine(papers, paperId, papers.get(paperId).getReferenceList(), pruning);
            } else if (combiningScheme == 2) {
                sumFVCosine(papers, paperId, papers.get(paperId).getCitationList(), pruning);
            } else if (combiningScheme == 3) {
                sumFVCosine(papers, paperId, papers.get(paperId).getReferenceList(), pruning);
                sumFVCosine(papers, paperId, papers.get(paperId).getCitationList(), pruning);
            }
        } else if (weightingScheme == 2) {
            if (combiningScheme == 1) {
                sumFVRPY(papers, paperId, papers.get(paperId).getReferenceList(), pruning);
            } else if (combiningScheme == 2) {
                sumFVRPY(papers, paperId, papers.get(paperId).getCitationList(), pruning);
            } else if (combiningScheme == 3) {
                sumFVRPY(papers, paperId, papers.get(paperId).getReferenceList(), pruning);
                sumFVRPY(papers, paperId, papers.get(paperId).getCitationList(), pruning);
            }
        }
    }

    /**
     * This method compute sum of Papers(Citation or Reference Paper) with
     * linear weight
     *
     * @param combiningPaperIds
     * @return featureVector
     */
    private static void sumFVLinear(HashMap<String, Paper> papers, String paperId, List<String> combiningPaperIds,
            double pruning) throws Exception {
        for (String combiningPaperId : combiningPaperIds) {
            if (papers.containsKey(combiningPaperId)) {
                double cosine = WeightingUtility.computeCosine(papers.get(paperId).getTfidfVector(), papers.get(combiningPaperId).getTfidfVector());
                if (cosine < pruning) {
                    continue;
                }
                papers.get(paperId).getFeatureVector().add(papers.get(combiningPaperId).getTfidfVector());
            }
        }
    }

    /**
     * This method compute sum of Papers(Citation or Reference Paper) with
     * cosine weight
     *
     * @param paper
     * @param combiningPaperIds
     * @return featureVector
     */
    private static void sumFVCosine(HashMap<String, Paper> papers, String paperId, List<String> combiningPaperIds,
            double pruning) throws Exception {
        for (String combiningPaperId : combiningPaperIds) {
            if (papers.containsKey(combiningPaperId)) {
                double cosine = WeightingUtility.computeCosine(papers.get(paperId).getTfidfVector(), papers.get(combiningPaperId).getTfidfVector());
                if (cosine < pruning) {
                    continue;
                }
                cosine = WeightingUtility.computeCosine(papers.get(paperId).getTfidfVector(), papers.get(combiningPaperId).getTfidfVector());
                papers.get(paperId).getFeatureVector().addScaled(papers.get(combiningPaperId).getTfidfVector(), cosine);
            }
        }
    }

    /**
     * This method compute sum of Papers(Citation or Reference Paper) with rpy
     * weight
     *
     * @param paper
     * @param combiningPaperIds
     * @return featureVector
     */
    private static void sumFVRPY(HashMap<String, Paper> papers, String paperId, List<String> combiningPaperIds,
            double pruning) throws Exception {
        for (String combiningPaperId : combiningPaperIds) {
            if (papers.containsKey(combiningPaperId)) {
                double cosine = WeightingUtility.computeCosine(papers.get(paperId).getTfidfVector(), papers.get(combiningPaperId).getTfidfVector());
                if (cosine < pruning) {
                    continue;
                }
                double rpy = WeightingUtility.computeRPY(papers.get(paperId).getYear(), papers.get(combiningPaperId).getYear(), 0.9);
                papers.get(paperId).getFeatureVector().addScaled(papers.get(combiningPaperId).getTfidfVector(), rpy);
            }
        }
    }
}
