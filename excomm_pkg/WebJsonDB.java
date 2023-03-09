package excomm_pkg;
import java.util.ArrayList;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.*;

public class WebJsonDB implements InputTypeStrategy {

	@Override
	public ArrayList<String> search(String directory, String scheme) throws Exception {
		/* implementation details
        	directory: The url to make the querey in the api
        	scheme: the location and values to be returned
        		format: [n] = get nth item in array, [*] = get all items, [n~m] = get n to mth (inclusive) item in array
        	 			{keyword} = get the value with "keyword", {keyword1, keyword2} = get an object with the values of "keyword1" and "keyword2"
        	 			[n{keyword}] = from the nth object in the array, get the value with "keyword"
        	 			"" (empty string) = special case used to return everyting in a single String
        	 	eg: [1[0~2{key}]]
        	 		Get the first element of the array. The element should be an JSONarray.
        	 		Within that array, get the value associated with "key" for the 0th to 2nd elements, which should be JSONObjects
        	 		eg: returns ArrayList<String> = [ "[{"key":\"num0of1stArray\"},{\"key\":\"num1of1stArray\"},{\"key\", \"num2of1stArray\"}" ];
        	 		-> only one element as outermost array in scheme is a single number
        	 	eg: [0~2[1{key}]]
        	 		Get the 0th to 2nd element of the array. The elements should be JSONarrays.
        	 		Within each array, get the value associated with "key" for the 1st element, which should be a JSONObject
        	 		eg: returns ArrayList<String> = [ "[{\"key\":\"num1of1stArray\"}]" , "[{\"key\":\"num1of2ndArray\"}]" , "[{\"key\":\"num1of1stArray\"}]"  ];
        	 		-> 3 elements as outermost array in scheme is 0~2 => 0,1,2
        	 	eg: [0{key0,key1}]
        	 		Of the 0th element in the array, return the values associated with "key0" and "key1"
        	 		eg: returns ArrayList<String> = [ "{\"key0\":\"val0\"},{\"key1\":\"val1\"}" ];
        	 		-> only one element as outermost array in scheme is a single number
		*/
		
		//edgecase: scheme was null
			if(scheme == null) { throw new IllegalArgumentException("class WebJsonDB: scheme was set to null"); }
		//set up Http elements
			HttpClient cli = HttpClient.newHttpClient();
			HttpRequest req = null;
			try {
				req = HttpRequest.newBuilder()
					.uri(URI.create(directory))
		            .build();
			}
			catch(NullPointerException eUri1) { throw new FileNotFoundException("class WebJsonDB: directory was set to null"); }
			catch(IllegalArgumentException eUri2) { throw new FileNotFoundException("class WebJsonDB: the directory was invalid as a URI"); }
		//querey online database
			HttpResponse<String> resp = null;
			try {
				resp = cli.send(req, HttpResponse.BodyHandlers.ofString());
			} 
			catch (IOException e1) { throw new IOException("class WebJsonDB: " + e1.toString() + ", possibly the specified directory \"" + directory +  "\" was incorrect?"); }
			catch(SecurityException e2) { throw new SecurityException("class WebJsonDB: A security manager has denied access to the specified directory \"" + directory + "\""); }
			catch(InterruptedException e3) { throw new InterruptedException("class WebJsonDB: the send() operation was interrupted"); }
			catch(Exception e) { throw new Exception("class WebJsonDB: unspecified error has occured, " + e.toString()); }
			JSONTokener content = new JSONTokener(resp.body());
		//special case: scheme == "", return content in a single String
			if(scheme == "") {
				ArrayList<String> spRet = new ArrayList<String>(1);
				String spString = "";
				JSONArray spJArray = (JSONArray)content.nextValue();
				for(int i=0; i<spJArray.length(); i++){ spString += spJArray.get(i).toString() + ","; }
				spRet.add(spString.substring(0, spString.length()-1));
				return(spRet);
			}
		//get specified elements provided by scheme
			JSONTokener finalContent = schemeSelector(scheme, content);
			//JSONTokener should be in the form of a JSONArray
		//convert JSONTokener finalContent to ArrayList<String>
			ArrayList<String> retArr = new ArrayList<String>();
			JSONArray jArray = null;
			try {
				jArray = (JSONArray)finalContent.nextValue();
			}
			catch(JSONException ej) { throw new Exception("class WebJsonDB: unexpected error has occured, " + ej.toString()); }
			catch(ClassCastException ec) { throw new IllegalArgumentException("class WebJsonDB: the final form of the JSON (after going through scheme) was not in JSONArray form"); }
			for(int i=0; i<jArray.length(); i++) {
				retArr.add(jArray.get(i).toString());
			}
		//all done, return ArrayList
			return(retArr);
	}

	
	
	private JSONTokener schemeSelector(String scheme, JSONTokener content) throws IllegalArgumentException{
		//check if scheme is empty
			if(scheme.length() == 0) {
				return(content);
			}
		//get next instruction from scheme
			char c = scheme.charAt(0);
		//next instruction is an JSONArray
			if(c == '[') {
				//get the JSONArray from content
					JSONArray jArray = null;
					try {
						jArray = (JSONArray)content.nextValue();
					}
					catch(JSONException ej) { tokenerError("the content returned by the query was not in valid JSON form.", scheme); }
					catch(ClassCastException ec) { tokenerError("the scheme and the content returned by the query did not match.", scheme); }
				//get which elements to extract from parsing scheme
					int splitCount = 0;
					boolean flag = true;
					boolean tildeCheck = false;
					boolean starCheck = false;
					int startInt = -1; 
					int finInt = -1;
					while(flag) {
						splitCount++;
						if(splitCount > scheme.length()-1){ tokenerError("invalid scheme detected.",  "index " + splitCount + " of \"" + scheme + "\""); }
						char splitChar = scheme.charAt(splitCount);
						if(splitChar == '{' || splitChar == '[' || splitChar == ']') {
							//valid end to section found
							flag = false;
							if(!tildeCheck && !starCheck){ finInt = startInt; }  
							if(startInt > finInt){ tokenerError("invalid scheme detected, starting index was greater than ending index.", "index " + splitCount + " of \"" + scheme + "\""); } 
						}
						else if(starCheck){ 
							//while loop must have a valid end to section immediately after an *
							tokenerError("invalid scheme detected, character encountered after \"*\".", "index " + splitCount + " of \"" + scheme + "\"");
						}
						else if(splitChar == '~'){
							//tilde - character splitting the index to start from and end at
							if(tildeCheck) { tokenerError("invalid scheme detected, multiple \"~\" encountered.", "index " + splitCount + " of \"" + scheme + "\""); } 
							tildeCheck = true;
						}
						else if(splitChar == '*'){
							//asterisk - character meaing all
							if(!tildeCheck){ startInt = 0; finInt = jArray.length()-1; starCheck = true; }
							else{ finInt = jArray.length()-1; starCheck = true; }
						}
						else if(Character.isDigit(splitChar)){
							//number character
							if(!tildeCheck){ 
								if(startInt == -1) { startInt = 0;} 
								startInt = startInt*10 + Character.getNumericValue(splitChar); 
							}
							else {
								if(finInt == -1) { finInt = 0; }
								finInt = finInt*10 + Character.getNumericValue(splitChar);
							}
						}
						else {
							//error: unexpected character
							tokenerError("invalid character found in scheme.", "index " + splitCount + " of \"" + scheme + "\"");
						}
					}
				//reformat scheme to cut out the executed command
					String reformatScheme = scheme.substring(splitCount, scheme.length()-1);
				//get the appropriate JSONArray based on scheme
					int i = startInt;
					String compilation = "[";
					JSONTokener reformatSubContent;
					while(i<=finInt && i>=0) {
						JSONTokener subContent = null;
						try {
							subContent = new JSONTokener(jArray.get(i).toString());
						}
						catch(JSONException ej){ tokenerError("invalid scheme detected, specified range falls outside the JSONArray.", "index " + i); }
						//recursive call
						reformatSubContent = schemeSelector(reformatScheme, subContent);
						compilation += reformatSubContent.nextValue().toString() + ",";
						i++;
					}
					compilation += "]";
				//finished, return final product
					return(new JSONTokener(compilation));
			}
		//next instruction is a JSONObject
			else if(c == '{') {
				//get the JSONObject from content
					JSONObject jObj = null;
					try {
						jObj = (JSONObject)content.nextValue();
					}
					catch(JSONException ej) { tokenerError("the content returned by the query was not in valid JSON form.", scheme); }
					catch(ClassCastException ec) { tokenerError("the scheme and the content returned by the query did not match.", scheme); }
				//get which elements to extract from parsing scheme
					int splitCount = 0;
					boolean flag = true;
					ArrayList<String> keywords = new ArrayList<String>();
					while(flag) {
						if(splitCount > scheme.length()-1){ tokenerError("invalid scheme detected.",  "index " + splitCount + " of \"" + scheme + "\""); }
						char splitChar = scheme.charAt(splitCount);
						if(splitChar == '}') {
							//valid end to section found
								flag = false;
						}
						else{
							splitCount++;
							int posComma = scheme.indexOf(',', splitCount);
							int posBrase = scheme.indexOf('}', splitCount);
							int posDelimit;
							if (posComma == -1 || (posBrase!=-1 && posBrase < posComma)) { posDelimit = posBrase; }
							else { posDelimit = posComma; }
							if(posDelimit == -1) { tokenerError("invalid scheme detected.", "index " + splitCount + " of \"" + scheme + "\""); }
							keywords.add(scheme.substring(splitCount, posDelimit));
							splitCount = posDelimit;
						}
					}
				//reformat scheme to cut out the executed command
					//TODO not used as the ability to recurse from an object is not implemented
					//thus the below code line is not used
					String reformatScheme = scheme.substring(splitCount, scheme.length()-1);
				//get the appropriate keywords in the JSONObject based on scheme
					String compilation = "{";
					for(int i=0; i<keywords.size(); i++) {
						Object subContent = null;
						String subString = null;
						try {
							subContent = jObj.get(keywords.get(i));
						}
						catch(JSONException ej){ tokenerError("invalid scheme detected, specified keyword was not found in JSONObject.", "keyword \"" + keywords.get(i) + "\" of scheme \"" + scheme + "\""); }
						subString = subContent.toString();
						if(subContent instanceof String) {
							subString = "\"" + subString + "\"";
						}
						if(subContent instanceof JSONObject) {}
						if(subContent instanceof JSONArray) {}
						//TODO: recursion from JSONObject not supported
						compilation += "\"" + keywords.get(i) +"\":" + subString + ",";
					}
					compilation = compilation.substring(0, compilation.length()-1) + "}";
				//finished, return final product
					return(new JSONTokener(compilation));
					
			}
		//error: scheme is invalid
			else {
				tokenerError("invalid scheme detected.", scheme);
			}
		//error: unexpected execution
			tokenerError("unexpected failure of code", scheme);
			//redundant return statement
			return null;
	}
	
	private void tokenerError(String message, String errloc) throws IllegalArgumentException{
		throw new IllegalArgumentException("class WebJsonDB: " + message + " Error at \"" + errloc + "\"");
	}
}
