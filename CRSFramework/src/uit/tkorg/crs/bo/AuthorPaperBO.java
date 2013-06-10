/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.bo;

import uit.tkorg.crs.dbaccess.AuthorPaperMapper;

/**
 *
 * @author tin
 */
public class AuthorPaperBO {

    public void getAllPaperForEachAuthorOutToTextFile(String pathDir, int year) throws Exception {
        AuthorPaperMapper mapper = null;
        try {
            mapper = new AuthorPaperMapper();
            mapper.getAllPaperForEachAuthorOutToTextFile(pathDir, year);
        } catch (Exception e) {
            throw e;
        } finally {
            mapper.closeConnection();
        }
    }
}
