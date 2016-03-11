




import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;

import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fanho.media.movies.beans.Artist;
import com.fanho.media.movies.beans.MovieView;
import com.fanho.media.movies.beans.Movies;
import com.fanho.media.util.Parser;
import com.netflix.astyanax.examples.AstCQLClient;



@Service
public class MovieInfoRetrieverService {

	
	private static final String BOX_OFFICE_MOVIES = "http://api.rottentomatoes.com/api/public/v1.0/lists/movies/box_office.json?limit=46&country=us&apikey=3mm9wqu9fpzdtakd2s7q87nj";
	private static final String MOVIE_URL = "http://api.rottentomatoes.com/api/public/v1.0/movies.json?apikey=3mm9wqu9fpzdtakd2s7q87nj&page_limit=1&q=";
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	
   //TODO Revisit
	AstCQLClient cassandraDB  = new AstCQLClient();
	
	@PostConstruct
	public void init(){
		logger.info("Inside init method in movie retriever");
		
		//TODO REMOVE
		//cassandraDB.init();
		//cassandraDB.insert(225, 333, "Eric47", "Cartman77");;
		processBoxOfficeMovies();
	}

	private void processBoxOfficeMovies() {
		try {
			
			
			    MovieView mv = getBoxOfficeMovies();
	            if(mv!=null){
	            	logger.info("Movie info is retrieved");
	            	
	            	//TODO Query DB / Mem cache to have the current list of Box office movies and intersect with this one
	                Set<Movies> mySet = new HashSet<Movies>();
	                
	                List<Movies> vendorList = new ArrayList<Movies>();
	                vendorList.addAll(mv.getMovies());
	                
	                
	                for(Movies m: vendorList){
	                Movies details= getMovie(m.getTitle());
					save(details);
	                }
	                
	               // vendorList.removeAll(mySet);
	               /*ExecutorService threadService = null;
	                if(mySet.size()>100)
	                	threadService = Executors.newFixedThreadPool(100);
	                else
	                	threadService = Executors.newFixedThreadPool(vendorList.size());
	                
	               
	                for(Movies m: vendorList){
	                	 final String name = m.getTitle();
	                	threadService.submit(new Runnable(){
	                		public void run(){
	                		
									Movies details= getMovie(name);
									save(details);
									
								
	                		}});
	                	
	                	break;
	                }*/
	            	
	            }
	            
	            
		} catch (ClientProtocolException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	
	private void save(Movies details) {
		
		System.out.println("=======Start===========");
		System.out.println(details.getTitle());
		System.out.println(details.getMpaa_rating());
		System.out.println(details.getRelease_dates().getTheater());
		System.out.println(details.getSynopsis());
		System.out.println(details.getCritics_consensus());
		System.out.println(details.getRuntime());
		System.out.println(details.getPosters().getThumbnail());
		System.out.println(details.getPosters().getProfile());
		System.out.println(details.getPosters().getDetailed());
		System.out.println(details.getPosters().getOriginal());
		System.out.println(details.getRatings().getCritics_rating());
		System.out.println(details.getRatings().getAudience_rating());
		System.out.println(details.getRatings().getCritics_score());
		System.out.println(details.getRatings().getAudience_score());
		
		
		System.out.println("---------Artist---save-----");
		
		List<Artist> cast = details.getAbridged_cast();
		
		if(cast!=null && cast.size() > 0){
			
			for(Artist o: cast){
				System.out.println("Artist"+o.getName());
				
				for(String character : o.getCharacters())
				System.out.println("Character name "+character);
			}
		}
		
		
		System.out.println("---------Rating--------");
		System.out.println(details.getRatings().getAudience_rating());
		System.out.println(details.getRatings().getAudience_score());
		System.out.println(details.getRatings().getCritics_rating());
		System.out.println(details.getRatings().getCritics_score());
		//System.out.println(details.getRatings().getAudience_rating());
		
		
		
		System.out.println(details.getCritics_consensus());
		System.out.println("=======End===========");
		
		cassandraDB.saveMovie(details);
	}

	private Movies getMovie(String movieName){
		
		String moviePath=MOVIE_URL+movieName;
		try {
			moviePath = MOVIE_URL+URLEncoder.encode(movieName, "UTF-8");
			System.out.println(moviePath);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} ;
		
		
		MovieView mv = getMovieView(moviePath);
		if(mv!=null && mv.getMovies()!=null && mv.getMovies().size() > 0){
			return (Movies) mv.getMovies().get(0);
		}
		return null;
	}
	
	private MovieView  getBoxOfficeMovies() throws ClientProtocolException, IOException {
		
		return getMovieView(BOX_OFFICE_MOVIES);
	}
	
	
	public MovieView getMovieView(String url){
		 Parser p = new Parser();
         MovieView mv = new MovieView();
         try {
			mv = (MovieView) p.parse(get(url), mv);
			System.out.println("parsed successfull");
			return mv;
		} catch (ClientProtocolException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
        return null;
	}

	private String get(String url) throws ClientProtocolException, IOException{
		String responseBody="";

		
		
		
		 HttpClient httpclient = new DefaultHttpClient();
		 ResponseHandler<String> responseHandler = new BasicResponseHandler();
	        try {
	            HttpGet httpget = new HttpGet(url);
	            
	        

	            logger.info("executing request " + httpget.getURI());

	        
	           
	            responseBody = httpclient.execute(httpget, responseHandler);
	            logger.info("----------------------------------------");
	            logger.info(responseBody);
	            logger.info("----------------------------------------");
	            
	         

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
				logger.info("finally");
				
				logger.info(responseBody);
				
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
	            

	            /*logger.info("executing request " + httpget.getURI());

	            Header[] headers = httpget.getAllHeaders();
	            for(Header temp: headers){
	            	logger.info(temp.getName());
	            }*/
	            // Create a response handler
	           
	            responseBody = httpclient.execute(httpget, responseHandler);
	            logger.info("----------------------------------------");
	            logger.info(responseBody);
	            logger.info("----------------------------------------");

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
				logger.info("finally");
				
				logger.info(responseBody);
				
	            httpclient.getConnectionManager().shutdown();
	        }
		return responseBody;
	}
	
	
	
	
}
