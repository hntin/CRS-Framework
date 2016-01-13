/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package weka.visualize;

import java.io.File;
import java.io.IOException;
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
            // Create an empty training set
            
            ArffLoader arffLoader = new ArffLoader();
            
            File datasetFile = new File("/Users/thucnt/Desktop/TrainingData/weka_balancing_downsampling.arff");
            arffLoader.setFile(datasetFile);
            
            Instances isTrainingSet = arffLoader.getDataSet();
            // Set class index
            isTrainingSet.setClassIndex(5);
            
            // Create a naïve bayes classifier
            Classifier cModel = (Classifier)new MultilayerPerceptron();
            cModel.buildClassifier(isTrainingSet);
            
            // Test the model
//            Evaluation eTest = new Evaluation(isTrainingSet);
            datasetFile = new File("/Users/thucnt/Downloads/3Hobs/TestingData/Testing_FullData.arff");
            arffLoader.setFile(datasetFile);
            Instances isTestingSet = arffLoader.getDataSet();
            // Set class index
            //isTestingSet.setClassIndex(5);

            int[] delAttr = {0,1,1,1,1,1,1};
                
            for (int i = 0; i < isTestingSet.numInstances(); i++){
                Instance instance = isTestingSet.instance(i);
                Instance t = new Instance(instance);
//                System.out.println(t);
                t = createInstance(instance,delAttr);
                System.out.println(t);
               instance.setDataset(isTrainingSet);
                double[] fDistribution = cModel.distributionForInstance(t);
                for (int j = 0; j < fDistribution.length; j++){
                    System.out.print(fDistribution[j] + "\t");
                    System.out.println();
                }
            }
            
            
            //eTest.evaluateModel(cModel, isTestingSet);
        } catch (IOException ex) {
            Logger.getLogger(TestAPI.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(TestAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
