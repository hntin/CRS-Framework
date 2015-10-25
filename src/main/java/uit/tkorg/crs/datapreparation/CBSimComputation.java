/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.datapreparation;

import ir.vsr.HashMapVector;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.apache.mahout.common.Pair;
import uit.tkorg.crs.constant.Constant;
import uit.tkorg.crs.datapreparation.CBFAuthorFVComputation;
import uit.tkorg.crs.model.Author;
import uit.tkorg.crs.model.Paper;
import uit.tkorg.crs.utility.BinaryFileUtility;
import uit.tkorg.crs.utility.DatabaseTool;
import uit.tkorg.crs.utility.MahoutFile;
import uit.tkorg.crs.utility.NumericUtility;
import uit.tkorg.crs.utility.TextFileUtility;

public class CBSimComputation {
    
    private CBSimComputation() {}
    
    /**
     *
     * @param authorID
     * @param year
     * @return
     * @throws Exception
     */
    public static HashMap<String, Paper> readPaperList(int authorID, int year) throws Exception {
        HashMap<String, Paper> papers = new HashMap();
        DatabaseTool db = new DatabaseTool("jdbc:mysql://localhost:3306/test","root","thuc1980");
        db.connect();
        ResultSet rs = db.getPapersByAuthor(authorID,year);
        while (rs.next()) {
            Paper paper = new Paper();
            String paperId = rs.getInt(1) + "";
            paper.setPaperId(paperId);
            paper.setPaperTitle(rs.getString(2));
            paper.setYear(rs.getInt(3));
            papers.put(paperId, paper);
        }
        db.disconnect();
        return papers;
    }
    
    public static HashMap<String, Paper> readPaperList(int authorID, int year, String papersFolder) throws Exception {
        HashMap<String, Paper> papers = new HashMap();
        
        List<String> paperList = TextFileUtility.getPathFile(new File(papersFolder));
        
        for (int i = 0; i < paperList.size(); i++) {
            //
        }
        return papers;
    }

    private static void createTestDatabase() {
        String filePath = "input/4.txt";
        String sql = "INSERT INTO paper (idPaper, title, abstract, year) values (?, ?, ?, ?)";
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/test", "root", "thuc1980");
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, 4);
            statement.setString(2, "Paper 4");
            InputStream inputStream = new FileInputStream(new File(filePath));
            statement.setBlob(3, inputStream);
            statement.setInt(4, 2015);
            int row = statement.executeUpdate();
            if (row > 0) {
                System.out.println("A paper was inserted");
            }
            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void buildAuthorProfile(int authorId,int year) {
        HashMap<String, Paper> papers = null;
        HashMapVector fv = null;
        try {
            papers = readPaperList(authorId,year);
        } catch (Exception ex) {
            Logger.getLogger(CBSimComputation.class.getName()).log(Level.SEVERE, null, ex);
        }
        List<String> paperList = new ArrayList<String>(papers.keySet());
        Author author = new Author();
        author.setAuthorId("1");
        author.setPaperList(paperList);
        try {
            fv = CBFAuthorFVComputation.computeAuthorFV(author, papers, 1, 0.5);
        } catch (Exception ex) {
            Logger.getLogger(CBSimComputation.class.getName()).log(Level.SEVERE, null, ex);
        }
        author.setFeatureVector(fv);
    }

    /**
     * read author list from posivtive/negative sample
     * @param type 1: positive; 0; negative
     * @return list of author existing in the positive/negative sample
     */
    public static LinkedHashSet<Integer> readAuthorList(String sampleFile){
        
        LinkedHashSet<Integer> authorList = new LinkedHashSet<Integer>();
        ArrayList<Pair<Integer,Integer>> authorPairs = readSample(sampleFile);
        
        for (int i = 0; i < authorPairs.size(); i++){
            Pair p = authorPairs.get(i);
            Integer idAuthor1 = (Integer)p.getFirst();
            Integer idAuthor2 = (Integer)p.getSecond();
            authorList.add(idAuthor1);
            authorList.add(idAuthor2);
        }
        return authorList;
    }
    
    /**
     *read positive sample
     * @param dataFile, type: 1 for positive sample
     */
    public static ArrayList<Pair<Integer,Integer>> readSample(String dataFile){
        final String REGEX = "\\D";
        Pattern p = Pattern.compile(REGEX);
        ArrayList<Pair<Integer,Integer>> listOfPairs = new ArrayList<Pair<Integer,Integer>>();
        
        try {
            FileInputStream fis = new FileInputStream(dataFile);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine(); // skip the first line
            String line = null;
            
            while ((line = bufferReader.readLine()) != null) {
                String[] elements = p.split(line.trim());

                if (elements.length > 3 || elements.length < 2) {
                    continue;
                }
                int author1 = Integer.parseInt(elements[1]);
                int author2 = Integer.parseInt(elements[2]);
                Pair pair = new Pair(new Integer(author1),new Integer(author2));
                listOfPairs.add(pair);
            }
            bufferReader.close();
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listOfPairs;
    }
    
    /**
     * ghi vao sampleFile do giong nhau giua 2 tac gia dua tren do do cosine
     * @param sampleFile chua mau am/duong
     * @param year nam hien tai dung de tinh profile
     */
    public static void computeCosine(String sampleFile, int year, String outputFile){
        try {
            HashMap<String, Paper> papers = readPaperList(1,year);//lay ds bai bao tu year ve truoc
            List<String> paperList = new ArrayList<String>(papers.keySet());
            
            LinkedHashSet<Integer> authorList = readAuthorList(sampleFile);
            Iterator<Integer> ir = authorList.iterator();
            HashMap<String, Author> authors = new HashMap<String, Author>();
            //doc thong tin cac tac gia nam trong authorList tu sequence file
            while (ir.hasNext()){
                Integer idAuthor = ir.next();
                Author author = new Author();
                author.setAuthorId(idAuthor.toString());
                author.setPaperList(paperList);
                HashMapVector fv = CBFAuthorFVComputation.computeAuthorFV(author, papers, 1, 0.5);
                author.setFeatureVector(fv);
                authors.put(author.getAuthorId(), author);
            }
            //tinh do do cosine cho tung cap tac gia trong mau duong/am va ghi ra file
            ArrayList<Pair<Integer,Integer>> listOfPairs = readSample(sampleFile);
            StringBuilder content = new StringBuilder();
            content.append("(idAuhtor1,idAuthor2) \t cosine\n");
            for (int i = 0; i < listOfPairs.size(); i++){
                Pair<Integer,Integer> pair = listOfPairs.get(i);
                Author author1 = authors.get(pair.getFirst().toString());
                Author author2 = authors.get(pair.getSecond().toString());
                double cosine = author1.getFeatureVector().cosineTo(author2.getFeatureVector());
                String line = "("+ pair.getFirst() + "," + pair.getSecond() + ")" + "\t" + cosine;
                content.append(line + "\n");
            }
            TextFileUtility.writeTextFile(outputFile, content.toString());
        } catch (Exception ex) {
            Logger.getLogger(CBSimComputation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Test.
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
//        createTestDatabase();
//        CBFPaperFVComputation.vectorzie("/Users/thucnt/temp/input/papers", "/Users/thucnt/temp/output/TFIDF/");
//        CBFPaperFVComputation.vectorzie(2005, "output/TFIDF/");
        
        computeCosine("/Users/thucnt/temp/input/positive.txt",2015,"/Users/thucnt/temp/output/positive.txt");
    }
}
