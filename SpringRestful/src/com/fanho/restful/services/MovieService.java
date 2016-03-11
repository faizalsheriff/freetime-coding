package com.fanho.restful.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import com.fanho.restful.exception.BadRequestException;
import com.fanho.restful.response.Movie;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonAnyFormatVisitor;

@Service
public class MovieService {
	
	public static Map<String, Movie> movie;
	
	static{
		movie = new HashMap<String, Movie>();
		
		Movie m = new Movie();
		m.setName("IronMan");
		m.setRating("PG-13");
		List<String> starring = new ArrayList<String>();
		starring.add("bbc");
		starring.add("dbc");
		starring.add("ebc");
		
		m.setStarring(starring);
		m.setYearReleased("2009");
		movie.put(m.getName(), m);
		
		
		m = new Movie();
		m.setName("XMan");
		m.setRating("PG-13");
		starring = new ArrayList<String>();
		starring.add("bdfbc");
		starring.add("dffbc");
		starring.add("ebffc");
		
		m.setStarring(starring);
		m.setYearReleased("2010");
		movie.put(m.getName(), m);
		
		
		
	}

	public Movie retrieve(String name) {
		return movie.get(name);
	}

	public String addMovie(Movie movie2) throws BadRequestException {
		if(movie2==null || movie2.getName()==null){
			throw new BadRequestException();
		}
		movie.put(movie2.getName(), movie2);
		return movie2.getName();
	}

	public String addMovie(MultiValueMap<String, String> map) throws BadRequestException {
		if(map==null || !map.containsKey("name")){
			throw new BadRequestException();
		}
		
		Movie n = new Movie();
		n.setName(map.getFirst("name"));
		
		if(map.containsKey("yearReleased")){
			n.setYearReleased(map.getFirst("yearReleased"));
		}
		
		if(map.containsKey("rating")){
			n.setYearReleased(map.getFirst("rating"));
		}
		
		movie.put(n.getName(), n);
		return n.getName();
	}

	public void delete(String movieName) {
		movie.remove(movieName);
		
	}

	public void update(Movie movieName) throws BadRequestException   {
		
		if(movieName==null || movieName.getName()==null || !movie.containsKey(movieName.getName())){
			throw new BadRequestException();
		}
		
		Movie n = movie.get(movieName.getName());
		
		if(movieName.getRating() != null && movieName.getRating().trim().length()>0)
		n.setRating(movieName.getRating());
		
		if(movieName.getYearReleased() != null && movieName.getYearReleased().trim().length()>0)
			n.setYearReleased(movieName.getYearReleased());
		
		if(movieName.getStarring() != null && movieName.getStarring().size()>0)
			n.setStarring(movieName.getStarring());
		
	System.out.println(n.getYearReleased()+"::"+n.getName());
		
		movie.put(n.getName(), n);
		
		
		
		
		
	}

}
