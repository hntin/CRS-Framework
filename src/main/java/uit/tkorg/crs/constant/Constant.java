package uit.tkorg.crs.constant;

import java.util.logging.Level;

public class Constant {

    public static final String FOLDER_NUS_DATASET1 = "C:\\3.PRS-Experiment\\NUS Data\\Dataset 1\\20100825-SchPaperRecData";
    public static final String FOLDER_NUS_DATASET2 = null;
//    public static final String FOLDER_MAS_DATASET = "E:\\! Research\\Research Topics\\3. Recommendation Systems\\PRS\\Experiment\\1. Data\\Sample Data\\CSV\\Sample 3\\";
//    public static final String FOLDER_MAS_DATASET = "C:\\1. Experimental Data\\4. PRS\\MAS\\Dataset 2\\Small dataset\\";
//    public static final String FOLDER_MAS_DATASET = "D:\\3.PRS-Experiment\\Dataset1 - MAS\\PRS Experimental data\\";
    public static final String FOLDER_MAS_DATASET = "output/";
    public static final String SAVEDATAFOLDER = "D:\\ResE\\Paper recommendation\\Save Object Dataset 1";
//    public static final String TFIDFDIR = "D:\\3.PRS-Experiment\\Dataset1 - MAS\\PRS Experimental data\\T0-T1\\TF-IDF\\Vector\\";
    public static final String TFIDFDIR = "/Users/thucnt/temp/output/TFIDF/";
    public static final String PROFILE_DIR = "/Users/thucnt/temp/ouput/profiles/";
    
    public static final String DB = "MYSQL";
    //public static final String DB = "SQLSERVER";

    public static final String HOST = "localhost";
    public static final String PORT = "3306";
    public static final String DATABASE = "CSPublicationCrawler";
    public static final String USERNAME = "root";
    public static final String PASSWORD = "root";

    public static final String HOSTMSSQLSERVER = "localhost";
    public static final String PORTMSSQLSERVER = "1433";
    public static final String DATABASEMSSQLSERVER = "CSPublicationCrawler";
    public static final String USERNAMEMSSQLSERVER = "sa";
    public static final String PASSWORDMSSQLSERVER = "12345";
   
    //public static final Level LOGGING_LEVEL = Level.ALL;
    public static final Level LOGGING_LEVEL = Level.WARNING;
}
