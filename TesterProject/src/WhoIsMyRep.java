import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;




public class WhoIsMyRep {
	
	private URL url;
	private URLConnection urlConnection;
	private DataOutputStream outStream;
	private DataInputStream inStream;
	
public void whoIsMyRep(int zipcode) {
		
		try {
	      
	        
		
	        String myurl = "http://whoismyrepresentative.com/getall_mems.php?zip="+String.valueOf(zipcode+"&output=json");
	        // Create connection
	        url = new URL(myurl);
	        urlConnection = url.openConnection();
	        ((HttpURLConnection)urlConnection).setRequestMethod("GET");
	        
	        urlConnection.setDoInput(true);
	        urlConnection.setDoOutput(true);
	        urlConnection.setUseCaches(false);
	        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	      //  urlConnection.setRequestProperty("Content-Length", ""+ body.length());
	 
	        // Create I/O streams
	        outStream = new DataOutputStream(urlConnection.getOutputStream());
	        
	 
	        // Send request
	        //outStream.writeBytes(body);
	        outStream.flush();
	        outStream.close();
	 
	        // Get Response
	        // - For debugging purposes only!
	        inStream = new DataInputStream(urlConnection.getInputStream());
	        String buffer;
	        StringBuilder builder = new StringBuilder();
	        //builder.append("<");
	        while((buffer = inStream.readLine()) != null) {
	            //System.out.println(buffer);
	            builder.append(buffer);
	          
	        }
	       // builder.append(">");
	        System.out.println(builder.toString());
	        //parseJson(builder.toString());
	       
	      
	        // Close I/O streams
	        inStream.close();
	        outStream.close();
	        
	      
	    }
	    catch(Exception ex) {
	    	ex.printStackTrace();
	        System.out.println("Exception cought:\n"+ ex.toString());
	    }
		
	}

}
