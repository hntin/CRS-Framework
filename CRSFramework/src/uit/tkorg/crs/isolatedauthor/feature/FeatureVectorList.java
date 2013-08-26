package uit.tkorg.crs.isolatedauthor.feature;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import uit.tkorg.utility.XMLFileUtility;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.trees.J48;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
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

    public ArrayList<FeatureVectorObject> buildingFeatureVectorListFromXMLFile(String file_TrueOrFalsePairs) {
        ArrayList<FeatureVectorObject> featureVectorList = new ArrayList<>();

        try {
            // Loadding file_TruePairs
            Document domObject = XMLFileUtility.readXMLFile(file_TrueOrFalsePairs);
            Element root = domObject.getRootElement();
            // iterate through child elements of root with element name "pair"
            for (Iterator i = root.elementIterator("pair"); i.hasNext();) {
                FeatureVectorObject featureVectorObject = new FeatureVectorObject();

                Element pairElement = (Element) i.next();
                Element contentSimNode = pairElement.element("ContentSim");
                float contentSimValue = Float.parseFloat(contentSimNode.getText());
                Element orgRSSNode = pairElement.element("OrgRSS");
                float orgRSSValue = Float.parseFloat(orgRSSNode.getText());

                Element coAuthorNode = pairElement.element("CoAuthor");
                Element importantRateNode = coAuthorNode.element("ImportantRate");
                Element activeScoreNode = coAuthorNode.element("ActiveScore");
                float importantRateValue = Float.parseFloat(importantRateNode.getText());
                float activeScoreValue = Float.parseFloat(activeScoreNode.getText());

                featureVectorObject.setContentSimValue(contentSimValue);
                featureVectorObject.setOrgRSSValue(orgRSSValue);
                featureVectorObject.setImportantRateValue(importantRateValue);
                featureVectorObject.setActiveScoreValue(activeScoreValue);

                Element labelNode = pairElement.element("tag");
                if (labelNode.getText().equalsIgnoreCase("1")) {
                    featureVectorObject.setLabelValue("YES");
                } else {
                    featureVectorObject.setLabelValue("NO");
                }

                featureVectorList.add(featureVectorObject);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return featureVectorList;
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
        Attribute labelAttribute = new Attribute(FeatureVectorObject.LABEL_CLASS, classLabel);
        featureVector.addElement(labelAttribute);

        // Finish building the structure of feature vector based on the Weka format
        instances = new Instances("FeatureVectorInstances", featureVector, numberOfVectors);
        instances.setClassIndex(numberOfFeatures);
        return instances;
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

    public static void main(String args[]) {
        System.out.println("START ...");
        try {
            // Loading data from XML file and build the feature vector list
            FeatureVectorList temp = new FeatureVectorList();
            ArrayList<FeatureVectorObject> featureVectorList1 = temp.buildingFeatureVectorListFromXMLFile(
                    "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\TruePair1.xml");

            ArrayList<FeatureVectorObject> featureVectorList2 = temp.buildingFeatureVectorListFromXMLFile(
                    "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\FalsePair1.xml");

            ArrayList<FeatureVectorObject> featureVectorList = new ArrayList<>();
            featureVectorList.addAll(featureVectorList1);
            featureVectorList.addAll(featureVectorList2);

            // Tach featureVectorList thanh tap train va tap test 
            int sizeOfTrainingSet = (int) 60 * featureVectorList.size() / 100;
            int sizeOfTestingSet = featureVectorList.size() - sizeOfTrainingSet;
            ArrayList<FeatureVectorObject> trainingSet = new ArrayList<>();
            ArrayList<FeatureVectorObject> testingSet = new ArrayList<>();

            int numOfTruePairInTrainingSet = sizeOfTrainingSet / 2;
            int numOfFalsePairInTrainingSet = sizeOfTrainingSet - numOfTruePairInTrainingSet;

            // Duyet qua cac truepair
            for (int i = 0; i < featureVectorList1.size(); i++) {
                if (i <= numOfTruePairInTrainingSet) {
                    trainingSet.add(featureVectorList1.get(i));
                } else {
                    testingSet.add(featureVectorList1.get(i));
                }
            }

            for (int i = 0; i < featureVectorList2.size(); i++) {
                if (i <= numOfFalsePairInTrainingSet) {
                    trainingSet.add(featureVectorList2.get(i));
                } else {
                    testingSet.add(featureVectorList2.get(i));
                }
            }

            // Initial Instance for the training & testing set
            System.out.println("TrainingSet size:" + trainingSet.size());
            System.out.println("TestingSet size:" + testingSet.size());
            Instances train = temp.formatFeatureVectorAsInstances(trainingSet, trainingSet.size()); // Train Data
            Instances test = temp.formatFeatureVectorAsInstances(testingSet, testingSet.size()); // Testing Data

            // Call Weka API to build classifier and evaluate
            // Using LibSVM
            Classifier clsSVM = new LibSVM();
            clsSVM.buildClassifier(train);
            Evaluation eval = new Evaluation(train);
            eval.evaluateModel(clsSVM, test);
            System.out.println(eval.toSummaryString("\nResults Using SVM \n======\n", false));
            
            // Using RandomForest
            Classifier clsRF = new RandomForest();
            clsRF.buildClassifier(train);
            eval.evaluateModel(clsRF, test);
            System.out.println(eval.toSummaryString("\nResults Using Random Forest \n======\n", false));
            
            // Using NaiveBayes
            Classifier clsNB = new RandomForest();
            clsNB.buildClassifier(train);
            eval.evaluateModel(clsNB, test);
            System.out.println(eval.toSummaryString("\nResults Using Random NaiveBayes \n======\n", false));
            
            // Using KNN
            Classifier clsKNN = new IBk();
            clsKNN.buildClassifier(train);
            eval.evaluateModel(clsKNN, test);
            System.out.println(eval.toSummaryString("\nResults Using KNN \n======\n", false));
            
            // Using C45            
            Classifier clsC45 = new J48();
            clsC45.buildClassifier(train);
            eval.evaluateModel(clsC45, test);
            System.out.println(eval.toSummaryString("\nResults Using C45 \n======\n", false));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("END...");
    }
}
