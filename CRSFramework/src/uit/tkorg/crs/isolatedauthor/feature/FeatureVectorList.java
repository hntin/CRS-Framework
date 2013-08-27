package uit.tkorg.crs.isolatedauthor.feature;

import java.awt.BorderLayout;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import uit.tkorg.utility.TextFileUtility;
import uit.tkorg.utility.XMLFileUtility;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.ThresholdCurve;
import weka.classifiers.trees.J48;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.lazy.IBk;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.gui.visualize.PlotData2D;
import weka.gui.visualize.ThresholdVisualizePanel;

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
//                featureVectorObject.setActiveScoreValue(activeScoreValue);

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

            Instances data = temp.formatFeatureVectorAsInstances(featureVectorList, featureVectorList.size());
            data.setClassIndex(data.numAttributes() - 1);

            // train classifier
            Classifier cslSVM = new LibSVM();
            Evaluation eval = new Evaluation(data);
            eval.crossValidateModel(cslSVM, data, 10, new Random(1));

            StringBuffer strBuff = new StringBuffer();
            strBuff.append("Feature" + "\t" + "Class" + "\t" + "Correctly Classified Instances" + "\t" + 
                    "Incorrectly Classified Instances" + "\t" + "Precision" + "\t" + "Recall" + "\t" + "F-Measure" + "\n");
            strBuff.append("ContentSim & OrgRSS & I.Rate & A.Score" + "\t" + "HasLink (Yes)" + "\t" + eval.pctCorrect() + "\t" + eval.pctIncorrect() + "\t" + eval.precision(0)*100 + "\t" + eval.recall(0)*100 + "\t" + eval.fMeasure(0)*100 + "\n");
            strBuff.append("" + "\t" + "HasLink (No)" + "\t" + eval.pctCorrect() + "\t" + eval.pctIncorrect() + "\t" + eval.precision(1)*100 + "\t" + eval.recall(1)*100 + "\t" + eval.fMeasure(1)*100 + "\n");
            strBuff.append("" + "\t" + "Weighted Average" + "\t" + eval.pctCorrect() + "\t" + eval.pctIncorrect() + "\t" + eval.weightedPrecision()*100 + "\t" + eval.weightedRecall()*100 + "\t" + eval.weightedFMeasure()*100 + "\n");
            
            System.out.println(eval.toSummaryString("\nResults Using C45 \n======\n", false));
            System.out.println(eval.weightedPrecision());
            System.out.println(eval.weightedRecall());
            System.out.println(eval.weightedFMeasure());
            TextFileUtility.writeTextFile("C:\\CRS-Experiment\\MAS\\ColdStart\\Output\\4_Features_Result.txt", strBuff.toString());

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        System.out.println("END...");
    }
}
