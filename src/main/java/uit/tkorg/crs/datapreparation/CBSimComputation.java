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

public class CBSimComputation extends FeatureComputation{
    
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
    
    /**
     *
     * @param paperListFile
     * @return
     * @throws Exception
     */
    public static HashMap<String, List<Paper>> readPaperIdByAuthor(String paperListFile) throws Exception {
        HashMap<String, List<Paper>> papers = new HashMap();
        final String REGEX = "\\D";
        
        System.out.println("Bat dau doc danh sach bai bao tu file text");
        Scanner input = new Scanner(new FileReader(paperListFile));
        input.nextLine();//bo dong dau tien
        while (input.hasNext()){
            String line = input.nextLine().trim();
            String[] tokens = line.split(REGEX);
            String key = tokens[0];
            String value = tokens[1];
            if (papers.containsKey(key)){
                List<Paper> paperList = papers.get(key);
                Paper p = new Paper();
                p.setPaperId(value);
                paperList.add(p);
                papers.replace(key, paperList);
            }
            else{
                List<Paper> paperList = new ArrayList<Paper>();
                Paper p = new Paper();
                p.setPaperId(value);
                paperList.add(p);
                papers.put(key, paperList);
            }
        }
        //before 2001
//        input = new Scanner(new FileReader("D:\\1.CRS-Experiment\\MLData\\TrainingData\\AuthorID_PaperID_Before_2001.txt"));
//        input.nextLine();//bo dong dau tien
//        while (input.hasNext()){
//            String line = input.nextLine().trim();
//            String[] tokens = line.split(REGEX);
//            String key = tokens[0];
//            String value = tokens[1];
//            if (papers.containsKey(key)){
//                List<Paper> paperList = papers.get(key);
//                Paper p = new Paper();
//                p.setPaperId(value);
//                paperList.add(p);
//                papers.replace(key, paperList);
//            }
//            else{
//                List<Paper> paperList = new ArrayList<Paper>();
//                Paper p = new Paper();
//                p.setPaperId(value);
//                paperList.add(p);
//                papers.put(key, paperList);
//            }
//        }
        System.out.println("So tac gia da co bai bao: " + papers.size());
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
     */
    public static void computeCosine(int year, String sampleFile, String outputFile){
        //doc danh sach tac gia tu mau am/duong
        LinkedHashSet<Integer> authorList = readAuthorList(sampleFile);
        //Lay danh sach bai bao cua cac tac gia trong mau am/duong
        HashMap<Integer,List<String>> authorPaperList = new HashMap<Integer,List<String>>();
        HashMap<String,Paper> papers = new HashMap<String,Paper>();
        readAuthorPaperList(authorList,2003,authorPaperList,papers);//tra ket qua thong qua 2 tham so cuoi
        try {
//            HashMap<String,Integer> paperIdYear = readPaperIdByYear(2003);
//            Collection c = authorPaperList.values();
//            Iterator itr = c.iterator();
            
            //Tinh FV cho tat ca cac tac gia
            HashMap<String, Author> authors = new HashMap<String, Author>();
            Iterator<Integer> ir = authorList.iterator();
            while (ir.hasNext()){
                Integer idAuthor = ir.next();
                Author author = new Author();
                author.setAuthorId(idAuthor.toString());
                List<String> paperList = authorPaperList.get(idAuthor);
                author.setPaperList(paperList);
//                HashMapVector fv = CBFAuthorFVComputation.computeAuthorFV(author, papers, 1, 0.5);
//                author.setFeatureVector(fv);
                authors.put(author.getAuthorId(), author);
            }
            CBFPaperFVComputation.readTFIDFFromMahoutFile(papers,"D:\\1.CRS-Experiment\\TFIDF\\2003\\");
            CBFAuthorFVComputation.computeFVForAllAuthors(authors, papers, 1, 0.5);
            test(authors, papers);
            //tinh do do cosine cho tung cap tac gia trong mau duong/am va ghi ra file
            ArrayList<Pair<Integer,Integer>> listOfPairs = readSample(sampleFile);
            StringBuilder content = new StringBuilder();
            content.append("(idAuhtor1,idAuthor2) \t cosine\n");
            for (int i = 0; i < listOfPairs.size(); i++){
                Pair<Integer,Integer> pair = listOfPairs.get(i);
                Author author1 = authors.get(pair.getFirst().toString());
                Author author2 = authors.get(pair.getSecond().toString());
                HashMapVector fv1 = author1.getFeatureVector();
                HashMapVector fv2 = author2.getFeatureVector();
                double cosine = fv1.cosineTo(fv2);
                String line = "("+ pair.getFirst() + "," + pair.getSecond() + ")" + "\t" + cosine;
                content.append(line + "\n");
            }
            TextFileUtility.writeTextFile(outputFile, content.toString());
        } catch (Exception ex) {
            Logger.getLogger(CBSimComputation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void computeFeatureValues(String outputFile){
        //doc danh sach tac gia tu mau am/duong
        this.
    }
    
    private static void readAuthorPaperList(Set<Integer> authorList, int year, 
            HashMap<Integer,List<String>> authorPaperList, //output parameter
            HashMap<String,Paper> papers){//output parameter
        DatabaseTool db = new DatabaseTool();
        db.connect();
        Iterator<Integer> ir = authorList.iterator();
        while (ir.hasNext()){//lay danh sach bai bao cua 1 tac gia
            int authorId = ir.next().intValue();
            System.out.println("Lay danh sach bai bao cua tac gia " + authorId);
            List<String> list = new ArrayList<String>();
            try {
                ResultSet rs = db.getPapersByAuthor(authorId, year);
                while (rs.next()){
                    Paper p = new Paper();
                    String paperId = rs.getString("idPaper");
                    p.setPaperId(paperId);
                    p.setPaperTitle(rs.getString("title"));
                    p.setYear(rs.getInt("year"));
                    list.add(paperId);
                    papers.put(paperId, p);//chuan bi cho viec tinh authorFV
                }
            } catch (SQLException ex) {
                Logger.getLogger(CBSimComputation.class.getName()).log(Level.SEVERE, null, ex);
            }
            authorPaperList.put(new Integer(authorId), list);
        }
        db.disconnect();
    }
    
    private static HashMap<String,Integer> readPaperIdByYear(String dataFile){
        HashMap<String,Integer> paperIdYear = new HashMap();
        final String REGEX = "\\D";
        try {
            System.out.println("Bat dau doc danh sach bai bao tu file text");
            Scanner input = new Scanner(new FileReader(dataFile));
            input.nextLine();//bo dong dau tien
            while (input.hasNext()){
                String line = input.nextLine().trim();
                String[] tokens = line.split(REGEX);
                String key = tokens[0];
                Integer value = new Integer(tokens[1]);
                paperIdYear.put(key, value);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(CBSimComputation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return paperIdYear;
    }
    
    private static HashMap<String,Integer> readPaperIdByYear(int year){
        HashMap<String,Integer> paperIdYear = new HashMap<String,Integer>();
        try {
            DatabaseTool db = new DatabaseTool();
            db.connect();
            ResultSet rs = db.getPapersByYear(year);
            while (rs.next()){
                paperIdYear.put(rs.getString("idPaper"), new Integer(rs.getInt("year")));
            }
            db.disconnect();
            return paperIdYear;
        } catch (SQLException ex) {
            Logger.getLogger(CBSimComputation.class.getName()).log(Level.SEVERE, null, ex);
        }
        return paperIdYear;
    }
    
    public static void test(HashMap<String, Author> authors, HashMap<String,Paper> papers){
        Author a1 = authors.get("875881");
        Author a2 = authors.get("894707");
        List<String> list1 = a1.getPaperList();
        List<String> list2 = a2.getPaperList();
        System.out.println("Auhtor 1");
        for (int i = 0; i < list1.size(); i++){
            Paper p = papers.get(list1.get(i));
            System.out.println("vector of " + list1.get(i) + p.getTfidfVector());
        }
        System.out.println("Auhtor 2");
        for (int i = 0; i < list2.size(); i++){
            Paper p = papers.get(list2.get(i));
            System.out.println("vector of " + list2.get(i) + p.getTfidfVector());
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
//        CBFPaperFVComputation.vectorzie(2003, "D:\\1.CRS-Experiment\\TFIDF\\2003\\");
//        read 'PaperIdByAuthor("/Users/thucnt/temp/input/AuthorID_PaperID_2001_2003.txt");
        computeCosine(2013,"D:\\1.CRS-Experiment\\MLData\\TrainingData\\NegativeSamples.txt","D:\\1.CRS-Experiment\\MLData\\TrainingData\\NegativeSamples_Cosine.txt");
    }
}
