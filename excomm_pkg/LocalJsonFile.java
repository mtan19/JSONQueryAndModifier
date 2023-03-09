package excomm_pkg;
import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import org.json.*;
import java.lang.IllegalArgumentException;

public class LocalJsonFile implements InputTypeStrategy{

    public ArrayList<String> search(String directory, String scheme) throws FileNotFoundException, IllegalArgumentException{
        //implementation details
        //      directory: the directory for the file. If inside a folder, then must be folderName\fileName;
        //      scheme: the name of the JSON array within the file 
        //              eg:  if scheme = "Arrayname", will get {Arrayname:[1,2,3]}


        String content = findFile(directory);
        ArrayList<String> element = new ArrayList<String>();
        try {
        	 element = findTable(content, scheme);
        }
        catch(JSONException ej) {
        	throw new IllegalArgumentException("class LocalJSONFile: file " + directory + " had invalid JSON notation");
        }
        return element;
    }

    private String findFile(String directory) throws FileNotFoundException{
        String r = "";
        //load file into scanner
            String fulldir = System.getProperty("user.dir") + "\\" + directory;
            File db = new File(fulldir);
            Scanner s = new Scanner(db);
        //concatenate all string into single string
            while(s.hasNextLine()){
                r += s.nextLine();
            }
        //close scanner
            s.close();
        return r;
    }

    private ArrayList<String>  findTable(String content, String scheme) throws JSONException, IllegalArgumentException{
    	JSONObject jObj = new JSONObject(content);
        JSONArray jElement = jObj.optJSONArray(scheme);
        if(jElement == null){
            throw new IllegalArgumentException("class LocalJSONFile: No array element with the name \"" + scheme + "\" was found in JSON file");
        }
        ArrayList<String> element = new ArrayList<String>();
        for(int i=0; i<jElement.length(); i++){
            element.add(jElement.getJSONObject(i).toString());
        }
        return element;
    }
}