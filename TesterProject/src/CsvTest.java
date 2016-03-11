import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;


public class CsvTest {

  public void readFile() {

    BufferedReader br = null;
    
    try {
      File file = new File("C:/home/fsheriff/riskchallenge.csv");
      br = new BufferedReader(new FileReader(file));
      String line = null;
      
      while ((line = br.readLine()) != null) {
        
        String[] values = line.split(",");
        
        //Do necessary work with the values, here we just print them out
        for (int index=0; index<values.length; index++) {
          System.out.println(index+"=="+values[index]);
        }
        System.out.println();
      }
    }
    catch (FileNotFoundException ex) {
      ex.printStackTrace();
    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    finally {
      try {
        if (br != null)
          br.close();
      }
      catch (IOException ex) {
        ex.printStackTrace();
      }
    }
  }

  public static void main(String[] args) {
    CsvTest test = new CsvTest();
    test.readFile();
  }
}
 