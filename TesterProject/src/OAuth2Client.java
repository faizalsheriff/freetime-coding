import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
/*
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonToken;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.JsonMappingException;

import com.fasterxml.jackson.core.JsonParser;*/


public class OAuth2Client {

	public static void main(String[] args) {
	    try {
	        URL url;
	        URLConnection urlConnection;
	        DataOutputStream outStream;
	        DataInputStream inStream;
	 
	        // Build request body
	        
	    
	        
	      
	        		
	        		
	        String body =
	        "code=" + URLEncoder.encode("4/IJoE7r1yHNNSO9JRdEVdFZuF9sfj.IhOhpASHlW8UshQV0ieZDArEmwmgfQI","UTF-8")+
	        "&client_id=" + "650214226023-00mto9qecuuh39a0hq8r0jsg1876k1ci.apps.googleusercontent.com" +
	        "&client_secret=" + "{7Ok1-MhXq8ChdqJh6qthnbs2}" +
	        "&redirect_uri=" +"urn:ietf:wg:oauth:2.0:oob" +
	        "&grant_type=" + "authorization_code";
	  	    
	        
	        System.out.println(body);
	        // Create connection
	        url = new URL("https://accounts.google.com/o/oauth2/token");
	        urlConnection = url.openConnection();
	        ((HttpURLConnection)urlConnection).setRequestMethod("POST");
	        
	       // urlConnection.setDoInput(true);
	        urlConnection.setDoOutput(true);
	        //urlConnection.setUseCaches(false);
	        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        urlConnection.setRequestProperty("Content-Length", ""+ body.length());
	 
	        // Create I/O streams
	        outStream = new DataOutputStream(urlConnection.getOutputStream());
	        
	 System.out.println(outStream);
	        // Send request
	        outStream.writeBytes(body);
	        outStream.flush();
	        outStream.close();
	 
	        Thread.sleep(1000);
	        
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
	       // builder.append(">");
	        System.out.println(builder.toString());
	       // parseJson(builder.toString());
	       
	           
	        // Close I/O streams
	        inStream.close();
	        outStream.close();
	        
	      
	    }
	    catch(Exception ex) {
	    	ex.printStackTrace();
	        System.out.println("Exception cought:\n"+ ex.toString());
	    }
	}	
	
	
	
	/*public static void parseJson(String jsonString) throws JsonParseException, IOException{
		System.out.println("---Parsing JSON ----");
		JsonFactory jfactory = new JsonFactory();
		
		*//*** read from file ***//*
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
*/
	
}



