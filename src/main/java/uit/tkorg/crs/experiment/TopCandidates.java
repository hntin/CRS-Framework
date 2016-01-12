/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.experiment;

import java.io.File;
import java.io.IOException;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

/**
 *
 * @author thucnt
 */
public class TopCandidates {
    public static void main(String[] args) throws IOException {
        ArffLoader arffLoader = new ArffLoader();

        File datasetFile = new File("/Users/thucnt/Desktop/TrainingData/Bayesian.arff");
        arffLoader.setFile(datasetFile);

        Instances dataInstances = arffLoader.getDataSet();

        for (int i = 0; i <= dataInstances.numInstances() - 1; i++) {
            Instance instance = dataInstances.instance(i);
            System.out.println(instance.value(1)); //get Attribute 0 as String
        }
    }
}
