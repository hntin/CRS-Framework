/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.visualize;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 *
 * @author thucnt
 */
public class TestAPI {
    public static Instance createInstance(Instance instance, int[] delAttr){
        Instance ret = new Instance(instance);
        for (int i = delAttr.length - 1; i >= 0; i--){
            if (delAttr[i] == 0)
                ret.deleteAttributeAt(i);
//            if (i == instance.classIndex())
//                ret.setValue(i, instance.stringValue(index[i]));
//            else
//                ret.setValue(i, instance.value(index[i]));
        }
        return ret;
    }
    public static void main(String[] args){
        try {
            File f = new File("D:\\1.CRS-Experiment\\MLData\\3-Hub\\Senior\\TestingData\\Evaluation.txt");
            FileWriter fstream = new FileWriter(f, true);
            BufferedWriter out = new BufferedWriter(fstream);
            ObjectOutputStream objOut = new ObjectOutputStream(new FileOutputStream("D:\\1.CRS-Experiment\\MLData\\3-Hub\\Senior\\TrainedModel\\MLP_3F.bin"));
            // Create an empty training set
            
            ArffLoader arffLoader = new ArffLoader();
            
            File datasetFile = new File("D:\\1.CRS-Experiment\\MLData\\3-Hub\\Senior\\TrainingData\\Weka_Training_AllFeatures_DownSampling.arff");
            arffLoader.setFile(datasetFile);
            
            Instances isTrainingSet = arffLoader.getDataSet();
            isTrainingSet.deleteAttributeAt(0);
            isTrainingSet.deleteAttributeAt(0);
//            isTrainingSet.deleteAttributeAt(0);
//            isTrainingSet.deleteAttributeAt(0);
            // Set class index
            isTrainingSet.setClassIndex(3);
            
            // Create a MLP classifier
            System.out.println("Building model");
            String[] options = {"-L", "0.3", "-N", "2000", "-H", "20"};
            Classifier cModel = (Classifier)new MultilayerPerceptron(); 
            cModel.setOptions(options);
            cModel.buildClassifier(isTrainingSet);
            objOut.writeObject(cModel);
            
            System.out.println("Loadding test data");
            datasetFile = new File("D:\\1.CRS-Experiment\\MLData\\3-Hub\\Senior\\TestingData\\Testing_FullData.arff");
            arffLoader.setFile(datasetFile);
            Instances isTestingSet = arffLoader.getDataSet();
            // Set class index
            //isTestingSet.setClassIndex(5);

            int[] delAttr = {0,0,0,1,1,1,1};
                
            for (int i = 0; i < isTestingSet.numInstances(); i++){
                Instance instance = isTestingSet.instance(i);
                Instance t = new Instance(instance);
//                System.out.println(t);
                t = createInstance(instance,delAttr);
                t.setDataset(isTrainingSet);
                double[] fDistribution = cModel.distributionForInstance(t);
                double max = fDistribution[0];
                String sample = "Positive";
                if (fDistribution[1] > max){
                    max = fDistribution[1];
                    sample = "Negative";
                }
//                for (int j = 0; j < fDistribution.length; j++){
//                    System.out.print(fDistribution[j] + "\t");
//                    System.out.println();
//                }
                String output = instance.stringValue(0) + "," + max + "," + sample;
                System.out.println(output);
                out.append(output);
                out.newLine();
            }
            objOut.close();
            out.close();
            //eTest.evaluateModel(cModel, isTestingSet);
        } catch (IOException ex) {
            Logger.getLogger(TestAPI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(TestAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
