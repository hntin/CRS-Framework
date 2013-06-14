/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.dbaccess;

import java.io.File;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import uit.tkorg.utility.TextFileProcessor;

/**
 *
 * @author TinHuynh
 */
public class AuthorPaperMapper extends MapperDB {

    public AuthorPaperMapper() throws Exception {
        super();
    }

    public AuthorPaperMapper(Connection con) {
        super(con);
        // TODO Auto-generated constructor stub
    }

    public boolean isAuthorPaperExist(int idAuthor, int idPaper) throws Exception {
        boolean isExist = false;
        try {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT * FROM mas.author_paper ap");
            sql.append(" WHERE ap.idAuthor = ? AND ap.idPaper = ?");
            PreparedStatement stmt = getConnection().prepareStatement(sql.toString());
            stmt.setInt(1, idAuthor);
            stmt.setInt(2, idPaper);
            ResultSet rs = stmt.executeQuery();
            if ((rs != null) && (rs.next())) {
                isExist = true;
            }
            stmt.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

        return isExist;
    }

    /**
     * Year < 2005 for selecting paper before 2005 for training data 2006 - 2011
     * for testing data @throws Exception
     */
    public void getAllPaperForEachAuthorOutToTextFile(String pathDir, int yearFrom, int yearTo) throws Exception {
        ArrayList<String> authorIdList = new ArrayList<String>();
        StringBuffer outStringBuffer = null;
        try {
            Statement st = getConnection().createStatement();
            StringBuffer sqlString = new StringBuffer();
            sqlString.append(" SELECT DISTINCT ap.idAuthor FROM paper p, author_paper ap");
            sqlString.append(" WHERE p.idPaper = ap.idPaper AND p.year >= " + yearFrom + " AND p.year <= " + yearTo);
            sqlString.append(" ORDER BY idAuthor");
            ResultSet rs = st.executeQuery(sqlString.toString());
            while (rs != null && rs.next()) {
                authorIdList.add(rs.getString("idAuthor"));
            }
            rs.close();

            for (int i = 0; i < authorIdList.size(); i++) {
                String idAuthor = (String) authorIdList.get(i);
                sqlString = new StringBuffer();
                sqlString.append(" SELECT ap.idPaper, p.title, p.abstract ");
                sqlString.append(" FROM mas.author_paper ap join mas.paper p on ap.idPaper = p.idPaper");
                sqlString.append(" WHERE p.year <= " + yearTo + " AND ap.idAuthor = '" + idAuthor + "'");
                System.out.println(sqlString.toString());
                rs = st.executeQuery(sqlString.toString());

                String idPaper = null;
                String pTitle = null;
                //String pAbstract = null;
                outStringBuffer = new StringBuffer();
                outStringBuffer.append("idAuthor" + ";" + "idPaper" + ";" + "Title" + ";" + "Abstract" + "\n");
                while (rs != null && rs.next()) {
                    idPaper = rs.getString("idPaper");
                    pTitle = rs.getString("title");
                    outStringBuffer.append(idAuthor + ";");
                    outStringBuffer.append(idPaper + ";");  
                    if (pTitle != null) outStringBuffer.append(pTitle + ";");
                    
                    byte[] pAbs = rs.getBytes("abstract");
                    if (pAbs != null && pAbs.length >0) {
                        String pAbstract = new String(pAbs, "UTF8");
                        outStringBuffer.append(pAbstract);
                    }
                    
                    outStringBuffer.append("\n");
                }
                rs.close();
                
                if (i%1000 ==0 ) {
                    (new File(pathDir + "/Folder" + (i/1000+1))).mkdir();
                }
                TextFileProcessor.writeTextFile(pathDir + "/Folder" + (i/1000+1) + "/AuthorID_" + idAuthor, outStringBuffer.toString());
            }
            
            st.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
