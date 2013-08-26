package uit.tkorg.crs.isolatedauthor.feature;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import uit.tkorg.utility.XMLFileUtility;
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

    public List<FeatureVectorObject> buildingFeatureVectorListFromXMLFile(String file_TrueOrFalsePairs) {
        ArrayList<FeatureVectorObject> featureVectorList = new ArrayList<>();

        // Loadding file_TruePairs
        Document domObject = XMLFileUtility.readXMLFile(file_TrueOrFalsePairs);
        Element root = domObject.getRootElement();
        // iterate through child elements of root with element name "pair"
        for (Iterator i = root.elementIterator("pair"); i.hasNext();) {
            FeatureVectorObject featureVectorObject = new FeatureVectorObject();

            Element pairElement = (Element) i.next();
            Node contentSimNode = pairElement.selectSingleNode("ContentSim");
            float contentSimValue = Float.parseFloat(contentSimNode.getText());
            Node orgRSSNode = pairElement.selectSingleNode("OrgRSS");
            float orgRSSValue = Float.parseFloat(orgRSSNode.getText());
            
            Node coAuthorNode = pairElement.selectSingleNode("CoAuthor");
            Node importantRateNode = coAuthorNode.selectSingleNode("ImportantRate");
            Node activeScoreNode = coAuthorNode.selectSingleNode("ActiveScore");
            float importantRateValue = Float.parseFloat(importantRateNode.getText());
            float activeScoreValue = Float.parseFloat(activeScoreNode.getText());
            
            featureVectorObject.setContentSimValue(contentSimValue);
            featureVectorObject.setOrgRSSValue(orgRSSValue);
            featureVectorObject.setImportantRateValue(importantRateValue);
            featureVectorObject.setActiveScoreValue(activeScoreValue);
            
            Node labelNode = pairElement.selectSingleNode("tag");
            if (labelNode.getText().equalsIgnoreCase("1"))
                featureVectorObject.setLabelValue("YES");
            else 
                featureVectorObject.setLabelValue("NO");
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
        // Loading data from XML file and build the feature vector list
        FeatureVectorList temp = new FeatureVectorList();
        List<FeatureVectorObject> featureVectorList1 = temp.buildingFeatureVectorListFromXMLFile(
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\TruePair1.xml");
        
        List<FeatureVectorObject> featureVectorList2 = temp.buildingFeatureVectorListFromXMLFile(
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\FalsePair1.xml");

        // Tach featureVectorList thanh tap train va tap test 

        // Dua cac Instances cho tap train vao

        // Dua cac Instances cho tap test vao

        // Call Weka API to build classifier and evaluate

//        // TODO code application logic here
//        Instances train = BuildVector.buildVector(listTrain, 100); // Dữ liệu train
//        Instances test =  BuildVector.buildVector(listTest, 50); // Dữ liệu Test
//        // train classifier Gọi thuật toán phân lớp 
//        
//        Classifier cls = new J48(); //Gọi thuật toán phân lớp 
//        cls.buildClassifier(train); //Build model đối với dữ liệu train
//        
//        // evaluate classifier and print some statistics
//        Evaluation eval = new Evaluation(train); //Chạy đánh giá model xây dựng
//        eval.evaluateModel(cls, test);
//        System.out.println(eval.toSummaryString("\nResults\n======\n", false));

    }
}
