package uit.tkorg.crs.isolatedauthor.feature;

import java.util.ArrayList;
import java.util.List;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

/**
 *
 * @author TinHuynh
 */
public class FeatureVectorList {
    
    public List<FeatureVectorObject> buildingFeatureVectorListFromXMLFile(String file_TruePairs, String file_FalsePairs) {
        ArrayList<FeatureVectorObject> featureVectorList = new ArrayList<>();
        
        return featureVectorList;
    }

    public static Instances formatFeatureVectorAsWekaStructure(int numberOfFeatures, int numberOfVectors) {
        Instances instances;

        FastVector featureVector = new FastVector(numberOfFeatures + 1);
        Attribute contentSim = new Attribute(FeatureVectorObject.CONTENT_SIM);
        featureVector.addElement(contentSim);
        Attribute orgRSS = new Attribute(FeatureVectorObject.ORGANIZATION_RSS);
        featureVector.addElement(orgRSS);
        Attribute importantRate = new Attribute(FeatureVectorObject.IMPORTANT_RATE);
        featureVector.addElement(importantRate);
        Attribute activeScore = new Attribute(FeatureVectorObject.ACTIVE_SCORE);
        featureVector.addElement(activeScore);

        // Create a attribute for the output's Label of classification
        FastVector classLabel = new FastVector(2);
        classLabel.addElement("YES");
        classLabel.addElement("NO");
        Attribute labelAttribute = new Attribute(FeatureVectorObject.LABEL_CLASS , classLabel);
        featureVector.addElement(labelAttribute);

        // Finish building the structure of feature vector based on the Weka format
        instances = new Instances("FeatureVectorInstances", featureVector, numberOfVectors);
        instances.setClassIndex(numberOfFeatures);
        return instances;
    }

    public static Instances formatFeatureVectorAsInstances(List<FeatureVectorObject> listOfVectors, int numberOfVector) {      
        // Xây dựng să4n cấu trúc của các vector gồm bao nhiêu chiều có tên gì thuộc tính tên gi
        Instances instancesData = formatFeatureVectorAsWekaStructure(FeatureVectorObject.NUMBER_OF_FEATURE, numberOfVector);
        for (int i = 0; i < numberOfVector; i++) {
            // Insert values of features for each vector 
            Instance oneInstance = insertFeatureValue(instancesData, listOfVectors.get(i), FeatureVectorObject.NUMBER_OF_FEATURE + 1);
            instancesData.add(oneInstance);
        }
        return instancesData;
    }

    public static Instance insertFeatureValue(Instances instancesData, FeatureVectorObject featureVector, int dimension) {
        Instance oneInstance = new SparseInstance(dimension);

        // Add các giá trị của các đặc trưng vào ( các giá trị theo các chiều trong vector) 
        // Bao gồm cả thuộc tính gán nhãn của vector.
        oneInstance.setValue((Attribute) instancesData.attribute(FeatureVectorObject.CONTENT_SIM), featureVector.contentSimValue);
        oneInstance.setValue((Attribute) instancesData.attribute(FeatureVectorObject.ORGANIZATION_RSS), featureVector.orgRSSValue);
        oneInstance.setValue((Attribute) instancesData.attribute(FeatureVectorObject.IMPORTANT_RATE), featureVector.importantRateValue);
        oneInstance.setValue((Attribute) instancesData.attribute(FeatureVectorObject.ACTIVE_SCORE), featureVector.activeScoreValue);

        // Set value for label of classification
        oneInstance.setValue((Attribute) instancesData.attribute(FeatureVectorObject.LABEL_CLASS), featureVector.labelValue);
        
        return oneInstance;
    }

}
