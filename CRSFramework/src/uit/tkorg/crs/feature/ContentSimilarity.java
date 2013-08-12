/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.feature;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import uit.tkorg.crs.method.content.TFIDF;
import uit.tkorg.utility.TextFileUtility;

/**
 *
 * @author TinHuynh
 */
public class ContentSimilarity {

    private HashMap<Integer, String> _listAuthorRandom;
    private String _author_Publication_Content_File;
    private String _input_Author_List_File;
    private String _resultPath;

    public ContentSimilarity(String author_Publication_Content_File, String input_Author_List_File, String resultPath) {
        _author_Publication_Content_File = author_Publication_Content_File;
        _input_Author_List_File = input_Author_List_File;
        _resultPath = resultPath;
    }

    public static enum ContentSimOption {

        TFIDF,
        KLDIVERGENCE
    }

    private void loadInputAuthorList() {
        try {
            if (_listAuthorRandom == null || _listAuthorRandom.size() == 0) {
                _listAuthorRandom = new HashMap<>();
                // <editor-fold defaultstate="collapsed" desc="Load Author">
                try {
                    FileInputStream fis = new FileInputStream(_input_Author_List_File);
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
                            _listAuthorRandom.put(authorId, groupLMD);
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

    public void processContentSimilarity(ContentSimOption option) {
        HashMap<Integer, HashMap<Integer, Float>> tfidfResult = null;
        TFIDF tfidfMethod = new TFIDF();
        if (option == ContentSimOption.TFIDF) {
            tfidfResult = tfidfMethod.process(_author_Publication_Content_File, _listAuthorRandom);
        } else {
            if (option == ContentSimOption.KLDIVERGENCE) {
            }
        }

        // Write result to text files
        for (int authorID : tfidfResult.keySet()) {
            StringBuffer strBuff = new StringBuffer();
            HashMap<Integer, Float> tfidfValueHM =  tfidfResult.get(authorID);
            TextFileUtility.writeTextFile(_resultPath + "\\" + authorID + ".txt", tfidfValueHM);
        }
    }

    public static void main(String args[]) {
        ContentSimilarity contentSim = new ContentSimilarity(
                "C:\\CRS-Experiment\\Sampledata\\LDATest\\MALLET-LDA\\Output\\CRS-InputParallelLDA.txt", 
                "C:\\CRS-Experiment\\Sampledata\\LDATest\\MALLET-LDA\\Output\\ListRandomAuthor_.txt", 
                "C:\\CRS-Experiment\\Sampledata\\LDATest\\MALLET-LDA\\Output\\ContentSimilarity");
        contentSim.loadInputAuthorList();
        contentSim.processContentSimilarity(ContentSimOption.TFIDF);
    }
}
