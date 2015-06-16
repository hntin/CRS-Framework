package uit.tkorg.crs.isolatedauthor.feature;

import java.util.ArrayList;

/**
 *
 * @author TinHuynh
 */
public class TrainTestDataset {

    public static void main(String args[]) {
        System.out.println("START ...");
        try {
            // Loading data from XML file and build the feature vector list
            ArrayList<FeatureVectorObject> positiveVectors = FeatureVectorList.buildingFeatureVectorListFromXMLFile(
                    "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\TruePair1.xml");
            ArrayList<FeatureVectorObject> negativeVectors = FeatureVectorList.buildingFeatureVectorListFromXMLFile(
                    "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\FalsePair1.xml");

            buildingTrainTestDataset(positiveVectors, negativeVectors);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void buildingTrainTestDataset(ArrayList<FeatureVectorObject> positiveVectors, ArrayList<FeatureVectorObject> negativeVectors) {
        int numberOfPostiveForTrain = positiveVectors.size() / 2;
        int numberOfNegativeForTrain = negativeVectors.size() / 2;

        ArrayList<FeatureVectorObject> trainingVectors = new ArrayList<>();
        ArrayList<FeatureVectorObject> testingVectors = new ArrayList<>();

        for (int i = 0; i < positiveVectors.size(); i++) {
            FeatureVectorObject fectureVectorObj = positiveVectors.get(i);
            if (i < numberOfPostiveForTrain) {
                trainingVectors.add(fectureVectorObj);
            } else {
                testingVectors.add(fectureVectorObj);
            }
        }

        for (int i = 0; i < negativeVectors.size(); i++) {
            FeatureVectorObject fectureVectorObj = negativeVectors.get(i);
            if (i < numberOfNegativeForTrain) {
                trainingVectors.add(fectureVectorObj);
            } else {
                testingVectors.add(fectureVectorObj);
            }
        }
        
        FeatureVectorList.writingFeatureVectorListToTextFile(
                trainingVectors, 
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\TrainDataset.txt",
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\TrainDatasetMapping.txt");
        FeatureVectorList.writingFeatureVectorListToTextFile(
                testingVectors, 
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\TestDataset.txt",
                "C:\\CRS-Experiment\\MAS\\ColdStart\\Input\\Input1\\TestDatasetMapping.txt");
    }
}
