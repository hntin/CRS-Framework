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
public class ImportDataController {
    private static ImportDataController instance;
    private AuthorGraph authorGraph = AuthorGraph.getInstance();

    public static ImportDataController getInstance() {
        if (instance == null) {
            instance = new ImportDataController();
        }
        return instance;
    }
    
    public void load_AuthorID_PaperID(String trainingFile_AuthorID_PaperID) {
        authorGraph.loadTrainingData_AuthorID_PaperID_File(trainingFile_AuthorID_PaperID);
    }
    
    public void load_PaperID_Year(String trainingFile_PaperID_Year) {
        authorGraph.loadTrainingData_PaperID_Year_File(trainingFile_PaperID_Year);
    }
    
    public void load_Ground_Truth_File(String testingFile_AuthorID_PaperID){
        authorGraph.loadTestingData_GroundTruthFile(testingFile_AuthorID_PaperID);
    }
    
    public void loadInputRandomAuthor(String inputAuthorFile) {
        try {
            if (authorGraph.listRandomAuthor == null || authorGraph.listRandomAuthor.size() == 0) {
                authorGraph.listRandomAuthor = new HashMap<>();
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
                            authorGraph.listRandomAuthor.put(authorId, groupLMD);
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
}
