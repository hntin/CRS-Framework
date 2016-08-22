/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package deeplearnazing4j;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.AutoEncoder;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.parallelism.ParallelWrapper;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.lossfunctions.LossFunctions.LossFunction;

/**
 *
 * @author thucnt
 */
public class MLPClassifier {
    public static void main(String[] args) throws IOException, InterruptedException{
        int seed = 123;
        double learningRate = 0.01;
        int batchSize = 50;
        int nEpochs = 100;
        int iterations = 1000;

        int numInputs = 5;
        int numOutputs = 1;
        
        //Load the training data:
        RecordReader rr = new CSVRecordReader();
        rr.initialize(new FileSplit(new File("input/training.csv")));
        DataSetIterator trainIter = new RecordReaderDataSetIterator(rr,batchSize,5,2);

//
//        //Load the test/evaluation data:
        RecordReader rrTest = new CSVRecordReader();
        rrTest.initialize(new FileSplit(new File("input/testing.csv")));
        DataSetIterator testIter = new RecordReaderDataSetIterator(rrTest,batchSize,5,2);
        
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
            .seed(seed)
            .gradientNormalization(GradientNormalization.ClipElementWiseAbsoluteValue)
            .gradientNormalizationThreshold(1.0)
            .iterations(iterations)
            .momentum(0.05)
            .momentumAfter(Collections.singletonMap(3, 0.9))
            .optimizationAlgo(OptimizationAlgorithm.CONJUGATE_GRADIENT)
            .list()
                 .layer(0, new AutoEncoder.Builder().nIn(5).nOut(3)
                    .weightInit(WeightInit.XAVIER).lossFunction(LossFunction.RMSE_XENT)
                    .corruptionLevel(0.5)
                    .build())
                 .layer(1, new AutoEncoder.Builder().nIn(3).nOut(2)
                         .weightInit(WeightInit.XAVIER).lossFunction(LossFunction.RMSE_XENT)
                         .corruptionLevel(0.5)

                         .build())
                 .layer(2, new OutputLayer.Builder(LossFunction.NEGATIVELOGLIKELIHOOD).activation("softmax")
                         .nIn(2).nOut(1).build())
            .pretrain(true).backprop(false)
            .build();
        
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        ParallelWrapper wrapper = new ParallelWrapper.Builder(model)
            .prefetchBuffer(24)
            .workers(20)
            .averagingFrequency(1)
            .reportScoreAfterAveraging(true)
            .useLegacyAveraging(false)
            .build();
        model.setListeners(new ScoreIterationListener(100));  //Print score every 10 parameter updates

        for ( int n = 0; n < nEpochs; n++) {
            wrapper.fit( trainIter );
        }

        System.out.println("Evaluate model....");
        Evaluation eval = new Evaluation(numOutputs);
        while(testIter.hasNext()){
            DataSet t = testIter.next();
            INDArray features = t.getFeatureMatrix();
            INDArray lables = t.getLabels();
            INDArray predicted = model.output(features,false);

            eval.eval(lables, predicted);

        }

        //Print the evaluation statistics
        System.out.println(eval.stats());
    }
}
