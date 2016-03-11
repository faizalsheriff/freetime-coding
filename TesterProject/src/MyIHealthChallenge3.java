import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonToken;
/*import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonMappingException;
*/
import com.fasterxml.jackson.core.JsonParser;


public class MyIHealthChallenge3 {

	public static void main(String[] args) {
	    try {
	        URL url;
	        URLConnection urlConnection;
	        DataOutputStream outStream=null;
	        DataInputStream inStream=null;
	 
	        // Build request body
	        
	        for(int age=40; age<=60; age++){
	        	String strage=String.valueOf(age);
	        	System.out.println("Age============"+strage+"=========================");
	        String body =
	        "age=" + URLEncoder.encode("40", "UTF-8") +
	        "&gender=" + URLEncoder.encode("M", "UTF-8") +
	        "&height=" + URLEncoder.encode("70", "UTF-8") +
	        "&weight=" + URLEncoder.encode("160", "UTF-8") +
	        "&smoker=" + URLEncoder.encode("F", "UTF-8") +
	        "&mi=" + URLEncoder.encode("F", "UTF-8") +
	        "&stroke=" + URLEncoder.encode("F", "UTF-8") +
	        "&diabetes=" + URLEncoder.encode("T", "UTF-8") +
	        "&systolic=" + URLEncoder.encode("133", "UTF-8") +
	        "&diastolic=" + URLEncoder.encode("89", "UTF-8")+
	        "&cholesterol=" + URLEncoder.encode("170", "UTF-8") +
	        "&hdl=" + URLEncoder.encode("100", "UTF-8") +
	        "&ldl=" + URLEncoder.encode("70", "UTF-8") +
	        "&hba1c=" + URLEncoder.encode("", "UTF-8") +
	       // "&triglycerides=" + URLEncoder.encode("", "UTF-8") +
	        "&cholesterolmeds=" + URLEncoder.encode("F", "UTF-8") +
	        "&bloodpressuremeds=" + URLEncoder.encode("F", "UTF-8") +
	        "&bloodpressuremedcount=" + URLEncoder.encode("0", "UTF-8")+
	        "&aspirin=" + URLEncoder.encode("F", "UTF-8") +
	        "&moderateexercise=" + URLEncoder.encode("5", "UTF-8") +
	        "&vigorousexercise=" + URLEncoder.encode("1", "UTF-8") +
	        "&familymihistory=" + URLEncoder.encode("F", "UTF-8");
	        
	  	    
	        
	        System.out.println(body);
	        // Create connection
	        url = new URL("https://demo-indigo4health.archimedesmodel.com/IndiGO4Health/IndiGO4Health");
	        urlConnection = url.openConnection();
	        ((HttpURLConnection)urlConnection).setRequestMethod("POST");
	        
	        urlConnection.setDoInput(true);
	        urlConnection.setDoOutput(true);
	        urlConnection.setUseCaches(false);
	        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        urlConnection.setRequestProperty("Content-Length", ""+ body.length());
	 
	        // Create I/O streams
	        outStream = new DataOutputStream(urlConnection.getOutputStream());
	        
	 
	        // Send request
	        outStream.writeBytes(body);
	        outStream.flush();
	        outStream.close();
	 
	        // Get Response
	        // - For debugging purposes only!
	        inStream = new DataInputStream(urlConnection.getInputStream());
	        String buffer;
	        StringBuilder builder = new StringBuilder();
	        //builder.append("<");
	        while((buffer = inStream.readLine()) != null) {
	            System.out.println(buffer);
	            builder.append(buffer);
	          
	        }
	     
	        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
	        }
	       
	           
	        // Close I/O streams
	        inStream.close();
	        outStream.close();
	        
	      
	    }
	    catch(Exception ex) {
	    	ex.printStackTrace();
	        System.out.println("Exception cought:\n"+ ex.toString());
	    }
	}	
	
	
	
	public static void parseJson(String jsonString) throws JsonParseException, IOException{
		System.out.println("---Parsing JSON ----");
		JsonFactory jfactory = new JsonFactory();
		
		/*** read from file ***/
		JsonParser jParser = jfactory.createJsonParser(jsonString);
	 
		// loop until token equal to "}"
		while (jParser.nextToken() != JsonToken.END_OBJECT) {
	 
			String fieldname = jParser.getCurrentName();
			System.out.println("Current Name is "+ fieldname);
			if(fieldname==null){
				continue;
			}
			if ("ErrorMessageHashMap".equalsIgnoreCase(fieldname.trim())) {
	 
			  // current token is "name",
	                  // move to next, which is "name"'s value
			  jParser.nextToken();
			  System.out.println(jParser.getText()); // display mkyong
	 
			}
	 
			if ("Risk".equals(fieldname)) {
	 
			  // current token is "age", 
	                  // move to next, which is "name"'s value
			  jParser.nextToken();
			  System.out.println(jParser.getIntValue()); // display 29
	 
			}
	 
			if ("messages".equals(fieldname)) {
	 
			  jParser.nextToken(); // current token is "[", move next
	 
			  // messages is array, loop until token equal to "]"
			  while (jParser.nextToken() != JsonToken.END_ARRAY) {
	 
	                     // display msg1, msg2, msg3
			     System.out.println(jParser.getText()); 
	 
			  }
	 
			}
	 
		  }
		  jParser.close();
	}

	
}



