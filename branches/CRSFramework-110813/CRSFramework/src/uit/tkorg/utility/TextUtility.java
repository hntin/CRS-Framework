/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uit.tkorg.utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import weka.core.Stopwords;
import weka.core.stemmers.IteratedLovinsStemmer;
import weka.core.stemmers.LovinsStemmer;
import weka.core.tokenizers.WordTokenizer;

/**
 *
 * @author tin
 */
public class TextUtility {
    public static ArrayList<String> removeStopWord(String str) throws Exception {
        ArrayList<String> result = new ArrayList<String>();
        WordTokenizer wordTokenizer = new WordTokenizer();
        String delimiters = " \r\t\n.,;:\'\"()?!-><#$\\%&*+/@^_=[]{}|`~0123456789";
        wordTokenizer.setDelimiters(delimiters);
        wordTokenizer.tokenize(str);
        try {
            String token;
            while (wordTokenizer.hasMoreElements()) {
                token = wordTokenizer.nextElement().toString();
                if (!Stopwords.isStopword(token.toLowerCase())) {
                    result.add(token);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public static ArrayList<String> removeStopWordAndStemming(String str) throws Exception {
        ArrayList<String> result = new ArrayList<String>();
        WordTokenizer wordTokenizer = new WordTokenizer();
        IteratedLovinsStemmer stemmerLovin = new IteratedLovinsStemmer();
        String delimiters = " \r\t\n.,;:\'\"()?!-><#$\\%&*+/@^_=[]{}|`~0123456789";
        wordTokenizer.setDelimiters(delimiters);
        wordTokenizer.tokenize(str);
        try {
            String token;
            while (wordTokenizer.hasMoreElements()) {
                token = wordTokenizer.nextElement().toString();
                if (!Stopwords.isStopword(token.toLowerCase())) {
                    token = stemmerLovin.stem(token);
                    result.add(token);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        System.out.println(result.toString());
        return result;
    }
}
