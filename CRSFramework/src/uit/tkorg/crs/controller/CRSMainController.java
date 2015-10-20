package uit.tkorg.crs.controller;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import uit.tkorg.crs.model.AuthorGraph;

/**
 *
 * @author Huynh Ngoc Tin
 */
public class CRSMainController {
    private AuthorGraph authorGraph = AuthorGraph.getInstance();
    private HashMap<Integer, String> listRandomAuthor;
    
    public void loadDataIntoModel() {
       // coAuthorGraph.LoadTrainingData(_training_PaperId_AuthorIdPath, _training_PaperId_YearPath);
       // coAuthorGraph.LoadTestingData(_testing_PaperId_Year_NFPath, _testing_PaperId_Year_FFPath);
    }
    
    public void runRecommendation() {
        
    }
    
    public void runEvaluation() {
        
    }
    
    public void load_AuthorID_PaperID(String trainingFile_AuthorID_PaperID) {
        authorGraph.loadTrainingData_AuthorID_PaperID_File(trainingFile_AuthorID_PaperID);
    }
    
    public void load_PaperID_Year(String trainingFile_PaperID_Year) {
        authorGraph.loadTrainingData_AuthorID_PaperID_File(trainingFile_PaperID_Year);
    }
    
    public void load_Ground_Truth_File(String testingFile_AuthorID_PaperID){
        authorGraph.loadTestingData_GroundTruthFile(testingFile_AuthorID_PaperID);
    }
    
    public void loadInputRandomAuthor(String inputAuthorFile) {
        try {
            if (listRandomAuthor == null || listRandomAuthor.size() == 0) {
                listRandomAuthor = new HashMap<>();
                // <editor-fold defaultstate="collapsed" desc="Load Author">
                try {
                    FileInputStream fis = new FileInputStream(inputAuthorFile);
                    Reader reader = new InputStreamReader(fis, "UTF8");
                    BufferedReader bufferReader = new BufferedReader(reader);
                    bufferReader.readLine();
                    String line = null;
                    String[] tokens;
                    String groupLMD;
                    int authorId;
                    while ((line = bufferReader.readLine()) != null) {
                        if (!line.equals("")) {
                            tokens = line.split("\t");
                            authorId = Integer.parseInt(tokens[0]);
                            if (tokens.length <= 1) {
                                groupLMD = "";
                            } else {
                                groupLMD = tokens[1];
                            }
                            listRandomAuthor.put(authorId, groupLMD);
                        }
                    }
                    bufferReader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // </editor-fold>
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    public static void main(String args[]) {
        // Import Data into AuthorGraph
        ImportDataController importDataController = new ImportDataController();
        importDataController.load_AuthorID_PaperID("/1.CRS-ExperimetalData/TrainingData/AuthorID_PaperID_2001_2003.txt");
        importDataController.load_PaperID_Year("/1.CRS-ExperimetalData/TrainingData/PaperID_Year_2001_2003.txt");
        
        
    }
}
