/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.datapreparation;

/**
 * Tinh CoOrgStrength (Org_RSS+) cho tung cap tac gia trong mau am (-) va mau
 * duong (+)
 *
 * @author thucnt
 */
public class CoOrgStrengthComputation extends FeatureComputation {

    @Override
    public void computeFeatureValues(String outputFile, int typeOfSample) {
        // Step 1: Xay dung mang CoOrg_Net.
        // Step 2: Tinh toan trong so RSS cho tung cap OrgID trong mang.
        // Step 3: Doc file mau am (-), mau duong (+) de load tung cap <authorID, authorID>.
        // Step 4: Lay OrgID cua 2 tac gia tuong ung.
        // Step 5: Tra ve gia tri RSS+ cho tung cap OrgID lien quan cac cap authorID trong mau (+) va (-)
        // Step 6: Ghi ket qua gia tri Org_RSS xuong file 
    }

    public static void main(String args[]) {
        
    }
    
}
