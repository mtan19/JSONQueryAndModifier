package excomm_test;
import excomm_pkg.WebJsonDB;
import org.junit.*;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import com.sun.net.httpserver.HttpServer;

public class Test_WebJsonDB {
	
	static HttpServer server;
	static String hostname = "127.0.0.1";
	static int port = 8080;
	
	public static void main(String[] args) {
		//start local server
			try {
				server = HttpServer.create(new InetSocketAddress(hostname, port), 0);
			} 
			catch (BindException eb) {
				System.out.println("port " + port + " in use, could not create environment to test. " + eb.toString());
			}
			catch (IOException e1) {
					e1.printStackTrace();
			}
			server.start();
		//run tests
			System.out.println("Running class Test_WebJsonDB");
			System.out.println("This may take a few seconds. Please wait");
			Result res =  org.junit.runner.JUnitCore.runClasses(Test_WebJsonDB.class);
		//print results
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
		//stop server
			server.stop(0);
	}
	
	
	@Test
	public void WebJsonDB_search_S1() {
		//test in a simmilar environment to what is expected
		//tests	: whether * in scheme works correctly
		//		: whether multiple keywords can be used
		//setup
			String response = "[{\"page\":1,\"pages\":1,\"per_page\":100,\"total\":2,\"sourceid\":\"2\",\"sourcename\":\"World Development Indicators\",\"lastupdated\":\"2022-09-16\"},[{\"indicator\":{\"id\":\"SP.POP.TOTL\",\"value\":\"Population, total\"},\"country\":{\"id\":\"CA\",\"value\":\"Canada\"},\"countryiso3code\":\"CAN\",\"date\":\"2020\",\"value\":38037204,\"unit\":\"\",\"obs_status\":\"\",\"decimal\":0},{\"indicator\":{\"id\":\"SP.POP.TOTL\",\"value\":\"Population, total\"},\"country\":{\"id\":\"CA\",\"value\":\"Canada\"},\"countryiso3code\":\"CAN\",\"date\":\"2019\",\"value\":37601230,\"unit\":\"\",\"obs_status\":\"\",\"decimal\":0}]]"; 
			server.createContext("/api/s1", new TestingContext(response));
			String directory = "http://localhost:8080/api/s1";
			String scheme = "[1[*{date,value}]]";
			ArrayList<String> element = new ArrayList<String>();
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} catch (Exception e) {
				fail("test for success ended in failure. Error message: " + e.toString());
			}
		//test output
			String element0 = "[{\"date\":\"2020\",\"value\":38037204},{\"date\":\"2019\",\"value\":37601230}]";
			assertEquals("incorrect number of items in ArrayList", 1, element.size());
			assertEquals("index 0 did not have right value.", element0, element.get(0));
	}
	
	
	@Test
	public void WebJsonDB_search_S2() {
		//tests	: multiple elements for outermost array (0~1)
		//		: whether strings and non-strings are differentiated in output (strings should have ", non-strings like int should not)
		//setup
			String response = "[[{\"a\":1, \"b\":2, \"c\":3}, {\"a\":10,\"b\":20}, {\"a\":100}],  [{\"a\":\"A\"},{\"a\":\"AA\"}],  [{\"x\":4}]]"; 
			server.createContext("/api/s2", new TestingContext(response));
			String directory = "http://localhost:8080/api/s2";
			String scheme = "[0~1[*{a}]]";
			ArrayList<String> element = new ArrayList<String>();
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} catch (Exception e) {
				fail("test for success ended in failure. Error message: " + e.toString() + "\n" + e.getStackTrace());
			}
		//test output
			String element0 = "[{\"a\":1},{\"a\":10},{\"a\":100}]";
			String element1 = "[{\"a\":\"A\"},{\"a\":\"AA\"}]";
			assertEquals("incorrect number of items in ArrayList", 2, element.size());
			assertEquals("index 0 did not have right value.", element0, element.get(0));
			assertEquals("index 1 did not have right value.", element1, element.get(1));
	}
	
	@Test
	public void WebJsonDB_search_S3() {
		//tests	: testing * nested within array
		//setup
			String response = "[[{\"a\":1, \"b\":2, \"c\":3}, {\"a\":10,\"b\":20}, {\"a\":100}, \"xyz\"],  [{\"a\":\"A\"},{\"a\":\"AA\"}],  [{\"x\":4}]]"; 
			server.createContext("/api/s3", new TestingContext(response));
			String directory = "http://localhost:8080/api/s3";
			String scheme = "[0~1[*]]";
			ArrayList<String> element = new ArrayList<String>();
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} catch (Exception e) {
				fail("test for success ended in failure. Error message: " + e.toString() + "\n" + e.getStackTrace());
			}
		//test output
			String element0 = "[{\"a\":1,\"b\":2,\"c\":3},{\"a\":10,\"b\":20},{\"a\":100},\"xyz\"]";
			String element1 = "[{\"a\":\"A\"},{\"a\":\"AA\"}]";
			assertEquals("incorrect number of items in ArrayList", 2, element.size());
			assertEquals("index 0 did not have right value.", element0, element.get(0));
			assertEquals("index 1 did not have right value.", element1, element.get(1));
	}
	
	@Test
	public void WebJsonDB_search_S4() {
		//tests	: scheme is "[*]", aka return everything inside the outermost array
		//setup
			String response = "[[{\"a\":1, \"b\":2, \"c\":3}, {\"a\":10,\"b\":20}, {\"a\":100}, \"xyz\"],  [{\"a\":\"A\"},{\"a\":\"AA\"}],  [{\"x\":4}]]"; 
			server.createContext("/api/s4", new TestingContext(response));
			String directory = "http://localhost:8080/api/s4";
			String scheme = "[*]";
			ArrayList<String> element = new ArrayList<String>();
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} catch (Exception e) {
				fail("test for success ended in failure. Error message: " + e.toString() + "\n" + e.getStackTrace());
			}
		//test output
			String element0 = "[{\"a\":1,\"b\":2,\"c\":3},{\"a\":10,\"b\":20},{\"a\":100},\"xyz\"]";
			String element1 = "[{\"a\":\"A\"},{\"a\":\"AA\"}]";
			String element2 = "[{\"x\":4}]";
			assertEquals("incorrect number of items in ArrayList", 3, element.size());
			assertEquals("index 0 did not have right value.", element0, element.get(0));
			assertEquals("index 1 did not have right value.", element1, element.get(1));
			assertEquals("index 2 did not have right value.", element2, element.get(2));
	}
	
	@Test
	public void WebJsonDB_search_S5() {
		//tests : scheme is "[]", aka return an array everything in a single string
		//setup
			String response = "[[{\"a\":1, \"b\":2, \"c\":3}, {\"a\":10,\"b\":20}, {\"a\":100}, \"xyz\"],  [{\"a\":\"A\"},{\"a\":\"AA\"}],  [{\"x\":4}]]"; 
			server.createContext("/api/s5", new TestingContext(response));
			String directory = "http://localhost:8080/api/s5";
			String scheme = "[]";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
					element = testclass.search(directory, scheme);
			} catch (Exception e) {
					fail("test for success ended in failure. Error message: " + e.toString() + "\n" + e.getStackTrace());
			}
		//test output
			assertEquals("incorrect number of items in ArrayList", 0, element.size());
			assertFalse("no ArrayList was returned, potentially threw an error", element==elementAlias);
	}
	
	@Test
	public void WebJsonDB_search_S6() {
		//tests : scheme is "", special case to return everyting in a single String
		//setup
			String response = "[[{\"a\":1, \"b\":2, \"c\":3}, {\"a\":10,\"b\":20}, {\"a\":100}, \"xyz\"],  [{\"a\":\"A\"},{\"a\":\"AA\"}],  [{\"x\":4}]]"; 
			server.createContext("/api/s6", new TestingContext(response));
			String directory = "http://localhost:8080/api/s6";
			String scheme = "";
			ArrayList<String> element = new ArrayList<String>();
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
					element = testclass.search(directory, scheme);
			} catch (Exception e) {
					fail("test for success ended in failure. Error message: " + e.toString() + "\n" + e.getStackTrace());
			}
		//test output
			String element0 = "[{\"a\":1,\"b\":2,\"c\":3},{\"a\":10,\"b\":20},{\"a\":100},\"xyz\"],[{\"a\":\"A\"},{\"a\":\"AA\"}],[{\"x\":4}]";
			assertEquals("incorrect number of items in ArrayList", 1, element.size());
			assertEquals("index 0 did not have right value.", element0, element.get(0));
	}
	
	
	@Test
	public void WebJsonDB_search_F1_schemeNull(){
		//tests: scheme is null
		//setup
			String response = "[[{\"a\":1, \"b\":2, \"c\":3}, {\"a\":10,\"b\":20}, {\"a\":100}, \"xyz\"],  [{\"a\":\"A\"},{\"a\":\"AA\"}],  [{\"x\":4}]]"; 
			server.createContext("/api/f1", new TestingContext(response));
			String directory = "http://localhost:8080/api/f2";
			String scheme = null;
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (IllegalArgumentException e1) {
				assertEquals("correct type of exception thrown, but not for the correct reason", "class WebJsonDB: scheme was set to null", e1.getMessage());
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}
	
	@Test
	public void WebJsonDB_search_F2a_InvalidDirectory(){
		//tests: directory is not a URI
		//setup
			//String response = "[[{\"a\":1, \"b\":2, \"c\":3}, {\"a\":10,\"b\":20}, {\"a\":100}, \"xyz\"],  [{\"a\":\"A\"},{\"a\":\"AA\"}],  [{\"x\":4}]]"; 
			//server.createContext("/api/f2", new TestingContext(response));
			String directory = "notAdirectory";
			String scheme = "[*]";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an FileNotFoundException
		//test output
			catch (FileNotFoundException e1) {
				assertEquals("correct type of exception thrown, but not for the correct reason", "class WebJsonDB: the directory was invalid as a URI", e1.getMessage());
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}
	
	
	@Test
	public void WebJsonDB_search_F2b_InvalidDirectory(){
		//tests: directory is a URI, but does not connect to anywhere
		//setup
			//String response = "[[{\"a\":1, \"b\":2, \"c\":3}, {\"a\":10,\"b\":20}, {\"a\":100}, \"xyz\"],  [{\"a\":\"A\"},{\"a\":\"AA\"}],  [{\"x\":4}]]"; 
			//server.createContext("/api/f2", new TestingContext(response));
			String directory = "https://notAdirectory";
			String scheme = "[*]";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (IOException e1) {
				//expecting: "class WebJsonDB: " + e1.toString() + ", possibly the specified directory \"" + directory +  "\" was incorrect?"
				String expect = "possibly the specified directory \"https://notAdirectory\" was incorrect?";
				assertTrue("correct type of exception thrown, but not for the correct reason. Error message: " + e1.getMessage(), e1.getMessage().indexOf(expect) > 17);
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}
	
	@Test
	public void WebJsonDB_schemeSelector_F1_contentNotJSON(){
		//tests: the response form the server is not in json notation
		//setup
			String response = "\"notJson"; 
			server.createContext("/api/ss/f1", new TestingContext(response));
			String directory = "http://localhost:8080/api/ss/f1";
			String scheme = "[*]";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (IllegalArgumentException e1) {
				assertEquals("correct type of exception thrown, but not for the correct reason", 
						"class WebJsonDB: the content returned by the query was not in valid JSON form. Error at \"[*]\"", 
						e1.getMessage());
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}
	
	@Test
	public void WebJsonDB_schemeSelector_F2a_schemeNotMatching(){
		//tests: the scheme and content do not match, scheme says [] but content is {}
		//setup
			String response = "[ {\"item\":1} ]"; 
			server.createContext("/api/ss/f2a", new TestingContext(response));
			String directory = "http://localhost:8080/api/ss/f2a";
			String scheme = "[0[*]]";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (IllegalArgumentException e1) {
				assertEquals("correct type of exception thrown, but not for the correct reason", 
						"class WebJsonDB: the scheme and the content returned by the query did not match. Error at \"[*]\"", 
						e1.getMessage());
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}
	
	@Test
	public void WebJsonDB_schemeSelector_F2b_schemeNotMatching(){
		//tests: the scheme and content do not match, scheme says {} but content is []
		//setup
			String response = "[ [1,2,3,4] ]"; 
			server.createContext("/api/ss/f2b", new TestingContext(response));
			String directory = "http://localhost:8080/api/ss/f2b";
			String scheme = "[0{\"item\"}]";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (IllegalArgumentException e1) {
				assertEquals("correct type of exception thrown, but not for the correct reason", 
						"class WebJsonDB: the scheme and the content returned by the query did not match. Error at \"{\"item\"}\"", 
						e1.getMessage());
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}
	
	
	@Test
	public void WebJsonDB_schemeSelector_F3a_schemeInvalid(){
		//tests: the scheme does not follow specified syntax
		//setup
			String response = "[[{\"a\":1, \"b\":2, \"c\":3}, {\"a\":10,\"b\":20}, {\"a\":100}, \"xyz\"],  [{\"a\":\"A\"},{\"a\":\"AA\"}],  [{\"x\":4}]]"; 
			server.createContext("/api/ss/f3a", new TestingContext(response));
			String directory = "http://localhost:8080/api/ss/f3a";
			String scheme = "invalidScheme";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (IllegalArgumentException e1) {
				assertEquals("correct type of exception thrown, but not for the correct reason", 
						"class WebJsonDB: invalid scheme detected. Error at \"invalidScheme\"", 
						e1.getMessage());
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}
	
	@Test
	public void WebJsonDB_schemeSelector_F3b_schemeInvalid(){
		//tests: the scheme does not have a valid end to a section, unclosed []
		//setup
			String response = "[[{\"a\":1, \"b\":2, \"c\":3}, {\"a\":10,\"b\":20}, {\"a\":100}, \"xyz\"],  [{\"a\":\"A\"},{\"a\":\"AA\"}],  [{\"x\":4}]]"; 
			server.createContext("/api/ss/f3b", new TestingContext(response));
			String directory = "http://localhost:8080/api/ss/f3b";
			String scheme = "[0[2]";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (IllegalArgumentException e1) {
				assertEquals("correct type of exception thrown, but not for the correct reason", 
						"class WebJsonDB: invalid scheme detected. Error at \"index 2 of \"[2\"\"", 
						e1.getMessage());
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}
	
	
	@Test
	public void WebJsonDB_schemeSelector_F3c_schemeInvalid(){
		//tests: the scheme does not have a valid end to a section, unclosed {}, not ending with ","
		//setup
			String response = "[[{\"a\":1, \"b\":2, \"c\":3}, {\"a\":10,\"b\":20}, {\"a\":100}, \"xyz\"],  [{\"a\":\"A\"},{\"a\":\"AA\"}],  [{\"x\":4}]]"; 
			server.createContext("/api/ss/f3c", new TestingContext(response));
			String directory = "http://localhost:8080/api/ss/f3c";
			String scheme = "[0[2{a]]";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (IllegalArgumentException e1) {
				assertEquals("correct type of exception thrown, but not for the correct reason", 
						"class WebJsonDB: invalid scheme detected. Error at \"index 1 of \"{a\"\"", 
						e1.getMessage());
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}

	
	@Test
	public void WebJsonDB_schemeSelector_F3d_schemeInvalid(){
		//tests: the scheme does not have a valid end to a section, unclosed {}, ending with ","
		//setup
			String response = "[[{\"a\":1, \"b\":2, \"c\":3}, {\"a\":10,\"b\":20}, {\"a\":100}, \"xyz\"],  [{\"a\":\"A\"},{\"a\":\"AA\"}],  [{\"x\":4}]]"; 
			server.createContext("/api/ss/f3d", new TestingContext(response));
			String directory = "http://localhost:8080/api/ss/f3d";
			String scheme = "[0[2{a,]]";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (IllegalArgumentException e1) {
				assertEquals("correct type of exception thrown, but not for the correct reason", 
						"class WebJsonDB: invalid scheme detected. Error at \"index 3 of \"{a,\"\"", 
						e1.getMessage());
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}
	
	
	@Test
	public void WebJsonDB_schemeSelector_F3e_schemeInvalid(){
		//tests: the starting index (2) is greater than the ending index (0)
		//setup
			String response = "[[{\"a\":1, \"b\":2, \"c\":3}, {\"a\":10,\"b\":20}, {\"a\":100}, \"xyz\"],  [{\"a\":\"A\"},{\"a\":\"AA\"}],  [{\"x\":4}]]"; 
			server.createContext("/api/ss/f3e", new TestingContext(response));
			String directory = "http://localhost:8080/api/ss/f3e";
			String scheme = "[2~0[*]]";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (IllegalArgumentException e1) {
				assertEquals("correct type of exception thrown, but not for the correct reason", 
						"class WebJsonDB: invalid scheme detected, starting index was greater than ending index. Error at \"index 4 of \"[2~0[*]]\"\"", 
						e1.getMessage());
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}
	
	
	@Test
	public void WebJsonDB_schemeSelector_F3f_schemeInvalid(){
		//tests: character after * in scheme
		//setup
			String response = "[[{\"a\":1, \"b\":2, \"c\":3}, {\"a\":10,\"b\":20}, {\"a\":100}, \"xyz\"],  [{\"a\":\"A\"},{\"a\":\"AA\"}],  [{\"x\":4}]]"; 
			server.createContext("/api/ss/f3f", new TestingContext(response));
			String directory = "http://localhost:8080/api/ss/f3f";
			String scheme = "[*2[*]]";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (IllegalArgumentException e1) {
				assertEquals("correct type of exception thrown, but not for the correct reason", 
						"class WebJsonDB: invalid scheme detected, character encountered after \"*\". Error at \"index 2 of \"[*2[*]]\"\"", 
						e1.getMessage());
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}
	
	@Test
	public void WebJsonDB_schemeSelector_F3g_schemeInvalid(){
		//tests: multiple ~ in in scheme
		//setup
			String response = "[[{\"a\":1, \"b\":2, \"c\":3}, {\"a\":10,\"b\":20}, {\"a\":100}, \"xyz\"],  [{\"a\":\"A\"},{\"a\":\"AA\"}],  [{\"x\":4}]]"; 
			server.createContext("/api/ss/f3g", new TestingContext(response));
			String directory = "http://localhost:8080/api/ss/f3g";
			String scheme = "[0~1~2]";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (IllegalArgumentException e1) {
				assertEquals("correct type of exception thrown, but not for the correct reason", 
						"class WebJsonDB: invalid scheme detected, multiple \"~\" encountered. Error at \"index 4 of \"[0~1~2]\"\"", 
						e1.getMessage());
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}
	
	@Test
	public void WebJsonDB_schemeSelector_F3h_schemeInvalid(){
		//tests: invalid character in scheme
		//setup
			String response = "[[{\"a\":1, \"b\":2, \"c\":3}, {\"a\":10,\"b\":20}, {\"a\":100}, \"xyz\"],  [{\"a\":\"A\"},{\"a\":\"AA\"}],  [{\"x\":4}]]"; 
			server.createContext("/api/ss/f3h", new TestingContext(response));
			String directory = "http://localhost:8080/api/ss/f3h";
			String scheme = "[A]";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (IllegalArgumentException e1) {
				assertEquals("correct type of exception thrown, but not for the correct reason", 
						"class WebJsonDB: invalid character found in scheme. Error at \"index 1 of \"[A]\"\"", 
						e1.getMessage());
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}
	
	
	@Test
	public void WebJsonDB_schemeSelector_F3i_schemeInvalid(){
		//tests: range out of bounds in scheme
		//setup
			String response = "[[{\"a\":1, \"b\":2, \"c\":3}, {\"a\":10,\"b\":20}, {\"a\":100}, \"xyz\"],  [{\"a\":\"A\"},{\"a\":\"AA\"}],  [{\"x\":4}]]"; 
			server.createContext("/api/ss/f3i", new TestingContext(response));
			String directory = "http://localhost:8080/api/ss/f3i";
			String scheme = "[2~5]";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (IllegalArgumentException e1) {
				assertEquals("correct type of exception thrown, but not for the correct reason", 
						"class WebJsonDB: invalid scheme detected, specified range falls outside the JSONArray. Error at \"index 3\"",
						e1.getMessage());
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}
	
	
	@Test
	public void WebJsonDB_schemeSelector_F3j_schemeInvalid(){
		//tests: range out of bounds in scheme
		//setup
			String response = "[[{\"a\":1, \"b\":2, \"c\":3}, {\"a\":10,\"b\":20}, {\"a\":100}, \"xyz\"],  [{\"a\":\"A\"},{\"a\":\"AA\"}],  [{\"x\":4}]]"; 
			server.createContext("/api/ss/f3j", new TestingContext(response));
			String directory = "http://localhost:8080/api/ss/f3j";
			String scheme = "[0[0{z}]]";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (IllegalArgumentException e1) {
				assertEquals("correct type of exception thrown, but not for the correct reason", 
						"class WebJsonDB: invalid scheme detected, specified keyword was not found in JSONObject. Error at \"keyword \"z\" of scheme \"{z}\"\"",
						e1.getMessage());
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}
	
	@Test
	public void WebJsonDB_search_F3_(){
		//tests: the scheme specifies a non-JSONArray (eg: a JSONObject)
		//setup
			String response = "{\"a\":1,\"b\":2}"; 
			server.createContext("/api/f3", new TestingContext(response));
			String directory = "http://localhost:8080/api/f3";
			String scheme = "{a,b}";
			ArrayList<String> element = new ArrayList<String>();
			ArrayList<String> elementAlias = element;
			WebJsonDB testclass = new WebJsonDB();
		//execute
			try {
				element = testclass.search(directory, scheme);
			} //expects an IllegalArgumentException
		//test output
			catch (IllegalArgumentException e1) {
				assertEquals("correct type of exception thrown, but not for the correct reason", "class WebJsonDB: the final form of the JSON (after going through scheme) was not in JSONArray form", e1.getMessage());
			}
			catch (Exception e2) {
				fail("unexpected type of exception was thrown. Error Message: " + e2.toString());
			}
			assertEquals("Arraylist was modified when it shouldn't have", element.size(), 0);
			assertTrue("Arraylist was modified when it shouldn't have", element == elementAlias);
	}
}
