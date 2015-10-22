/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.datapreparation;

/**
 *
 * @author thucnt
 */

import ir.vsr.HashMapVector;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Blob;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import uit.tkorg.utility.common.DatabaseTool;

public class TFIDFTester {

    String outputFolder;
    String inputFolder;
    Configuration configuration;
    FileSystem fileSystem;
    Path corpusPath;
    Path documentsSequencePath;
    Path tokenizedDocumentsPath;
    Path tfidfPath;
    Path termFrequencyVectorsPath;

    public static void main(String args[]) throws Exception {

        TFIDFTester tester = new TFIDFTester();

//        tester.corpusToSequence();
        tester.createTestDocuments();
        tester.calculateTfIdf();

        //tester.printSequenceFile(tester.documentsSequencePath);

        System.out.println("\n Step 1: Word count ");
        tester.printSequenceFile(new Path(tester.outputFolder
                + "wordcount/part-r-00000"));

        System.out.println("\n Step 2: Word dictionary ");
        tester.printSequenceFile(new Path(tester.outputFolder,
                "dictionary.file-0"));

        System.out.println("\n Step 3: Term Frequency Vectors ");
        tester.printSequenceFile(new Path(tester.outputFolder
                + "tf-vectors/part-r-00000"));

        System.out.println("\n Step 4: Document Frequency ");
        tester.printSequenceFile(new Path(tester.outputFolder
                + "tfidf/df-count/part-r-00000"));

        System.out.println("\n Step 5: TFIDF ");
        tester.printSequenceFile(new Path(tester.outputFolder
                + "tfidf/tfidf-vectors/part-r-00000"));
        tester.buildProfile(new Path(tester.outputFolder
                + "tfidf/tfidf-vectors/part-r-00000"),1000);
    }

    public TFIDFTester() throws IOException {

        configuration = new Configuration();
        fileSystem = FileSystem.get(configuration);

        inputFolder = "/Users/thucnt/temp/test/";
        outputFolder = "/Users/thucnt/temp/output/2001/";
        
        documentsSequencePath = new Path(outputFolder, "sequence");
        tokenizedDocumentsPath = new Path(outputFolder,
                DocumentProcessor.TOKENIZED_DOCUMENT_OUTPUT_FOLDER);
        tfidfPath = new Path(outputFolder + "tfidf");
        termFrequencyVectorsPath = new Path(outputFolder
                + DictionaryVectorizer.DOCUMENT_VECTOR_OUTPUT_FOLDER);
    }

    public void corpusToSequence() throws Exception {
        final SequenceFile.Writer writer = new SequenceFile.Writer(fileSystem,
                configuration, documentsSequencePath, Text.class, Text.class);
        
        DatabaseTool db = new DatabaseTool();
        db.connect();
        ResultSet rs = db.getPapersByYear(2001);
        
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
    
    public void createTestDocuments() throws IOException {
        SequenceFile.Writer writer = new SequenceFile.Writer(fileSystem,
                configuration, documentsSequencePath, Text.class, Text.class);

        Text id1 = new Text("Document 1");
        String abstract1 = "John Rawls claims that " +
                "\"Justice is the first virtue of social institutions, as truth is of systems of thought.\"[9] Justice can be thought of as distinct from benevolence, " +
                "charity, prudence, mercy, generosity, or compassion, although these dimensions are regularly understood to also be interlinked. " +
                "Studies at UCLA in 2008 have indicated that reactions to fairness are \"wired\" into the brain and that, " +
                "\"Fairness is activating the same part of the brain that responds to food in rats... This is consistent with the notion that being treated fairly satisfies a basic need\".[11] " +
                "Research conducted in 2003 at Emory University, Georgia, USA, involving Capuchin Monkeys demonstrated that other cooperative animals also possess such a sense and that " +
                "\"inequity aversion may not be uniquely human\"[12] indicating that ideas of fairness and justice may be instinctual in nature.";
        Text text1 = new Text(abstract1);
        writer.append(id1, text1);

        Text id2 = new Text("Document 2");
        String abstract2 = "Utilitarianism is a form of consequentialism, where punishment is forward-looking. " +
                "Justified by the ability to achieve future social benefits resulting in crime reduction, the moral worth of an action is determined by its outcome. " +
                "Retributive justice regulates proportionate response to crime proven by lawful evidence, " +
                "so that punishment is justly imposed and considered as morally correct and fully deserved. " +
                "The law of retaliation (lex talionis) is a military theory of retributive justice, " +
                "which says that reciprocity should be equal to the wrong suffered; \"life for life, wound for wound, stripe for stripe.\"[13] " +
                "Restorative justice is concerned not so much with retribution and punishment as with (a) making the victim whole and (b) reintegrating the offender into society. " +
                "This approach frequently brings an offender and a victim together, so that the offender can better understand the effect his/her offense had on the victim. " +
                "Distributive justice is directed at the proper allocation of things—wealth, power, reward, respect—among different people.";
        Text text2 = new Text(abstract2);
        writer.append(id2, text2);
        
        Text id3 = new Text("Document 3");
        String abstract3 = "Wisdom is a deep understanding and realization of people, things, events or situations, resulting in the ability to apply perceptions, judgements and actions " +
                "in keeping with this understanding. It often requires control of one's emotional reactions (the \"passions\") so that universal principles, reason and knowledge " +
                "prevail to determine one's actions.";
        Text text3 = new Text(abstract3);
        writer.append(id3, text3);

        writer.close();
    }

    public void calculateTfIdf() throws ClassNotFoundException, IOException,
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

    void printSequenceFile(Path path) {
        SequenceFileIterable<Writable, Writable> iterable = new SequenceFileIterable<Writable, Writable>(
                path, configuration);
        for (Pair<Writable, Writable> pair : iterable) {
            System.out
                    .format("%10s -> %s\n", pair.getFirst(), pair.getSecond());
        }
    }
    
    void buildProfile(Path path, int idAuthor){
//        try {
//            tfidfPath = new Path("/Users/thucnt/NetBeansProjects/mahout/output/tfidf/");
//            MahoutFile.readMahoutVectorFiles(tfidfPath);
//        } catch (Exception ex) {
//            Logger.getLogger(TFIDFTester.class.getName()).log(Level.SEVERE, null, ex);
//        }
        SequenceFileIterable<Writable, Writable> iterable = new SequenceFileIterable<Writable, Writable>(
                path, configuration);
        for (Pair<Writable, Writable> pair : iterable) {
            System.out
                    .format("%10s -> %s\n", pair.getFirst(), pair.getSecond());
        }
        
    }
}