package Utils.FileIO;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class FileIOUtil {
    public FileIOUtil() {
    }

    public String getRawFileContents (String path) {
        String results = null;

        try {
            File file = new File(path);
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            char[] buffer = new char[(int)file.length()];
            br.read(buffer);

            results = String.valueOf(buffer);

            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            System.out.println("Cannot open the file: " + path);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Cannot close the file and its buffer/reader: " + path);
            e.printStackTrace();
        }
        return results;
    }

    public void saveResultsToFile (String path, String results) {

        try {
            File file = new File(path);
            FileWriter fw = new FileWriter(file);
            fw.write(results);

            fw.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
