package hello;


import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.Indexed;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;


@NodeEntity
public class Movie {
	
	@GraphId
	private Long id;
	
	@Indexed(unique = true)
	private String name;
	
	private String yearReleased;
	
	@Fetch
	@RelatedTo (type="ACTED_IN", direction = Direction.BOTH)
	private Set<Artists> artists;
	
	
	
	
	/* @RelatedToVia 
	 private Set<Like> likes;*/
	 
	 public Set<Artists> getArtists() {
		return artists;
	}
	public void setArtists(Set<Artists> artists) {
		this.artists = artists;
	}
	public Movie() {}
	    public Movie(String name) { this.name = name; 
	    }
	    
	    
	    public void actsIn(Artists artist) {
	        if (artists == null) {
	        	artists = new HashSet<Artists>();
	        }
	        artists.add(artist);
	    }
	    

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getYearReleased() {
		return yearReleased;
	}

	public void setYearReleased(String yearReleased) {
		this.yearReleased = yearReleased;
	}
	
	
	
	

}
