/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.crs.datapreparation;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Dictionary;
import java.util.HashMap;

/**
 *
 * @author daolv
 */
public class MappingNameToId {

    private String fileTrainingPath;
    private String fileNearTestingPath;
    private String fileFarTestingPath;
    private String fileOutputTraningPath;
    private String fileOutputNearTestingPath;
    private String fileOutputFarTestingPath;
    public HashMap<String,Integer> nameData;

    public MappingNameToId(String _fileTrainingPath,
            String _fileNearTestingPath,
            String _fileFarSTestingPath,
            String _fileOutputTrainingPath,
            String _fileOutputNearTestingPath,
            String _fileOutputFarTestingPath) {
        fileTrainingPath = _fileTrainingPath;
        fileNearTestingPath = _fileNearTestingPath;
        fileFarTestingPath = _fileFarSTestingPath;

        fileOutputTraningPath = _fileOutputTrainingPath;
        fileOutputNearTestingPath = _fileOutputNearTestingPath;
        fileOutputFarTestingPath = _fileOutputFarTestingPath;
        
        nameData = new HashMap<>();
    }
    
    private void MapToId(String filePathIn, String filePathOut)
    {
        try {
            FileInputStream fis = new FileInputStream(filePathIn);
            Reader reader = new InputStreamReader(fis,"UTF8");
            BufferedReader bufferReader = new BufferedReader(reader);
            bufferReader.readLine();
            String line = null;
            String[] tokens;
            String authorName;
            int counter = 0;
            
            FileOutputStream fos = new FileOutputStream(filePathOut);
            Writer writer = new OutputStreamWriter(fos,"UTF8");
            writer.write("AuthorId" + "\t" + "PaperId" + "\n");
            
            while((line=bufferReader.readLine()) != null)
            {
                tokens = line.split("\t");
                if (tokens.length >= 2)   
                {
                    authorName = tokens[1];
                    Integer id = nameData.get(authorName);
                    if (id == null)
                    {
                        counter++;
                        id = counter;
                        nameData.put(authorName, id);
                    }
                    
                    writer.write(String.valueOf(id) + "\t" + tokens[0] + "\n"); 
                }
            }
            
            bufferReader.close();
            writer.close();
        } catch (Exception e) {
        } 
    }
    
    public void Run()
    {
        MapToId(fileTrainingPath,fileOutputTraningPath);
        MapToId(fileNearTestingPath,fileOutputNearTestingPath);
        MapToId(fileFarTestingPath,fileOutputFarTestingPath);
    }
}
