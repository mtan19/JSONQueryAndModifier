package excomm_test;
import excomm_pkg.LocalJsonFile;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import org.junit.*;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Test_LocalJsonFile {
	
	public static void main(String[] args) {
		System.out.println("Running class Test_LocalJsonFile");
		Result res =  org.junit.runner.JUnitCore.runClasses(Test_LocalJsonFile.class);
		String report = "\tTests run=" + res.getRunCount() + " TestsFailed=" + res.getFailureCount();
		if(!res.wasSuccessful()) {
			Failure[] epicfail = res.getFailures().toArray(new Failure[0]);
			for(int i=0; i<epicfail.length; i++) {
				report += "\n\t>>>" + epicfail[i].toString();
			}
		}
		else{
			report += "\n\t>>>All tests successful";
		}
		System.out.println(report);
	}
	
	
	@Test
	public void LocalJsonFile_search_S1() {
		//sucess testing, containing elements
		//setup
			String directory = "excomm_test\\TestJson_S1.json";
			String scheme = "Array";
			ArrayList<String> element = new ArrayList<String>();
			LocalJsonFile testclass = new LocalJsonFile();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} catch (Exception e) {
				fail("test for success ended in failure. Error message: " + e.toString());
			}
		//test output
			String element0 = "{\"password\":\"p\",\"username\":\"u\"}";
			String element1 = "{\"p\":\"password\",\"u\":\"username\"}";
			String element2 = "{\"a\":\"3311\",\"b\":\"EECS\",\"c\":\"hello\"}";
			String element3 = "{\"password\":\"symbol\",\"username\":\"!@#$%^&*()\"}";
			String element4 = "{\"password\":\"many words here\",\"username\":\"two words\"}";
			String element5 = "{\"\":\"\"}";
			String element6 = "{}";
			assertEquals("incorrect number of items in ArrayList", 7, element.size());
			assertEquals("index 0 did not have right value.", element0, element.get(0));
			assertEquals("index 1 did not have right value.", element1, element.get(1));
			assertEquals("index 2 did not have right value.", element2, element.get(2));
			assertEquals("index 3 did not have right value.", element3, element.get(3));
			assertEquals("index 4 did not have right value.", element4, element.get(4));
			assertEquals("index 5 did not have right value.", element5, element.get(5));
			assertEquals("index 6 did not have right value.", element6, element.get(6));
	}
	
	
	@Test
	public void LocalJsonFile_search_S2() {
		//sucess testing, empty array
		//setup
			String directory = "excomm_test\\TestJson_S2.json";
			String scheme = "Array";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			LocalJsonFile testclass = new LocalJsonFile();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} catch (Exception e) {
				fail("test for success ended in failure. Error message: " + e.toString());
			}
		//test output
			assertEquals("incorrect number of items in ArrayList", 0, element.size());
			assertFalse("no ArrayList was returned, potentially threw an error", element==elementAlias);
	}
	
	
	@Test
	public void LocalJsonFile_search_F_nonobjectInArray(){
		//found an array with that matched scheme, and it was indeed an array, but the array contained non-object elements
		//setup
			String directory = "excomm_test\\TestJson_F1.json";
			String scheme = "Array";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			LocalJsonFile testclass = new LocalJsonFile();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (IllegalArgumentException e1) {
				assertEquals("correct type of exception thrown, but not for the correct reason", "class LocalJSONFile: file excomm_test\\TestJson_F1.json had invalid JSON notation", e1.getMessage());
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}
	
	
	@Test
	public void LocalJsonFile_search_F_noArrayThatMatchesScheme1(){
		//there exists array elements, but none of them match the scheme
		//setup
			String directory = "excomm_test\\TestJson_F2a.json";
			String scheme = "Array";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			LocalJsonFile testclass = new LocalJsonFile();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (IllegalArgumentException e1) {
				assertEquals("correct type of exception thrown, but not for the correct reason", "class LocalJSONFile: No array element with the name \"Array\" was found in JSON file", e1.getMessage());
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}

	
	@Test
	public void LocalJsonFile_search_F_noArrayThatMatchesScheme2(){
		//there is no array elements to begin with
		//setup
			String directory = "excomm_test\\TestJson_F2b.json";
			String scheme = "Array";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			LocalJsonFile testclass = new LocalJsonFile();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (IllegalArgumentException e1) {
				assertEquals("correct type of exception thrown, but not for the correct reason", "class LocalJSONFile: No array element with the name \"Array\" was found in JSON file", e1.getMessage());
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}
	
	@Test
	public void LocalJsonFile_search_F_matchingSchemeNotArray(){
		//there a matching scheme but it is not an array
		//setup
			String directory = "excomm_test\\TestJson_F3.json";
			String scheme = "Array";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			LocalJsonFile testclass = new LocalJsonFile();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (IllegalArgumentException e1) {
				assertEquals("correct type of exception thrown, but not for the correct reason", "class LocalJSONFile: No array element with the name \"Array\" was found in JSON file", e1.getMessage());
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}
	
	@Test
	public void LocalJsonFile_search_F_filenotJson(){
		//there a matching scheme but it is not an array
		//setup
			String directory = "excomm_test\\TestTxt_F4.txt";
			String scheme = "Array";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			LocalJsonFile testclass = new LocalJsonFile();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (IllegalArgumentException e1) {
				assertEquals("correct type of exception thrown, but not for the correct reason", "class LocalJSONFile: file excomm_test\\TestTxt_F4.txt had invalid JSON notation", e1.getMessage());
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}
	
	@Test
	public void LocalJsonFile_search_F_fileNotFound(){
		//there a matching scheme but it is not an array
		//setup
			String directory = "excomm_test\\nonexistentDirectory";
			String scheme = "Array";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			LocalJsonFile testclass = new LocalJsonFile();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (FileNotFoundException e1) {
				//correct behavior
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}
}
