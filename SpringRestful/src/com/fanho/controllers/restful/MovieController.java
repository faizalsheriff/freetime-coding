package com.fanho.controllers.restful;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.util.UriComponentsBuilder;

import com.fanho.restful.exception.BadRequestException;
import com.fanho.restful.response.Movie;
import com.fanho.restful.services.MovieService;


@Controller
@RequestMapping("/movie")
public class MovieController {
	
	private MovieService movieService;
	
	
	MovieController(){
		
	}
	
	@Autowired
	MovieController(MovieService service){
		movieService = service;
	}

	@RequestMapping(value="/{name}", method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public ResponseEntity<Movie> getMovie(@PathVariable String name, ModelMap model) {

		//model.addAttribute("movie", name);
		System.out.println("Service"+movieService);
		Movie movie = movieService.retrieve(name);
		System.out.println(movie);
		ResponseEntity<Movie> response ;
		if(movie!= null)
			response = new ResponseEntity<Movie>(movie, HttpStatus.OK);
		else
			response = new ResponseEntity<Movie>(HttpStatus.NOT_FOUND);
		
		
		return response;

	}
	
	
	 @RequestMapping(method = RequestMethod.POST)
	    public ResponseEntity<Movie> addMovie(@RequestHeader HttpHeaders s,  @RequestBody Movie movie, UriComponentsBuilder builder) {
		 System.out.println("Novie add");
		

		String key;
		try {
			key = movieService.addMovie(movie);
			
		} catch (BadRequestException e) {
			return new ResponseEntity<Movie>(HttpStatus.BAD_REQUEST);
		}
		 
	     

	        HttpHeaders headers = new HttpHeaders();
	       // headers.setLocation(location);
	        headers.setLocation(
	                builder.path("/aggregators/orders/{id}")
	                        .buildAndExpand(key).toUri());

	        System.out.println("Novie added");
	        return new ResponseEntity<Movie>( headers, HttpStatus.CREATED);
	    }
	 
	 
	 @RequestMapping(value="/Form", method = RequestMethod.POST)
	    public ResponseEntity<Movie> addMovieViaFormPost(@RequestBody MultiValueMap<String, String> map, UriComponentsBuilder builder) {
		 System.out.println("Novie add via form post");

		String key;
		try {
			key = movieService.addMovie(map);
		} catch (BadRequestException e) {
			return new ResponseEntity<Movie>(HttpStatus.BAD_REQUEST);
		}
		 
	     

	        HttpHeaders headers = new HttpHeaders();
	       // headers.setLocation(location);
	        headers.setLocation(
	                builder.path("/aggregators/orders/{id}")
	                        .buildAndExpand(key).toUri());

	        System.out.println("Novie added");
	        return new ResponseEntity<Movie>( headers, HttpStatus.CREATED);
	    }
	 
	 //RequestMethod.PUT
	
	 @RequestMapping(method = RequestMethod.DELETE, value = "/{name}")
	 public ResponseEntity<Movie> delete(@PathVariable("name") String movieName){
		 System.out.println("Delete movie");
		 
		 movieService.delete(movieName);
		 
		 ResponseEntity<Movie> response = new ResponseEntity<Movie>(HttpStatus.OK);
		 return response;
		 
		 
	 }
	 
	 
	 @RequestMapping(method = RequestMethod.PUT)
	 public ResponseEntity<Movie> update(@RequestBody Movie movieName){
		 System.out.println("update movie");
		 
		 try {
			movieService.update(movieName);
		} catch (BadRequestException e) {
			
			 ResponseEntity<Movie> response = new ResponseEntity<Movie>(HttpStatus.NOT_FOUND);
			 return response;
		}
		 
		 ResponseEntity<Movie> response = new ResponseEntity<Movie>(HttpStatus.OK);
		 return response;
		 
		 
	 }
	 
	 
}