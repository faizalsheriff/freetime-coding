package com.javacodegeeks.snippets.enterprise.impl;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fanho.media.movies.beans.MovieView;
import com.javacodegeeks.snippets.enterprise.SimpleService;

public class SimpleServiceImpl implements SimpleService {

	private String name;

	private int id;
	
	private Map<String, String> mediaTypes;

	public Map<String, String> getMediaTypes() {
		return mediaTypes;
	}

	public void setMediaTypes(Map<String, String> mediaTypes) {
		this.mediaTypes = mediaTypes;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void printNameId() {
		System.out.println("SimpleService : Method printNameId() : My name is " + name
			 + " and my id is " + id);
	}

	/*public void checkName() {
		if (name.length() < 20) {
			throw new IllegalArgumentException();
		}
	}
*/
	
	
		
		
		
		public void checkName(){
			
			System.out.println("!!!!!! Checkin Movie Name !!!!!");

		    RestTemplate template = new RestTemplate();

		   

		    ResponseEntity<MovieView> entity = template.getForEntity(
		        "http://api.rottentomatoes.com/api/public/v1.0/movies.json?apikey=3mm9wqu9fpzdtakd2s7q87nj&q=Toy+Story+3&page_limit=1",
		      MovieView.class);

		 

		
		    MovieView movieView = entity.getBody();
		    
		    if(movieView != null){
		    	System.out.println("It is not null");
		    }
		}

	

	
	public String sayHello(String message){
		System.out.println("SimpleService : Method sayHello() : Hello! " + message);
		System.out.println("getting"+mediaTypes.get("json"));
		System.out.println("getting"+mediaTypes.get("csv"));
		return message;
	}
}