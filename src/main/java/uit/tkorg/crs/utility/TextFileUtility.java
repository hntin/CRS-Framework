/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.utility;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tin
 */
public class TextFileUtility {

    /**
     *
     * @param dir: directory containing txt and dat files.
     * @return list of full path of txt and dat files in the directory including
     * subdirectory.
     */
    public static List<String> getPathFile(File dir) throws Exception {
        File[] files = dir.listFiles();
        List<String> listFiles = new ArrayList<>();
        for (File file : files) {
            if (file.isFile()) {
                String name = file.getName();
                if (name.endsWith(".txt") || name.endsWith(".dat")) {
                    listFiles.add(file.getAbsolutePath());
                }
            } else {
                listFiles.addAll(getPathFile(file));
            }
        }
        return listFiles;
    }

    /**
     * writeTextFile
     *
     * @param textFilePath
     * @param appendText
     */
    public static void writeTextFile(String textFilePath, String textContent) {
        try {
            FileOutputStream fos = new FileOutputStream(textFilePath);
            Writer out = new OutputStreamWriter(fos, "UTF8");
            out.write(textContent);
            out.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param dir
     * @param idAuthor: Ten file.
     * @param rs
     */
    public static void writeTextFile(File dir, int idAuthor, ResultSet rs) {
        FileWriter fstream = null;
        try {
            File f = new File(dir, idAuthor + ".txt");
            fstream = new FileWriter(f, true);
            BufferedWriter out = new BufferedWriter(fstream);
            String delim = "#";
            StringBuilder line;
            out.write(idAuthor + "");
            out.newLine();
            try {
                while (rs.next()) {
                    line = new StringBuilder();
                    line.append(rs.getInt(1) + "");
                    line.append(delim);
                    line.append(rs.getString(2));
                    line.append(delim);
                    Blob blob = rs.getBlob(3);
                    if (blob != null) {
                        line.append(new String(blob.getBytes(1, (int) blob.length())));
                    }
                    line.append(delim);
                    line.append(rs.getInt(4) + "");
                    out.write(line.toString());
                    out.newLine();
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }
            out.flush();
        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
            try {
                fstream.close();
                rs.close();
            } catch (IOException ex) {
                System.out.println(ex);
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        }
    }

    /**
     *
     * @param dir
     * @param idAuthor: Ten file.
     * @param rs
     */
    public static void writeTextFile(File dir, String fileName, ResultSet rs) {
        FileWriter fstream = null;
        try {
            File f = new File(dir, fileName + ".txt");
            fstream = new FileWriter(f, true);
            BufferedWriter out = new BufferedWriter(fstream);
            String delim = "#";
            StringBuilder line;
            out.write(fileName + "");
            out.newLine();
            try {
                while (rs.next()) {
                    line = new StringBuilder();
                    line.append(rs.getInt(1) + "");
                    line.append(delim);
                    line.append(rs.getString(2));
                    line.append(delim);
                    Blob blob = rs.getBlob(3);
                    if (blob != null) {
                        line.append(new String(blob.getBytes(1, (int) blob.length())));
                    }
                    line.append(delim);
                    line.append(rs.getInt(4) + "");
                    out.write(line.toString());
                    out.newLine();
                }
            } catch (SQLException ex) {
                System.out.println(ex);
            }
            out.flush();
        } catch (IOException ex) {
            System.out.println(ex);
        } finally {
            try {
                fstream.close();
                rs.close();
            } catch (IOException ex) {
                System.out.println(ex);
            } catch (SQLException ex) {
                System.out.println(ex);
            }
        }
    }

    public static void writeTextFile(String textFile, HashMap<Integer, Float> hashMap) {
        try {
            FileWriter fstream = new FileWriter(textFile, false);
            BufferedWriter out = new BufferedWriter(fstream);
            StringBuffer buff = new StringBuffer();
            buff.append("Key" + "\t" + "Value" + "\n");
            for (int key : hashMap.keySet()) {
                buff.append(key + "\t" + hashMap.get(key) + "\n");
            }
            out.write(buff.toString());
            out.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * writeTextFileFromHM
     *
     * @param textFilePath
     * @param hashMap
     */
    public static void writeTextFileFromHM(String textFilePath, HashMap<Integer, HashMap<Integer, Float>> hashMap) {
        try {
            FileWriter fstream = new FileWriter(textFilePath, false);
            BufferedWriter out = new BufferedWriter(fstream);
            StringBuffer buff = new StringBuffer();
            for (Integer instanceID1 : hashMap.keySet()) {
                buff.append(instanceID1 + "#");
                Set<Integer> otherInstances = (hashMap.get(instanceID1)).keySet();
                for (Integer instanceID2 : otherInstances) {
                    float value = (hashMap.get(instanceID1)).get(instanceID2);
                    buff.append(instanceID2 + ":" + value + ";");
                }
                buff.append("\n");
            }

            out.write(buff.toString());
            out.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * appendTextFile
     *
     * @param textFilePath
     * @param appendText
     */
    public static void appendTextFile(String textFilePath, String appendText) {
        try {
            // Create file
            FileWriter fstream = new FileWriter(textFilePath, true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(appendText);
            //Close the output stream
            out.close();
        } catch (Exception e) {//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
    }

    /**
     * read a Text file with UTF8 format,
     *
     * @param filePath
     */
    public static String readTextFile(String filePath) {
        StringBuffer strBuffer = new StringBuffer();
        try {
            FileInputStream fis = new FileInputStream(filePath);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine(); // skip the first line
            String line = null;

            while ((line = bufferReader.readLine()) != null) {
                strBuffer.append(line + " ");
            }
            bufferReader.close();
            fis.close();
        } catch (Exception e) {
            System.out.println(e);
        }

        return strBuffer.toString();
    }
    
    public static int readNumLinesOfFile(String filePath) {
        int count = 0;
        try {
            FileInputStream fis = new FileInputStream(filePath);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine(); // skip the first line
            String line = null;

            while ((line = bufferReader.readLine()) != null && !line.equalsIgnoreCase("")) {
                count++;
            }
            bufferReader.close();
            fis.close();
        } catch (Exception e) {
            System.out.println(e);
        }

        return count;
    }
    
    public static void readFeaturesFile(String featureFile){
        StringBuffer strBuffer = new StringBuffer();
        try {
            FileInputStream fis = new FileInputStream(featureFile);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine(); // skip the first line
            String line = null;

            while ((line = bufferReader.readLine()) != null) {
                String delim = ","; //insert here all delimitators
                StringTokenizer st = new StringTokenizer(line,delim);
             
                while (st.hasMoreTokens()) {
                    double d = Double.parseDouble(st.nextToken());
                    System.out.print(d + ",");
                    System.out.println();
                }
            }
            bufferReader.close();
            fis.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
    public static void writeWekaResultBuffer(String fileInput, String fileOutput){
        try{
            FileInputStream fis = new FileInputStream(fileInput);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            
            FileOutputStream fos = new FileOutputStream(fileOutput);
            Writer out = new OutputStreamWriter(fos, "UTF8");
            
            String line = null;

            while ((line = bufferReader.readLine()) != null) {
                if (line.startsWith("inst#"))
                    break;
            }
            out.write("inst#,predicted,probability,error\n");
            while ((line = bufferReader.readLine()) != null) {
                String delim = " "; //insert here all delimitators
                StringTokenizer st = new StringTokenizer(line,delim);
                
                boolean error = false;
                int count = 0;
                while (st.hasMoreTokens()) {
                    String token = st.nextToken();
                    count++;
                    if ((count == 1) || (count == 3))
                        out.write(token + ",");
                    else if (token.startsWith("*"))
                        out.write(token.substring(1) + ",");
                    else if ("+".equals(token))
                        error = true;       
                }
                if (error == true){
                    out.write("true");
                    error = false;
                }
                out.write("\n");
            }
            out.close();
            fos.close();

            bufferReader.close();
            fis.close();
        }
        catch(IOException e){
            System.out.println(e);
        }
    }

    /**
     * Splitting a text file for many piece with 5.000 rows/file
     *
     * @param filePath
     */
    public static void splitTextFile(String filePath) {
        int count = 1;
        StringBuffer strBufferSpliter = new StringBuffer();
        try {
            FileInputStream fis = new FileInputStream(filePath);
            Reader reader = new InputStreamReader(fis, "UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine(); // skip the first line
            String line = null;
            String path = null;
            String fileNamePiece = null;
            while ((line = bufferReader.readLine()) != null) {
                strBufferSpliter.append(line + "\n");
                if (count % 5000 == 0) {
                    path = (new File(filePath)).getParent();
                    fileNamePiece = path + "\\file_" + (count / 5000) + ".txt";
                    TextFileUtility.writeTextFile(fileNamePiece, strBufferSpliter.toString());
                    strBufferSpliter = new StringBuffer();
                }
                count++;
            }

            path = (new File(filePath)).getParent();
            fileNamePiece = path + "\\file_" + (count / 5000 + 1) + ".txt";
            TextFileUtility.writeTextFile(fileNamePiece, strBufferSpliter.toString());

            bufferReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void appendTwoFiles(String inFile1, String inFile2, String outFile) throws IOException {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(outFile)))) {

            try {
                FileInputStream fis = new FileInputStream(inFile1);
                Reader reader = new InputStreamReader(fis, "UTF8");
                BufferedReader bufferReader = new BufferedReader(reader);
                //bufferReader.readLine(); // skip the first line
                String line = null;
                while ((line = bufferReader.readLine()) != null) {
                    out.println(line);
                    out.flush();
                }
                bufferReader.close();
                fis.close();
                
                
                fis = new FileInputStream(inFile2);
                reader = new InputStreamReader(fis, "UTF8");
                bufferReader = new BufferedReader(reader);
                bufferReader.readLine(); // skip the first line
                line = null;
                while ((line = bufferReader.readLine()) != null) {
                    out.println(line);
                    out.flush();
                }
                bufferReader.close();
                fis.close();
                
            } catch (IOException e) {
                System.out.println(e);
            }

        }
    }
    
    public static void main(String args[]) {
//        try {
//            TextFileUtility.appendTwoFiles(
//                    "/Users/thucnt/Downloads/3Hobs/positive.txt", 
//                    "/Users/thucnt/Downloads/3Hobs/positive.txt", 
//                    "/Users/thucnt/Downloads/3Hobs/merged.txt");
//            System.out.println("DONE");
//        }
//        catch(Exception e) {
//            e.printStackTrace();
//        }
//        writeWekaResultBuffer("/Users/thucnt/Downloads/3Hobs/Result_Buffer/CBSim","/Users/thucnt/Downloads/3Hobs/Result_Buffer/CBSim.csv");
//    readFeaturesFile("/Users/thucnt/Downloads/3Hobs/TestingData/Testing_PositiveSample_AllFeatures.txt");
        
        int lines = readNumLinesOfFile("D:\\1.CRS-Experiment\\MLData\\3-Hub\\Senior\\TestingData\\Testing_NegativeSamples_Full.txt");
        System.out.println("LINES:" + lines);
    }
}
