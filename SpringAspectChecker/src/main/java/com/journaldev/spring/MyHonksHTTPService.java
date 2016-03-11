package com.journaldev.spring;




import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;


public class MyHonksHTTPService {

	private URL url;
	private URLConnection urlConnection;
	private DataOutputStream outStream;
	private DataInputStream inStream;
	
	
	public String post(HashMap<String, String> payloadData,
			String googleTokenUrl) throws IOException {
		StringBuilder strBuilder = new StringBuilder();
		
		for (Map.Entry<String, String> entry : payloadData.entrySet()) {
			System.out.println("Key : " + entry.getKey() + " Value : "
				+ entry.getValue());
			strBuilder.append(entry.getKey());
			strBuilder.append("=");
			strBuilder.append( URLEncoder.encode(entry.getValue(),"UTF-8"));
			strBuilder.append("&");
			}

		strBuilder.deleteCharAt(strBuilder.length()-1);
		
		System.out.println("<---Body-->\n"+strBuilder.toString()+"\n<---- URL ---->"+googleTokenUrl);
        
        // Create connection
        url = new URL(googleTokenUrl);
        urlConnection = url.openConnection();
        ((HttpURLConnection)urlConnection).setRequestMethod("POST");
        
        urlConnection.setDoInput(true);
        urlConnection.setDoOutput(true);
        urlConnection.setUseCaches(false);
        urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        urlConnection.setRequestProperty("Content-Length", ""+ strBuilder.toString().length());
 
        // Create I/O streams
        outStream = new DataOutputStream(urlConnection.getOutputStream());
        
 
        // Send request
        outStream.writeBytes(strBuilder.toString());
        outStream.flush();
        outStream.close();
 
        // Get Response
        // - For debugging purposes only!
        String buffer;
        StringBuilder builder = new StringBuilder();
        if (((HttpURLConnection) urlConnection).getResponseCode() == 200) {
          
        System.out.println("<---Success---->");
        inStream = new DataInputStream(urlConnection.getInputStream());
     
        
        while((buffer = inStream.readLine()) != null) {
           
            builder.append(buffer);
          
        }
     
        System.out.println(builder.toString());
        
        } else {
        	
        	
        	 System.out.println("<---Error We Think---->");
        	 inStream = new DataInputStream(((HttpURLConnection) urlConnection).getErrorStream());
           
             
             while((buffer = inStream.readLine()) != null) {
                
                 builder.append(buffer);
               
             }
       
             System.out.println(builder.toString());
        	
        }
   
        // Close I/O streams
        inStream.close();
        outStream.close();
        
        return builder.toString();
    }
 

	public String get(/*HashMap<String, String> payloadData,
			String googleContactsUrl*/) throws ClientProtocolException, IOException{
		String responseBody="";
//		HttpClient httpclient = new DefaultHttpClient();
//        try {
//        	
//        	HttpGet httpget = new HttpGet("https://www.google.com/m8/feeds/contacts/default/full/");
//        	  
//        	  
//        	for (Map.Entry<String, String> entry : payloadData.entrySet()) {
//    			System.out.println("Key : " + entry.getKey() + " Value : "
//    				+ entry.getValue());
//    			httpget.addHeader(entry.getKey(), entry.getValue());
//    			}
//        	System.out.println("executing request " + httpget.getURI());
//        	Header[] headers = httpget.getAllHeaders();
//            for(Header temp: headers){
//            	System.out.println(temp.getName()+"::"+temp.getValue());
//            }
//            // Create a response handler
//            ResponseHandler<String> responseHandler = new BasicResponseHandler();
//           
//		
//			responseBody = httpclient.execute(httpget, responseHandler);
//			
//            System.out.println("----------------------------------------");
//            System.out.println(responseBody);
//            System.out.println("----------------------------------------");
//
//       
//			
//        } catch (HttpResponseException e) {
//			
//			e.printStackTrace();
//			throw new TokenExpiredException();
//		}catch (ClientProtocolException e) {
//			
//			e.printStackTrace();
//		} catch (IOException e) {
//			
//			e.printStackTrace();
//		}catch (Exception e) {
//			
//			e.printStackTrace();
//		}finally {
//            // When HttpClient instance is no longer needed,
//            // shut down the connection manager to ensure
//            // immediate deallocation of all system resources
//			System.out.println("Finally Block");
//            httpclient.getConnectionManager().shutdown();
//        }
		
		
		
		 HttpClient httpclient = new DefaultHttpClient();
		 ResponseHandler<String> responseHandler = new BasicResponseHandler();
	        try {
	            HttpGet httpget = new HttpGet("https://www.google.com/m8/feeds/contacts/default/full?alt=json");
	            
	            httpget.addHeader("Authorization", "OAuth ya29.AHES6ZRPmOdPH5NegD3vQByXUe_1L2ikXZEbECGPEkwXiDxzPLg9oi8");
	            httpget.addHeader("Gdata-version", "3.0");
	            

	            System.out.println("executing request " + httpget.getURI());

	            Header[] headers = httpget.getAllHeaders();
	            for(Header temp: headers){
	            	System.out.println(temp.getName());
	            }
	            // Create a response handler
	           
	            responseBody = httpclient.execute(httpget, responseHandler);
	            System.out.println("----------------------------------------");
	            System.out.println(responseBody);
	            System.out.println("----------------------------------------");

	        }catch (HttpResponseException e) {
				
				e.printStackTrace();
				//throw new TokenExpiredException();
			}catch (ClientProtocolException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}catch (Exception e) {
				
				e.printStackTrace();
			} finally {
//	             When HttpClient instance is no longer needed,
//	             shut down the connection manager to ensure
//	             immediate deallocation of all system resources
				System.out.println("finally");
				
				System.out.println(responseBody);
				
	            httpclient.getConnectionManager().shutdown();
	        }
		return responseBody;
	}
	
	
	
	public String get1() throws ClientProtocolException, IOException{
		String responseBody="";
	
		
		 HttpClient httpclient = new DefaultHttpClient();
		 ResponseHandler<String> responseHandler = new BasicResponseHandler();
	        try {
	            HttpGet httpget = new HttpGet("http://api.rottentomatoes.com/api/public/v1.0/movies.json?apikey=3mm9wqu9fpzdtakd2s7q87nj&q=Toy+Story+3&page_limit=1");
	            
	            //httpget.addHeader("Authorization", "OAuth ya29.AHES6ZRPmOdPH5NegD3vQByXUe_1L2ikXZEbECGPEkwXiDxzPLg9oi8");
	           // httpget.addHeader("Gdata-version", "3.0");
	            

	            /*System.out.println("executing request " + httpget.getURI());

	            Header[] headers = httpget.getAllHeaders();
	            for(Header temp: headers){
	            	System.out.println(temp.getName());
	            }*/
	            // Create a response handler
	           
	            responseBody = httpclient.execute(httpget, responseHandler);
	            System.out.println("----------------------------------------");
	            System.out.println(responseBody);
	            System.out.println("----------------------------------------");

	        }catch (HttpResponseException e) {
				
				e.printStackTrace();
				//throw new TokenExpiredException();
			}catch (ClientProtocolException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				
				e.printStackTrace();
			}catch (Exception e) {
				
				e.printStackTrace();
			} finally {
//	             When HttpClient instance is no longer needed,
//	             shut down the connection manager to ensure
//	             immediate deallocation of all system resources
				System.out.println("finally");
				
				System.out.println(responseBody);
				
	            httpclient.getConnectionManager().shutdown();
	        }
		return responseBody;
	}
	
	
	
	 public final static void main(String[] args) throws Exception {

	       /* HttpClient httpclient = new DefaultHttpClient();
	        try {
	            HttpGet httpget = new HttpGet("https://www.google.com/m8/feeds/contacts/default/full/");
	            
	            httpget.addHeader("Authorization", "OAuth ya29.AHES6ZRPmOdPH5NegD3vQByXUe_1L2ikXZEbECGPEkwXiDxzPLg9oi8");
	            httpget.addHeader("Gdata-version", "3.0");
	            

	            System.out.println("executing request " + httpget.getURI());

	            Header[] headers = httpget.getAllHeaders();
	            for(Header temp: headers){
	            	System.out.println(temp.getName());
	            }
	            // Create a response handler
	            ResponseHandler<String> responseHandler = new BasicResponseHandler();
	            String responseBody = httpclient.execute(httpget, responseHandler);
	            System.out.println("----------------------------------------");
	            System.out.println(responseBody);
	            System.out.println("----------------------------------------");

	        } finally {
	            // When HttpClient instance is no longer needed,
	            // shut down the connection manager to ensure
	            // immediate deallocation of all system resources
	            httpclient.getConnectionManager().shutdown();
	        }*/
		 MyHonksHTTPService summa = new MyHonksHTTPService();
	    	summa.get1();
	    	
	    }

}
