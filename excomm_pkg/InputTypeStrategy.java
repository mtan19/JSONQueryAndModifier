package excomm_pkg;
import java.util.ArrayList;

public interface InputTypeStrategy{
    public ArrayList<String> search(String directory, String scheme) throws Exception;
}