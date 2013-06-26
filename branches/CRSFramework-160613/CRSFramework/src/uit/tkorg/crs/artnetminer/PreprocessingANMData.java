/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.artnetminer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Dao
 */
public class PreprocessingANMData {

    private String _dataPath;
    private int _startTrainingYear;
    private int _endTrainingYear;
    private int _startNearTestingYear;
    private int _endNearTestingYear;
    private int _startFarTestingYear;
    private int _endFarTestingYear;
    private String _Training_AuthorId_PaperId;
    private String _Training_PaperId_Year;
    private String _NearTesting_AuthorId_PaperId;
    private String _NearTesting_PaperId_Year;
    private String _FarTesting_AuthorId_PaperId;
    private String _FarTesting_PaperId_Year;
    private HashMap<String, Integer> _authorIdData;
    private HashMap<Integer, Integer> _paperYearData;
    private HashMap<Integer, ArrayList<Integer>> _authorPaper;
    private HashMap<Integer, ArrayList<Integer>> _paperAuthor;

    public PreprocessingANMData(String dataPath,
            int startTrainingYear,
            int endTrainingYear,
            int startNearTesingYear,
            int endNearTestingYear,
            int startFarTestingYear,
            int endFarTestingYear,
            String Training_AuthorId_PaperId,
            String Training_PaperId_Year,
            String NearTesting_AuthorId_PaperId,
            String NearTesting_PaperId_Year,
            String FarTesting_AuthorId_PaperId,
            String FarTesting_PaperId_Year) {
        _dataPath = dataPath;
        _startTrainingYear = startTrainingYear;
        _endTrainingYear = endTrainingYear;
        _startNearTestingYear = startNearTesingYear;
        _endNearTestingYear = endNearTestingYear;
        _startFarTestingYear = startFarTestingYear;
        _endFarTestingYear = endFarTestingYear;

        _Training_AuthorId_PaperId = Training_AuthorId_PaperId;
        _Training_PaperId_Year = Training_PaperId_Year;
        _NearTesting_AuthorId_PaperId = NearTesting_AuthorId_PaperId;
        _NearTesting_PaperId_Year = NearTesting_PaperId_Year;
        _FarTesting_AuthorId_PaperId = FarTesting_AuthorId_PaperId;
        _FarTesting_PaperId_Year = FarTesting_PaperId_Year;

        _authorIdData = new HashMap<>();
        _paperYearData = new HashMap<>();
        _authorPaper = new HashMap<>();
        _paperAuthor = new HashMap<>();
    }

    public void processing() {
        try {
            FileInputStream fis = new FileInputStream(_dataPath);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            String line = null;
            String[] tokens;
            String listAuthorName;
            int year;
            int idPaper = 0;
            int idAuthor = 0;
            while ((line = bufferReader.readLine()) != null) {
                tokens = line.split("\t");
                if (tokens.length == 5) {
                    listAuthorName = tokens[2];
                    year = Integer.parseInt(tokens[3]);

                    idPaper++;

                    _paperYearData.put(idPaper, year);

                    String[] authorNames = listAuthorName.split(",");
                    for (String authorName : authorNames) {
                        Integer id = _authorIdData.get(authorName);
                        if (id == null) {
                            idAuthor++;
                            id = idAuthor;
                            _authorIdData.put(authorName, id);
                        }

                        ArrayList<Integer> listPaper = _authorPaper.get(id);
                        if (listPaper == null) {
                            listPaper = new ArrayList<>();
                        }
                        listPaper.add(idPaper);
                        _authorPaper.put(id, listPaper);

                        ArrayList<Integer> listAuthor = _paperAuthor.get(idPaper);
                        if (listAuthor == null) {
                            listAuthor = new ArrayList<>();
                        }
                        listAuthor.add(id);
                        _paperAuthor.put(idPaper, listAuthor);
                    }
                }
            }
            bufferReader.close();
        } catch (Exception e) {
        }

        write_AuthorID_PaperID(getTraining_AuthorID_PaperID(), _Training_AuthorId_PaperId);
        write_AuthorID_PaperID(getNearTesting_AuthorID_PaperID(), _NearTesting_AuthorId_PaperId);
        write_AuthorID_PaperID(getFarTesting_AuthorID_PaperID(), _FarTesting_AuthorId_PaperId);

        write_PaperID_Year(getTraining_PaperID_Year(), _Training_PaperId_Year);
        write_PaperID_Year(getNearTesting_PaperID_Year(), _NearTesting_PaperId_Year);
        write_PaperID_Year(getFarTesting_PaperID_Year(), _FarTesting_PaperId_Year);
    }

    public void write_AuthorID_PaperID(HashMap<Integer, ArrayList<Integer>> data, String Path) {
        try {
            FileOutputStream fos = new FileOutputStream(Path);
            Writer writer = new OutputStreamWriter(fos, "UTF8");
            writer.write("AuthorId" + "\t" + "PaperId" + "\n");
            for (Integer idAuthor : data.keySet()) {
                for (Integer idPaper : data.get(idAuthor)) {
                    writer.write(String.valueOf(idAuthor) + "\t" + String.valueOf(idPaper) + "\n");
                }
            }
            writer.close();
        } catch (Exception e) {
        }
    }

    public void write_PaperID_Year(HashMap<Integer, Integer> data, String Path) {
        try {
            FileOutputStream fos = new FileOutputStream(Path);
            Writer writer = new OutputStreamWriter(fos, "UTF8");
            writer.write("PaperId" + "\t" + "Year" + "\n");
            for (Integer idPaper : data.keySet()) {
                writer.write(String.valueOf(idPaper) + "\t" + String.valueOf(data.get(idPaper)) + "\n");
            }
            writer.close();
        } catch (Exception e) {
        }
    }

    private HashMap<Integer, ArrayList<Integer>> getTraining_AuthorID_PaperID() {
        HashMap<Integer, ArrayList<Integer>> result = new HashMap<>();
        for (Integer idPaper : _paperYearData.keySet()) {
            if (_paperYearData.get(idPaper) >= _startTrainingYear && _paperYearData.get(idPaper) <= _endTrainingYear) {
                ArrayList<Integer> listAuthor = _paperAuthor.get(idPaper);
                for (Integer idAuthor : listAuthor) {
                    ArrayList<Integer> listPaper = result.get(idAuthor);
                    if (listPaper == null) {
                        listPaper = new ArrayList<>();
                    }
                    listPaper.add(idPaper);
                    result.put(idAuthor, listPaper);
                }
            }
        }
        return result;
    }

    private HashMap<Integer, Integer> getTraining_PaperID_Year() {
        HashMap<Integer, Integer> result = new HashMap<>();
        for (Integer idPaper : _paperYearData.keySet()) {
            if (_paperYearData.get(idPaper) >= _startTrainingYear && _paperYearData.get(idPaper) <= _endTrainingYear) {
                result.put(idPaper, _paperYearData.get(idPaper));
            }
        }
        return result;
    }

    private HashMap<Integer, ArrayList<Integer>> getNearTesting_AuthorID_PaperID() {
        HashMap<Integer, ArrayList<Integer>> result = new HashMap<>();
        for (Integer idPaper : _paperYearData.keySet()) {
            if (_paperYearData.get(idPaper) >= _startNearTestingYear && _paperYearData.get(idPaper) <= _endNearTestingYear) {
                ArrayList<Integer> listAuthor = _paperAuthor.get(idPaper);
                for (Integer idAuthor : listAuthor) {
                    ArrayList<Integer> listPaper = result.get(idAuthor);
                    if (listPaper == null) {
                        listPaper = new ArrayList<>();
                    }
                    listPaper.add(idPaper);
                    result.put(idAuthor, listPaper);
                }
            }
        }
        return result;
    }

    private HashMap<Integer, Integer> getNearTesting_PaperID_Year() {
        HashMap<Integer, Integer> result = new HashMap<>();
        for (Integer idPaper : _paperYearData.keySet()) {
            if (_paperYearData.get(idPaper) >= _startNearTestingYear && _paperYearData.get(idPaper) <= _endNearTestingYear) {
                result.put(idPaper, _paperYearData.get(idPaper));
            }
        }
        return result;
    }

    private HashMap<Integer, ArrayList<Integer>> getFarTesting_AuthorID_PaperID() {
        HashMap<Integer, ArrayList<Integer>> result = new HashMap<>();
        for (Integer idPaper : _paperYearData.keySet()) {
            if (_paperYearData.get(idPaper) >= _startFarTestingYear && _paperYearData.get(idPaper) <= _endFarTestingYear) {
                ArrayList<Integer> listAuthor = _paperAuthor.get(idPaper);
                for (Integer idAuthor : listAuthor) {
                    ArrayList<Integer> listPaper = result.get(idAuthor);
                    if (listPaper == null) {
                        listPaper = new ArrayList<>();
                    }
                    listPaper.add(idPaper);
                    result.put(idAuthor, listPaper);
                }
            }
        }
        return result;
    }

    private HashMap<Integer, Integer> getFarTesting_PaperID_Year() {
        HashMap<Integer, Integer> result = new HashMap<>();
        for (Integer idPaper : _paperYearData.keySet()) {
            if (_paperYearData.get(idPaper) >= _startFarTestingYear && _paperYearData.get(idPaper) <= _endFarTestingYear) {
                result.put(idPaper, _paperYearData.get(idPaper));
            }
        }
        return result;
    }
}
