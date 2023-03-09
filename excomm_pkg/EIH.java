package excomm_pkg;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.lang.IllegalArgumentException;

public abstract class EIH{
    protected String directory;
    protected InputTypeStrategy strategy;
    protected String scheme;
    protected ArrayList<String> element;

    protected abstract void preprocessing(ArrayList<Object> args) throws Exception;
    
    protected abstract void postprocessing(ArrayList<Object> args) throws Exception;

    protected void getElement(){
        try{
            element = strategy.search(directory, scheme);
        }
        catch(FileNotFoundException e1){
            System.out.println("ERROR: " + directory + " was an invalid directory");
            System.out.println(e1.toString());
            element = null;
        }
        catch(IllegalArgumentException e2){
            System.out.println("ERROR: an illegal argument was passed as a parameter");
            System.out.println(e2.toString());
            element = null;
        }
        catch(Exception e) {
        	System.out.println("ERROR: an error has occured when handling external input");
        	System.out.println(e.toString());
            element = null;
        }
    }
}