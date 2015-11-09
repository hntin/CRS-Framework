/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.datapreparation;

import ir.vsr.HashMapVector;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import uit.tkorg.crs.model.Pair;
import uit.tkorg.crs.model.Author;
import uit.tkorg.crs.model.Paper;
import uit.tkorg.crs.model.Sample;
import uit.tkorg.crs.utility.DatabaseTool;
import uit.tkorg.crs.utility.TextFileUtility;

public class CBSimComputation extends FeatureComputation {

    private String tfIdfDir = "D:\\1.CRS-Experiment\\TFIDF\\2003\\";
    private String dbUrl = "jdbc:mysql://localhost:3306/mas";
    private String dbUsername = "root";
    private String dbPassword = "root";
    private int year;

    public CBSimComputation(String positive, String negative, String tfIdf, int year) {
        this._positiveSample = Sample.readSampleFile(positive);
        this._negativeSample = Sample.readSampleFile(negative);
        this.tfIdfDir = tfIdf;
        this.year = year;
    }

    public void setDbUrl(String url) {
        this.dbUrl = url;
    }

    public void setDbUsername(String usr) {
        this.dbUsername = usr;
    }

    public void setDbPassword(String pwd) {
        this.dbPassword = pwd;
    }

    /**
     *
     * @param paperListFile
     * @return
     * @throws Exception
     */
    private HashMap<Integer, List<Integer>> readPaperIdByAuthor(String paperListDir) throws Exception {
        HashMap<Integer, List<Integer>> authorPaperId = new HashMap();
        final String REGEX = "\\D";

        System.out.println("Bat dau doc danh sach Id bai bao tu file text");
        List<String> paperListFile = TextFileUtility.getPathFile(new File(paperListDir));
        for (int i = 0; i < paperListFile.size(); i++) {
            String fileName = paperListFile.get(i);
            int year = Integer.parseInt(fileName.substring(fileName.length() - 4));
            if (year > this.year) {
                continue;
            }
            Scanner input = new Scanner(new FileReader(fileName));
            input.nextLine();//bo dong dau tien
            while (input.hasNext()) {
                String line = input.nextLine().trim();
                String[] tokens = line.split(REGEX);
                Integer key = new Integer(tokens[0]);
                Integer value = new Integer(tokens[1]);
                if (authorPaperId.containsKey(key)) {
                    List<Integer> paperIdList = authorPaperId.get(key);
                    paperIdList.add(value);
                    authorPaperId.replace(key, paperIdList);
                } else {
                    List<Integer> paperIdList = new ArrayList<Integer>();
                    paperIdList.add(value);
                    authorPaperId.put(key, paperIdList);
                }
            }
        }
        System.out.println("So tac gia da co bai bao: " + authorPaperId.size());
        return authorPaperId;
    }
    
    @Override
    public void computeFeatureValues(String outputFile, int typeOfSample) {

        //doc danh sach tac gia tu mau am/duong
        LinkedHashSet<Integer> authorList = null;
        if (typeOfSample == 1)
            authorList = this._positiveSample.readAllAuthorIDList();
        else
            authorList = this._negativeSample.readAllAuthorIDList();

        //Lay danh sach bai bao cua cac tac gia trong mau am/duong
        HashMap<Integer, List<String>> authorPaperList = new HashMap<Integer, List<String>>();
        HashMap<String, Paper> papers = new HashMap<String, Paper>();
        readAuthorPaperList(authorList, this.year, authorPaperList, papers);//tra ket qua thong qua 2 tham so cuoi
        try {
            //Tinh FV cho tat ca cac tac gia
            HashMap<String, Author> authors = new HashMap<String, Author>();
            Iterator<Integer> ir = authorList.iterator();
            while (ir.hasNext()) {
                Integer idAuthor = ir.next();
                Author author = new Author();
                author.setAuthorId(idAuthor.toString());
                List<String> paperList = authorPaperList.get(idAuthor);
                author.setPaperList(paperList);
                authors.put(author.getAuthorId(), author);
            }
            CBFPaperFVComputation.readTFIDFFromMahoutFile(papers, this.tfIdfDir);
            CBFAuthorFVComputation.computeFVForAllAuthors(authors, papers, 1, 0.5);
            
            //tinh do do cosine cho tung cap tac gia trong mau duong/am va ghi ra file
            ArrayList<Pair> listOfPairs = null;
            if (typeOfSample == 1)
                listOfPairs = this._positiveSample.getPairOfAuthor();
            else
                listOfPairs = this._negativeSample.getPairOfAuthor();
            
            StringBuilder content = new StringBuilder();
            content.append("(idAuhtor1,idAuthor2) \t cosine\n");
            for (int i = 0; i < listOfPairs.size(); i++) {
                Pair pair = listOfPairs.get(i);
                Author author1 = authors.get(pair.getFirst().toString());
                Author author2 = authors.get(pair.getSecond().toString());
                HashMapVector fv1 = author1.getFeatureVector();
                HashMapVector fv2 = author2.getFeatureVector();
                double cosine = fv1.cosineTo(fv2);
                String line = "(" + pair.getFirst() + "," + pair.getSecond() + ")" + "\t" + cosine;
                content.append(line + "\n");
            }
            TextFileUtility.writeTextFile(outputFile, content.toString());
        } catch (Exception ex) {
            Logger.getLogger(CBSimComputation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void readAuthorPaperList(Set<Integer> authorList, int year,
            HashMap<Integer, List<String>> authorPaperList, //output parameter
            HashMap<String, Paper> papers) {//output parameter
        DatabaseTool db = new DatabaseTool(this.dbUrl, this.dbUsername, this.dbPassword);
        db.connect();
        Iterator<Integer> ir = authorList.iterator();
        while (ir.hasNext()) {//lay danh sach bai bao cua 1 tac gia
            int authorId = ir.next().intValue();
            System.out.println("Lay danh sach bai bao cua tac gia " + authorId);
            List<String> list = new ArrayList<String>();
            try {
                ResultSet rs = db.get_Papers_BeforeYear_ByAuthorID(authorId, year);
                while (rs.next()) {
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

    private static void test(HashMap<String, Author> authors, HashMap<String, Paper> papers) {
        Author a1 = authors.get("875881");
        Author a2 = authors.get("894707");
        List<String> list1 = a1.getPaperList();
        List<String> list2 = a2.getPaperList();
        System.out.println("Auhtor 1");
        for (int i = 0; i < list1.size(); i++) {
            Paper p = papers.get(list1.get(i));
            System.out.println("vector of " + list1.get(i) + p.getTfidfVector());
        }
        System.out.println("Auhtor 2");
        for (int i = 0; i < list2.size(); i++) {
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
//        CBFPaperFVComputation.vectorzie(2011, "D:\\1.CRS-Experiment\\TFIDF\\2011\\");
//        read 'PaperIdByAuthor("/Users/thucnt/temp/input/AuthorID_PaperID_2001_2003.txt");
        CBSimComputation cbSim = new CBSimComputation(
                "D:\\1.CRS-Experiment\\MLData\\TrainingData\\PositiveSamples.txt",
                "D:\\1.CRS-Experiment\\MLData\\TrainingData\\NegativeSamples.txt",
                "D:\\1.CRS-Experiment\\TFIDF\\2003\\", 2003);
        cbSim.computeFeatureValues("D:\\1.CRS-Experiment\\MLData\\TrainingData\\PositiveSample_Cosine.txt",1);
        cbSim.computeFeatureValues("D:\\1.CRS-Experiment\\MLData\\TrainingData\\NegativeSample_Cosine.txt",0);
    }
}
