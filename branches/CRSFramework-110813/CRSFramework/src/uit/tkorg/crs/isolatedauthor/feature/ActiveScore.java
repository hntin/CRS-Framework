/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.isolatedauthor.feature;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import uit.tkorg.utility.TextFileUtility;

/**
 *
 * @author TinHuynh
 */
public class ActiveScore {

    private HashMap<Integer, Integer> _paperID_Year;
    private HashMap<Integer, ArrayList<Integer>> _paperID_AuthorID_List;
    private HashMap<Integer, ArrayList<Integer>> _authorID_PaperID_List;
    private String _file_AuthorID_PaperID;
    private String _file_PaperID_Year;
    private int _startYear;
    private int _currentYear;

    public ActiveScore(String file_AuthorID_PaperID, String file_PaperID_Year, int startYear, int currentYear) {
        _file_AuthorID_PaperID = file_AuthorID_PaperID;
        _file_PaperID_Year = file_PaperID_Year;
        _startYear = startYear;
        _currentYear = currentYear;
    }

    private void load_PaperID_Year() {
        try {
            _paperID_Year = new HashMap<>();
            FileInputStream fis = new FileInputStream(_file_PaperID_Year);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            int paperId;
            Integer year;
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split("\t");
                paperId = Integer.parseInt(tokens[0]);
                if (tokens.length <= 1) {
                    year = 0;
                } else {
                    year = Integer.parseInt(tokens[1]);
                }
                _paperID_Year.put(paperId, year);
            }
            bufferReader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void load_AuthorID_PaperID() {
        try {
            _authorID_PaperID_List = new HashMap<>();
            _paperID_AuthorID_List = new HashMap<>();
            FileInputStream fis = new FileInputStream(_file_AuthorID_PaperID);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            int authorId;
            int paperId;
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split(",");
                if (tokens.length == 2) {
                    authorId = Integer.parseInt(tokens[0]);
                    paperId = Integer.parseInt(tokens[1]);

                    ArrayList<Integer> listPaper = _authorID_PaperID_List.get(authorId);
                    if (listPaper == null) {
                        listPaper = new ArrayList<>();
                    }
                    listPaper.add(paperId);
                    _authorID_PaperID_List.put(authorId, listPaper);

                    ArrayList<Integer> listAuthor = _paperID_AuthorID_List.get(paperId);
                    if (listAuthor == null) {
                        listAuthor = new ArrayList<>();
                    }
                    listAuthor.add(authorId);
                    _paperID_AuthorID_List.put(paperId, listAuthor);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private HashMap<Integer, Float> calculateActiveScore() {
        HashMap<Integer, Float> activeScoreHM = new HashMap<>();
        int count = 0;
        for (int authorID : _authorID_PaperID_List.keySet()) {
            count++;
            if (count % 100 == 0) {
                System.out.println(count);
            }

            int minYear = Integer.MAX_VALUE;
            ArrayList<Integer> paperIDList = _authorID_PaperID_List.get(authorID);

            HashMap<Integer, Integer> year_NumOfPub = new HashMap<>();
            for (int paperID : paperIDList) {
                if (_paperID_Year.containsKey(paperID)) {
                    int paperYear = _paperID_Year.get(paperID);
                    if (paperYear != 0) {
                        int numOfPub = 0;
                        if (year_NumOfPub.containsKey(paperYear)) {
                            numOfPub = year_NumOfPub.get(paperYear);
                        }
                        numOfPub++;
                        year_NumOfPub.put(paperYear, numOfPub);

                        if (paperYear < minYear) {
                            minYear = paperYear;
                        }
                    }
                }
            }

            float activeScore = 0.f;
            for (int startYear = minYear; startYear <= _currentYear; startYear++) {
                if (year_NumOfPub.containsKey(startYear)) {
                    int num_Pub_In_Year = year_NumOfPub.get(startYear);
                    activeScore += (float) num_Pub_In_Year * (1 / Math.exp(_currentYear - startYear));
                }
            }

            activeScoreHM.put(authorID, activeScore);
        }

        return activeScoreHM;
    }

    private HashMap<Integer, Float> normalizeActiveScore(HashMap<Integer, Float> activeScoreHM) {
        HashMap<Integer, Float> activeScoreNormalizedHM = new HashMap<>();
        float minValue = Float.MAX_VALUE;
        float maxValue = Float.MIN_VALUE;
        float activeValue = 0.f;
        for (int authorID : activeScoreHM.keySet()) {
            activeValue = activeScoreHM.get(authorID);
            if (activeValue < minValue) {
                minValue = activeValue;
            }

            if (activeValue > maxValue) {
                maxValue = activeValue;
            }
        }

        float normalizedValue = 0.f;
        for (int authorID : activeScoreHM.keySet()) {
            activeValue = activeScoreHM.get(authorID);
            normalizedValue = (activeValue - minValue) / (maxValue - minValue);
            activeScoreNormalizedHM.put(authorID, normalizedValue);
        }

        return activeScoreNormalizedHM;
    }

    public static void main(String args[]) {
        ActiveScore activeScore = new ActiveScore(
                "C:\\CRS-Experiment\\Input\\MAS\\Input2\\[TrainingData]AuthorID_PaperID_Before_2005.txt",
                "C:\\CRS-Experiment\\Input\\MAS\\Input2\\Link-Net\\[TrainingData]PaperID_Year_1995_2005.txt",
                1995, 2005);

        activeScore.load_PaperID_Year();
        activeScore.load_AuthorID_PaperID();
        HashMap<Integer, Float> activeScoreHM = activeScore.calculateActiveScore();
        activeScoreHM = activeScore.normalizeActiveScore(activeScoreHM);
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("AuthorID" + "\t" + "ActiveScore" + "\n");
        for (int authorID : activeScoreHM.keySet()) {
            strBuff.append(authorID + "\t" + activeScoreHM.get(authorID) + "\n");
        }
        TextFileUtility.writeTextFile("C:\\CRS-Experiment\\Output\\MAS\\IsolatedAuthor\\ActiveScore\\ActiveScore.txt", strBuff.toString());
        System.out.println("DONE");

//
//        int minYear = 2000;
//        int _currentYear = 2005;
//        float score = 0.f;
//        for (int startYear = minYear; startYear <= _currentYear; startYear++) {
//             score +=  (float)1*(1 / Math.exp(_currentYear - startYear));
//             System.out.println(":" + score);
//        }
//        System.out.println(":" + score);
    }
}
